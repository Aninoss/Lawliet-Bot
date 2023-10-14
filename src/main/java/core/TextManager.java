package core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import commands.Category;
import constants.Emojis;
import constants.Language;
import constants.RegexPatterns;
import core.utils.EmojiUtil;
import core.utils.StringUtil;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextManager {

    public static final String COMMANDS = "commands";
    public static final String GENERAL = "general";
    public static final String PERMISSIONS = "permissions";
    public static final String VERSIONS = "versions";
    public static final String FAQ = "faq";

    private static final LoadingCache<Pair<String, Locale>, ResourceBundle> bundles = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public ResourceBundle load(@NotNull Pair<String, Locale> pair) {
                    return ResourceBundle.getBundle(pair.getKey(), pair.getValue(), new UTF8Control());
                }
            });

    public static String getString(Locale locale, Category category, String key, boolean secondOption, String... args) {
        return getString(locale, category.getId(), key, secondOption, args);
    }

    public static String getString(Locale locale, String category, String key, boolean secondOption, String... args) {
        if (!secondOption) {
            return getString(locale, category, key, 0, args);
        } else {
            return getString(locale, category, key, 1, args);
        }
    }

    public static String getString(Locale locale, Category category, String key, String... args) {
        return getString(locale, category.getId(), key, args);
    }

    public static String getString(Locale locale, String category, String key, String... args) {
        return getString(locale, category, key, -1, args);
    }

    public static String getString(Locale locale, Category category, String key, int option, String... args) {
        return getString(locale, category.getId(), key, option, args);
    }

    public static String getString(Locale locale, String category, String key, int option, String... args) {
        ResourceBundle texts;
        try {
            texts = bundles.get(new Pair<>(category, new Locale(locale.toString().toLowerCase())));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (texts.containsKey(key)) {
            try {
                String text = texts.getString(key);

                String textOverrides = System.getenv("TEXT_OVERRIDES");
                if (textOverrides != null) {
                    for (String entry : textOverrides.split(",")) {
                        String[] parts = entry.split(":");
                        text = text.replaceAll("\\b" + Pattern.quote(parts[0]) + "\\b", parts[1]);
                    }
                }

                String[] placeholders = extractGroups(RegexPatterns.TEXT_PLACEHOLDER, text);
                text = processMultiOptions(text, option);
                text = processReferences(text, placeholders, category, locale);
                text = processParams(text, args);
                text = processEmojis(text, placeholders);
                text = text.replace("{BOT}", System.getenv("BOT_NAME"));

                return text;
            } catch (Throwable e) {
                MainLogger.get().error("Text error for key {} in {} with locale {}", key, category, locale, e);
            }
        } else {
            MainLogger.get().error("Key {} not found in {} with locale {}", key, category, locale);
        }

        return "???";
    }

    private static String[] extractGroups(Pattern pattern, String text) {
        ArrayList<String> placeholderList = new ArrayList<>();
        Matcher m = pattern.matcher(text);
        while(m.find()) {
            placeholderList.add(m.group("inner"));
        }
        return placeholderList.toArray(new String[0]);
    }

    private static String processEmojis(String text, String[] placeholders) {
        List<Pair<String, String>> emojiPairs = List.of(
                new Pair<>("CURRENCY", EmojiUtil.getEmojiFromOverride(Emojis.FISH, "FISH").getFormatted()),
                new Pair<>("COINS", Emojis.COINS.getFormatted()),
                new Pair<>("GROWTH", EmojiUtil.getEmojiFromOverride(Emojis.GROWTH, "GROWTH").getFormatted()),
                new Pair<>("DAILY_STREAK", Emojis.DAILY_STREAK.getFormatted()),
                new Pair<>("COUPONS", Emojis.COUPONS.getFormatted())
        );

        for (String placeholder : placeholders) {
            for (Pair<String, String> emojiPair : emojiPairs) {
                if (emojiPair.getKey().equals(placeholder)) {
                    text = text.replace("{" + emojiPair.getKey() + "}", emojiPair.getValue());
                }
            }
        }

        return text;
    }

    private static String processParams(String text, String[] args) {
        for (int i = 0; i < args.length; i++) {
            text = text.replace("{" + i + "}", args[i]);
        }

        return text;
    }

    private static String processMultiOptions(String text, int option) {
        String[] groups = extractGroups(RegexPatterns.TEXT_MULTIOPTION, text);

        for (String group : groups) {
            if (group.contains("|")) {
                text = text.replace("[" + group + "]", group.split("\\|")[option]);
            }
        }

        return text.replace("\\[", "[").replace("\\]", "]");
    }

    private static String processReferences(String text, String[] placeholders, String category, Locale locale) {
        for (String placeholder : placeholders) {
            if (placeholder.contains(".")) {
                String[] parts = placeholder.split("\\.");
                if (parts[0].equals("this")) {
                    parts[0] = category;
                }
                String newValue = getString(locale, parts[0], parts[1]);
                text = text.replace("{" + placeholder + "}", newValue);
            }
        }

        return text;
    }

    public static String getNoResultsString(Locale locale, String content) {
        return TextManager.getString(locale, TextManager.GENERAL, "no_results_description", StringUtil.shortenString(content, 32));
    }

    public static int getKeySize(String category) {
        ResourceBundle texts = ResourceBundle.getBundle(category, Language.EN.getLocale());
        return texts.keySet().size();
    }

}

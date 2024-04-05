package modules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Welcome {

    public static String resolveVariables(String string, String arg1, String arg2, String arg3, String arg4, String arg5, String arg6) {
        return string.replaceAll("(?i)" + Pattern.quote("%server"), Matcher.quoteReplacement(arg1))
                .replaceAll("(?i)" + Pattern.quote("%user_mention"), Matcher.quoteReplacement(arg2))
                .replaceAll("(?i)" + Pattern.quote("%user_discriminated"), Matcher.quoteReplacement(arg4))
                .replaceAll("(?i)" + Pattern.quote("%user_name"), Matcher.quoteReplacement(arg3))
                .replaceAll("(?i)" + Pattern.quote("%username"), Matcher.quoteReplacement(arg3))
                .replaceAll("(?i)" + Pattern.quote("%display_name"), Matcher.quoteReplacement(arg6))
                .replaceAll("(?i)" + Pattern.quote("%user\\_mention"), Matcher.quoteReplacement(arg2))
                .replaceAll("(?i)" + Pattern.quote("%user\\_discriminated"), Matcher.quoteReplacement(arg4))
                .replaceAll("(?i)" + Pattern.quote("%user\\_name"), Matcher.quoteReplacement(arg3))
                .replaceAll("(?i)" + Pattern.quote("%display\\_name"), Matcher.quoteReplacement(arg6))
                .replaceAll("(?i)" + Pattern.quote("%members"), Matcher.quoteReplacement(arg5));
    }

}

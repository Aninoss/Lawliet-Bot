package modules.fishery;

import commands.Category;
import commands.Command;
import commands.runnables.fisherysettingscategory.FisheryCommand;
import constants.ExceptionIds;
import constants.Settings;
import core.EmbedFactory;
import core.ExceptionLogger;
import core.Program;
import core.TextManager;
import core.components.ActionRows;
import core.schedule.MainScheduler;
import modules.JoinRoles;
import mysql.hibernate.EntityManagerWrapper;
import mysql.hibernate.entity.guild.GuildEntity;
import mysql.modules.staticreactionmessages.DBStaticReactionMessages;
import mysql.modules.staticreactionmessages.StaticReactionMessageData;
import mysql.redis.fisheryusers.FisheryMemberData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.time.Duration;
import java.util.*;

public class Fishery {

    private static final HashSet<Long> unusedPowerUpSet = new HashSet<>();

    public static void synchronizeRoles(Member member, GuildEntity guildEntity) {
        Guild guild = member.getGuild();
        Locale locale = guildEntity.getLocale();
        if (guildEntity.getFishery().getFisheryStatus() != FisheryStatus.ACTIVE) {
            return;
        }

        HashSet<Role> rolesToAdd = new HashSet<>();
        HashSet<Role> rolesToRemove = new HashSet<>();
        JoinRoles.getFisheryRoles(locale, member, guildEntity, rolesToAdd, rolesToRemove);

        if (rolesToAdd.size() > 0 || rolesToRemove.size() > 0) {
            guild.modifyMemberRoles(member, rolesToAdd, rolesToRemove)
                    .reason(Command.getCommandLanguage(FisheryCommand.class, locale).getTitle())
                    .queue();
        }
    }

    public static long getFisheryRolePrice(long rolePriceMin, long rolePriceMax, int size, int n) {
        if (size == 1) {
            return rolePriceMin;
        }

        double power = Math.pow((double) rolePriceMax / (double) rolePriceMin, 1 / (double) (size - 1));

        double price = Math.pow(power, n);
        double priceMax = Math.pow(power, size - 1);

        return Math.round(price * ((double) rolePriceMax / priceMax));
    }

    public static List<Member> getValidVoiceMembers(EntityManagerWrapper entityManager, VoiceChannel voiceChannel) {
        ArrayList<Member> validMembers = new ArrayList<>();
        for (Member member : voiceChannel.getMembers()) {
            GuildVoiceState voice = member.getVoiceState();
            if (voice != null &&
                    !member.getUser().isBot() &&
                    !voice.isMuted() &&
                    !voice.isDeafened() &&
                    !voice.isSuppressed() &&
                    entityManager.findUserEntityReadOnly(member.getIdLong()).getBanReason() == null
            ) {
                validMembers.add(member);
            }
        }

        if (validMembers.size() >= (Program.productionMode() ? 2 : 1)) {
            return Collections.unmodifiableList(validMembers);
        } else {
            return Collections.emptyList();
        }
    }

    public static long getClaimValue(FisheryMemberData userBean) {
        return Math.round(userBean.getMemberGear(FisheryGear.DAILY).getEffect() * 0.25);
    }

    public static void spawnTreasureChest(StandardGuildMessageChannel channel, GuildEntity guildEntity) {
        Locale locale = guildEntity.getLocale();
        EmbedBuilder eb = EmbedFactory.getEmbedDefault()
                .setTitle(FisheryCommand.EMOJI_TREASURE + " " + TextManager.getString(locale, Category.FISHERY_SETTINGS, "fishery_treasure_title"))
                .setDescription(TextManager.getString(locale, Category.FISHERY_SETTINGS, "fishery_treasure_desription"))
                .setImage("https://cdn.discordapp.com/attachments/711665837114654781/711665915355201576/treasure_closed.png");

        Button button = Button.of(ButtonStyle.SECONDARY, FisheryCommand.BUTTON_ID_TREASURE, TextManager.getString(locale, Category.FISHERY_SETTINGS, "fishery_treasure_button"))
                .withEmoji(Emoji.fromUnicode(FisheryCommand.EMOJI_KEY));
        channel.sendMessageEmbeds(eb.build())
                .setComponents(ActionRows.of(button))
                .queue(m -> {
                    DBStaticReactionMessages.getInstance().retrieve(channel.getGuild().getIdLong())
                            .put(m.getIdLong(), new StaticReactionMessageData(m, Command.getCommandProperties(FisheryCommand.class).trigger()));
                });
    }

    public static void spawnPowerUp(StandardGuildMessageChannel channel, Member member, GuildEntity guildEntity) {
        Locale locale = guildEntity.getLocale();
        EmbedBuilder eb = EmbedFactory.getEmbedDefault()
                .setTitle(FisheryCommand.EMOJI_POWERUP + " " + TextManager.getString(locale, Category.FISHERY_SETTINGS, "fishery_powerup_title"))
                .setDescription(TextManager.getString(locale, Category.FISHERY_SETTINGS, "fishery_powerup_desc", member.getEffectiveName()))
                .setThumbnail("https://cdn.discordapp.com/attachments/1077245845440827562/1078702766865788989/question.png");

        Button button = Button.of(ButtonStyle.SECONDARY, FisheryCommand.BUTTON_ID_POWERUP, TextManager.getString(locale, Category.FISHERY_SETTINGS, "fishery_powerup_button"));
        channel.sendMessage(member.getAsMention())
                .addEmbeds(eb.build())
                .setComponents(ActionRows.of(button))
                .queue(m -> {
                    unusedPowerUpSet.add(m.getIdLong());
                    DBStaticReactionMessages.getInstance().retrieve(channel.getGuild().getIdLong())
                            .put(m.getIdLong(), new StaticReactionMessageData(m, Command.getCommandProperties(FisheryCommand.class).trigger(), member.getId()));

                    MainScheduler.schedule(Duration.ofMinutes(Settings.FISHERY_POWERUP_TIMEOUT_MINUTES), () -> {
                        if (unusedPowerUpSet.contains(m.getIdLong())) {
                            m.delete().submit().exceptionally(ExceptionLogger.get(ExceptionIds.UNKNOWN_MESSAGE, ExceptionIds.UNKNOWN_CHANNEL));
                            deregisterPowerUp(m.getIdLong());
                        }
                    });
                });
    }

    public static void deregisterPowerUp(long messageId) {
        unusedPowerUpSet.remove(messageId);
    }

    public static String getChangeEmoji() {
        return getChangeEmoji(0);
    }

    public static String getChangeEmoji(int offset) {
        int rateNow = ExchangeRate.get(offset);
        int rateBefore = ExchangeRate.get(offset - 1);

        if (rateNow > rateBefore) {
            return "🔺";
        } else {
            if (rateNow < rateBefore) {
                return "🔻";
            } else {
                return "•";
            }
        }
    }

}

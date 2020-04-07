package Commands.InformationCategory;

import CommandListeners.CommandProperties;

import CommandSupporters.Command;
import Constants.Permission;
import Constants.Settings;
import General.*;
import General.Survey.SurveyManager;
import General.Tools.StringTools;
import General.Tools.TimeTools;
import General.Tracker.TrackerManager;
import MySQL.DBBot;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

@CommandProperties(
        trigger = "stats",
        botPermissions = Permission.USE_EXTERNAL_EMOJIS,
        emoji = "\uD83D\uDCCA",
        thumbnail = "http://icons.iconarchive.com/icons/webalys/kameleon.pics/128/Graph-Magnifier-icon.png",
        executable = true,
        aliases = {"info"}
)
public class StatsCommand extends Command {

    @Override
    public boolean onMessageReceived(MessageCreateEvent event, String followedString) throws Throwable {
        User dephord = DiscordApiCollection.getInstance().getUserById(303085910784737281L).get();

        EmbedBuilder eb = EmbedFactory.getCommandEmbedStandard(this,
                getString("template",
                DiscordApiCollection.getInstance().getOwner().getMentionTag(),
                Settings.BOT_INVITE_URL,
                StringTools.getCurrentVersion(),
                TimeTools.getInstantString(getLocale(), DBBot.getCurrentVersionDate(), true),
                StringTools.numToString(getLocale(), DiscordApiCollection.getInstance().getServerTotalSize()),
                StringTools.numToString(getLocale(), TrackerManager.getSize()),
                DiscordApiCollection.getInstance().getOwner().getDiscriminatedName(),
                StringTools.numToString(getLocale(), SurveyManager.getCurrentFirstVotesNumber())
                ) +
                "\n\n" +
                getString("translator", dephord.getMentionTag(), dephord.getDiscriminatedName()));

        event.getServerTextChannel().get().sendMessage(eb).get();
        return true;
    }

}

package commands.runnables.informationcategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import commands.Command;
import commands.listeners.CommandProperties;
import core.EmbedFactory;
import core.TextManager;
import core.utils.EmbedUtil;
import core.utils.MentionUtil;
import core.utils.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

@CommandProperties(
        trigger = "channelinfo",
        emoji = "\uD83D\uDCDD",
        executableWithoutArgs = true,
        requiresMemberCache = true,
        aliases = { "channelinfos", "channelstat", "channelstats", "channel" }
)
public class ChannelInfoCommand extends Command {

    public ChannelInfoCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(GuildMessageReceivedEvent event, String args) {
        boolean noMention = false;
        Guild guild = event.getGuild();
        ArrayList<TextChannel> list = new ArrayList<>(MentionUtil.getTextChannels(event.getMessage(), args).getList());
        if (list.size() == 0) {
            list.add(event.getChannel());
            noMention = true;
        }

        TextChannel channel = list.get(0);
        List<Member> members = channel.getMembers();

        String[] argsArray = {
                StringUtil.escapeMarkdown(channel.getName()),
                channel.getId(),
                TimeFormat.DATE_TIME_SHORT.atInstant(channel.getTimeCreated().toInstant()).toString(),
                StringUtil.numToString(members.size()),
                StringUtil.numToString(members.stream().filter(member -> !member.getUser().isBot()).count()),
                StringUtil.numToString(members.stream().filter(member -> member.getUser().isBot()).count())
        };

        EmbedBuilder eb = EmbedFactory.getEmbedDefault(this, getString("template", argsArray));
        if (guild.getIconUrl() != null) {
            eb.setThumbnail(guild.getIconUrl());
        }

        if (noMention) {
            EmbedUtil.setFooter(eb, this, TextManager.getString(getLocale(), TextManager.GENERAL, "channel_mention_optional"));
            if (args.length() > 0) {
                EmbedUtil.addNoResultsLog(eb, getLocale(), args);
            }
        }

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
        return true;
    }

}

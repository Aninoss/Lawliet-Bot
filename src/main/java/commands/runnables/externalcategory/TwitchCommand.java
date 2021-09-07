package commands.runnables.externalcategory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import commands.Command;
import commands.listeners.CommandProperties;
import commands.listeners.OnAlertListener;
import constants.Emojis;
import modules.schedulers.AlertResponse;
import core.EmbedFactory;
import core.TextManager;
import core.utils.EmbedUtil;
import core.utils.StringUtil;
import modules.twitch.TwitchDownloader;
import modules.twitch.TwitchStream;
import modules.twitch.TwitchUser;
import mysql.modules.tracker.TrackerData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@CommandProperties(
        trigger = "twitch",
        emoji = "\uD83D\uDCF9",
        executableWithoutArgs = false
)
public class TwitchCommand extends Command implements OnAlertListener {

    private static final String TWITCH_ICON = "https://www.twitch.tv/favicon.ico";
    private static final TwitchDownloader twitchDownloader = new TwitchDownloader();

    public TwitchCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(GuildMessageReceivedEvent event, String args) throws ExecutionException {
        if (args.isEmpty()) {
            event.getChannel().sendMessageEmbeds(EmbedFactory.getEmbedError(this, TextManager.getString(getLocale(), TextManager.GENERAL, "no_args")).build())
                    .queue();
            return false;
        }

        addLoadingReactionInstantly();
        Optional<TwitchStream> streamOpt = twitchDownloader.getStream(args);
        if (streamOpt.isEmpty()) {
            EmbedBuilder eb = EmbedFactory.getEmbedError(this)
                    .setTitle(TextManager.getString(getLocale(), TextManager.GENERAL, "no_results"))
                    .setDescription(TextManager.getNoResultsString(getLocale(), args));
            event.getChannel().sendMessageEmbeds(eb.build()).queue();
            return false;
        }

        EmbedBuilder eb = EmbedUtil.addTrackerNoteLog(getLocale(), event.getMember(), getEmbed(streamOpt.get()), getPrefix(), getTrigger());
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
        return true;
    }

    private EmbedBuilder getEmbed(TwitchStream twitchStream) {
        TwitchUser twitchUser = twitchStream.getTwitchUser();
        EmbedBuilder eb;
        if (twitchStream.isLive()) {
            String twitchStatus = twitchStream.getStatus().get();
            eb = EmbedFactory.getEmbedDefault()
                    .setAuthor(getString("streamer", twitchUser.getDisplayName(), twitchStream.getGame().get()), twitchUser.getChannelUrl(), TWITCH_ICON)
                    .setTitle(twitchStatus.isEmpty() ? Emojis.ZERO_WIDTH_SPACE : twitchStatus, twitchUser.getChannelUrl())
                    .setImage(twitchStream.getPreviewImage().get());
            EmbedUtil.setFooter(eb, this, getString("footer", StringUtil.numToString(twitchStream.getViewers().get()), StringUtil.numToString(twitchStream.getFollowers().get())));
        } else {
            eb = EmbedFactory.getEmbedDefault()
                    .setAuthor(twitchUser.getDisplayName(), twitchUser.getChannelUrl(), TWITCH_ICON)
                    .setDescription(getString("offline", twitchUser.getDisplayName()));
            EmbedUtil.setFooter(eb, this);
        }

        eb.setThumbnail(twitchUser.getLogoUrl());
        return eb;
    }

    @Override
    public AlertResponse onTrackerRequest(TrackerData slot) throws Throwable {
        slot.setNextRequest(Instant.now().plus(5, ChronoUnit.MINUTES));
        TextChannel channel = slot.getTextChannel().get();

        Optional<TwitchStream> streamOpt;
        try {
            streamOpt = twitchDownloader.getStream(slot.getCommandKey());
        } catch (Throwable e) {
            if (slot.getArgs().isEmpty()) {
                streamOpt = Optional.empty();
            } else {
                throw e;
            }
        }

        if (streamOpt.isEmpty()) {
            if (slot.getArgs().isEmpty()) {
                EmbedBuilder eb = EmbedFactory.getEmbedError(this)
                        .setTitle(TextManager.getString(getLocale(), TextManager.GENERAL, "no_results"))
                        .setDescription(TextManager.getNoResultsString(getLocale(), slot.getCommandKey()));
                EmbedUtil.addTrackerRemoveLog(eb, getLocale());
                channel.sendMessageEmbeds(eb.build()).complete();
                return AlertResponse.STOP_AND_DELETE;
            } else {
                return AlertResponse.CONTINUE;
            }
        }

        TwitchStream twitchStream = streamOpt.get();
        EmbedBuilder eb = getEmbed(twitchStream);

        if (slot.getArgs().isEmpty()) {
            long messageId = slot.sendMessage(true, eb.build()).get(); /* always post current twitch status at first run */
            slot.setMessageId(messageId);
        } else if (twitchStream.isLive()) {
            if (slot.getArgs().get().equals("false")) {
                long messageId = slot.sendMessage(true, eb.build()).get(); /* post twitch status if live and not live before */
                slot.setMessageId(messageId);
            } else {
                slot.editMessage(true, eb.build()); /* edit twitch status if live and live before */
            }
        }

        slot.setArgs(String.valueOf(twitchStream.isLive()));
        return AlertResponse.CONTINUE_AND_SAVE;
    }

    @Override
    public boolean trackerUsesKey() {
        return true;
    }

}

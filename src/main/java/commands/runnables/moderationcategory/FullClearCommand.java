package commands.runnables.moderationcategory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import commands.Command;
import commands.CommandEvent;
import commands.listeners.CommandProperties;
import commands.listeners.OnAlertListener;
import commands.listeners.OnButtonListener;
import core.EmbedFactory;
import core.ExceptionLogger;
import core.PermissionCheckRuntime;
import core.TextManager;
import core.mention.MentionList;
import core.utils.BotPermissionUtil;
import core.utils.EmbedUtil;
import core.utils.EmojiUtil;
import core.utils.MentionUtil;
import modules.ClearResults;
import modules.schedulers.AlertResponse;
import mysql.modules.tracker.TrackerData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

@CommandProperties(
        trigger = "fullclear",
        botChannelPermissions = { Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY },
        userChannelPermissions = { Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY },
        emoji = "\uD83E\uDDF9",
        executableWithoutArgs = true,
        turnOffLoadingReaction = true,
        maxCalculationTimeSec = 20 * 60,
        aliases = { "fclear", "allclear", "clearall" }
)
public class FullClearCommand extends Command implements OnAlertListener, OnButtonListener {

    private static final int HOURS_MAX = 20159;
    private static final Random random = new Random();

    private boolean interrupt = false;
    private List<Member> memberFilter;
    private long hoursMin;
    TextChannel channel;

    public FullClearCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(@NotNull CommandEvent event, @NotNull String args) throws InterruptedException, ExecutionException {
        MentionList<TextChannel> channelMention = MentionUtil.getTextChannels(event.getGuild(), args);
        args = channelMention.getFilteredArgs();
        channel = event.getChannel();
        if (channelMention.getList().size() > 0) {
            channel = channelMention.getList().get(0);
        }
        EmbedBuilder errEmbed = BotPermissionUtil.getUserAndBotPermissionMissingEmbed(
                getLocale(),
                channel,
                event.getMember(),
                new Permission[0],
                new Permission[] { Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY },
                new Permission[0],
                new Permission[] { Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY },
                new Permission[0]
        );
        if (errEmbed != null) {
            drawMessageNew(errEmbed).exceptionally(ExceptionLogger.get());
            return false;
        }

        MentionList<Member> memberMention = MentionUtil.getMembers(event.getGuild(), args, null);
        memberFilter = memberMention.getList();
        args = memberMention.getFilteredArgs();
        hoursMin = Math.max(0, MentionUtil.getAmountExt(args));

        if (hoursMin < HOURS_MAX) {
            long messageId = registerButtonListener(event.getMember()).get();
            TimeUnit.SECONDS.sleep(1);
            long authorMessageId = event.isGuildMessageReceivedEvent() ? event.getGuildMessageReceivedEvent().getMessage().getIdLong() : 0L;
            ClearResults clearResults = fullClear(channel, (int) hoursMin, memberFilter, authorMessageId, messageId);

            String key = clearResults.getRemaining() > 0 ? "finished_too_old" : "finished_description";
            EmbedBuilder eb = EmbedFactory.getEmbedDefault(this, getString(key, clearResults.getDeleted() != 1, String.valueOf(clearResults.getDeleted())));
            EmbedUtil.setFooter(eb, this, TextManager.getString(getLocale(), TextManager.GENERAL, "deleteTime", "8"));

            if (!interrupt) {
                deregisterListenersWithComponents();
                drawMessage(eb).exceptionally(ExceptionLogger.get());
            }

            RestAction<Void> restAction;
            if (event.isGuildMessageReceivedEvent()) {
                restAction = event.getChannel().deleteMessagesByIds(List.of(String.valueOf(messageId), event.getGuildMessageReceivedEvent().getMessage().getId()));
            } else {
                restAction = event.getChannel().deleteMessageById(messageId);
            }
            restAction.queueAfter(8, TimeUnit.SECONDS);
            return true;
        } else {
            drawMessageNew(EmbedFactory.getEmbedError(this, getString("wrong_args", "0", "20159")))
                    .exceptionally(ExceptionLogger.get());
            return false;
        }
    }

    private void fullClear(TextChannel channel, int hours) throws InterruptedException {
        fullClear(channel, hours, Collections.emptyList(), 0L);
    }

    private ClearResults fullClear(TextChannel channel, int hours, List<Member> memberFilter, long... messageIdsIgnore) throws InterruptedException {
        int deleted = 0;
        boolean tooOld = false;

        MessageHistory messageHistory = channel.getHistory();
        do {
            /* Check for message date and therefore permissions */
            List<Message> messageList = messageHistory.retrievePast(100).complete();
            if (messageList.isEmpty() || interrupt) {
                break;
            }

            ArrayList<Message> messagesDelete = new ArrayList<>();
            for (Message message : messageList) {
                if (message.getTimeCreated().toInstant().isBefore(Instant.now().minus(14, ChronoUnit.DAYS))) {
                    tooOld = true;
                    break;
                } else if (!message.isPinned() &&
                        Arrays.stream(messageIdsIgnore).noneMatch(mId -> message.getIdLong() == mId) &&
                        message.getTimeCreated().toInstant().isBefore(Instant.now().minus(hours, ChronoUnit.HOURS)) &&
                        (memberFilter.isEmpty() || memberFilter.contains(message.getMember()))
                ) {
                    messagesDelete.add(message);
                    deleted++;
                }
            }

            if (messagesDelete.size() >= 1) {
                if (messagesDelete.size() == 1) {
                    messagesDelete.get(0).delete().complete();
                } else {
                    channel.deleteMessages(messagesDelete).complete();
                }
            }

            Thread.sleep(500);
        } while (!tooOld && !interrupt);

        return new ClearResults(deleted, tooOld ? 1 : 0);
    }

    @Override
    public @NotNull AlertResponse onTrackerRequest(@NotNull TrackerData slot) throws Throwable {
        TextChannel textChannel = slot.getTextChannel().get();
        if (PermissionCheckRuntime.botHasPermission(getLocale(), getClass(), textChannel, Permission.MESSAGE_HISTORY, Permission.MESSAGE_MANAGE)) {
            long hoursMin = Math.max(0, MentionUtil.getAmountExt(slot.getCommandKey()));
            if (hoursMin < HOURS_MAX) {
                try {
                    fullClear(textChannel, (int) hoursMin);
                    if (slot.getEffectiveUserMessage().isPresent()) {
                        slot.sendMessage(true, "");
                    }
                } catch (InterruptedException e) {
                    //ignore
                }
                slot.setNextRequest(Instant.now().plus(60 + random.nextInt(120), ChronoUnit.MINUTES));
                return AlertResponse.CONTINUE_AND_SAVE;
            } else {
                EmbedBuilder eb = EmbedFactory.getEmbedError(this, getString("wrong_args", "0", "20159"));
                textChannel.sendMessageEmbeds(eb.build()).queue();
            }
        }

        return AlertResponse.STOP_AND_DELETE;
    }

    @Override
    public boolean trackerUsesKey() {
        return true;
    }

    @Override
    public boolean onButton(@NotNull ButtonClickEvent event) throws Throwable {
        deregisterListenersWithComponents();
        interrupt = true;
        return true;
    }

    @Override
    public EmbedBuilder draw(@NotNull Member member) throws Throwable {
        if (!interrupt) {
            setComponents(Button.of(ButtonStyle.SECONDARY, "cancel", TextManager.getString(getLocale(), TextManager.GENERAL, "process_abort")));
            if (memberFilter.isEmpty()) {
                return EmbedFactory.getEmbedDefault(this, getString("progress", channel.getAsMention(), EmojiUtil.getLoadingEmojiMention(getTextChannel().get())));
            } else {
                return EmbedFactory.getEmbedDefault(this, getString("progress_filter", MentionUtil.getMentionedStringOfMembers(getLocale(), memberFilter).getMentionText(), channel.getAsMention(), EmojiUtil.getLoadingEmojiMention(getTextChannel().get())));
            }
        } else {
            EmbedBuilder eb = EmbedFactory.getEmbedDefault(this, TextManager.getString(getLocale(), TextManager.GENERAL, "process_abort_description"));
            EmbedUtil.setFooter(eb, this, TextManager.getString(getLocale(), TextManager.GENERAL, "deleteTime", "8"));
            return eb;
        }
    }

}

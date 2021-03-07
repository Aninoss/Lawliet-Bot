package commands.listeners;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import commands.Command;
import commands.CommandContainer;
import commands.CommandListenerMeta;
import constants.Response;
import core.MainLogger;
import core.utils.ExceptionUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface OnMessageInputListener {

    Response onMessageInput(GuildMessageReceivedEvent event, String input) throws Throwable;

    EmbedBuilder draw() throws Throwable;

    default void registerMessageInputListener() {
        Command command = (Command) this;
        command.getMember().ifPresent(member -> {
            registerMessageInputListener(member.getIdLong(), event -> event.getMember().getIdLong() == member.getIdLong() &&
                    event.getChannel().getIdLong() == command.getTextChannelId().orElse(0L)
            );
        });
    }

    default void registerMessageInputListener(long authorId, Function<GuildMessageReceivedEvent, Boolean> validityChecker) {
        Command command = (Command) this;

        Runnable onTimeOut = () -> {
            try {
                onMessageInputTimeOut();
            } catch (Throwable throwable) {
                MainLogger.get().error("Exception on time out", throwable);
            }
        };

        Runnable onOverridden = () -> {
            try {
                onMessageInputOverridden();
            } catch (Throwable throwable) {
                MainLogger.get().error("Exception on overridden", throwable);
            }
        };

        CommandListenerMeta<GuildMessageReceivedEvent> commandListenerMeta =
                new CommandListenerMeta<>(authorId, validityChecker, onTimeOut, onOverridden, command);
        CommandContainer.getInstance().registerListener(OnMessageInputListener.class, commandListenerMeta);

        try {
            EmbedBuilder eb = draw();
            if (eb != null) {
                command.drawMessage(eb.build());
            }
        } catch (Throwable e) {
            command.getTextChannel().ifPresent(channel -> {
                ExceptionUtil.handleCommandException(e, command, channel);
            });
        }
    }

    default void deregisterMessageInputListener() {
        CommandContainer.getInstance().deregisterListener(OnMessageInputListener.class, (Command) this);
    }

    default Response processMessageInput(GuildMessageReceivedEvent event) {
        Command command = (Command) this;
        AtomicBoolean isProcessing = new AtomicBoolean(true);
        CommandContainer.getInstance().refreshListener(OnMessageInputListener.class, command);

        command.addLoadingReaction(event.getMessage(), isProcessing);
        try {
            Response response = onMessageInput(event, event.getMessage().getContentRaw());
            if (response != null) {
                EmbedBuilder eb = draw();
                if (eb != null) {
                    ((Command) this).drawMessage(eb.build());
                }
            }
            return response;
        } catch (Throwable e) {
            ExceptionUtil.handleCommandException(e, command, event.getChannel());
            return Response.ERROR;
        } finally {
            isProcessing.set(false);
        }
    }

    default void onMessageInputTimeOut() throws Throwable {
    }

    default void onMessageInputOverridden() throws Throwable {
    }

}
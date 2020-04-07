package CommandSupporters;

import CommandListeners.*;
import Commands.InformationCategory.HelpCommand;
import General.*;
import CommandSupporters.Cooldown.Cooldown;
import CommandSupporters.RunningCommands.RunningCommandManager;
import MySQL.CommandUsages.DBCommandUsages;
import MySQL.DBServerOld;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class CommandManager {

    public static void manage(MessageCreateEvent event, Command command, String followedString) throws IOException, ExecutionException, InterruptedException, SQLException {
        if (botCanPost(event, command) &&
                isWhiteListed(event) &&
                isNSFWCompliant(event, command) &&
                botCanUseEmbeds(event, command) &&
                checkPermissions(event, command) &&
                checkCooldown(event, command) &&
                checkRunningCommands(event, command)
        ) {
            DBCommandUsages.getInstance().getBean(command.getTrigger()).increase();
            cleanPreviousActivities(event.getServer().get(), event.getMessageAuthor().asUser().get());
            manageSlowCommandLoadingReaction(command, event.getMessage());

            try {
                sendOverwrittenSignals(event);
                if (command instanceof onNavigationListener)
                    command.onNavigationMessageSuper(event, followedString, true);
                else
                    command.onRecievedSuper(event, followedString);
            } catch (Throwable e) {
                ExceptionHandler.handleException(e, command.getLocale(), event.getServerTextChannel().get());
            }
            command.removeLoadingReaction();
        }
    }

    private static boolean checkRunningCommands(MessageCreateEvent event, Command command) throws ExecutionException, InterruptedException {
        if (RunningCommandManager.getInstance().canUserRunCommand(event.getMessage().getUserAuthor().get().getId(), event.getApi().getCurrentShard())) {
            return true;
        }

        EmbedBuilder eb = EmbedFactory.getEmbedError()
                .setTitle(TextManager.getString(command.getLocale(), TextManager.GENERAL, "alreadyused_title"))
                .setDescription(TextManager.getString(command.getLocale(), TextManager.GENERAL, "alreadyused_desc"));
        event.getChannel().sendMessage(eb).get();

        return false;
    }

    private static boolean checkCooldown(MessageCreateEvent event, Command command) throws ExecutionException, InterruptedException {
        Optional<Integer> waitingSec = Cooldown.getInstance().getWaitingSec(event.getMessageAuthor().asUser().get().getId(), command.getCooldownTime());
        if (!waitingSec.isPresent()) {
            return true;
        }

        User user = event.getMessageAuthor().asUser().get();
        if (Cooldown.getInstance().isFree(user.getId())) {
            EmbedBuilder eb = EmbedFactory.getEmbedError()
                    .setTitle(TextManager.getString(command.getLocale(), TextManager.GENERAL, "cooldown_title"))
                    .setDescription(TextManager.getString(command.getLocale(), TextManager.GENERAL, "cooldown_description", waitingSec.get() != 1, user.getMentionTag(), String.valueOf(waitingSec.get())));
            event.getChannel().sendMessage(eb).get();
        }

        return false;
    }

    private static boolean checkPermissions(MessageCreateEvent event, Command command) throws ExecutionException, InterruptedException {
        EmbedBuilder errEmbed = PermissionCheck.getUserAndBotPermissionMissingEmbed(command.getLocale(), event.getServer().get(), event.getServerTextChannel().get(), event.getMessage().getUserAuthor().get(), command.getUserPermissions(), command.getBotPermissions());
        if (errEmbed == null || command instanceof HelpCommand) {
            return true;
        }

        event.getChannel().sendMessage(errEmbed).get();
        return false;
    }

    private static boolean botCanUseEmbeds(MessageCreateEvent event, Command command) {
        if (event.getChannel().canYouEmbedLinks() || !command.requiresEmbeds()) {
            return true;
        }

        event.getChannel().sendMessage("**" + TextManager.getString(command.getLocale(), TextManager.GENERAL, "missing_permissions_title") + "**\n" + TextManager.getString(command.getLocale(), TextManager.GENERAL, "no_embed"));
        event.getMessage().addReaction("❌");
        return false;
    }

    private static boolean isNSFWCompliant(MessageCreateEvent event, Command command) throws IOException {
        if (!command.isNsfw() || event.getServerTextChannel().get().isNsfw()) {
            return true;
        }

        event.getChannel().sendMessage(EmbedFactory.getNSFWBlockEmbed(command.getLocale()));
        event.getMessage().addReaction("❌");
        return false;
    }

    private static boolean isWhiteListed(MessageCreateEvent event) throws SQLException {
        return event.getServer().get().canManage(event.getMessage().getUserAuthor().get()) || DBServerOld.isChannelWhitelisted(event.getServerTextChannel().get());
    }

    private static boolean botCanPost(MessageCreateEvent event, Command command) {
        if (event.getChannel().canYouWrite() || command instanceof HelpCommand) {
            return true;
        }

        if (event.getChannel().canYouAddNewReactions()) {
            event.addReactionsToMessage("✏");
            event.addReactionsToMessage("❌");
            event.getMessage().getUserAuthor().get().sendMessage(TextManager.getString(command.getLocale(), TextManager.GENERAL, "no_writing_permissions", event.getServerTextChannel().get().getName()));
        }
        return false;
    }

    private static void sendOverwrittenSignals(MessageCreateEvent event) {
        ArrayList<Command> list = CommandContainer.getInstance().getMessageForwardInstances();
        for (int i=list.size() - 1; i >= 0; i--) {
            Command command = list.get(i);
            if ((event.getChannel().getId() == command.getForwardChannelID() || command.getForwardChannelID() == -1) && (event.getMessage().getUserAuthor().get().getId() == command.getForwardUserID() || command.getForwardUserID() == -1)) {
                if (command instanceof onForwardedRecievedListener) ((onForwardedRecievedListener)command).onNewActivityOverwrite();
                else if (command instanceof onNavigationListener) ((onNavigationListener)command).onNewActivityOverwrite();
                break;
            }
        }
    }

    private static void cleanPreviousActivities(Server server, User user) {
        ArrayList<Long> openedMessages = new ArrayList<>();

        //Count Forwarded Listeners
        ArrayList<Command> list = CommandContainer.getInstance().getMessageForwardInstances();
        for (Command command : list) {
            Message message = null;
            long activityUserId = command.getReactionUserID();

            if (command instanceof onForwardedRecievedListener)
                message = ((onForwardedRecievedListener) command).getForwardedMessage();
            else if (command instanceof onNavigationListener) message = command.getNavigationMessage();

            if (message != null && message.getServer().isPresent() && message.getServer().get().getId() == server.getId() && activityUserId == user.getId()) {
                long messageID = message.getId();
                if (!openedMessages.contains(messageID)) openedMessages.add(messageID);
            }
        }

        //Count Reaction Listeners
        list = CommandContainer.getInstance().getReactionInstances();
        for (Command command : list) {
            Message message = null;
            long activityUserId = command.getReactionUserID();

            if (command instanceof onReactionAddListener)
                message = ((onReactionAddListener) command).getReactionMessage();
            else if (command instanceof onNavigationListener) message = command.getNavigationMessage();

            if (message != null && message.getServer().isPresent() && message.getServer().get().getId() == server.getId() && activityUserId == user.getId()) {
                long messageID = message.getId();
                if (!openedMessages.contains(messageID)) openedMessages.add(messageID);
            }
        }

        while (openedMessages.size() >= 3) {
            long removeMessageId = openedMessages.get(0);
            openedMessages.remove(0);

            //Remove Forwarded Listeners
            list = CommandContainer.getInstance().getMessageForwardInstances();
            for (Command command : list) {
                Message message = null;

                if (command instanceof onForwardedRecievedListener)
                    message = ((onForwardedRecievedListener) command).getForwardedMessage();
                else if (command instanceof onNavigationListener) message = command.getNavigationMessage();

                if (message != null && removeMessageId == message.getId()) {
                    if (command instanceof onNavigationListener) command.removeNavigation();
                    else command.removeReactionListener(message);
                    break;
                }
            }

            //Remove Reaction Listeners
            list = CommandContainer.getInstance().getReactionInstances();
            for (Command command : list) {
                Message message = null;

                if (command instanceof onReactionAddListener)
                    message = ((onReactionAddListener) command).getReactionMessage();
                else if (command instanceof onNavigationListener) message = command.getNavigationMessage();

                if (message != null && removeMessageId == message.getId()) {
                    if (command instanceof onNavigationListener) command.removeNavigation();
                    else command.removeMessageForwarder();
                    break;
                }
            }
        }
    }

    private static void manageSlowCommandLoadingReaction(Command command, Message userMessage) {
        final Thread commandThread = Thread.currentThread();
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(1000);
                if (RunningCommandManager.getInstance().isActive(userMessage.getUserAuthor().get().getId(), commandThread)) command.addLoadingReaction();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t.setName("command_slow_loading_reaction_countdown");
        t.start();
    }

    public static Command createCommandByTrigger(String trigger, Locale locale, String prefix) throws IllegalAccessException, InstantiationException {
        Class<? extends Command> clazz = CommandContainer.getInstance().getCommands().get(trigger);
        if (clazz == null) return null;
        return createCommandByClass(clazz, locale, prefix);
    }


    public static Command createCommandByClassName(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return (Command) Class.forName(className).newInstance();
    }

    public static Command createCommandByClassName(String className, Locale locale) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Command command = createCommandByClassName(className);
        command.setLocale(locale);

        return command;
    }

    public static Command createCommandByClassName(String className, Locale locale, String prefix) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Command command = createCommandByClassName(className, locale);
        command.setPrefix(prefix);

        return command;
    }


    public static Command createCommandByClass(Class<? extends Command> clazz) throws IllegalAccessException, InstantiationException {
        return clazz.newInstance();
    }

    public static Command createCommandByClass(Class<? extends Command> clazz, Locale locale) throws IllegalAccessException, InstantiationException {
        Command command = createCommandByClass(clazz);
        command.setLocale(locale);

        return command;
    }

    public static Command createCommandByClass(Class<? extends Command> clazz, Locale locale, String prefix) throws IllegalAccessException, InstantiationException {
        Command command = createCommandByClass(clazz, locale);
        command.setPrefix(prefix);

        return command;
    }

    public static CommandProperties getCommandProperties(Class<? extends Command> command) {
        return (CommandProperties) command.getAnnotation(CommandProperties.class);
    }

}

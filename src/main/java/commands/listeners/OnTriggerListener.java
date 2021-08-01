package commands.listeners;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import commands.Command;
import commands.CommandContainer;
import commands.runnables.utilitycategory.TriggerDeleteCommand;
import core.MemberCacheController;
import core.PermissionCheckRuntime;
import core.Program;
import core.cache.ServerPatreonBoostCache;
import core.schedule.MainScheduler;
import core.utils.ExceptionUtil;
import mysql.modules.commandusages.DBCommandUsages;
import mysql.modules.guild.DBGuild;
import mysql.modules.guild.GuildData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface OnTriggerListener {

    boolean onTrigger(GuildMessageReceivedEvent event, String args) throws Throwable;

    default boolean processTrigger(GuildMessageReceivedEvent event, String args) throws Throwable {
        Command command = (Command) this;
        AtomicBoolean isProcessing = new AtomicBoolean(true);
        command.setAtomicAssets(event.getChannel(), event.getMember());
        command.setGuildMessageReceivedEvent(event);

        if (Program.isPublicVersion()) {
            DBCommandUsages.getInstance().retrieve(command.getTrigger()).increase();
        }

        command.addLoadingReaction(event.getMessage(), isProcessing);
        addKillTimer(isProcessing);
        processTriggerDelete(event);
        try {
            if (command.getCommandProperties().requiresMemberCache()) {
                MemberCacheController.getInstance().loadMembers(event.getGuild()).get();
            }
            return onTrigger(event, args);
        } catch (Throwable e) {
            ExceptionUtil.handleCommandException(e, command, event.getChannel());
            return false;
        } finally {
            isProcessing.set(false);
        }
    }

    private void addKillTimer(AtomicBoolean isProcessing) {
        Command command = (Command) this;
        Thread commandThread = Thread.currentThread();
        MainScheduler.getInstance().schedule(command.getCommandProperties().maxCalculationTimeSec(), ChronoUnit.SECONDS, "command_timeout", () -> {
            if (!command.getCommandProperties().turnOffTimeout()) {
                CommandContainer.getInstance().addCommandTerminationStatus(command, commandThread, isProcessing.get());
            }
        });
    }

    private void processTriggerDelete(GuildMessageReceivedEvent event) {
        GuildData guildBean = DBGuild.getInstance().retrieve(event.getGuild().getIdLong());
        if (guildBean.isCommandAuthorMessageRemove() &&
                ServerPatreonBoostCache.getInstance().get(event.getGuild().getIdLong()) &&
                PermissionCheckRuntime.getInstance().botHasPermission(guildBean.getLocale(), TriggerDeleteCommand.class, event.getChannel(), Permission.MESSAGE_MANAGE)
        ) {
            event.getMessage().delete().queue();
        }
    }

}
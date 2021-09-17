package commands.runnables;

import commands.Command;
import commands.listeners.OnTriggerListener;
import modules.fishery.FisheryStatus;
import core.EmbedFactory;
import core.TextManager;
import mysql.modules.guild.DBGuild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface FisheryInterface extends OnTriggerListener {

    boolean onFisheryAccess(GuildMessageReceivedEvent event, String args) throws Throwable;

    @Override
    default boolean onTrigger(GuildMessageReceivedEvent event, String args) throws Throwable {
        return onFisheryTrigger(event, args);
    }

    default boolean onFisheryTrigger(GuildMessageReceivedEvent event, String args) throws Throwable {
        Command command = (Command) this;
        FisheryStatus status = DBGuild.getInstance().retrieve(event.getGuild().getIdLong()).getFisheryStatus();
        if (status == FisheryStatus.ACTIVE) {
            return onFisheryAccess(event, args);
        } else {
            command.drawMessageNew(
                    EmbedFactory.getEmbedError(
                            command,
                            TextManager.getString(command.getLocale(), TextManager.GENERAL, "fishing_notactive_description").replace("{PREFIX}", command.getPrefix()),
                            TextManager.getString(command.getLocale(), TextManager.GENERAL, "fishing_notactive_title")
                    )
            );
            return false;
        }
    }

}

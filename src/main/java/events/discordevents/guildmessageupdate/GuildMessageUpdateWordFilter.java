package events.discordevents.guildmessageupdate;

import events.discordevents.DiscordEvent;
import events.discordevents.EventPriority;
import events.discordevents.eventtypeabstracts.GuildMessageUpdateAbstract;
import modules.automod.WordFilter;
import mysql.hibernate.EntityManagerWrapper;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

@DiscordEvent(priority = EventPriority.HIGH, allowBannedUser = true)
public class GuildMessageUpdateWordFilter extends GuildMessageUpdateAbstract {

    @Override
    public boolean onGuildMessageUpdate(MessageUpdateEvent event, EntityManagerWrapper entityManager) throws Throwable {
        return new WordFilter(event.getMessage()).check();
    }

}

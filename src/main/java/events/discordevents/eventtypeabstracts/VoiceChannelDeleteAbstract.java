package events.discordevents.eventtypeabstracts;

import java.util.ArrayList;
import events.discordevents.DiscordEventAbstract;
import mysql.hibernate.EntityManagerWrapper;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;

public abstract class VoiceChannelDeleteAbstract extends DiscordEventAbstract {

    public abstract boolean onVoiceChannelDelete(ChannelDeleteEvent event, EntityManagerWrapper entityManager) throws Throwable;

    public static void onVoiceChannelDeleteStatic(ChannelDeleteEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getGuild().getIdLong(),
                (listener, entityManager) -> ((VoiceChannelDeleteAbstract) listener).onVoiceChannelDelete(event, entityManager)
        );
    }

}

package events.discordevents.eventtypeabstracts;

import core.AsyncTimer;
import core.MainLogger;
import core.utils.ExceptionUtil;
import events.discordevents.DiscordEventAbstract;
import mysql.hibernate.EntityManagerWrapper;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

import java.time.Duration;
import java.util.ArrayList;

public abstract class GuildMessageReactionRemoveAbstract extends DiscordEventAbstract {

    public abstract boolean onGuildMessageReactionRemove(MessageReactionRemoveEvent event, EntityManagerWrapper entityManager) throws Throwable;

    public static void onGuildMessageReactionRemoveStatic(MessageReactionRemoveEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        try(AsyncTimer timeOutTimer = new AsyncTimer(Duration.ofSeconds(30))) {
            timeOutTimer.setTimeOutListener(t -> {
                MainLogger.get().error("Reaction remove \"{}\" of guild {} stuck", event.getEmoji().getAsReactionCode(), event.getGuild().getIdLong(), ExceptionUtil.generateForStack(t));
            });

            execute(listenerList, event.getUser(), event.getGuild().getIdLong(),
                    (listener, entityManager) -> ((GuildMessageReactionRemoveAbstract) listener).onGuildMessageReactionRemove(event, entityManager)
            );
        }
    }

}

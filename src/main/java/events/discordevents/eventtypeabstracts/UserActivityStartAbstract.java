package events.discordevents.eventtypeabstracts;

import java.util.ArrayList;
import events.discordevents.DiscordEventAbstract;
import mysql.hibernate.EntityManagerWrapper;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;

public abstract class UserActivityStartAbstract extends DiscordEventAbstract {

    public abstract boolean onUserActivityStart(UserActivityStartEvent event, EntityManagerWrapper entityManager) throws Throwable;

    public static void onUserActivityStartStatic(UserActivityStartEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getUser(),
                (listener, entityManager) -> ((UserActivityStartAbstract) listener).onUserActivityStart(event, entityManager)
        );
    }

}

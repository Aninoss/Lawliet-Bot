package events.scheduleevents.events;

import core.Program;
import constants.ExceptionRunnable;
import events.scheduleevents.ScheduleEventDaily;
import mysql.modules.botstats.DBBotStats;

@ScheduleEventDaily
public class SaveBotStatsUpvotes implements ExceptionRunnable {

    @Override
    public void run() throws Throwable {
        if (Program.productionMode() && Program.publicVersion() && Program.getClusterId() == 1) {
            DBBotStats.saveStatsUpvotes();
        }
    }

}

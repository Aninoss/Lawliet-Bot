package events.scheduleevents.events;

import java.time.temporal.ChronoUnit;
import core.Program;
import core.ShardManager;
import core.schedule.ScheduleInterface;
import events.scheduleevents.ScheduleEventFixedRate;
import websockets.*;

@ScheduleEventFixedRate(rateValue = 5, rateUnit = ChronoUnit.MINUTES)
public class SendBotGuildCount implements ScheduleInterface {

    @Override
    public void run() throws Throwable {
        if (Program.productionMode() && Program.isPublicVersion() && Program.getClusterId() == 1) {
            ShardManager.getInstance().getGlobalGuildSize().ifPresent(totalServers -> {
                TopGG.getInstance().updateServerCount(totalServers);
                Botsfordiscord.updateServerCount(totalServers);
                BotsOnDiscord.updateServerCount(totalServers);
                Discordbotlist.updateServerCount(totalServers);
                Discordbotsgg.updateServerCount(totalServers);
            });
        }
    }

}
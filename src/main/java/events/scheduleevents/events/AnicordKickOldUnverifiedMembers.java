package events.scheduleevents.events;

import constants.AssetIds;
import core.Bot;
import core.ShardManager;
import core.MainLogger;
import core.schedule.ScheduleInterface;
import events.scheduleevents.ScheduleEventHourly;
import net.dv8tion.jda.api.entities.Role;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@ScheduleEventHourly
public class AnicordKickOldUnverifiedMembers implements ScheduleInterface {

    @Override
    public void run() throws Throwable {
        if (Bot.isProductionMode() && Bot.isPublicVersion()) {
            ShardManager.getInstance().getLocalGuildById(AssetIds.ANICORD_SERVER_ID).ifPresent(guild -> {
                Role memberRole = guild.getRoleById(462410205288726531L);
                AtomicInteger counter = new AtomicInteger(0);
                guild.getMembers().forEach(member -> {
                    if (!member.getRoles().contains(memberRole) &&
                            !member.getUser().isBot() &&
                            member.hasTimeJoined() &&
                            member.getTimeJoined().toInstant().isBefore(Instant.now().minus(Duration.ofDays(3)))
                    ) {
                        MainLogger.get().info("Kicked Unverified Member: " + member.getUser().getAsTag());
                        counter.incrementAndGet();
                        guild.kick(member).reason("Unverified").queue();
                    }
                });
                MainLogger.get().info("Removed Members: " + counter.get());
            });
        }
    }

}

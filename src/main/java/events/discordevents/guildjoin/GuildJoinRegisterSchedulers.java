package events.discordevents.guildjoin;

import core.Program;
import events.discordevents.DiscordEvent;
import events.discordevents.eventtypeabstracts.GuildJoinAbstract;
import modules.schedulers.*;
import mysql.hibernate.EntityManagerWrapper;
import mysql.hibernate.entity.guild.GuildEntity;
import mysql.modules.giveaway.DBGiveaway;
import mysql.modules.giveaway.GiveawayData;
import mysql.modules.jails.DBJails;
import mysql.modules.servermute.DBServerMute;
import mysql.modules.tempban.DBTempBan;
import mysql.modules.tracker.DBTracker;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;

@DiscordEvent
public class GuildJoinRegisterSchedulers extends GuildJoinAbstract {

    @Override
    public boolean onGuildJoin(GuildJoinEvent event, EntityManagerWrapper entityManager) {
        if (!Program.publicVersion()) {
            GuildEntity guildEntity = entityManager.findGuildEntity(event.getGuild().getIdLong());

            DBGiveaway.getInstance().retrieve(event.getGuild().getIdLong()).values().stream()
                    .filter(GiveawayData::isActive)
                    .forEach(GiveawayScheduler::loadGiveawayBean);
            DBJails.getInstance().retrieve(event.getGuild().getIdLong()).values()
                    .forEach(JailScheduler::loadJail);
            guildEntity.getReminders()
                    .forEach(ReminderScheduler::loadReminder);
            DBServerMute.getInstance().retrieve(event.getGuild().getIdLong()).values()
                    .forEach(ServerMuteScheduler::loadServerMute);
            DBTempBan.getInstance().retrieve(event.getGuild().getIdLong()).values()
                    .forEach(TempBanScheduler::loadTempBan);
            DBTracker.getInstance().retrieve(event.getGuild().getIdLong()).values()
                    .forEach(AlertScheduler::loadAlert);
        }
        return true;
    }

}

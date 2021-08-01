package events.discordevents.guildmemberremove;

import constants.FisheryStatus;
import events.discordevents.DiscordEvent;
import events.discordevents.eventtypeabstracts.GuildMemberRemoveAbstract;
import mysql.modules.fisheryusers.DBFishery;
import mysql.modules.fisheryusers.FisheryGuildData;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;

@DiscordEvent
public class ServerMemberLeaveFisheryOnGuildStatus extends GuildMemberRemoveAbstract {

    @Override
    public boolean onGuildMemberRemove(GuildMemberRemoveEvent event) {
        FisheryGuildData fisheryGuildBean = DBFishery.getInstance().retrieve(event.getGuild().getIdLong());
        if (fisheryGuildBean.getGuildData().getFisheryStatus() == FisheryStatus.STOPPED) {
            return true;
        }
        //TODO: remove processed recent fish gains
        return true;
    }

}

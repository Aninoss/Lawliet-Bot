package modules;

import java.util.Locale;
import commands.Command;
import commands.runnables.configurationcategory.PrefixCommand;
import core.utils.BotPermissionUtil;
import core.utils.StringUtil;
import mysql.hibernate.EntityManagerWrapper;
import mysql.hibernate.entity.GuildEntity;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class Prefix {

    public static void changePrefix(Guild guild, Locale locale, String prefix, EntityManagerWrapper entityManager) {
        GuildEntity guildEntity = entityManager.findGuildEntity(guild.getIdLong());
        entityManager.getTransaction().begin();
        guildEntity.setPrefix(prefix);
        entityManager.getTransaction().commit();

        if (BotPermissionUtil.can(guild, Permission.NICKNAME_CHANGE)) {
            Member self = guild.getSelfMember();
            String nickname = self.getEffectiveName().trim();
            String[] nicknameArray = nickname.split("\\[");

            String effectiveNickname;
            if (nicknameArray.length == 2 && nicknameArray[1].contains("]")) {
                effectiveNickname = nicknameArray[0].trim() + " [" + prefix + "]";
            } else {
                effectiveNickname = nickname + " [" + prefix + "]";
            }

            guild.modifyNickname(self, StringUtil.shortenString(effectiveNickname, 32))
                    .reason(Command.getCommandLanguage(PrefixCommand.class, locale).getTitle())
                    .queue();
        }
    }

}

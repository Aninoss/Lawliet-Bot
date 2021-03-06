package modules.automod;

import commands.Command;
import commands.runnables.moderationcategory.InviteFilterCommand;
import constants.AssetIds;
import constants.Category;
import core.PermissionCheckRuntime;
import core.TextManager;
import core.cache.InviteCache;
import core.utils.BotPermissionUtil;
import core.utils.JDAUtil;
import mysql.modules.spblock.DBSPBlock;
import mysql.modules.spblock.SPBlockBean;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class InviteFilter extends AutoModAbstract {

    private final SPBlockBean spBlockBean;

    public InviteFilter(Message message) throws ExecutionException {
        super(message);
        spBlockBean = DBSPBlock.getInstance().getBean(message.getGuild().getIdLong());
    }

    @Override
    protected boolean withAutoActions(Message message, Locale locale) {
        if (spBlockBean.getAction() == SPBlockBean.ActionList.BAN_USER &&
                PermissionCheckRuntime.getInstance().botHasPermission(locale, getCommandClass(), message.getTextChannel(), Permission.BAN_MEMBERS)
        ) {
            message.getGuild()
                    .ban(message.getMember(), 0, TextManager.getString(spBlockBean.getServerBean().getLocale(), Category.MODERATION, "invitefilter_auditlog_sp"))
                    .queue();
            return false;
        } else if (spBlockBean.getAction() == SPBlockBean.ActionList.KICK_USER &&
                PermissionCheckRuntime.getInstance().botHasPermission(locale, getCommandClass(), message.getTextChannel(), Permission.KICK_MEMBERS)
        ) {
            message.getGuild()
                    .kick(message.getMember(), TextManager.getString(spBlockBean.getServerBean().getLocale(), Category.MODERATION, "invitefilter_auditlog_sp"))
                    .queue();
            return false;
        }

        return true;
    }

    @Override
    protected void designEmbed(Message message, Locale locale, EmbedBuilder eb) {
        eb.setDescription(TextManager.getString(locale, Category.MODERATION, "invitefilter_log", message.getAuthor().getAsTag()))
                .addField(TextManager.getString(locale, Category.MODERATION, "invitefilter_state0_maction"), TextManager.getString(locale, Category.MODERATION, "invitefilter_state0_mactionlist").split("\n")[spBlockBean.getAction().ordinal()], true)
                .addField(TextManager.getString(locale, Category.MODERATION, "invitefilter_log_channel"), message.getTextChannel().getAsMention(), true);

        spBlockBean.getLogReceiverUserIds().transform(message.getGuild()::getMemberById, ISnowflake::getIdLong)
                .forEach(member -> JDAUtil.sendPrivateMessage(member, eb.build()).queue());
    }

    @Override
    protected Class<? extends Command> getCommandClass() {
        return InviteFilterCommand.class;
    }

    @Override
    protected boolean checkCondition(Message message) {
        if (spBlockBean.isActive() &&
                !spBlockBean.getIgnoredUserIds().contains(message.getAuthor().getIdLong()) &&
                !spBlockBean.getIgnoredChannelIds().contains(message.getAuthor().getIdLong()) &&
                !BotPermissionUtil.can(message.getMember(), Permission.ADMINISTRATOR)
        ) {
            List<String> inviteLinks = message.getInvites();
            if (inviteLinks.size() > 0) {
                return inviteLinks.stream()
                        .map(inviteCode -> InviteCache.getInstance().getInviteByCode(inviteCode))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .anyMatch(invite -> invite.getGuild() != null &&
                                invite.getGuild().getIdLong() != message.getGuild().getIdLong() &&
                                invite.getGuild().getIdLong() != AssetIds.SUPPORT_SERVER_ID
                        );
            }
        }

        return false;
    }

}

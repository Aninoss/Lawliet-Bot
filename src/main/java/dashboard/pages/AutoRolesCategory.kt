package dashboard.pages

import commands.Category
import commands.Command
import commands.runnables.utilitycategory.AutoRolesCommand
import core.TextManager
import dashboard.ActionResult
import dashboard.DashboardCategory
import dashboard.DashboardProperties
import dashboard.component.DashboardButton
import dashboard.component.DashboardSeparator
import dashboard.component.DashboardText
import dashboard.components.DashboardMultiRolesComboBox
import dashboard.container.HorizontalContainer
import dashboard.container.HorizontalPusher
import dashboard.container.VerticalContainer
import modules.RoleAssigner
import mysql.hibernate.entity.BotLogEntity
import mysql.hibernate.entity.guild.GuildEntity
import mysql.modules.autoroles.DBAutoRoles
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import java.util.*

@DashboardProperties(
        id = "autoroles",
        userPermissions = [Permission.MANAGE_ROLES],
        botPermissions = [Permission.MANAGE_ROLES],
        commandAccessRequirements = [AutoRolesCommand::class]
)
class AutoRolesCategory(guildId: Long, userId: Long, locale: Locale, guildEntity: GuildEntity) : DashboardCategory(guildId, userId, locale, guildEntity) {

    override fun retrievePageTitle(): String {
        return Command.getCommandLanguage(AutoRolesCommand::class.java, locale).title
    }

    override fun generateComponents(guild: Guild, mainContainer: VerticalContainer) {
        val descText = DashboardText(getString(Category.UTILITY, "autoroles_state0_description"))
        val rolesComboBox = DashboardMultiRolesComboBox(
                this,
                Command.getCommandLanguage(AutoRolesCommand::class.java, locale).title,
                { DBAutoRoles.getInstance().retrieve(guild.idLong).roleIds },
                true,
                AutoRolesCommand.MAX_ROLES,
                true,
                null,
                BotLogEntity.Event.AUTO_ROLES
        )
        mainContainer.add(descText, rolesComboBox, DashboardSeparator())

        val buttonContainer = HorizontalContainer()
        val syncButton = DashboardButton(getString(Category.UTILITY, "autoroles_dashboard_syncbutton")) {
            val roleList: List<Role> = DBAutoRoles.getInstance().retrieve(guild.idLong).roleIds.mapNotNull { guild.getRoleById(it) }
            val future = RoleAssigner.assignRoles(guild, roleList, true, locale, AutoRolesCommand::class.java)

            if (future.isEmpty) {
                return@DashboardButton ActionResult()
                        .withErrorMessage(getString(Category.UTILITY, "autoroles_syncactive"))
            }

            BotLogEntity.log(entityManager, BotLogEntity.Event.AUTO_ROLES_SYNC, atomicMember)
            return@DashboardButton ActionResult()
                    .withSuccessMessage(getString(Category.UTILITY, "autoroles_syncstart"))
        }
        syncButton.isEnabled = isPremium
        syncButton.style = DashboardButton.Style.PRIMARY
        buttonContainer.add(syncButton, HorizontalPusher())
        mainContainer.add(buttonContainer)

        if (!isPremium) {
            val text = DashboardText(getString(TextManager.GENERAL, "patreon_description_noembed"))
            text.style = DashboardText.Style.ERROR
            mainContainer.add(text)
        }
    }

}
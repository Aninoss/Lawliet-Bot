package dashboard.pages

import commands.Command
import commands.runnables.utilitycategory.AutoRolesCommand
import core.TextManager
import dashboard.DashboardCategory
import dashboard.DashboardProperties
import dashboard.component.DashboardText
import dashboard.container.VerticalContainer
import net.dv8tion.jda.api.entities.Guild
import java.util.*

@DashboardProperties(
    id = "autoroles"
)
class AutoRolesCategory(guildId: Long, userId: Long, locale: Locale) : DashboardCategory(guildId, userId, locale) {

    override fun retrievePageTitle(): String {
        return Command.getCommandLanguage(AutoRolesCommand::class.java, locale).title
    }

    override fun generateComponents(guild: Guild, mainContainer: VerticalContainer) {
        val text = getString(TextManager.GENERAL, "dashboard_wip", Command.getCommandProperties(AutoRolesCommand::class.java).trigger)
        mainContainer.add(DashboardText(text))
    }

}
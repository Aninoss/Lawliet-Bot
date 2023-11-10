package dashboard.pages

import commands.Category
import commands.Command
import commands.CommandContainer
import commands.runnables.utilitycategory.CommandChannelShortcutsCommand
import core.TextManager
import core.atomicassets.AtomicStandardGuildMessageChannel
import core.atomicassets.AtomicTextChannel
import core.utils.BotPermissionUtil
import dashboard.ActionResult
import dashboard.DashboardCategory
import dashboard.DashboardComponent
import dashboard.DashboardProperties
import dashboard.component.*
import dashboard.container.HorizontalContainer
import dashboard.container.VerticalContainer
import dashboard.data.DiscordEntity
import dashboard.data.GridRow
import mysql.hibernate.entity.GuildEntity
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import java.util.*

@DashboardProperties(
        id = "ccshortcuts",
        userPermissions = [Permission.MANAGE_SERVER],
        commandAccessRequirements = [CommandChannelShortcutsCommand::class]
)
class CommandChannelShortcutsCategory(guildId: Long, userId: Long, locale: Locale, guildEntity: GuildEntity) : DashboardCategory(guildId, userId, locale, guildEntity) {

    var trigger: String? = null
    var channelId: Long? = null

    val commandChannelShortcuts
        get() = guildEntity.commandChannelShortcuts

    override fun retrievePageTitle(): String {
        return Command.getCommandLanguage(CommandChannelShortcutsCommand::class.java, locale).title
    }

    override fun generateComponents(guild: Guild, mainContainer: VerticalContainer) {
        mainContainer.add(
                DashboardText(getString(Category.UTILITY, "ccshortcuts_default_desc")),
                generateShortcutGrid(guild),
                generateNewShortcutField(guild)
        )
    }

    fun generateShortcutGrid(guild: Guild): DashboardComponent {
        val rows = commandChannelShortcuts.entries
                .map {
                    val atomicChannel = AtomicStandardGuildMessageChannel(guild.idLong, it.key)
                    val values = arrayOf(atomicChannel.getPrefixedName(locale), it.value)
                    GridRow(it.key.toString(), values)
                }

        val headers = arrayOf(getString(Category.UTILITY, "ccshortcuts_add_channel"), getString(Category.UTILITY, "ccshortcuts_add_command"))
        val grid = DashboardGrid(headers, rows) {
            guildEntity.beginTransaction()
            commandChannelShortcuts.remove(it.data.toLong())
            guildEntity.commitTransaction()

            ActionResult()
                    .withRedraw()
        }
        grid.isEnabled = isPremium
        grid.rowButton = getString(Category.UTILITY, "ccshortcuts_dashboard_remove")
        grid.enableConfirmationMessage(getString(Category.UTILITY, "ccshortcuts_dashboard_gridconfirm"))

        return grid
    }

    fun generateNewShortcutField(guild: Guild): DashboardComponent {
        val container = VerticalContainer()
        container.add(
                DashboardTitle(getString(Category.UTILITY, "ccshortcuts_add_title")),
        )

        val horizontalContainerer = HorizontalContainer()
        horizontalContainerer.alignment = HorizontalContainer.Alignment.BOTTOM
        horizontalContainerer.allowWrap = true

        val channelLabel = getString(Category.UTILITY, "ccshortcuts_add_channel")
        val channelComboBox = DashboardComboBox(channelLabel, DashboardComboBox.DataType.TEXT_CHANNELS, false, 1) {
            channelId = it.data.toLong()
            ActionResult()
                    .withRedraw()
        }
        channelComboBox.isEnabled = isPremium
        if (channelId != null) {
            val atomicChannel = AtomicTextChannel(atomicGuild.idLong, channelId!!)
            channelComboBox.selectedValues = listOf(DiscordEntity(channelId.toString(), atomicChannel.getPrefixedName(locale)))
        }
        horizontalContainerer.add(channelComboBox)

        val commandLabel = getString(Category.UTILITY, "ccshortcuts_add_command")
        val commandValues = CommandContainer.getFullCommandList()
                .map {
                    val trigger = Command.getCommandProperties(it).trigger
                    DiscordEntity(trigger, trigger)
                }
                .sortedBy { it.id }
        val commandComboBox = DashboardComboBox(commandLabel, commandValues, false, 1) {
            trigger = it.data
            ActionResult()
                    .withRedraw()
        }
        commandComboBox.isEnabled = isPremium
        if (trigger != null) {
            commandComboBox.selectedValues = listOf(DiscordEntity(trigger!!, trigger!!))
        }
        horizontalContainerer.add(commandComboBox)

        val addButton = DashboardButton(getString(Category.UTILITY, "ccshortcuts_dashboard_add")) {
            if (!isPremium || channelId == null || trigger == null) {
                return@DashboardButton ActionResult()
                        .withRedraw()
            }

            val channel = channelId?.let { guild.getTextChannelById(it.toString()) }
            if (channel == null) { /* invalid channel */
                return@DashboardButton ActionResult()
                        .withRedraw()
            }
            if (!BotPermissionUtil.canWrite(channel)) { /* no permissions in channel */
                return@DashboardButton ActionResult()
                        .withErrorMessage(getString(TextManager.GENERAL, "permission_channel_send", "#${channel.getName()}"))
            }
            if (commandChannelShortcuts.containsKey(channelId)) {
                return@DashboardButton ActionResult()
                        .withErrorMessage(getString(Category.UTILITY, "ccshortcuts_log_channel_exist"))
            }

            guildEntity.beginTransaction()
            commandChannelShortcuts.put(channelId!!, trigger!!)
            guildEntity.commitTransaction()

            channelId = null
            trigger = null

            ActionResult()
                    .withRedraw()
        }
        addButton.isEnabled = isPremium && trigger != null && channelId != null
        addButton.style = DashboardButton.Style.PRIMARY

        horizontalContainerer.add(addButton)
        container.add(horizontalContainerer)

        if (!isPremium) {
            val text = DashboardText(getString(TextManager.GENERAL, "patreon_description_noembed"))
            text.style = DashboardText.Style.ERROR
            container.add(text)
        }
        return container
    }

}
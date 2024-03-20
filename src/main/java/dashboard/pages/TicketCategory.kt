package dashboard.pages

import commands.Category
import commands.Command
import commands.runnables.configurationcategory.TicketCommand
import core.TextManager
import core.utils.BotPermissionUtil
import dashboard.ActionResult
import dashboard.DashboardCategory
import dashboard.DashboardComponent
import dashboard.DashboardProperties
import dashboard.component.*
import dashboard.components.DashboardChannelComboBox
import dashboard.components.DashboardMultiRolesComboBox
import dashboard.container.HorizontalContainer
import dashboard.container.VerticalContainer
import dashboard.data.DiscordEntity
import modules.Ticket
import mysql.hibernate.entity.BotLogEntity
import mysql.hibernate.entity.guild.GuildEntity
import mysql.hibernate.entity.guild.TicketsEntity
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import java.util.*

@DashboardProperties(
        id = "ticket",
        userPermissions = [Permission.MANAGE_CHANNEL],
        botPermissions = [Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES],
        commandAccessRequirements = [TicketCommand::class]
)
class TicketCategory(guildId: Long, userId: Long, locale: Locale, guildEntity: GuildEntity) : DashboardCategory(guildId, userId, locale, guildEntity) {

    var createMessageChannelId: Long? = null

    val ticketsEntity: TicketsEntity
        get() = guildEntity.tickets

    override fun retrievePageTitle(): String {
        return Command.getCommandLanguage(TicketCommand::class.java, locale).title
    }

    override fun generateComponents(guild: Guild, mainContainer: VerticalContainer) {
        mainContainer.add(
                generateTicketMessageField(),
                generateGeneralField(),
                generateTicketCreateOptionsField(),
                generateTicketCloseOptionsField(),
                generateAutoCloseOnInactivityField()
        )
    }

    private fun generateTicketMessageField(): DashboardComponent {
        val container = VerticalContainer()
        container.isCard = true
        container.putCssProperties("margin-bottom", "1rem")

        val innerContainer = HorizontalContainer()
        innerContainer.alignment = HorizontalContainer.Alignment.CENTER
        innerContainer.allowWrap = true

        val channelComboBox = DashboardChannelComboBox(
                this,
                "",
                DashboardComboBox.DataType.STANDARD_GUILD_MESSAGE_CHANNELS,
                createMessageChannelId,
                false
        ) {
            createMessageChannelId = it.data.toLong()
            ActionResult()
                    .withRedraw()
        }
        channelComboBox.putCssProperties("margin-top", "0.5em")

        val sendButton = DashboardButton(getString(category = Category.CONFIGURATION, "ticket_state4_title")) {
            val error = Ticket.sendTicketMessage(guildEntity, locale, atomicGuild.get().get().getChannelById(StandardGuildMessageChannel::class.java, createMessageChannelId!!))
            if (error == null) {
                entityManager.transaction.begin()
                BotLogEntity.log(entityManager, BotLogEntity.Event.TICKETS_CREATE_TICKET_MESSAGE, atomicMember, createMessageChannelId!!)
                entityManager.transaction.commit()

                return@DashboardButton ActionResult()
                        .withSuccessMessage(getString(Category.CONFIGURATION, "ticket_message_sent"))
            } else {
                return@DashboardButton ActionResult()
                        .withErrorMessage(error)
            }
        }
        sendButton.isEnabled = createMessageChannelId != null
        sendButton.style = DashboardButton.Style.PRIMARY
        sendButton.setCanExpand(false)

        innerContainer.add(
                channelComboBox,
                sendButton
        )
        container.add(innerContainer)
        return container
    }

    private fun generateGeneralField(): DashboardComponent {
        val container = VerticalContainer()

        val logChannel = DashboardChannelComboBox(
                this,
                getString(Category.CONFIGURATION, "ticket_state0_mannouncement"),
                DashboardComboBox.DataType.GUILD_MESSAGE_CHANNELS,
                ticketsEntity.logChannelId,
                true
        ) { e ->
            val channel = if (e.data != null) {
                atomicGuild.get().map { it.getChannelById(GuildMessageChannel::class.java, e.data.toLong()) }
                        .orElse(null)
            } else {
                null
            }

            if (channel != null) {
                val channelMissingPerms = BotPermissionUtil.getBotPermissionsMissingText(locale, channel, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS)
                if (channelMissingPerms != null) {
                    return@DashboardChannelComboBox ActionResult()
                            .withRedraw()
                            .withErrorMessage(channelMissingPerms)
                }
            }

            ticketsEntity.beginTransaction()
            BotLogEntity.log(entityManager, BotLogEntity.Event.TICKETS_LOG_CHANNEL, atomicMember, ticketsEntity.logChannelId, channel?.idLong)
            ticketsEntity.logChannelId = channel?.idLong
            ticketsEntity.commitTransaction()
            return@DashboardChannelComboBox ActionResult()
        }
        container.add(logChannel)

        val staffContainer = HorizontalContainer()
        staffContainer.allowWrap = true

        val staffRoles = DashboardMultiRolesComboBox(
                this,
                getString(Category.CONFIGURATION, "ticket_state0_mstaffroles"),
                { it.tickets.staffRoleIds },
                true,
                TicketCommand.MAX_STAFF_ROLES,
                false,
                null,
                BotLogEntity.Event.TICKETS_STAFF_ROLES
        )
        staffContainer.add(staffRoles)

        val assignmentValues = TicketsEntity.AssignmentMode.values().mapIndexed() { i, mode ->
            DiscordEntity(i.toString(), getString(Category.CONFIGURATION, "ticket_assignment_modes").split("\n")[i])
        }
        val assignmentMode = DashboardComboBox(
                getString(Category.CONFIGURATION, "ticket_state0_massign"),
                assignmentValues,
                false,
                1
        ) {
            ticketsEntity.beginTransaction()
            BotLogEntity.log(entityManager, BotLogEntity.Event.TICKETS_ASSIGNMENT_MODE, atomicMember, ticketsEntity.assignmentMode, TicketsEntity.AssignmentMode.values()[it.data.toInt()])
            ticketsEntity.assignmentMode = TicketsEntity.AssignmentMode.values()[it.data.toInt()]
            ticketsEntity.commitTransaction()
            return@DashboardComboBox ActionResult()
        }
        assignmentMode.selectedValues = listOf(assignmentValues[ticketsEntity.assignmentMode.ordinal])
        staffContainer.add(assignmentMode)
        container.add(staffContainer)

        return container
    }

    private fun generateAutoCloseOnInactivityField(): DashboardComponent {
        val container = VerticalContainer()
        container.add(DashboardTitle(getString(Category.CONFIGURATION, "ticket_state0_mcloseoninactivity")))

        val active = DashboardSwitch(getString(Category.CONFIGURATION, "ticket_dashboard_active")) {
            ticketsEntity.beginTransaction()
            if (it.data) {
                BotLogEntity.log(entityManager, BotLogEntity.Event.TICKETS_AUTO_CLOSE, atomicMember, ticketsEntity.autoCloseHoursEffectively?.times(60), 60)
                ticketsEntity.autoCloseHours = 1
            } else {
                BotLogEntity.log(entityManager, BotLogEntity.Event.TICKETS_AUTO_CLOSE, atomicMember, ticketsEntity.autoCloseHoursEffectively?.times(60), null)
                ticketsEntity.autoCloseHours = null
            }
            ticketsEntity.commitTransaction()
            return@DashboardSwitch ActionResult()
                    .withRedraw()
        }
        active.isChecked = ticketsEntity.autoCloseHoursEffectively != null
        active.isEnabled = isPremium
        active.enableConfirmationMessage(getString(Category.CONFIGURATION, "ticket_dashboard_autoclose_warning"))
        container.add(active, DashboardSeparator())

        val duration = DashboardDurationField(getString(Category.CONFIGURATION, "ticket_dashboard_autoclose_duration")) {
            ticketsEntity.beginTransaction()
            BotLogEntity.log(entityManager, BotLogEntity.Event.TICKETS_AUTO_CLOSE, atomicMember, ticketsEntity.autoCloseHoursEffectively?.times(60), it.data.toInt())
            ticketsEntity.autoCloseHours = it.data.toInt() / 60
            ticketsEntity.commitTransaction()

            return@DashboardDurationField ActionResult()
                    .withRedraw()
        }
        duration.includeMinutes = false
        duration.enableConfirmationMessage(getString(Category.CONFIGURATION, "ticket_dashboard_autoclose_warning"))
        if (ticketsEntity.autoCloseHoursEffectively != null) {
            duration.value = ticketsEntity.autoCloseHoursEffectively!!.toLong() * 60
        }
        duration.isEnabled = isPremium
        container.add(duration)

        if (!isPremium) {
            val text = DashboardText(getString(TextManager.GENERAL, "patreon_description_noembed"))
            text.style = DashboardText.Style.ERROR
            container.add(text)
        }

        return container
    }

    private fun generateTicketCreateOptionsField(): DashboardComponent {
        val container = VerticalContainer()
        container.add(DashboardTitle(getString(Category.CONFIGURATION, "ticket_state0_mcreateoptions")))

        val pingStaff = DashboardSwitch(getString(Category.CONFIGURATION, "ticket_state0_mping")) {
            ticketsEntity.beginTransaction()
            ticketsEntity.pingStaffRoles = it.data
            BotLogEntity.log(entityManager, BotLogEntity.Event.TICKETS_PING_STAFF_ROLES, atomicMember, null, it.data)
            ticketsEntity.commitTransaction()

            return@DashboardSwitch ActionResult()
        }
        pingStaff.isChecked = ticketsEntity.pingStaffRoles
        container.add(pingStaff, DashboardSeparator())

        val enforceTextInputs = DashboardSwitch(getString(Category.CONFIGURATION, "ticket_state0_mtextinput")) {
            ticketsEntity.beginTransaction()
            ticketsEntity.enforceModal = it.data
            BotLogEntity.log(entityManager, BotLogEntity.Event.TICKETS_ENFORCE_MODAL, atomicMember, null, it.data)
            ticketsEntity.commitTransaction()

            return@DashboardSwitch ActionResult()
        }
        enforceTextInputs.isChecked = ticketsEntity.enforceModal
        container.add(enforceTextInputs, DashboardSeparator())

        val greetingText = DashboardMultiLineTextField(
                getString(Category.CONFIGURATION, "ticket_state0_mcreatemessage"),
                0,
                TicketCommand.MAX_GREETING_TEXT_LENGTH
        ) {
            val newGreetingText = it.data.ifEmpty { null }

            ticketsEntity.beginTransaction()
            BotLogEntity.log(entityManager, BotLogEntity.Event.TICKETS_GREETING_TEXT, atomicMember, ticketsEntity.greetingText, newGreetingText)
            ticketsEntity.greetingText = newGreetingText
            ticketsEntity.commitTransaction()

            return@DashboardMultiLineTextField ActionResult()
        }
        greetingText.value = ticketsEntity.greetingText ?: ""
        container.add(greetingText)

        return container
    }

    private fun generateTicketCloseOptionsField(): DashboardComponent {
        val container = VerticalContainer()
        container.add(DashboardTitle(getString(Category.CONFIGURATION, "ticket_state0_mcloseoptions")))

        val membersCanCloseTickets = DashboardSwitch(getString(Category.CONFIGURATION, "ticket_state0_mmembercanclose")) {
            ticketsEntity.beginTransaction()
            ticketsEntity.membersCanCloseTickets = it.data
            BotLogEntity.log(entityManager, BotLogEntity.Event.TICKETS_MEMBERS_CAN_CLOSE_TICKETS, atomicMember, null, it.data)
            ticketsEntity.commitTransaction()

            return@DashboardSwitch ActionResult()
        }
        membersCanCloseTickets.isChecked = ticketsEntity.membersCanCloseTickets
        container.add(membersCanCloseTickets, DashboardSeparator())

        val saveProtocols = DashboardSwitch(getString(Category.CONFIGURATION, "ticket_dashboard_protocols")) {
            ticketsEntity.beginTransaction()
            ticketsEntity.protocols = it.data
            BotLogEntity.log(entityManager, BotLogEntity.Event.TICKETS_PROTOCOLS, atomicMember, null, it.data)
            ticketsEntity.commitTransaction()

            return@DashboardSwitch ActionResult()
        }
        saveProtocols.isChecked = ticketsEntity.protocolsEffectively
        saveProtocols.isEnabled = isPremium
        container.add(saveProtocols, DashboardSeparator())

        val deleteChannels = DashboardSwitch(getString(Category.CONFIGURATION, "ticket_state0_mdeletechannel")) {
            ticketsEntity.beginTransaction()
            ticketsEntity.deleteChannelsOnClose = it.data
            BotLogEntity.log(entityManager, BotLogEntity.Event.TICKETS_DELETE_CHANNELS_ON_CLOSE, atomicMember, null, it.data)
            ticketsEntity.commitTransaction()

            return@DashboardSwitch ActionResult()
        }
        deleteChannels.isChecked = ticketsEntity.deleteChannelsOnClose
        container.add(deleteChannels)

        return container
    }

}
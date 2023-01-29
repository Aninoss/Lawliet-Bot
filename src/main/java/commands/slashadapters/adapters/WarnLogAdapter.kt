package commands.slashadapters.adapters

import commands.runnables.moderationcategory.WarnLogCommand
import commands.slashadapters.Slash
import commands.slashadapters.SlashAdapter
import commands.slashadapters.SlashMeta
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

@Slash(command = WarnLogCommand::class)
class WarnLogAdapter : SlashAdapter() {

    public override fun addOptions(commandData: SlashCommandData): SlashCommandData {
        return commandData.addOptions(
            generateOptionData(OptionType.USER, "member", "warnlog_member", false),
            generateOptionData(OptionType.STRING, "member_id", "warnlog_member", false),
            generateOptionData(OptionType.INTEGER, "page", "warnlog_page", false)
        )
    }

    override fun process(event: SlashCommandInteractionEvent): SlashMeta {
        return SlashMeta(WarnLogCommand::class.java, collectArgs(event))
    }

}
package commands.slashadapters.adapters

import commands.runnables.aitoyscategory.Waifu2xCommand
import commands.slashadapters.Slash
import commands.slashadapters.SlashMeta
import mysql.hibernate.entity.GuildEntity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Slash(command = Waifu2xCommand::class)
class Waifu2xAdapter : AIAdapterAbstract() {

    override fun process(event: SlashCommandInteractionEvent, guildEntity: GuildEntity): SlashMeta {
        return SlashMeta(Waifu2xCommand::class.java, collectArgs(event))
    }

}
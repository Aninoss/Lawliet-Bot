package commands.slashadapters.adapters;

import commands.runnables.externalcategory.AnimeNewsCommand;
import commands.slashadapters.Slash;
import commands.slashadapters.SlashAdapter;
import commands.slashadapters.SlashMeta;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Slash(command = AnimeNewsCommand.class)
public class AnimeNewsAdapter extends SlashAdapter {

    public CommandData addOptions(CommandData commandData) {
        return commandData;
    }

    @Override
    public SlashMeta process(SlashCommandEvent event) {
        return new SlashMeta(AnimeNewsCommand.class, "");
    }

}

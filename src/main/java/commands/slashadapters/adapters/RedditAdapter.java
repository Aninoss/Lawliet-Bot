package commands.slashadapters.adapters;

import commands.runnables.externalcategory.RedditCommand;
import commands.slashadapters.Slash;
import commands.slashadapters.SlashAdapter;
import commands.slashadapters.SlashMeta;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Slash(command = RedditCommand.class)
public class RedditAdapter extends SlashAdapter {

    public CommandData addOptions(CommandData commandData) {
        OptionData orderBy = new OptionData(OptionType.STRING, "sort_by", "Sort the posts of a subreddit", false)
                .addChoice("hot", "hot")
                .addChoice("new", "new")
                .addChoice("top", "top")
                .addChoice("rising", "rising");

        return commandData
                .addOption(OptionType.STRING, "subreddit", "View a specific subreddit", true)
                .addOptions(orderBy);
    }

    @Override
    public SlashMeta process(SlashCommandEvent event) {
        String args = event.getOption("subreddit").getAsString();
        if (event.getOption("sort_by") != null) {
            args += "/" + event.getOption("sort_by").getAsString();
        }

        return new SlashMeta(RedditCommand.class, args);
    }

}

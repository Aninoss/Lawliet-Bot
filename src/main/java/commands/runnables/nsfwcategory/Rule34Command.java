package commands.runnables.nsfwcategory;

import java.util.Locale;
import commands.listeners.CommandProperties;
import commands.runnables.PornSearchAbstract;

@CommandProperties(
        trigger = "r34",
        executableWithoutArgs = true,
        emoji = "\uD83D\uDD1E",
        nsfw = true,
        maxCalculationTimeSec = 5 * 60,
        requiresEmbeds = false,
        aliases = { "rule34", "34" }
)
public class Rule34Command extends PornSearchAbstract {

    public Rule34Command(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    protected String getDomain() {
        return "rule34.xxx";
    }

    @Override
    public boolean isExplicit() {
        return true;
    }

}

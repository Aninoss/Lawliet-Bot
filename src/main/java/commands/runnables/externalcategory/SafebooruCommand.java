package commands.runnables.externalcategory;

import java.util.Locale;
import commands.listeners.CommandProperties;
import commands.runnables.PornSearchAbstract;

@CommandProperties(
        trigger = "safeb",
        emoji = "\uD83D\uDE07",
        executableWithoutArgs = true,
        maxCalculationTimeSec = 5 * 60,
        requiresEmbeds = false,
        aliases = { "softb", "safebooru", "softbooru", "sbooru" }
)
public class SafebooruCommand extends PornSearchAbstract {

    public SafebooruCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    protected String getDomain() {
        return "safebooru.org";
    }

    @Override
    public boolean isExplicit() {
        return false;
    }

}

package commands.runnables.nsfwcategory;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import commands.listeners.CommandProperties;
import commands.runnables.GelbooruAbstract;

@CommandProperties(
        trigger = "hdick",
        executableWithoutArgs = true,
        emoji = "\uD83D\uDD1E",
        nsfw = true,
        maxCalculationTimeSec = 5 * 60,
        requiresEmbeds = false,
        aliases = { "hentaidick", "hcock", "hentaicock", "hpenis", "hentaipenis" }
)
public class HentaiDick extends GelbooruAbstract {

    public HentaiDick(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    protected String getSearchKey() {
        return "penis male_focus";
    }

    @Override
    protected Set<String> getAdditionalFilters() {
        HashSet<String> filters = new HashSet<>(super.getAdditionalFilters());
        filters.addAll(Set.of("multiple_boys", "sex", "anal", "2boys", "censored", "trap", "futa", "futanari", "fellatio",
                "crossdressing", "otoko_no_ko", "vaginal", "pussy"));
        return filters;
    }

    @Override
    protected boolean isAnimatedOnly() {
        return false;
    }

}
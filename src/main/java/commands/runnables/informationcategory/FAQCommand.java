package commands.runnables.informationcategory;

import java.util.ArrayList;
import java.util.Locale;
import commands.CommandEvent;
import commands.listeners.CommandProperties;
import commands.runnables.ListAbstract;
import core.TextManager;
import javafx.util.Pair;

@CommandProperties(
        trigger = "faq",
        emoji = "❔",
        executableWithoutArgs = true
)
public class FAQCommand extends ListAbstract {

    private ArrayList<Pair<String, String>> slots;

    public FAQCommand(Locale locale, String prefix) {
        super(locale, prefix, 3);
    }

    @Override
    public boolean onTrigger(CommandEvent event, String args) throws Throwable {
        slots = new ArrayList<>();
        for (int i = 0; i < TextManager.getKeySize(TextManager.FAQ) / 2; i++) {
            String question = TextManager.getString(getLocale(), TextManager.FAQ, String.format("faq.%d.question", i)).replace("{PREFIX}", getPrefix());
            String answer = TextManager.getString(getLocale(), TextManager.FAQ, String.format("faq.%d.answer", i)).replace("{PREFIX}", getPrefix());
            slots.add(new Pair<>(question, answer));
        }

        registerList(event.getMember(), slots.size(), args);
        return true;
    }

    protected Pair<String, String> getEntry(int i) {
        Pair<String, String> slot = slots.get(i);
        return new Pair<>(getString("question", slot.getKey()), slot.getValue());
    }

}
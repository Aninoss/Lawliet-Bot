package commands.stateprocessor;

import commands.listeners.MessageInputResponse;
import commands.runnables.NavigationAbstract;
import constants.LogStatus;
import core.EmbedFactory;
import core.TextManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class AbstractStateProcessor<T> {

    public static final String BUTTON_ID_CLEAR = "clear";

    private final NavigationAbstract command;
    private final int state;
    private final int stateBack;
    private final String propertyName;
    private final String description;
    private final boolean clearButton;
    private final Consumer<T> setter;

    public AbstractStateProcessor(NavigationAbstract command, int state, int stateBack, String propertyName, String description, boolean clearButton, Consumer<T> setter) {
        this.command = command;
        this.state = state;
        this.stateBack = stateBack;
        this.propertyName = propertyName;
        this.description = description;
        this.clearButton = clearButton;
        this.setter = setter;
    }

    public int getState() {
        return state;
    }

    public MessageInputResponse controllerMessage(MessageReceivedEvent event, String input) throws Throwable {
        return null;
    }

    public boolean controllerButton(ButtonInteractionEvent event, int i) throws Throwable {
        if (i == -1) {
            command.setState(stateBack);
            return true;
        }
        if (clearButton && event.getComponentId().equals(BUTTON_ID_CLEAR)) {
            set(null);
            return true;
        }
        return false;
    }

    public boolean controllerStringSelectMenu(StringSelectInteractionEvent event, int i) throws Throwable {
        return false;
    }

    public boolean controllerEntitySelectMenu(EntitySelectInteractionEvent event) throws Throwable {
        return false;
    }

    public EmbedBuilder draw(Member member) throws Throwable {
        ArrayList<ActionRow> actionRows = new ArrayList<>();
        addActionRows(actionRows);
        if (clearButton) {
            Button button = Button.of(ButtonStyle.PRIMARY, BUTTON_ID_CLEAR, TextManager.getString(command.getLocale(), TextManager.COMMANDS, "stateprocessor_clear"));
            actionRows.add(ActionRow.of(button));
        }

        if (!actionRows.isEmpty()) {
            command.setActionRows(actionRows);
        }
        return EmbedFactory.getEmbedDefault(command, description, TextManager.getString(command.getLocale(), TextManager.COMMANDS, "stateprocessor_adjust", propertyName));
    }

    protected NavigationAbstract getCommand() {
        return command;
    }

    protected void set(T t) {
        setter.accept(t);
        command.setLog(LogStatus.SUCCESS, TextManager.getString(command.getLocale(), TextManager.COMMANDS, "stateprocessor_log_success", propertyName));
        command.setState(stateBack);
    }

    protected void addActionRows(ArrayList<ActionRow> actionRows) {
    }

}

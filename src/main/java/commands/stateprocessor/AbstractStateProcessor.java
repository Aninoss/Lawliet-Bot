package commands.stateprocessor;

import commands.listeners.MessageInputResponse;
import commands.runnables.NavigationAbstract;
import constants.LogStatus;
import core.EmbedFactory;
import core.TextManager;
import mysql.hibernate.EntityManagerWrapper;
import mysql.hibernate.entity.BotLogEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.glassfish.jersey.internal.util.Producer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractStateProcessor<T, U extends AbstractStateProcessor<T, U>> {

    public static final String BUTTON_ID_CLEAR = "clear";

    private final NavigationAbstract command;
    private final int state;
    private final int stateBack;
    private final String propertyName;
    private String description;
    private boolean clearButton = false;
    private BotLogEntity.Event logEvent = null;
    private boolean hibernateTransaction = false;
    private Producer<T> getter = () -> null;
    private Consumer<T> setter = newValue -> {
    };

    public AbstractStateProcessor(NavigationAbstract command, int state, int stateBack, String propertyName, String description) {
        this.command = command;
        this.state = state;
        this.stateBack = stateBack;
        this.propertyName = propertyName;
        this.description = description;
    }

    public int getState() {
        return state;
    }

    protected NavigationAbstract getCommand() {
        return command;
    }

    public U setDescription(String description) {
        this.description = description;
        return (U) this;
    }

    public U setClearButton(boolean clearButton) {
        this.clearButton = clearButton;
        return (U) this;
    }

    public U setLogEvent(BotLogEntity.Event logEvent) {
        this.logEvent = logEvent;
        if (logEvent != null) {
            enableHibernateTransaction();
        }
        return (U) this;
    }

    public U enableHibernateTransaction() {
        this.hibernateTransaction = true;
        return (U) this;
    }

    public U setGetter(Producer<T> getter) {
        this.getter = getter;
        return (U) this;
    }

    public U setSetter(Consumer<T> setter) {
        this.setter = setter;
        return (U) this;
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

    protected T get() {
        return getter.call();
    }

    protected void set(T t) {
        EntityManagerWrapper entityManager = command.getEntityManager();
        if (hibernateTransaction) {
            entityManager.getTransaction().begin();
        }

        T old = getter.call();
        if (logEvent != null) {
            if (t instanceof List<?>) {
                addBotLogEntryForList(entityManager, command.getGuildId().get(), command.getMemberId().get(), (List<?>) old, (List<?>) t);
            } else {
                addBotLogEntry(entityManager, command.getGuildId().get(), command.getMemberId().get(), old, t);
            }
        }

        setter.accept(t);
        if (hibernateTransaction) {
            entityManager.getTransaction().commit();
        }

        if (!Objects.equals(getter.call(), old)) {
            command.setLog(LogStatus.SUCCESS, TextManager.getString(command.getLocale(), TextManager.COMMANDS, "stateprocessor_log_success", propertyName));
        } else {
            command.setLog(LogStatus.FAILURE, TextManager.getString(command.getLocale(), TextManager.COMMANDS, "stateprocessor_log_notchanged", propertyName));
        }
        command.setState(stateBack);
    }

    private void addBotLogEntry(EntityManagerWrapper entityManager, long guildId, long memberId, T oldValue, T newValue) {
        BotLogEntity.log(entityManager, logEvent, guildId, memberId, oldValue, newValue);
    }

    private void addBotLogEntryForList(EntityManagerWrapper entityManager, long guildId, long memberId, List<?> oldValues, List<?> newValues) {
        switch (logEvent.getValuesRelationship()) {
            case EMPTY -> {
            }
            case OLD_AND_NEW -> BotLogEntity.log(entityManager, logEvent, guildId, memberId, oldValues, newValues);
            case ADD_AND_REMOVE -> {
                ArrayList<Object> addedValues = new ArrayList<>();
                ArrayList<Object> removedValues = new ArrayList<>();

                for (Object newValue : newValues) {
                    if (!oldValues.contains(newValue)) {
                        addedValues.add(newValue);
                    }
                }
                for (Object oldValue : oldValues) {
                    if (!newValues.contains(oldValue)) {
                        removedValues.add(oldValue);
                    }
                }

                BotLogEntity.log(entityManager, logEvent, guildId, memberId, addedValues, removedValues);
            }
            case SINGLE_VALUE_COLUMN -> BotLogEntity.log(entityManager, logEvent, guildId, memberId, null, newValues);
        }
    }

    protected void addActionRows(ArrayList<ActionRow> actionRows) {
    }

}

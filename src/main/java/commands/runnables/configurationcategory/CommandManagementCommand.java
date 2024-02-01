package commands.runnables.configurationcategory;

import commands.Category;
import commands.Command;
import commands.CommandContainer;
import commands.CommandEvent;
import commands.listeners.CommandProperties;
import commands.listeners.MessageInputResponse;
import commands.runnables.NavigationAbstract;
import constants.LogStatus;
import core.CustomObservableList;
import core.EmbedFactory;
import core.TextManager;
import core.utils.StringUtil;
import mysql.hibernate.entity.BotLogEntity;
import mysql.modules.commandmanagement.CommandManagementData;
import mysql.modules.commandmanagement.DBCommandManagement;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@CommandProperties(
        trigger = "cman",
        userGuildPermissions = Permission.ADMINISTRATOR,
        emoji = "🚦",
        executableWithoutArgs = true,
        aliases = { "commandmanagement", "cmanagement", "cm", "commandmanagements", "commandmanager", "commandm", "comman" }
)
public class CommandManagementCommand extends NavigationAbstract {

    private static final int MAIN = 0,
            SET_CATEGORIES = 1,
            ADD_COMMANDS = 2,
            REMOVE_COMMANDS = 3;

    private CommandManagementData commandManagementData;

    public CommandManagementCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(@NotNull CommandEvent event, @NotNull String args) {
        commandManagementData = DBCommandManagement.getInstance().retrieve(event.getGuild().getIdLong());
        registerNavigationListener(event.getMember());
        return true;
    }

    @ControllerMessage(state = ADD_COMMANDS)
    public MessageInputResponse onMessageAddCommands(MessageReceivedEvent event, String input) {
        List<String> commands = Arrays.stream(input.split(" "))
                .map(trigger -> CommandContainer.getCommandMap().get(trigger))
                .filter(Objects::nonNull)
                .filter(clazz -> {
                    CommandProperties commandProperties = Command.getCommandProperties(clazz);
                    return commandProperties.exclusiveGuilds().length == 0 && commandProperties.exclusiveUsers().length == 0;
                })
                .map(clazz -> Command.getCommandProperties(clazz).trigger())
                .distinct()
                .collect(Collectors.toList());

        if (commands.isEmpty()) {
            setLog(LogStatus.FAILURE, TextManager.getNoResultsString(getLocale(), input));
            return MessageInputResponse.FAILED;
        }

        List<String> switchedOffElements = commandManagementData.getSwitchedOffElements();
        List<String> newCommands = commands.stream()
                .filter(trigger -> !switchedOffElements.contains(trigger))
                .collect(Collectors.toList());

        if (newCommands.isEmpty()) {
            setLog(LogStatus.FAILURE, TextManager.getString(getLocale(), TextManager.GENERAL, "element_exists", commands.size() != 1));
            return MessageInputResponse.FAILED;
        }

        commandManagementData.getSwitchedOffElements().addAll(newCommands);
        BotLogEntity.log(getEntityManager(), BotLogEntity.Event.COMMAND_MANAGEMENT, event.getMember(), newCommands, null);
        setLog(LogStatus.SUCCESS, getString("addcommands_set", newCommands.size() != 1, StringUtil.numToString(newCommands.size())));
        setState(MAIN);
        return MessageInputResponse.SUCCESS;
    }

    @ControllerButton(state = MAIN)
    public boolean onButtonMain(ButtonInteractionEvent event, int i) {
        switch (i) {
            case -1 -> {
                deregisterListenersWithComponentMessage();
                return false;
            }
            case 0 -> {
                setState(SET_CATEGORIES);
                return true;
            }
            case 1 -> {
                setState(ADD_COMMANDS);
                return true;
            }
            case 2 -> {
                if (!commandManagementData.getSwitchedOffCommands().isEmpty()) {
                    setState(REMOVE_COMMANDS);
                    return true;
                } else {
                    setLog(LogStatus.FAILURE, getString("nocommand"));
                    return true;
                }
            }
            default -> {
                return false;
            }
        }
    }

    @ControllerButton(state = SET_CATEGORIES)
    public boolean onButtonSetCategories(ButtonInteractionEvent event, int i) {
        if (i == -1) {
            setState(MAIN);
            return true;
        }
        return false;
    }

    @ControllerButton(state = ADD_COMMANDS)
    public boolean onButtonAddCommands(ButtonInteractionEvent event, int i) {
        if (i == -1) {
            setState(MAIN);
            return true;
        }
        return false;
    }

    @ControllerButton(state = REMOVE_COMMANDS)
    public boolean onButtonRemoveCommands(ButtonInteractionEvent event, int i) {
        if (i == -1) {
            setState(MAIN);
            return true;
        } else {
            CustomObservableList<String> switchedOffElements = commandManagementData.getSwitchedOffElements();
            String trigger = event.getButton().getLabel();
            if (switchedOffElements.contains(trigger)) {
                switchedOffElements.remove(trigger);
                BotLogEntity.log(getEntityManager(), BotLogEntity.Event.COMMAND_MANAGEMENT, event.getMember(), null, trigger);
            }

            setLog(LogStatus.SUCCESS, getString("commandremoved", trigger));
            if (commandManagementData.getSwitchedOffCommands().isEmpty()) {
                setState(MAIN);
            }
            return true;
        }
    }

    @ControllerStringSelectMenu(state = SET_CATEGORIES)
    public boolean onSelectMenu(StringSelectInteractionEvent event, int i) {
        CustomObservableList<String> switchedOffElements = commandManagementData.getSwitchedOffElements();

        ArrayList<String> added = new ArrayList<>();
        for (String value : event.getValues()) {
            if (!switchedOffElements.contains(value)) {
                switchedOffElements.add(value);
                added.add(value);
            }
        }
        ArrayList<String> removed = new ArrayList<>();
        for (Category category : Category.independentValues()) {
            if (switchedOffElements.contains(category.getId()) && !event.getValues().contains(category.getId())) {
                switchedOffElements.remove(category.getId());
                removed.add(category.getId());
            }
        }

        BotLogEntity.log(getEntityManager(), BotLogEntity.Event.COMMAND_MANAGEMENT, event.getMember(), added, removed);
        setLog(LogStatus.SUCCESS, getString("categoryset_set"));
        setState(MAIN);
        return true;
    }

    @Draw(state = MAIN)
    public EmbedBuilder onDrawMain(Member member) {
        setComponents(getString("state0_options").split("\n"));
        List<String> categoryNameList = commandManagementData.getSwitchedOffCategories().stream()
                .map(category -> TextManager.getString(getLocale(), TextManager.COMMANDS, category.getId()))
                .collect(Collectors.toList());
        return EmbedFactory.getEmbedDefault(this, getString("state0_desc"))
                .addField(getString("state0_mcategories"), StringUtil.shortenString(generateList(categoryNameList), 1024), false)
                .addField(getString("state0_mcommands"), StringUtil.shortenString(generateList(commandManagementData.getSwitchedOffCommands()), 1024), false);
    }

    @Draw(state = SET_CATEGORIES)
    public EmbedBuilder onDrawSetCategory(Member member) {
        List<String> categoryIdList = commandManagementData.getSwitchedOffCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        StringSelectMenu.Builder selectionMenuBuilder = StringSelectMenu.create("categories");
        selectionMenuBuilder = selectionMenuBuilder.setRequiredRange(0, Category.independentValues().length);
        for (Category category : Category.independentValues()) {
            selectionMenuBuilder = selectionMenuBuilder.addOption(TextManager.getString(getLocale(), TextManager.COMMANDS, category.getId()), category.getId());
        }
        selectionMenuBuilder = selectionMenuBuilder.setDefaultValues(categoryIdList);
        setComponents(selectionMenuBuilder.build());
        return EmbedFactory.getEmbedDefault(this, getString("state1_desc"), getString("state1_title"));
    }

    @Draw(state = ADD_COMMANDS)
    public EmbedBuilder onDrawAddCommands(Member member) {
        return EmbedFactory.getEmbedDefault(this, getString("state2_desc"), getString("state2_title"));
    }

    @Draw(state = REMOVE_COMMANDS)
    public EmbedBuilder onDrawRemoveCommands(Member member) {
        setComponents(commandManagementData.getSwitchedOffCommands().toArray(new String[0]));
        return EmbedFactory.getEmbedDefault(this, getString("state3_desc"), getString("state3_title"));
    }

    private String generateList(List<String> elements) {
        StringBuilder sb = new StringBuilder();
        for (String element : elements) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append('`').append(element).append('`');
        }
        if (!sb.isEmpty()) {
            return sb.toString();
        } else {
            return TextManager.getString(getLocale(), TextManager.GENERAL, "notset");
        }
    }


}

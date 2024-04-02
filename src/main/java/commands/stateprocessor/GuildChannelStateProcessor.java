package commands.stateprocessor;

import commands.runnables.NavigationAbstract;
import constants.LogStatus;
import core.TextManager;
import core.atomicassets.AtomicGuildChannel;
import core.utils.BotPermissionUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import org.glassfish.jersey.internal.util.Producer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuildChannelStateProcessor extends AbstractStateProcessor<List<Long>> {

    public static final String SELECT_MENU_ID = "entities";

    private final int min;
    private final int max;
    private final Collection<ChannelType> channelTypes;
    private final Permission[] checkPermissions;
    private final Producer<List<Long>> getter;

    public GuildChannelStateProcessor(NavigationAbstract command, int state, int stateBack, String propertyName, int min, int max,
                                      Collection<ChannelType> channelTypes, Producer<List<Long>> getter, Consumer<List<Long>> setter
    ) {
        this(command, state, stateBack, propertyName, min, max, channelTypes, null, getter, setter);
    }

    public GuildChannelStateProcessor(NavigationAbstract command, int state, int stateBack, String propertyName, int min, int max,
                                      Collection<ChannelType> channelTypes, Permission[] checkPermissions,
                                      Producer<List<Long>> getter, Consumer<List<Long>> setter
    ) {
        this(command, state, stateBack, propertyName, TextManager.getString(command.getLocale(), TextManager.COMMANDS, "stateprocessor_channels_desc"), min, max, channelTypes, checkPermissions, getter, setter);
    }

    public GuildChannelStateProcessor(NavigationAbstract command, int state, int stateBack, String propertyName, String description,
                                      int min, int max, Collection<ChannelType> channelTypes,
                                      Producer<List<Long>> getter, Consumer<List<Long>> setter
    ) {
        this(command, state, stateBack, propertyName, description, min, max, channelTypes, null, getter, setter);
    }

    public GuildChannelStateProcessor(NavigationAbstract command, int state, int stateBack, String propertyName, String description,
                                      int min, int max, Collection<ChannelType> channelTypes, Permission[] checkPermissions,
                                      Producer<List<Long>> getter, Consumer<List<Long>> setter
    ) {
        super(command, state, stateBack, propertyName, description, false, setter);
        this.min = min;
        this.max = max;
        this.channelTypes = channelTypes;
        this.checkPermissions = checkPermissions != null ? checkPermissions : new Permission[0];
        this.getter = getter;
    }

    @Override
    public boolean controllerEntitySelectMenu(EntitySelectInteractionEvent event) {
        List<GuildChannel> channels = event.getMentions().getChannels();

        if (checkPermissions.length > 0) {
            for (GuildChannel channel : channels) {
                if (!BotPermissionUtil.can(channel, checkPermissions)) {
                    StringBuilder sb = new StringBuilder();
                    for (Permission permission : checkPermissions) {
                        if (!sb.isEmpty()) {
                            sb.append(", ");
                        }
                        sb.append(TextManager.getString(getCommand().getLocale(), TextManager.PERMISSIONS, permission.name()));
                    }

                    String str = TextManager.getString(getCommand().getLocale(), TextManager.COMMANDS, "stateprocessor_channels_missingpermissions", sb.toString(), new AtomicGuildChannel(channel).getPrefixedName(getCommand().getLocale()));
                    getCommand().setLog(LogStatus.FAILURE, str);
                    return true;
                }
            }
        }

        set(channels.stream().map(ISnowflake::getIdLong).collect(Collectors.toList()));
        return true;
    }

    @Override
    protected void addActionRows(ArrayList<ActionRow> actionRows) {
        List<EntitySelectMenu.DefaultValue> defaultValues = getter.call().stream().filter(id -> id != null && id != 0L).map(EntitySelectMenu.DefaultValue::channel).collect(Collectors.toList());
        EntitySelectMenu entitySelectMenu = EntitySelectMenu.create(SELECT_MENU_ID, EntitySelectMenu.SelectTarget.CHANNEL)
                .setChannelTypes(channelTypes)
                .setDefaultValues(defaultValues.stream().limit(max).collect(Collectors.toList()))
                .setRequiredRange(min, max)
                .build();
        actionRows.add(ActionRow.of(entitySelectMenu));
    }

}

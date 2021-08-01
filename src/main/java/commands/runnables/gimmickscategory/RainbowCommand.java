package commands.runnables.gimmickscategory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import commands.listeners.CommandProperties;
import commands.runnables.MemberAccountAbstract;
import core.EmbedFactory;
import core.TextManager;
import core.components.ActionRows;
import core.utils.StringUtil;
import modules.graphics.RainbowGraphics;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

@CommandProperties(
        trigger = "rainbow",
        botChannelPermissions = Permission.MESSAGE_ATTACH_FILES,
        emoji = "\uD83C\uDF08",
        executableWithoutArgs = true,
        requiresMemberCache = true,
        aliases = { "lgbt", "pride" }
)
public class RainbowCommand extends MemberAccountAbstract {

    private InputStream inputStream;

    public RainbowCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    protected EmbedBuilder processMember(GuildMessageReceivedEvent event, Member member, boolean memberIsAuthor, String args) throws IOException {
        long opacity = StringUtil.filterLongFromString(args);
        if (opacity == -1) {
            opacity = 50;
        } else {
            setFound();
            if (opacity < 0) opacity = 0;
            if (opacity > 100) opacity = 100;
        }

        addLoadingReactionInstantly();
        inputStream = RainbowGraphics.createImageRainbow(member.getUser(), opacity);
        return EmbedFactory.getEmbedDefault(this, getString("template", member.getEffectiveName()))
                .setImage("attachment://avatar.png");
    }

    @Override
    protected void sendMessage(Member member, TextChannel channel, MessageEmbed eb) {
        channel.sendMessageEmbeds(eb)
                .addFile(inputStream, "avatar.png")
                .queue(message -> {
                    message.editMessageEmbeds(eb)
                            .setActionRows(ActionRows.of(Button.of(ButtonStyle.LINK, message.getEmbeds().get(0).getImage().getUrl(), TextManager.getString(getLocale(), TextManager.GENERAL, "download_image"))))
                            .queue();
                });
    }

}

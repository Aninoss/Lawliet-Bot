package Commands.ManagementCategory;

import CommandListeners.CommandProperties;
import CommandListeners.OnNavigationListener;
import CommandSupporters.Command;
import Constants.LogStatus;
import Constants.Permission;
import Constants.Response;
import Constants.Settings;
import Core.*;
import Core.Mention.MentionUtil;
import Core.Utils.InternetUtil;
import Core.Utils.StringUtil;
import Modules.ImageCreator;
import Modules.Welcome;
import MySQL.Modules.WelcomeMessage.DBWelcomeMessage;
import MySQL.Modules.WelcomeMessage.WelcomeMessageBean;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.SingleReactionEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

@CommandProperties(
    trigger = "welcome",
    botPermissions = Permission.ATTACH_FILES,
    userPermissions = Permission.MANAGE_SERVER,
    emoji = "🙋",
    executable = true
)
public class WelcomeCommand extends Command implements OnNavigationListener {
    
    private WelcomeMessageBean welcomeMessageBean;
    private User author;

    public WelcomeCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    protected boolean onMessageReceived(MessageCreateEvent event, String followedString) throws Throwable {
        welcomeMessageBean = DBWelcomeMessage.getInstance().getBean(event.getServer().get().getId());
        author = event.getMessage().getUserAuthor().get();
        welcomeMessageBean.getWelcomeChannel().ifPresent(this::checkWriteInChannelWithLog);
        welcomeMessageBean.getGoodbyeChannel().ifPresent(this::checkWriteInChannelWithLog);
        return true;
    }

    @Override
    public Response controllerMessage(MessageCreateEvent event, String inputString, int state) throws Throwable {
        switch (state) {
            case 1:
                if (inputString.length() > 0) {
                    if (inputString.length() <= 20) {
                        welcomeMessageBean.setWelcomeTitle(inputString);
                        setLog(LogStatus.SUCCESS, getString("titleset"));
                        setState(0);
                        return Response.TRUE;
                    } else {
                        setLog(LogStatus.FAILURE, getString("titletoolarge", "20"));
                        return Response.FALSE;
                    }
                }
                return Response.FALSE;

            case 2:
                if (inputString.length() > 0) {
                    if (inputString.length() <= 500) {
                        welcomeMessageBean.setWelcomeText(inputString);
                        setLog(LogStatus.SUCCESS, getString("descriptionset"));
                        setState(0);
                        return Response.TRUE;
                    } else {
                        setLog(LogStatus.FAILURE, getString("descriptiontoolarge", "500"));
                        return Response.FALSE;
                    }
                }
                return Response.FALSE;

            case 3:
                ArrayList<ServerTextChannel> channelList = MentionUtil.getTextChannels(event.getMessage(), inputString).getList();
                if (channelList.size() == 0) {
                    setLog(LogStatus.FAILURE, TextManager.getString(getLocale(), TextManager.GENERAL, "no_results_description", inputString));
                    return Response.FALSE;
                } else {
                    if (checkWriteInChannelWithLog(channelList.get(0))) {
                        welcomeMessageBean.setWelcomeChannelId(channelList.get(0).getId());
                        setLog(LogStatus.SUCCESS, getString("channelset"));
                        setState(0);
                        return Response.TRUE;
                    } else return Response.FALSE;
                }

            case 4:
                List<MessageAttachment> attachmentList = event.getMessage().getAttachments();
                if (attachmentList.size() > 0 && attachmentList.get(0).isImage()) {
                    MessageAttachment messageAttachment = attachmentList.get(0);

                    long size = messageAttachment.getSize();
                    if (size >= 20000000) {
                        setLog(LogStatus.FAILURE, TextManager.getString(getLocale(), TextManager.GENERAL, "file_too_large"));
                        return Response.FALSE;
                    }

                    BufferedImage bi;
                    try {
                        bi = messageAttachment.downloadAsImage().get();
                    } catch (Throwable e) {
                        setLog(LogStatus.FAILURE, TextManager.getString(getLocale(), TextManager.GENERAL, "imagenotfound"));
                        return Response.FALSE;
                    }
                    if (bi == null) {
                        setLog(LogStatus.FAILURE, TextManager.getString(getLocale(), TextManager.GENERAL, "imagenotfound"));
                        return Response.FALSE;
                    }

                    ImageIO.write(bi, "png", new File("data/welcome_backgrounds/" + event.getServer().get().getIdAsString() + ".png"));

                    setLog(LogStatus.SUCCESS, getString("backgroundset"));
                    setState(0);
                    return Response.TRUE;
                }

                setLog(LogStatus.FAILURE, TextManager.getString(getLocale(), TextManager.GENERAL, "imagenotfound"));
                return Response.FALSE;

            case 6:
                if (inputString.length() > 0) {
                    if (inputString.length() <= 500) {
                        welcomeMessageBean.setGoodbyeText(inputString);
                        setLog(LogStatus.SUCCESS, getString("goodbyetextset"));
                        setState(0);
                        return Response.TRUE;
                    } else {
                        setLog(LogStatus.FAILURE, getString("goodbyetoolarge", "500"));
                        return Response.FALSE;
                    }
                }
                return Response.FALSE;

            case 7:
                channelList = MentionUtil.getTextChannels(event.getMessage(), inputString).getList();
                if (channelList.size() == 0) {
                    setLog(LogStatus.FAILURE, TextManager.getString(getLocale(), TextManager.GENERAL, "no_results_description", inputString));
                    return Response.FALSE;
                } else {
                    if (checkWriteInChannelWithLog(channelList.get(0))) {
                        welcomeMessageBean.setGoodbyeChannelId(channelList.get(0).getId());
                        setLog(LogStatus.SUCCESS, getString("farechannelset"));
                        setState(0);
                        return Response.TRUE;
                    } else return Response.FALSE;
                }

            default:
                return null;
        }
    }

    @Override
    public boolean controllerReaction(SingleReactionEvent event, int i, int state) throws Throwable {

        switch (state) {
            case 0:
                switch (i) {
                    case -1:
                        removeNavigationWithMessage();
                        return false;

                    case 0:
                        welcomeMessageBean.toggleWelcomeActive();
                        setLog(LogStatus.SUCCESS, getString("activateset", !welcomeMessageBean.isWelcomeActive()));
                        return true;

                    case 1:
                        setState(1);
                        return true;

                    case 2:
                        setState(2);
                        return true;

                    case 3:
                        setState(3);
                        return true;

                    case 4:
                        setState(4);
                        return true;

                    case 5:
                        welcomeMessageBean.toggleGoodbyeActive();
                        setLog(LogStatus.SUCCESS, getString("goodbyeset", !welcomeMessageBean.isGoodbyeActive()));
                        return true;

                    case 6:
                        setState(6);
                        return true;

                    case 7:
                        setState(7);
                        return true;

                    case 8:
                        setState(5);
                        return true;

                    default:
                        return false;
                }

            default:
                if (i == -1) {
                    setState(0);
                    return true;
                } return false;
        }
    }

    @Override
    public EmbedBuilder draw(DiscordApi api, int state) throws Throwable {
        String notSet = TextManager.getString(getLocale(), TextManager.GENERAL, "notset");

        switch (state) {
            case 0:
                setOptions(getString("state0_options").split("\n"));
                return EmbedFactory.getCommandEmbedStandard(this, getString("state0_description"))
                        .addField(Settings.EMPTY_EMOJI, Settings.EMPTY_EMOJI, false)
                        .addField(getString("state0_menabled"), StringUtil.getOnOffForBoolean(getLocale(), welcomeMessageBean.isWelcomeActive()), true)
                        .addField(getString("state0_mtitle"), welcomeMessageBean.getWelcomeTitle(), true)
                        .addField(getString("state0_mdescription"),
                               Welcome.resolveVariables(welcomeMessageBean.getWelcomeText(),
                                       "`%SERVER`",
                                       "`%USER_MENTION`",
                                       "`%USER_NAME`",
                                       "`%USER_DISCRIMINATED`",
                                      "`%MEMBERS`"),
                               true)
                        .addField(getString("state0_mchannel"), welcomeMessageBean.getWelcomeChannel().map(Mentionable::getMentionTag).orElse(notSet), true)
                        .addField(Settings.EMPTY_EMOJI, Settings.EMPTY_EMOJI, false)
                        .addField(getString("state0_mgoodbye"), StringUtil.getOnOffForBoolean(getLocale(), welcomeMessageBean.isGoodbyeActive()), true)
                        .addField(getString("state0_mgoodbyeText"),
                               Welcome.resolveVariables(welcomeMessageBean.getGoodbyeText(),
                                       "`%SERVER`",
                                       "`%USER_MENTION`",
                                       "`%USER_NAME`",
                                       "`%USER_DISCRIMINATED`",
                                       "`%MEMBERS`").replace("``", "` `"),
                                true)
                        .addField(getString("state0_mfarewellchannel"), welcomeMessageBean.getGoodbyeChannel().map(Mentionable::getMentionTag).orElse(notSet), true);

            default:
                if (state == 5) {
                    return getWelcomeMessageTest(author);
                }
                return EmbedFactory.getCommandEmbedStandard(this, getString("state"+state+"_description"), getString("state"+state+"_title"));
        }
    }

    @Override
    public void onNavigationTimeOut(Message message) throws Throwable {
    }

    @Override
    public int getMaxReactionNumber() {
        return 9;
    }

    public EmbedBuilder getWelcomeMessageTest(User user) throws ExecutionException, InterruptedException {
        Server server = welcomeMessageBean.getServer().get();
        EmbedBuilder eb = EmbedFactory.getEmbed()
                .setDescription(Welcome.resolveVariables(welcomeMessageBean.getWelcomeText(),
                        StringUtil.escapeMarkdown(server.getName()),
                        user.getMentionTag(),
                        StringUtil.escapeMarkdown(user.getName()),
                        StringUtil.escapeMarkdown(user.getDiscriminatedName()),
                        StringUtil.numToString(getLocale(), server.getMemberCount())));

        eb.setImage(InternetUtil.getURLFromInputStream(ImageCreator.createImageWelcome(user, server, welcomeMessageBean.getWelcomeTitle())).toString());
        return eb;
    }

}

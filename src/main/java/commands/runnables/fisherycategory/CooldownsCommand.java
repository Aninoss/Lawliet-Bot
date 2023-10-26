package commands.runnables.fisherycategory;

import commands.Command;
import commands.CommandEvent;
import commands.listeners.CommandProperties;
import commands.listeners.OnStringSelectMenuListener;
import commands.runnables.FisheryInterface;
import constants.ExternalLinks;
import constants.LogStatus;
import core.CustomObservableMap;
import core.EmbedFactory;
import core.utils.TimeUtil;
import javafx.util.Pair;
import mysql.redis.fisheryusers.FisheryUserManager;
import mysql.redis.fisheryusers.FisheryMemberData;
import mysql.modules.subs.DBSubs;
import mysql.modules.subs.SubSlot;
import mysql.modules.survey.DBSurvey;
import mysql.modules.survey.SurveyData;
import mysql.modules.upvotes.DBUpvotes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@CommandProperties(
        trigger = "cooldowns",
        emoji = "⏲️",
        executableWithoutArgs = true,
        aliases = { "cooldown", "cd" }
)
public class CooldownsCommand extends Command implements FisheryInterface, OnStringSelectMenuListener {

    private FisheryMemberData fisheryMemberData;

    public CooldownsCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onFisheryAccess(CommandEvent event, String args) throws Throwable {
        this.fisheryMemberData = FisheryUserManager.getGuildData(event.getGuild().getIdLong())
                .getMemberData(event.getMember().getIdLong());
        registerStringSelectMenuListener(event.getMember());
        return true;
    }

    @Override
    public boolean onStringSelectMenu(StringSelectInteractionEvent event) throws Throwable {
        DBSubs.Command[] commands = DBSubs.Command.values();
        List<Integer> activeSubs = event.getValues().stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        for (int i = 0; i < commands.length; i++) {
            DBSubs.Command command = commands[i];
            CustomObservableMap<Long, SubSlot> slotMap = DBSubs.getInstance().retrieve(command);
            boolean newActive = activeSubs.contains(i);
            boolean isActive = slotMap.containsKey(event.getMember().getIdLong());
            if (newActive && !isActive) {
                SubSlot subSlot = new SubSlot(command, event.getMember().getIdLong(), getLocale(), 0);
                slotMap.put(subSlot.getUserId(), subSlot);
            } else if (!newActive && isActive) {
                slotMap.remove(event.getMember().getIdLong());
            }
            setLog(LogStatus.SUCCESS, getString("subset"));
        }
        return true;
    }

    @Override
    public EmbedBuilder draw(@NotNull Member member) throws Throwable {
        DBSubs.Command[] commands = DBSubs.Command.values();

        StringSelectMenu.Builder builder = StringSelectMenu.create("reminders");
        ArrayList<String> defaultValues = new ArrayList<>();
        for (int i = 0; i < commands.length; i++) {
            DBSubs.Command command = commands[i];
            String property = getString("property_" + commands[i].name().toLowerCase());

            builder.addOption(property, String.valueOf(i));
            if (DBSubs.getInstance().retrieve(command).containsKey(member.getIdLong())) {
                defaultValues.add(String.valueOf(i));
            }
        }
        builder.setRequiredRange(0, 4)
                .setDefaultValues(defaultValues);
        setActionRows(
                ActionRow.of(builder.build()),
                ActionRow.of(Button.of(ButtonStyle.LINK, ExternalLinks.UPVOTE_URL, getString("upvotebutton")))
        );

        String template = getString("template",
                getRemainingTimeDaily(),
                getRemainingTimeWork(),
                getRemainingTimeUpvotes(),
                getRemainingTimeSurvey()
        );

        return EmbedFactory.getEmbedDefault(this, template)
                .addField(getString("dmreminders"), getString("dmreminders_desc"), false);
    }

    private String getRemainingTimeDaily() {
        Instant nextDaily = TimeUtil.localDateToInstant(fisheryMemberData.getDailyReceived()).plus(24, ChronoUnit.HOURS);
        return getRemainingTime(nextDaily);
    }

    private String getRemainingTimeWork() {
        Instant nextWork = fisheryMemberData.getNextWork().orElse(Instant.MIN);
        return getRemainingTime(nextWork);
    }

    private String getRemainingTimeUpvotes() {
        Instant lastUpvote = DBUpvotes.getUpvoteSlot(fisheryMemberData.getMemberId()).getLastUpvote().plus(12, ChronoUnit.HOURS);
        return getRemainingTime(lastUpvote);
    }

    private String getRemainingTimeSurvey() {
        SurveyData surveyData = DBSurvey.getInstance().getCurrentSurvey();
        Instant nextSurvey = surveyData.getSecondVotes().containsKey(new Pair<>(fisheryMemberData.getGuildId(), fisheryMemberData.getMemberId())) ? TimeUtil.localDateToInstant(surveyData.getNextDate()) : Instant.MIN;
        return getRemainingTime(nextSurvey);
    }

    private String getRemainingTime(Instant instant) {
        if (Instant.now().isAfter(instant)) {
            return getString("now");
        } else {
            return TimeFormat.RELATIVE.atInstant(instant).toString();
        }
    }

}

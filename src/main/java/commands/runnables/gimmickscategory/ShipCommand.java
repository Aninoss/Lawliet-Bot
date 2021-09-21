package commands.runnables.gimmickscategory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import commands.Command;
import commands.listeners.CommandProperties;
import core.EmbedFactory;
import core.ExceptionLogger;
import core.RandomPicker;
import core.utils.MentionUtil;
import modules.graphics.ShipGraphics;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@CommandProperties(
        trigger = "ship",
        botChannelPermissions = Permission.MESSAGE_ATTACH_FILES,
        emoji = "\uD83D\uDC6B",
        requiresFullMemberCache = true,
        executableWithoutArgs = false
)
public class ShipCommand extends Command {

    public ShipCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(GuildMessageReceivedEvent event, String args) throws IOException, ExecutionException, InterruptedException {
        Message message = event.getMessage();
        ArrayList<Member> list = new ArrayList<>(MentionUtil.getMembers(message, args).getList());
        if (list.size() == 1 && list.get(0).getIdLong() != event.getMember().getIdLong()) {
            list.add(event.getMember());
        }

        if (list.size() != 2) {
            drawMessageNew(EmbedFactory.getEmbedError(this, getString("not_2")))
                    .exceptionally(ExceptionLogger.get());
            return false;
        }

        list.sort(Comparator.comparingLong(ISnowflake::getIdLong));
        String idString = String.valueOf(list.get(0).getIdLong() + list.get(1).getIdLong());
        int randomNum = String.valueOf(idString.hashCode()).hashCode();
        int percentage = new Random(randomNum).nextInt(101);

        if (list.get(0).getIdLong() == 272037078919938058L && list.get(1).getIdLong() == 326714012022865930L) {
            percentage = 100;
        }
        if (list.get(0).getIdLong() == 397209883793162240L && list.get(1).getIdLong() == 710120672499728426L) {
            percentage = 100;
        }
        if (list.get(0).getIdLong() == 321164798475894784L && list.get(1).getIdLong() == 326714012022865930L) {
            percentage = -999;
        }

        int n = RandomPicker.pick(getTrigger(), event.getGuild().getIdLong(), 7).get();
        if (event.getGuild().getIdLong() == 580048842020487180L) n = 7;

        addLoadingReactionInstantly();
        InputStream is = ShipGraphics.createImageShip(list.get(0).getUser(), list.get(1).getUser(), n, percentage);
        if (is == null) {
            drawMessageNew(EmbedFactory.getEmbedError(this, getString("noavatar")))
                    .exceptionally(ExceptionLogger.get());
            return false;
        }

        EmbedBuilder eb = EmbedFactory.getEmbedDefault(this)
                .setImage("attachment://ship.png");

        addFileAttachment(is, "ship.png");
        drawMessageNew(eb).exceptionally(ExceptionLogger.get());
        return true;
    }

}

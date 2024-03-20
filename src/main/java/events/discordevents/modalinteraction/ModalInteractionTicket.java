package events.discordevents.modalinteraction;

import core.EmbedFactory;
import core.TextManager;
import events.discordevents.DiscordEvent;
import events.discordevents.eventtypeabstracts.ModalInteractionAbstract;
import modules.Ticket;
import mysql.hibernate.EntityManagerWrapper;
import mysql.hibernate.entity.guild.GuildEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

@DiscordEvent
public class ModalInteractionTicket extends ModalInteractionAbstract {

    public static String ID = "ticket_create";

    @Override
    public boolean onModalInteraction(ModalInteractionEvent event, EntityManagerWrapper entityManager) {
        if (event.getChannel() instanceof StandardGuildMessageChannel && event.getModalId().equals(ID)) {
            GuildEntity guildEntity = entityManager.findGuildEntity(event.getGuild().getIdLong());
            StandardGuildMessageChannel channel = (StandardGuildMessageChannel) event.getChannel();
            Category category = channel.getParentCategory();

            if (category == null || category.getChannels().size() < 50) {
                Ticket.createTicket(guildEntity, channel, event.getMember(),
                        event.getValue("message").getAsString()
                );
                event.deferEdit().queue();
            } else {
                EmbedBuilder eb = EmbedFactory.getEmbedError()
                        .setTitle(TextManager.getString(guildEntity.getLocale(), TextManager.GENERAL, "rejected"))
                        .setDescription(TextManager.getString(guildEntity.getLocale(), commands.Category.CONFIGURATION, "ticket_toomanychannels"));
                event.replyEmbeds(eb.build())
                        .setEphemeral(true)
                        .queue();
            }

            return false;
        }

        return true;
    }

}

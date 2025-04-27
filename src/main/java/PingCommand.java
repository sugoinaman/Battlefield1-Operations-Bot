import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class PingCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getMember().getUser().isBot()) {
            return ;
        }
        if (event.getName().equalsIgnoreCase("ping")) {
            event.reply("PONG!").setEphemeral(true).queue();
            EmbedBuilder em = new EmbedBuilder();
//            em.setTitle("This is the title of the embed");
            em.setDescription("PONG");
            em.setColor(new Color(255, 255, 255));
//            em.setAuthor("This is the author of the embed");
            event.getChannel().sendMessageEmbeds(em.build()).queue();
//            event.getChannel().sendMessage("PONG").setEmbeds(em.build()).queue();

        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {

        CommandData commandData = Commands.slash("ping", "it pings?");
        event.getGuild().upsertCommand(commandData).queue();
    }
}

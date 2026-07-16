package commands;

import history.MapHistoryService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

/**
 * Discord adapter for map-history queries. Polling decisions and in-memory history live outside JDA.
 */
public class MapHistory extends ListenerAdapter {

    private final MapHistoryService historyService;
    private final String primaryGuildId;

    public MapHistory(MapHistoryService historyService, String primaryGuildId) {
        this.historyService = historyService;
        this.primaryGuildId = primaryGuildId;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("map_history")) {
            return;
        }

        if (event.getGuild() == null) {
            return;
        }

        String guildId = event.getGuild().getId();
        if (!guildId.equals(primaryGuildId) && !guildId.equals("1338475669000028182")) {
            event.reply("This command is not available in this server.").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue(hook -> {
            String history = historyService.readHistory();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(new Color(54, 232, 15));
            embed.setAuthor("Played maps are:");
            embed.setDescription(history.isEmpty() ? "No maps have been tracked yet." : history);
            hook.editOriginalEmbeds(embed.build()).queue();
        }, error -> System.err.println("Could not acknowledge /map_history: " + error.getMessage()));
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        event.getGuild().upsertCommand(
                Commands.slash("map_history", "Shows recently played maps")
                        .setContexts(InteractionContextType.GUILD)
                        .setDefaultPermissions(DefaultMemberPermissions.ENABLED)
        ).queue();
    }
}

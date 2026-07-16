package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import rotation.CustomMapRotationService;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Discord adapter for custom-map commands. Rotation state and map-provider decisions live in
 * {@link CustomMapRotationService}, keeping them separate from JDA.
 */
public class CustomMapSetter extends ListenerAdapter {

    private static final List<String> AVAILABLE_MAPS = List.of(
            "Giant's Shadow", "Monte Grappa", "River Somme", "Cape Helles", "Zeebrugge",
            "Fao Fortress", "Soissons", "Volga River", "St Quentin Scar", "Ballroom Blitz",
            "Łupków Pass", "Prise de Tahure", "Verdun Heights"
    );

    private final CustomMapRotationService rotation;
    private final String primaryGuildId;

    public CustomMapSetter(CustomMapRotationService rotation, String primaryGuildId) {
        this.rotation = rotation;
        this.primaryGuildId = primaryGuildId;
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("custom_map")) {
            return;
        }

        List<Command.Choice> options = AVAILABLE_MAPS.stream()
                .filter(map -> map.startsWith(event.getFocusedOption().getValue()))
                .map(map -> new Command.Choice(map, map))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        if (!commandName.equals("custom_map")
                && !commandName.equals("toggle_off")
                && !commandName.equals("custom_map_info")) {
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
            switch (commandName) {
                case "custom_map" -> handleCustomMap(event, hook);
                case "toggle_off" -> handleToggleOff(hook);
                case "custom_map_info" -> handleCustomMapInfo(hook);
                default -> throw new IllegalStateException("Unexpected command: " + commandName);
            }
        }, error -> System.err.println("Could not acknowledge /" + commandName + ": " + error.getMessage()));
    }

    private void handleCustomMap(SlashCommandInteractionEvent event, InteractionHook hook) {
        List<String> selectedMaps = event.getOptions().stream()
                .map(OptionMapping::getAsString)
                .collect(Collectors.toList());

        CustomMapRotationService.StartResult result = rotation.start(selectedMaps);
        hook.editOriginal(result.message()).queue();
    }

    private void handleToggleOff(InteractionHook hook) {
        hook.editOriginal(rotation.stop()).queue();
    }

    private void handleCustomMapInfo(InteractionHook hook) {
        CustomMapRotationService.Status status = rotation.status();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(76, 3, 154));
        embed.setTitle("Info about `custom_map` command");

        if (!status.running()) {
            embed.addField("custom_map is", "OFF", false);
        } else {
            embed.addField("custom_map is", "ON", false);
            embed.addField("List of set maps:", status.maps().toString(), false);
            if (status.nextMap() != null) {
                embed.addField("Next map:", status.nextMap(), false);
            }
        }
        hook.editOriginalEmbeds(embed.build()).queue();
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        CommandData commandData = Commands.slash("custom_map", "Sets a custom map from a FIXED list of maps")
                .addOptions(new OptionData(OptionType.STRING, "map1", "Required map to be set", true, true))
                .addOptions(new OptionData(OptionType.STRING, "map2", "Required map to be set", true, true))
                .addOptions(new OptionData(OptionType.STRING, "map3", "Required map to be set", true, true))
                .addOptions(new OptionData(OptionType.STRING, "map4", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map5", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map6", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map7", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map8", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map9", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map10", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map11", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map12", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map13", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map14", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map15", "Optional Map to be set", false, true))
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));

        CommandData loopOffCommand = Commands.slash("toggle_off", "Stops the custom map loop")
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));

        CommandData infoCommand = Commands.slash("custom_map_info", "shows info about custom map command")
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);

        event.getGuild().upsertCommand(commandData).queue();
        event.getGuild().upsertCommand(loopOffCommand).queue();
        event.getGuild().upsertCommand(infoCommand).queue();
    }
}

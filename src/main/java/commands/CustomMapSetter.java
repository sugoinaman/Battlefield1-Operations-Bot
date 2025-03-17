package commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomMapSetter extends ListenerAdapter {

    private List<String> mapHolder = new ArrayList<>();
    // Discord only allows 10 slash commands which can be auto completed
    private String[] availableMaps = new String[]{"Giants", "Monte Grappa", "River Somme", "Cape Helles", "Zeebruge", "Fao", "Soissons", "Volga", "Scar", "Ballroom Blitz", "Lupkow", "Prise", "Verdun"};

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null) {
            return;
        }
        if (event.getName().equals("custommap")) {
            //event.deferReply().queue();
            List<String> maps = event.getOptions().stream()
                    .map(OptionMapping::getAsString)
                    .collect(Collectors.toList());

            // Do something with the list of maps
            event.reply("Maps selected for rotation are: " + String.join(", ", maps)).queue();
            mapHolder = new ArrayList<>(maps);
        }
    }


    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
//        ArrayList<OptionData> listOfDifferentMaps = getOptionDataOfDifferentMaps();

        commandData.add(
                Commands.slash("custommap", "Sets a custom map from a FIXED list of maps")
//                        .addOptions(listOfDifferentMaps) //addOptions take collection of OptionData
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

                        .setContexts(InteractionContextType.GUILD)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
        );
        event.getGuild().updateCommands().addCommands(commandData).queue();

    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("custommap")) {
            List<Command.Choice> options = Stream.of(availableMaps)
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                    .map(word -> new Command.Choice(word, word)) // map the words to choices
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }

}

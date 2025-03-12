package commands;

import config.Configuration;
import implementation.MapLoopFix;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyCommands extends ListenerAdapter {


    private String[] availableMaps = new String[]{"Giants", "Fao", "Volga", "Verdun", "Prise", "Lupkow"};
    public List<String> setMaps = new ArrayList<>();
    private static MapLoopFix mapLoopFix;
    private static int curr = 0;

    public List<String> getSetMaps() {
        return setMaps;
    }

    public MyCommands() {

    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null) return;

        switch (event.getName()) {
            case "maphistory":

                event.deferReply().queue();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(Configuration.getFILE_PATH()));
                    StringBuilder history = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        history.append(line).append("\n");
                    }
                    br.close();
                    event.getHook().sendMessage("Played maps: " + history).queue();

                } catch (IOException e) {
                    e.fillInStackTrace();
                }
                break;

            case "customrotation":

                event.deferReply().queue();
                event.getHook().sendMessage("Check console").queue();
                setMaps.add(event.getOption("rotation1").getAsString());
                setMaps.add(event.getOption("rotation2").getAsString());
                setMaps.add(event.getOption("rotation3").getAsString());
                setMaps.add(event.getOption("rotation4").getAsString());
                setMaps.add(event.getOption("rotation5").getAsString());
                setMaps.add(event.getOption("rotation6").getAsString());

                if(mapLoopFix.onMapLoopEvent()){
                    System.out.println("Map loop detected");
                    curr++;
                }
        }

    }


    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("maphistory", "Shows maps played, history resets every 6:30AM"));
        // History reset done by cron delete map.txt :3

        ArrayList<OptionData> listOfDifferentRotations = getOptionDataOfDifferentRotations();

        commandData.add(
                Commands.slash("customrotation", "Sets a custom rotation from a FIXED list of rotations")
                        .addOptions(listOfDifferentRotations)
                        .setContexts(InteractionContextType.GUILD)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
        );
        event.getGuild().updateCommands().addCommands(commandData).queue();

    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("customrotation")) {
            List<Command.Choice> options = Stream.of(availableMaps)
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                    .map(word -> new Command.Choice(word, word)) // map the words to choices
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }


    @NotNull
    private static ArrayList<OptionData> getOptionDataOfDifferentRotations() {
        OptionData Rotation1 = new OptionData(OptionType.STRING, "rotation1", "map to be set", true, true);
        OptionData Rotation2 = new OptionData(OptionType.STRING, "rotation2", "map to be set", false, true);
        OptionData Rotation3 = new OptionData(OptionType.STRING, "rotation3", "map to be set", false, true);
        OptionData Rotation4 = new OptionData(OptionType.STRING, "rotation4", "map to be set", false, true);
        OptionData Rotation5 = new OptionData(OptionType.STRING, "rotation5", "map to be set", false, true);
        OptionData Rotation6 = new OptionData(OptionType.STRING, "rotation6", "map to be set", false, true);
        return new ArrayList<>(new ArrayList<>(Arrays.asList(Rotation1, Rotation2, Rotation3, Rotation4, Rotation5, Rotation6)));
    }
}

//ToDo: Need to give an option to only provide 1 rotation as well, so at night they can
// leave the bot on


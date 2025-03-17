package commands;

import config.Configuration;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyCommands extends ListenerAdapter {


    private String[] availableMaps = new String[]{"Giants", "Fao", "Volga", "Verdun", "Prise", "Lupkow"};
    public List<String> setMaps = new ArrayList<>();
    //    private static MapLoopFix mapLoopFix;
    private static int curr = 0;

    public List<String> getSetMaps() {
        return setMaps;
    }

    public MyCommands() {

    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null) return;


        if (event.getName().equals("maphistory")) {
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

        }
    }


    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("maphistory", "Shows maps played, history resets every 6:30AM"));
        event.getGuild().updateCommands().addCommands(commandData).queue();

    }
}

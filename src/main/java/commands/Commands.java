package commands;

import config.Configuration;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Commands extends ListenerAdapter {


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

            case "customrotation":

        }

    }


    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(net.dv8tion.jda.api.interactions.commands.build.Commands.slash("maphistory", "Shows maps played, history resets every 6:30AM"));
        // History reset done by cron delete map.txt :3
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}

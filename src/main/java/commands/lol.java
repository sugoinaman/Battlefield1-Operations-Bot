package commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class lol extends ListenerAdapter {
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(
                Commands.slash("custommap", "Sets a custom map from a FIXED list of maps")
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
        commandData.add(Commands.slash("maphistory", "Shows maps played, history resets every 6:30AM"));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

}

package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Hello extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("hello")) {
            event.deferReply().queue();
            event.getHook().sendMessage("Hello this is from getHook and deferReply since I wasnt acknowledging stuff fast enough").queue();
        }
    }
}

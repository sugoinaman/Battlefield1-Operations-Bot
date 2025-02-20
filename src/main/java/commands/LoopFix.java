//package commands;
//
//import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
//import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
//import net.dv8tion.jda.api.hooks.ListenerAdapter;
//import net.dv8tion.jda.api.interactions.commands.Command;
//import org.jetbrains.annotations.NotNull;
//
//import java.lang.reflect.Array;
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Stream;
//
//public class LoopFix extends ListenerAdapter {
//    @Override
//    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
//
//        String command = event.getName();
//        if()
//    }
//
//    @Override
//    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
//
//        if (event.getName().equals("loop") && event.getFocusedOption().getName().equals("toggle")) {
//            List<Command.Choice> options = Stream.of(Arrays.asList("On", "Off"));
//        }
//    }
//}

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
import tools.MapManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomMapSetter extends ListenerAdapter {

    private List<String> mapHolder = new ArrayList<>(); //Contains maps entered by the user from slash commands
    private String previousMap = null;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    MapManager mapManager = new MapManager();

    private String[] availableMaps = {"Giant's Shadow", "Monte Grappa", "River Somme", "Cape Helles", "Zeebrugge", "Fao Fortress", "Soissons", "Volga River", "St Quentin Scar", "Ballroom Blitz", "Łupków Pass", "Prise de Tahure", "Verdun Heights"};

    private static HashMap<String,Integer> hashMap = new HashMap<>();
    static {
        hashMap.put("Soissons", 0);
        hashMap.put("Achi Baba", 1);
        hashMap.put("Suez", 2);
        hashMap.put("Fort De Vaux", 3);
        hashMap.put("Giant's Shadow", 4);
        hashMap.put("Monte Grappa", 5);
        hashMap.put("Verdun Heights", 6);
        hashMap.put("River Somme", 7);
        hashMap.put("Cape Helles", 8);
        hashMap.put("Prise de Tahure", 9);
        hashMap.put("Zeebrugge", 10);
        hashMap.put("Empire's Edge", 11);
        hashMap.put("Volga River", 12);
        hashMap.put("Rupture", 13);
        hashMap.put("St Quentin Scar", 14);
        hashMap.put("Amiens", 15);
        hashMap.put("Ballroom Blitz", 16);
        hashMap.put("Galicia", 17);
        hashMap.put("Tsaritsyn", 18);
        hashMap.put("Brusilov Keep", 19);
        hashMap.put("Łupków Pass", 20);
        hashMap.put("Argonne Forest", 21);
        hashMap.put("Sinai Desert", 22);
        hashMap.put("Fao Fortress", 23);
    }

    public CustomMapSetter() {
        scheduler.scheduleAtFixedRate(this::getCurrentMap, 0, 15, TimeUnit.SECONDS);
    }

    /**
     * This method will handle the slash command
     *
     * @param event: the event itself
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null) return;

        if (event.getName().equals("custommap")) {
            List<String> customMapsSetByUser = event.getOptions().stream()
                    .map(OptionMapping::getAsString)
                    .collect(Collectors.toList());
            event.reply("Maps selected for rotation are: " + String.join(", ", customMapsSetByUser)).queue();
            mapHolder = customMapsSetByUser.stream().toList();
            //ToDo: What if user changes his set maps, need to update maps in this list
        }
    }

    /**
     * This is where we add the slash command
     */
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
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    /**
     * this is for the auto-completion of options
     */
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

    /**
     * Actual Logic which changes map from the list of maps entered by the user in the slash command
     * be careful
     */
    public void changeMap() {

        String currentMap = getCurrentMap();
        int c = 0;
        if (currentMap == null || currentMap.equals(previousMap)) return; // map didn't change

        if ((currentMap.equals("Argonne") && previousMap.equals("Ball Room")) ||
                (currentMap.equals("Fort") && previousMap.equals("Verdun")) ||
                (currentMap.equals("empire") && previousMap.equals("monte")) ||
                (currentMap.equals("Achi baba") && previousMap.equals("Cape Helles")) ||
                (currentMap.equals("Rupture") && previousMap.equals("Soissons")) ||
                (currentMap.equals("Tsaritsyn") && previousMap.equals("Volga")) ||
                (currentMap.equals("amiens") && previousMap.equals("Scar"))) {
            previousMap = currentMap;
            return;
        }
        //ToDo: Need a separate check for fao rotation

        if (!mapHolder.isEmpty()) {
           mapManager.bfMapChange(hashMap.get(mapHolder.get(c)));
            c = (c + 1) % mapHolder.size();
        }
    }

    /**
     * This fetches current map every 10 seconds
     * @return current map or null
     */
    private String getCurrentMap() {
        try {
            return mapManager.fetchCurrentMap();
        }
        catch (Exception e){
            System.out.println("GT is probably down");
        }
        return null;
    }
}

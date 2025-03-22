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
import java.util.ArrayList;
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
    private int c = 0;

    // availableMaps are starting maps of an OPS because that's what we are allowed to set.
    private final String[] availableMaps = {"Giant's Shadow", "Monte Grappa", "River Somme", "Cape Helles", "Zeebrugge", "Fao Fortress", "Soissons", "Volga River", "St Quentin Scar", "Ballroom Blitz", "Łupków Pass", "Prise de Tahure", "Verdun Heights"};

    // all maps are mapped to an integer value which we get from GameTools, we do it because the map change API uses a map index rather than a name
    private static final HashMap<String, Integer> hashMap = new HashMap<>();

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
        scheduler.scheduleAtFixedRate(this::changeMap, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Actual Logic which changes map from the list of maps entered by the user in the slash command
     * be careful
     */
    public void changeMap() {

        try {
            String currentMap = mapManager.fetchCurrentMap();

            // Check if the currentMap is the map set by user, here C is the index of the list of maps inputted by the user in mapHolder
            // Edge Case 1: if the current map is what is supposed to be in the list then return
            if (currentMap.equals(mapHolder.get(c))) {
                previousMap = currentMap;
                return;
            }

            //Edge Case 2: Check if it is part of the same operations, something that GHS doesn't do
            if ((currentMap.equals("Argonne Forest") && previousMap.equals("Ballroom Blitz")) ||
                    (currentMap.equals("Fort De Vaux") && previousMap.equals("Verdun Heights")) ||
                    (currentMap.equals("Empire's Edge") && previousMap.equals("Monte Grappa")) ||
                    (currentMap.equals("Achi Baba") && previousMap.equals("Cape Helles")) ||
                    (currentMap.equals("Rupture") && previousMap.equals("Soissons")) ||
                    (currentMap.equals("Tsaritsyn") && previousMap.equals("Volga River")) ||
                    (currentMap.equals("Amiens") && previousMap.equals("St Quentin Scar")) ||
                    (currentMap.equals("Suez") && previousMap.equals("Fao Fortress")) ||
                    (currentMap.equals("Sinai Desert") && previousMap.equals("Suez"))) {
                previousMap = currentMap;
                return;
            }

            // Edge Case 3: this avoids a map change when the bot is first started and custom maps are JUST set. **ONLY WORKS WITH SHOCK OPS**
            if (previousMap != null)
            {
                mapManager.bfMapChange(hashMap.get(mapHolder.get(c)));
                c = (c + 1) % mapHolder.size();
                // Edge Case 4:  Avoid multiple calls to map change to avoid potential map loop, for now sleep for 30 seconds in case API is slow (or i lose internet)
                Thread.sleep(300000);
            }

        } catch (Exception e) {
            System.out.println("GT is down lol");
        }
    }


    /**
     * this is for the auto-completion of options in the slash command
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
            c = 0; //reset the counter
            System.out.println(mapHolder);
        }
    }

    /**
     * This is where we add the slash command and the options
     */
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {

        CommandData commandData = Commands.slash("custommap", "Sets a custom map from a FIXED list of maps")
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
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));

        event.getGuild().upsertCommand(commandData).queue();
    }
}

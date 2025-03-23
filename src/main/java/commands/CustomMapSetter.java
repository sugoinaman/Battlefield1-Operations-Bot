package commands;

import config.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.MapManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private String previousMap;
    private static ScheduledExecutorService scheduler = null;
    MapManager mapManager = new MapManager();
    private int c = 0;
    private static final String LOG_CHANNEL_ID = Configuration.getLOG_CHANNEL_ID();
    private JDA jda;
    private boolean waitingForCurrentMapToFinish = false;
    private Instant lastMapChangeTime = Instant.EPOCH;
    private boolean sentTheOperationLog = false;

    private boolean isCustomMapLoopOn = false;

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

    public CustomMapSetter(JDA jda) {
        this.jda = jda;
    }


    //Actual Logic which changes map from the list of maps entered by the user in the slash command be careful
    public void changeMap() {

        try {
            String currentMap = mapManager.fetchCurrentMap();
            // This part tries to avoid skipping the map being played currently, does not work for entire operations
            // ToDo: Adding a check to avoid skipping map till entire OPS is completed can be implemented but who cares

            // Check if we’re in cooldown after a map change
            if (Duration.between(lastMapChangeTime, Instant.now()).getSeconds() < 60) {
                sendLog("Cooldown active, skipping map change check...");
                return;
            }

            if (waitingForCurrentMapToFinish) {
                if (!currentMap.equals(previousMap)) {
                    //Previous map which was set when slash command was ran will be checked till currentMap is different
                    //This helps with letting the current map finish.
                    waitingForCurrentMapToFinish = false;
                    sendLog("The current map is: " + currentMap + " and the previous map was: " + previousMap + ". " + "Starting custom rotation....");
                } else {
                    return;
                }
            }

            // Check if the currentMap is the map set by user, here C is the index of the list of maps inputted by the user in mapHolder
            // Edge Case 1: if the current map is what is supposed to be in the list then return
            if (currentMap.equals(mapHolder.get(c))) {
                c = (c + 1) % mapHolder.size();
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
                if (sentTheOperationLog) {
                    // we already sent the logs, no need to send it again
                    return;
                } else if (!sentTheOperationLog) {
                    sendLog("The current map is: " + currentMap + " and the previous map was: " + previousMap + ". " + "Skipping map change....");
                    sentTheOperationLog = true;
                    return;
                }
            }

//                if (!operationSkipLogged) {
//                    sendLog("Current map is a part of an existing operations, skipping map change....");
//                    operationSkipLogged = true;  // Prevent repeated logs
//                }
//                previousMap = currentMap;
            //ToDo: possible error

//                return;


            // All Good changing map here
            sendLog("The current map is: " + currentMap + " and the previous map was: " + previousMap + ". " + "Switching maps....");
            mapManager.bfMapChange(hashMap.get(mapHolder.get(c)));
            sentTheOperationLog = false;
            lastMapChangeTime = Instant.now();
            sendLog("Map changed from " + currentMap + " to " + mapHolder.get(c));

        } catch (
                Exception e) {
            sendLog("Caught an error. Here's the stack trace \n");
            sendLog(Arrays.toString(e.getStackTrace()));

        }
    }

    /**
     * this is for the auto-completion of options in the slash command
     */
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("custom_map")) {
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

        if (event.getName().equals("custom_map")) {
            List<String> customMapsSetByUser = event.getOptions().stream()
                    .map(OptionMapping::getAsString)
                    .collect(Collectors.toList());
            event.reply("Maps selected for rotation are: " + String.join(", ", customMapsSetByUser)).queue();
            sendLog("List of maps to be looped has been updated to: " +( String.join("-> ", customMapsSetByUser)) + ". Waiting for current map to be finished before turning on");

            mapHolder = new ArrayList<>(customMapsSetByUser);
            c = 0; //reset the counter
            startScheduler();
            try {
                previousMap = mapManager.fetchCurrentMap();
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
            waitingForCurrentMapToFinish = true;
            System.out.println(mapHolder);
            isCustomMapLoopOn = true;
        }
        if (event.getName().equals("toggle_off")) {
            if(isCustomMapLoopOn) {
                event.reply("Turned off custom map rotation").queue();
                try {
                    mapHolder.clear();
                } catch (Exception e) {
                    System.out.println("Error clearing mapHolder");
                    sendLog("Error clearing mapHolder");
                }
                stopScheduler();
            }
            else{
                event.reply("Cannot switch custom map loop because it wasn't ON").queue();
                sendLog("Cannot switch custom map loop because it wasn't ON");
            }
        }
    }

    /**
     * This is where we add the slash command and the options
     */
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

        event.getGuild().upsertCommand(commandData).queue();
        event.getGuild().upsertCommand(loopOffCommand).queue();
    }


    private void sendLog(String message) {
        TextChannel logChannel = jda.getTextChannelById(LOG_CHANNEL_ID);
        if (logChannel != null) {
            String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.println(currentTime + "   " + message);
            logChannel.sendMessage(currentTime + "   " + message).queue();
        } else {
            System.out.println("log channel not found or inaccessible");
        }
    }

    public void startScheduler() {
        try {
            if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
                scheduler = Executors.newScheduledThreadPool(1);
            }
        } catch (Exception e) {
            sendLog("Scheduler is null ");
        }
        scheduler.scheduleAtFixedRate(this::changeMap, 0, 10, TimeUnit.SECONDS);
    }

    public void stopScheduler() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                sendLog("Custom map loop turned off.");
            }
        } catch (Exception e) {
            sendLog("Error turning off scheduler, it is already off");
        }
    }
}


//ToDo: Have a command to check what the schedulor is doing, so we can be sure toggle_off actually worked.
//ToDo: Another big BUG is when you set operations from the rotation itself for example Ballroom after amiens is what we usually get but if u put it manually it will cause issues.
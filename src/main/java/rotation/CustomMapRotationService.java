package rotation;

import tools.MapManager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Owns the custom-map state machine without depending on Discord/JDA.
 *
 * <p>The rotation decisions intentionally mirror the original CustomMapSetter implementation.</p>
 */
public final class CustomMapRotationService {

    private static final Map<String, Integer> MAP_INDEXES = createMapIndexes();

    private final MapManager mapManager;
    private final RotationScheduler scheduler;
    private final Consumer<String> logger;
    private final Clock clock;
    private final Sleeper sleeper;

    private List<String> maps = new ArrayList<>();
    private boolean waitingForCurrentMapToFinish;
    private Instant lastMapChangeTime = Instant.EPOCH;
    private boolean sentTheOperationLog;
    private boolean customMapLoopOn;
    private String currentMapWhenCommandStarted;
    private String previousMap;
    private int nextMapIndex;

    public CustomMapRotationService(
            MapManager mapManager,
            RotationScheduler scheduler,
            Consumer<String> logger
    ) {
        this(mapManager, scheduler, logger, Clock.systemUTC(), Thread::sleep);
    }

    CustomMapRotationService(
            MapManager mapManager,
            RotationScheduler scheduler,
            Consumer<String> logger,
            Clock clock,
            Sleeper sleeper
    ) {
        this.mapManager = Objects.requireNonNull(mapManager);
        this.scheduler = Objects.requireNonNull(scheduler);
        this.logger = Objects.requireNonNull(logger);
        this.clock = Objects.requireNonNull(clock);
        this.sleeper = Objects.requireNonNull(sleeper);
    }

    public StartResult start(List<String> selectedMaps) {
        maps = new ArrayList<>(selectedMaps);
        nextMapIndex = 0;

        try {
            if (scheduler.isRunning()) {
                scheduler.stop();
                System.out.println("Scheduler was already running");
            }
        } catch (Exception e) {
            System.out.println("Scheduler was stopped");
        }

        try {
            currentMapWhenCommandStarted = mapManager.getCurrentMap();
            if (currentMapWhenCommandStarted == null) {
                throw new IllegalStateException("GameTools returned no current map");
            }
            previousMap = currentMapWhenCommandStarted;
        } catch (Exception e) {
            System.out.println("Error fetching the current map; GameTools may be unavailable");
            return new StartResult(
                    false,
                    "Could not start custom map rotation because the current map could not be fetched."
            );
        }

        waitingForCurrentMapToFinish = true;
        customMapLoopOn = true;
        scheduler.start(this::tick);
        System.out.println(maps);

        logger.accept("List of maps to be looped has been updated to: "
                + String.join("-> ", selectedMaps)
                + ". Waiting for current map to be finished before turning on");

        return new StartResult(
                true,
                "Maps selected for rotation are: " + String.join(", ", selectedMaps)
        );
    }

    public String stop() {
        if (customMapLoopOn) {
            maps.clear();
            customMapLoopOn = false;
            nextMapIndex = 0;
            stopScheduler();
            return "Turned off custom map rotation";
        }

        String message = "Cannot switch custom map loop because it wasn't ON";
        logger.accept(message);
        return message;
    }

    public Status status() {
        boolean running = scheduler.isRunning();
        String nextMap = running && !maps.isEmpty()
                ? maps.get(nextMapIndex % maps.size())
                : null;
        return new Status(running, maps, nextMap);
    }

    /**
     * One polling iteration of the original custom-map algorithm.
     */
    void tick() {
        nextMapIndex = nextMapIndex % maps.size();

        try {
            if (mapManager.getPlayerCount() < 10) {
                stopScheduler();
                logger.accept("Player count less than 10, stopping auto rotation");
                return;
            }

            String currentMapAfterCommand = mapManager.getCurrentMap();
            String currentMap = mapManager.getCurrentMap();

            if (Duration.between(lastMapChangeTime, Instant.now(clock)).getSeconds() < 60) {
                return;
            }

            if (waitingForCurrentMapToFinish) {
                if (currentMapWhenCommandStarted.equals(currentMapAfterCommand)) {
                    return;
                } else {
                    waitingForCurrentMapToFinish = false;
                    System.out.println("The current map is: " + currentMap
                            + " and the previous map was: " + previousMap
                            + ". Starting custom rotation");
                    logger.accept("Current map is finished, starting custom rotation now...");
                }
            }

            if (!previousMap.equals(currentMap)) {
                if (MapManager.isCurrentMapPartOfOperation(currentMap, previousMap)) {
                    if (!sentTheOperationLog) {
                        logger.accept("Current map is: " + currentMap
                                + " which is part of  " + previousMap + "'s operation.");
                        sentTheOperationLog = true;
                    }
                    return;
                } else if (currentMap.equals(maps.get(nextMapIndex))) {
                    logger.accept("Map is now " + currentMap);
                    previousMap = maps.get(nextMapIndex);
                    nextMapIndex++;
                    sentTheOperationLog = false;
                    System.out.println("Map is now " + currentMap + "updated C: " + nextMapIndex);

                    lastMapChangeTime = Instant.now(clock);
                    return;
                } else {
                    logger.accept("Current map: " + currentMap
                            + " | Previous map was: " + previousMap
                            + " | Switching maps to: " + maps.get(nextMapIndex));

                    sleeper.sleep(12000);
                    mapManager.changeMap(MAP_INDEXES.get(maps.get(nextMapIndex)));
                    previousMap = maps.get(nextMapIndex);
                    sentTheOperationLog = false;
                    lastMapChangeTime = Instant.now(clock);
                    logger.accept("Map changed from " + currentMap + " to " + maps.get(nextMapIndex));
                    nextMapIndex++;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void stopScheduler() {
        try {
            if (scheduler.isRunning()) {
                scheduler.stop();
                maps.clear();
                logger.accept("Custom map loop turned off.");
            }
        } catch (Exception e) {
            logger.accept("Error turning off scheduler, it is already off");
        }
    }

    private static Map<String, Integer> createMapIndexes() {
        Map<String, Integer> indexes = new HashMap<>();
        indexes.put("Soissons", 18);
        indexes.put("Achi Baba", 16);
        indexes.put("Suez", 20);
        indexes.put("Fort De Vaux", 1);
        indexes.put("Giant's Shadow", 4);
        indexes.put("Monte Grappa", 13);
        indexes.put("Verdun Heights", 9);
        indexes.put("River Somme", 3);
        indexes.put("Cape Helles", 0);
        indexes.put("Prise de Tahure", 17);
        indexes.put("Zeebrugge", 6);
        indexes.put("Empire's Edge", 22);
        indexes.put("Volga River", 21);
        indexes.put("Rupture", 14);
        indexes.put("St Quentin Scar", 8);
        indexes.put("Amiens", 5);
        indexes.put("Ballroom Blitz", 23);
        indexes.put("Galicia", 12);
        indexes.put("Tsaritsyn", 11);
        indexes.put("Brusilov Keep", 7);
        indexes.put("Łupków Pass", 19);
        indexes.put("Argonne Forest", 2);
        indexes.put("Sinai Desert", 15);
        indexes.put("Fao Fortress", 10);
        return Map.copyOf(indexes);
    }

    public record StartResult(boolean started, String message) {
    }

    public record Status(boolean running, List<String> maps, String nextMap) {
        public Status {
            maps = List.copyOf(maps);
        }
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long milliseconds) throws InterruptedException;
    }
}

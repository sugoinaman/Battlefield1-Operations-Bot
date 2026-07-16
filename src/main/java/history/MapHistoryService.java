package history;

import tools.MapManager;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Tracks a bounded, in-memory history of map changes.
 */
public final class MapHistoryService {

    private static final int MAX_RENDERED_CHARACTERS = 4_000;
    private static final ZoneId HISTORY_TIME_ZONE = ZoneId.of("Europe/Berlin");
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private final MapManager mapManager;
    private final Clock clock;
    private final int maxRenderedCharacters;
    private final Deque<String> entries = new ArrayDeque<>();

    private String previousMap;
    private int renderedCharacters;

    public MapHistoryService(MapManager mapManager) {
        this(mapManager, Clock.system(HISTORY_TIME_ZONE), MAX_RENDERED_CHARACTERS);
    }

    MapHistoryService(
            MapManager mapManager,
            Clock clock,
            int maxRenderedCharacters
    ) {
        this.mapManager = Objects.requireNonNull(mapManager);
        this.clock = Objects.requireNonNull(clock);
        if (maxRenderedCharacters < 1) {
            throw new IllegalArgumentException("History character limit must be positive");
        }
        this.maxRenderedCharacters = maxRenderedCharacters;
    }

    public void poll() {
        try {
            String currentMap = mapManager.getCurrentMap();
            if (currentMap == null) {
                return;
            }
            recordMapChange(currentMap);
        } catch (Exception e) {
            System.out.println("Issue with updateMapHistory, most likely internet issue");
        }
    }

    private synchronized void recordMapChange(String currentMap) {
        if (currentMap.equals(previousMap)) {
            return;
        }
        previousMap = currentMap;

        String timestamp = ZonedDateTime.now(clock)
                .withZoneSameInstant(HISTORY_TIME_ZONE)
                .format(TIMESTAMP_FORMAT);
        String entry = currentMap + " at " + timestamp;
        int entryCharacters = entry.length() + 1;

        if (entryCharacters > maxRenderedCharacters) {
            throw new IllegalArgumentException("A map-history entry exceeds the configured limit");
        }

        while (!entries.isEmpty()
                && renderedCharacters + entryCharacters > maxRenderedCharacters) {
            renderedCharacters -= entries.removeFirst().length() + 1;
        }

        entries.addLast(entry);
        renderedCharacters += entryCharacters;
    }

    public synchronized String readHistory() {
        if (entries.isEmpty()) {
            return "";
        }
        return String.join("\n", entries) + "\n";
    }
}

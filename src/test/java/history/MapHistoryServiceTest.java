package history;

import org.junit.jupiter.api.Test;
import tools.MapManager;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapHistoryServiceTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-01-15T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void recordsOnlyActualMapChangesInTheExistingTimestampedFormat() {
        FakeMapManager maps = new FakeMapManager(
                null,
                "Monte Grappa",
                "Monte Grappa",
                "Ballroom Blitz"
        );
        MapHistoryService service = new MapHistoryService(maps, FIXED_CLOCK, 4_000);

        service.poll();
        service.poll();
        service.poll();
        service.poll();

        assertEquals(
                "Monte Grappa at 2026-01-15 13:00:00 CET\n"
                        + "Ballroom Blitz at 2026-01-15 13:00:00 CET\n",
                service.readHistory()
        );
    }

    @Test
    void removesTheOldestEntriesBeforeTheRenderedLimitIsExceeded() {
        FakeMapManager maps =
                new FakeMapManager("Amiens", "Suez", "Monte Grappa");
        String retainedHistory = "Suez at 2026-01-15 13:00:00 CET\n"
                + "Monte Grappa at 2026-01-15 13:00:00 CET\n";
        MapHistoryService service =
                new MapHistoryService(maps, FIXED_CLOCK, retainedHistory.length());

        service.poll();
        service.poll();
        service.poll();

        assertEquals(retainedHistory, service.readHistory());
    }

    private static final class FakeMapManager implements MapManager {
        private final List<String> nullableMaps;
        private int index;

        private FakeMapManager(String... maps) {
            nullableMaps = java.util.Arrays.asList(maps);
        }

        @Override
        public String getCurrentMap() {
            return nullableMaps.get(index++);
        }

        @Override
        public int getPlayerCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void changeMap(int mapIndex) {
            throw new UnsupportedOperationException();
        }
    }
}

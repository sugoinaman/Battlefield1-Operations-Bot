package rotation;

import org.junit.jupiter.api.Test;
import tools.MapManager;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomMapRotationServiceTest {

    @Test
    void startsWithTheCurrentMapAndSchedulesTheOriginalPollingTask() {
        Fixture fixture = new Fixture("Ballroom Blitz");

        CustomMapRotationService.StartResult result = fixture.service.start(
                List.of("Monte Grappa", "Fao Fortress")
        );

        assertTrue(result.started());
        assertEquals(
                "Maps selected for rotation are: Monte Grappa, Fao Fortress",
                result.message()
        );
        assertTrue(fixture.scheduler.running);
        assertTrue(fixture.scheduler.task != null);
        assertEquals(
                "List of maps to be looped has been updated to: Monte Grappa-> Fao Fortress. "
                        + "Waiting for current map to be finished before turning on",
                fixture.logs.getFirst()
        );

        CustomMapRotationService.Status status = fixture.service.status();
        assertTrue(status.running());
        assertEquals(List.of("Monte Grappa", "Fao Fortress"), status.maps());
        assertEquals("Monte Grappa", status.nextMap());
    }

    @Test
    void doesNotStartWhenGameToolsCannotProvideTheCurrentMap() {
        Fixture fixture = new Fixture(null);

        CustomMapRotationService.StartResult result = fixture.service.start(List.of("Monte Grappa"));

        assertFalse(result.started());
        assertEquals(
                "Could not start custom map rotation because the current map could not be fetched.",
                result.message()
        );
        assertFalse(fixture.scheduler.running);
        assertTrue(fixture.logs.isEmpty());
    }

    @Test
    void waitsUntilTheMapThatWasActiveAtCommandTimeFinishes() {
        Fixture fixture = new Fixture("Ballroom Blitz");
        fixture.service.start(List.of("Monte Grappa"));
        fixture.gameServer.respondWith("Ballroom Blitz", "Ballroom Blitz");

        fixture.service.tick();

        assertTrue(fixture.gameServer.changedMapIndexes.isEmpty());
        assertTrue(fixture.sleeps.isEmpty());
        assertEquals(1, fixture.logs.size());
    }

    @Test
    void letsTheSecondMapOfAnOperationContinueAndLogsItOnlyOnce() {
        Fixture fixture = new Fixture("Monte Grappa");
        fixture.service.start(List.of("Soissons"));
        fixture.gameServer.respondWith(
                "Empire's Edge", "Empire's Edge",
                "Empire's Edge", "Empire's Edge"
        );

        fixture.service.tick();
        fixture.service.tick();

        assertTrue(fixture.gameServer.changedMapIndexes.isEmpty());
        assertEquals(
                1,
                fixture.logs.stream()
                        .filter(message -> message.contains("which is part of  Monte Grappa's operation"))
                        .count()
        );
    }

    @Test
    void advancesWhenTheServerAlreadyLoadedTheRequestedMapAndHonorsTheSixtySecondGuard() {
        Fixture fixture = new Fixture("Ballroom Blitz");
        fixture.service.start(List.of("Soissons", "Fao Fortress"));
        fixture.gameServer.respondWith("Soissons", "Soissons");

        fixture.service.tick();

        assertEquals("Fao Fortress", fixture.service.status().nextMap());
        assertTrue(fixture.gameServer.changedMapIndexes.isEmpty());

        fixture.gameServer.respondWith("Unwanted map", "Unwanted map");
        fixture.service.tick();

        assertTrue(fixture.gameServer.changedMapIndexes.isEmpty());
        assertTrue(fixture.sleeps.isEmpty());
    }

    @Test
    void retainsEveryExistingCommandMapIndexAndTheTwelveSecondDelay() {
        Map<String, Integer> expectedIndexes = Map.ofEntries(
                entry("Giant's Shadow", 4),
                entry("Monte Grappa", 13),
                entry("River Somme", 3),
                entry("Cape Helles", 0),
                entry("Zeebrugge", 6),
                entry("Fao Fortress", 10),
                entry("Soissons", 18),
                entry("Volga River", 21),
                entry("St Quentin Scar", 8),
                entry("Ballroom Blitz", 23),
                entry("Łupków Pass", 19),
                entry("Prise de Tahure", 17),
                entry("Verdun Heights", 9)
        );

        expectedIndexes.forEach((map, expectedIndex) -> {
            Fixture fixture = new Fixture("Initial map");
            fixture.service.start(List.of(map));
            fixture.gameServer.respondWith("Unwanted map", "Unwanted map");

            fixture.service.tick();

            assertEquals(List.of(expectedIndex), fixture.gameServer.changedMapIndexes, map);
            assertEquals(List.of(12000L), fixture.sleeps, map);
        });
    }

    @Test
    void stopsPollingAndClearsTheRotationWhenPlayerCountDropsBelowTen() {
        Fixture fixture = new Fixture("Ballroom Blitz");
        fixture.service.start(List.of("Monte Grappa"));
        fixture.gameServer.playerCount = 9;

        fixture.service.tick();

        CustomMapRotationService.Status status = fixture.service.status();
        assertFalse(status.running());
        assertTrue(status.maps().isEmpty());
        assertNull(status.nextMap());
        assertEquals(
                List.of(
                        "Custom map loop turned off.",
                        "Player count less than 10, stopping auto rotation"
                ),
                fixture.logs.subList(fixture.logs.size() - 2, fixture.logs.size())
        );
    }

    private static final class Fixture {
        private final FakeGameServer gameServer;
        private final FakeScheduler scheduler = new FakeScheduler();
        private final List<String> logs = new ArrayList<>();
        private final List<Long> sleeps = new ArrayList<>();
        private final CustomMapRotationService service;

        private Fixture(String initialMap) {
            gameServer = new FakeGameServer(initialMap);
            service = new CustomMapRotationService(
                    gameServer,
                    scheduler,
                    logs::add,
                    Clock.fixed(Instant.parse("2026-07-15T12:00:00Z"), ZoneOffset.UTC),
                    sleeps::add
            );
        }
    }

    private static final class FakeScheduler implements RotationScheduler {
        private boolean running;
        private Runnable task;

        @Override
        public void start(Runnable task) {
            this.task = task;
            running = true;
        }

        @Override
        public void stop() {
            running = false;
            task = null;
        }

        @Override
        public boolean isRunning() {
            return running;
        }
    }

    private static final class FakeGameServer implements MapManager {
        private final Deque<String> mapResponses = new ArrayDeque<>();
        private final List<Integer> changedMapIndexes = new ArrayList<>();
        private String currentMap;
        private int playerCount = 64;

        private FakeGameServer(String currentMap) {
            this.currentMap = currentMap;
        }

        private void respondWith(String... maps) {
            mapResponses.addAll(List.of(maps));
        }

        @Override
        public String getCurrentMap() {
            return mapResponses.isEmpty() ? currentMap : mapResponses.removeFirst();
        }

        @Override
        public int getPlayerCount() {
            return playerCount;
        }

        @Override
        public void changeMap(int mapIndex) {
            changedMapIndexes.add(mapIndex);
        }
    }
}

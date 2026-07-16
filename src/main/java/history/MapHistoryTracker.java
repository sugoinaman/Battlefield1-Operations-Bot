package history;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Production polling adapter retaining the original immediate start and 30-second interval.
 */
public final class MapHistoryTracker {

    private final MapHistoryService historyService;
    private final ScheduledExecutorService scheduler;

    public MapHistoryTracker(MapHistoryService historyService) {
        this(historyService, Executors.newSingleThreadScheduledExecutor());
    }

    MapHistoryTracker(MapHistoryService historyService, ScheduledExecutorService scheduler) {
        this.historyService = Objects.requireNonNull(historyService);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(historyService::poll, 0, 30, TimeUnit.SECONDS);
    }
}

package implementation;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapLoopFixImpl {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private List<String> recentMaps;

    public MapLoopFixImpl() {
        scheduler.scheduleAtFixedRate(this::mapLoopFix,0, 20, TimeUnit.SECONDS);
    }

    private void mapLoopFix() {
        recentMaps = // can only create one instance of mpahistoryimpl!
    }
}


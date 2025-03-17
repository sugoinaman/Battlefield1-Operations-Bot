package commands;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// This class is mainly used to get onMapLoopEvent which will either return true or false, which is later used someplace else.

public class MapLoopFix {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static List<String> recentMaps; // contains 2 most recent maps
    MapHistory mapHistory = new MapHistory();

    public MapLoopFix() {
        scheduler.scheduleAtFixedRate(this::onMapLoopEvent, 0, 10, TimeUnit.SECONDS);
    }

    public boolean onMapLoopEvent() {

        if (recentMaps.size() != 2) {
            return false;
        }
        recentMaps = mapHistory.getRecentMaps();
        //brute forcing
        String currentMap = recentMaps.get(1);
        String previousMap = recentMaps.get(0);

        // ToDo: Remember to wait(60s) when sending map change command

        if ((previousMap.equals("zee") && currentMap.equals("empire"))) return true;
        if ((previousMap.equals("Rupture") && currentMap.equals("suez"))) return true;
        if ((previousMap.equals("Argonne") && currentMap.equals("Tsar"))) return true;
        if ((previousMap.equals("Lupkow") && currentMap.equals("Argonne"))) return true;

        return false;
    }
}



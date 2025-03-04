package implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapLoopFixImpl {

    private static final Logger log = LoggerFactory.getLogger(MapLoopFixImpl.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static List<String> recentMaps; //contains 5 most recent maps
    MapHistoryImpl mapHistory = new MapHistoryImpl();

    public MapLoopFixImpl() {
        scheduler.scheduleAtFixedRate(this::mapLoopFix, 0, 40, TimeUnit.SECONDS);
    }

    //TODO:this timer needs a 20 sec difference between map history check to avoid an edge case
    // but i dont get paid enuf so i increased period to 40 muahahah
    private void mapLoopFix() {

        if (recentMaps.size() < 5) {
            return;
        }
        recentMaps = mapHistory.getRecentMaps();

        //brute forcing
        String latestMap = recentMaps.get(4);
        String secondLatestMap = recentMaps.get(3);

        if (latestMap.equalsIgnoreCase("Suez") &&
                (secondLatestMap.equalsIgnoreCase("Rupture") ||
                        secondLatestMap.equalsIgnoreCase("Soissons"))) {
            log.info("Map loop detected");
        }
        if (latestMap.equalsIgnoreCase("Empire's Edge") &&
                secondLatestMap.equalsIgnoreCase("Zeebrugge")) {
            log.info("loop detected");
        }
        if (latestMap.equalsIgnoreCase("Tsaritsyn") &&
                (secondLatestMap.equalsIgnoreCase("Argonne") ||
                        secondLatestMap.equalsIgnoreCase("ballroom"))) {
            log.info("loop detected");
        }
        if (latestMap.equalsIgnoreCase("Argonne") &&
                secondLatestMap.equalsIgnoreCase("Lupkow")) {
            log.info("loop detected");
        }
        //ToDo: Cannot check Brusilov loop
    }
}


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
        scheduler.scheduleAtFixedRate(this::onMapLoopEvent, 0, 10, TimeUnit.SECONDS);
    }

    public boolean onMapLoopEvent() {

        if (recentMaps.size() < 5) {
            return false;
        }
        recentMaps = mapHistory.getRecentMaps();

        //brute forcing
        String currentMap = recentMaps.get(4);
        String previousMap = recentMaps.get(3);

//        if (latestMap.equalsIgnoreCase("Suez") &&
//                (secondLatestMap.equalsIgnoreCase("Rupture") ||
//                        secondLatestMap.equalsIgnoreCase("Soissons"))) {
//            log.info("Map loop detected");
//        }
//        if (latestMap.equalsIgnoreCase("Empire's Edge") &&
//                secondLatestMap.equalsIgnoreCase("Zeebrugge")) {
//            log.info("loop detected");
//        }
//        if (latestMap.equalsIgnoreCase("Tsaritsyn") &&
//                (secondLatestMap.equalsIgnoreCase("Argonne") ||
//                        secondLatestMap.equalsIgnoreCase("ballroom"))) {
//            log.info("loop detected");
//        }
//        if (latestMap.equalsIgnoreCase("Argonne") &&
//                secondLatestMap.equalsIgnoreCase("Lupkow")) {
//            log.info("loop detected");
//        }

        //this function needs to be executed 24/7

        if ((previousMap.equals("zee") && currentMap.equals("empire"))) return true;
        if ((previousMap.equals("Rupture") && currentMap.equals("suez"))) return true;
        if ((previousMap.equals("Argonne") && currentMap.equals("Tsar"))) return true;
        if ((previousMap.equals("Lupkow") && currentMap.equals("Argonne"))) return true;

        return false;
    }


}



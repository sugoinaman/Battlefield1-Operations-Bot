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
        scheduler.scheduleAtFixedRate(this::mapLoopFix, 0, 20, TimeUnit.SECONDS);
    }

    private void mapLoopFix() {
        recentMaps = mapHistory.getRecentMaps();

        //brute forcing
        String latestMap = recentMaps.get(4);
        String secondLatestMap = recentMaps.get(3);

        if (latestMap.equalsIgnoreCase("Sinai") &&
                (secondLatestMap.equalsIgnoreCase("ballroom blitz") ||
                secondLatestMap.equalsIgnoreCase("Argonne"))) {
            System.out.println("Map loop detected, change map to fao");
        }
        if (latestMap.equalsIgnoreCase("Fort De Vaux") && !secondLatestMap.equalsIgnoreCase("Verdun")) {
            System.out.println("Fort loop detected");

        }
        if (latestMap.equalsIgnoreCase("Tsaritsyn") && !secondLatestMap.equalsIgnoreCase("Volga")) {
            System.out.println("tsar loop");
        }
        if (latestMap.equalsIgnoreCase("Empire's Edge") && secondLatestMap.equalsIgnoreCase("Monte"))  {
            //also handles empire before tsar loop!
            System.out.println("tsar loop detected");
        }
        //TODO: Rupture loop
    }
}


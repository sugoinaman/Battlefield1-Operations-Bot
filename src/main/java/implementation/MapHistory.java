package implementation;
import io.github.cdimascio.dotenv.Dotenv;
import tools.MapManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapHistory {

    Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .load();

    private String lastMap = null;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final MapManager mapManager =  new MapManager();
    private static final List<String> recentMaps = new ArrayList<>();

    public MapHistory() {
        scheduler.scheduleAtFixedRate(this::updateMapHistory, 0, 10, TimeUnit.SECONDS);
    }

    public final List <String> getRecentMaps(){
        return new ArrayList<>(recentMaps);
    }

    private void updateMapHistory() {
        try {
            String currentMap = mapManager.fetchCurrentMap();
            if (currentMap == null || currentMap.equals(lastMap)) return; // Map didn't change!

            mapManager.writeToFile(currentMap);
            lastMap = currentMap;

            if (recentMaps.size() == 2) {
                recentMaps.removeFirst();
                recentMaps.add(currentMap);
            } else recentMaps.add(currentMap);
        } catch (Exception e) {
        }
    }

}


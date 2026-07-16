package ea;

import tools.MapAccessException;
import tools.MapManager;

public class EaMapManager implements MapManager {
    @Override
    public String getCurrentMap() {
        throw new MapAccessException("EA map lookup has not been implemented yet");
    }

    @Override
    public int getPlayerCount() {
        throw new MapAccessException("EA player-count lookup has not been implemented yet");
    }

    @Override
    public void changeMap(int mapIndex) {
        throw new MapAccessException("EA map changes have not been implemented yet");
    }
}

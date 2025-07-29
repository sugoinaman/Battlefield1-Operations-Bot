package Model;

import java.util.List;
import java.util.Map;


public class GameRecord {
    public List<MapRecord> mapHistory;
}

class MapRecord {
    public List<String> time;
    public Map<String, String> mapName;
}

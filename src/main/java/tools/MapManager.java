package tools;

public interface MapManager {

    String getCurrentMap();

    int getPlayerCount();

    void changeMap(int mapIndex);

    static boolean isCurrentMapPartOfOperation(String currentMap, String previousMap) {
        return (currentMap.equals("Argonne Forest") && previousMap.equals("Ballroom Blitz"))
                || (currentMap.equals("Fort De Vaux") && previousMap.equals("Verdun Heights"))
                || (currentMap.equals("Empire's Edge") && previousMap.equals("Monte Grappa"))
                || (currentMap.equals("Achi Baba") && previousMap.equals("Cape Helles"))
                || (currentMap.equals("Rupture") && previousMap.equals("Soissons"))
                || (currentMap.equals("Tsaritsyn") && previousMap.equals("Volga River"))
                || (currentMap.equals("Amiens") && previousMap.equals("St Quentin Scar"))
                || (currentMap.equals("Suez") && previousMap.equals("Fao Fortress"))
                || (currentMap.equals("Sinai Desert") && previousMap.equals("Suez"))
                || (currentMap.equals("Sinai Desert") && previousMap.equals("Fao Fortress"));
    }
}

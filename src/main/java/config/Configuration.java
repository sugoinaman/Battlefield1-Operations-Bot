package config;

import io.github.cdimascio.dotenv.Dotenv;

public class Configuration {
    private final static Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .load();


    public static String getDiscordToken() {
        return dotenv.get("DISCORD_TOKEN");
    }

    public static String getFILE_PATH() {
        return dotenv.get("FILE_PATH");
    }

    public static String getGTToken() {
        return dotenv.get("GT_TOKEN");
    }

    public static String getLOG_CHANNEL_ID() {
        return dotenv.get("LOG_CHANNEL_ID");
    }

    public static String getGROUP_ID(){
        return dotenv.get("GROUP_ID");
    }

    public static String getSERVER_ID(){
        return dotenv.get("SERVER_ID");
    }

    public static String getSERVER_URL(){
        return dotenv.get("SERVER_URL");
    }

    public static String getDISCORD_SERVER_ID(){
        return dotenv.get("DISCORD_SERVER_ID");
    }

}

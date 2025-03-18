package config;

import io.github.cdimascio.dotenv.Dotenv;

public class Configuration {
    private final static  Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .load();


    public static String getDiscordToken() {
        return dotenv.get("DISCORD_TOKEN");
    }

    public static String getFILE_PATH() {
        return dotenv.get("FILE_PATH");
    }

    public static String getGTToken(){
        return dotenv.get("GT_TOKEN");
    }
}

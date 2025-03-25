import commands.CustomMapSetter;
import commands.MapHistory;
import config.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import tools.MapManager;

public class Main {

    public static void main(String[] args) {

        JDA jda = JDABuilder.createDefault(Configuration.getDiscordToken())
                .addEventListeners(new MapHistory())
                .build();
        jda.addEventListener(new CustomMapSetter(jda));
    }
}


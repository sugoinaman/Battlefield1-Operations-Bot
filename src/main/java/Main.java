import commands.MapHistory;
import config.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;


public class Main {


    public static void main(String[] args) {

        JDA api = JDABuilder.createDefault(Configuration.getDiscordToken())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new MapHistory())
                .build();

        //Map History Implementation triggers on bot start
         // MapHistoryImpl mapHistory = new MapHistoryImpl();
        // commented this since i am calling it in another class which will be called here.
    }
}

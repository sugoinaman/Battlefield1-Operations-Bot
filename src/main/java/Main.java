import commands.MapHistory;
import commands.MyCommands;
import config.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;


public class Main {


    public static void main(String[] args) {

        JDA api = JDABuilder.createDefault(Configuration.getDiscordToken())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new MyCommands())
                .build();

        //Map History triggers on bot start
        MapHistory mapHistory = new MapHistory();
    }
}

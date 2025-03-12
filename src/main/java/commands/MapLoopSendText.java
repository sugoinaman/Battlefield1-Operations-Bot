package commands;

import implementation.MapLoopFix;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapLoopSendText {
    private MapLoopFix mapLoopFix;
    private String channelId =  "1347188395704713229";
    private  JDA jda;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public MapLoopSendText(JDA jda,MapLoopFix mapLoopFix) {
        this.mapLoopFix=mapLoopFix;
        this.jda = jda;
        scheduler.scheduleAtFixedRate(this::checkForMapLoop, 0, 10, TimeUnit.SECONDS);
    }
    private void checkForMapLoop() {
        if (mapLoopFix.onMapLoopEvent()) {
            TextChannel textChannel = jda.getTextChannelById(channelId);
            if (textChannel != null) textChannel.sendMessage("Map loop detected").queue();
            else System.out.println("text channel not found");
        }
    }
}

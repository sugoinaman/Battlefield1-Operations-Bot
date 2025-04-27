package tools;

import config.Configuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DiscordTools {

    private static JDA jda;
    private static final String LOG_CHANNEL_ID = Configuration.getLOG_CHANNEL_ID();

    public static void sendLog(String message) {
        TextChannel logChannel = jda.getTextChannelById(LOG_CHANNEL_ID);
        if (logChannel != null) {
            String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(new Color(8, 60, 219, 144)).setDescription(currentTime + "   " + message);
            logChannel.sendMessageEmbeds(embedBuilder.build()).queue();

            System.out.println(currentTime + "   " + message);
        } else {
            System.out.println("log channel not found or inaccessible");
        }
    }

}

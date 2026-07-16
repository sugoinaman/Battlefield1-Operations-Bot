package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Discord-specific logging adapter kept outside of the rotation state machine.
 */
public final class DiscordLogSink implements Consumer<String> {

    private final JDA jda;
    private final String channelId;

    public DiscordLogSink(JDA jda, String channelId) {
        this.jda = Objects.requireNonNull(jda);
        this.channelId = channelId;
    }

    @Override
    public void accept(String message) {
        TextChannel logChannel = jda.getTextChannelById(channelId);
        if (logChannel == null) {
            System.out.println("log channel not found or inaccessible");
            return;
        }

        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(new Color(8, 60, 219, 144))
                .setDescription(currentTime + "   " + message);
        logChannel.sendMessageEmbeds(embed.build()).queue();
        System.out.println(currentTime + "   " + message);
    }
}

package commands;

import history.MapHistoryService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.MapManager;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class MapHistoryTest {

    private static final String ALLOWED_GUILD_ID = "allowed-guild";

    @Test
    void ignoresCommandsOwnedByOtherListeners() {
        MapHistory listener = listener();
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
        when(event.getName()).thenReturn("custom_map_info");

        listener.onSlashCommandInteraction(event);

        verify(event).getName();
        verifyNoMoreInteractions(event);
    }

    @Test
    void readsAndRepliesWithHistoryOnlyAfterDiscordAcknowledgesTheCommand() {
        MapHistory listener = listener("Monte Grappa", "Ballroom Blitz");

        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
        Guild guild = mock(Guild.class);
        ReplyCallbackAction deferAction = mock(ReplyCallbackAction.class);
        InteractionHook hook = mock(InteractionHook.class);
        @SuppressWarnings("unchecked")
        WebhookMessageEditAction<Message> editAction = mock(WebhookMessageEditAction.class);

        when(event.getName()).thenReturn("map_history");
        when(event.getGuild()).thenReturn(guild);
        when(guild.getId()).thenReturn(ALLOWED_GUILD_ID);
        when(event.deferReply()).thenReturn(deferAction);
        when(hook.editOriginalEmbeds(any(MessageEmbed[].class))).thenReturn(editAction);

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<Consumer<? super InteractionHook>> successCaptor =
                (ArgumentCaptor) ArgumentCaptor.forClass(Consumer.class);

        listener.onSlashCommandInteraction(event);

        verify(deferAction).queue(successCaptor.capture(), any());
        verifyNoInteractions(hook);

        successCaptor.getValue().accept(hook);

        ArgumentCaptor<MessageEmbed[]> embedCaptor = ArgumentCaptor.forClass(MessageEmbed[].class);
        verify(hook).editOriginalEmbeds(embedCaptor.capture());
        verify(editAction).queue();

        MessageEmbed embed = embedCaptor.getValue()[0];
        assertEquals("Played maps are:", embed.getAuthor().getName());
        assertTrue(embed.getDescription().contains("Monte Grappa"));
        assertTrue(embed.getDescription().contains("Ballroom Blitz"));
    }

    @Test
    void rejectsCommandsFromAnUnconfiguredGuildWithoutDeferring() {
        MapHistory listener = listener();
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
        Guild guild = mock(Guild.class);
        ReplyCallbackAction replyAction = mock(ReplyCallbackAction.class);

        when(event.getName()).thenReturn("map_history");
        when(event.getGuild()).thenReturn(guild);
        when(guild.getId()).thenReturn("other-guild");
        when(event.reply("This command is not available in this server.")).thenReturn(replyAction);
        when(replyAction.setEphemeral(true)).thenReturn(replyAction);

        listener.onSlashCommandInteraction(event);

        verify(replyAction).setEphemeral(true);
        verify(replyAction).queue();
        verify(event, never()).deferReply();
    }

    @Test
    void registersMapHistoryForEveryoneInGuildContext() {
        MapHistory listener = listener();
        GuildReadyEvent event = mock(GuildReadyEvent.class);
        Guild guild = mock(Guild.class);
        @SuppressWarnings("unchecked")
        RestAction<Command> upsertAction = mock(RestAction.class);

        when(event.getGuild()).thenReturn(guild);
        when(guild.upsertCommand(any(CommandData.class))).thenReturn(upsertAction);

        listener.onGuildReady(event);

        ArgumentCaptor<CommandData> commandCaptor = ArgumentCaptor.forClass(CommandData.class);
        verify(guild).upsertCommand(commandCaptor.capture());
        verify(upsertAction).queue();

        CommandData command = commandCaptor.getValue();
        assertEquals("map_history", command.getName());
        assertSame(DefaultMemberPermissions.ENABLED, command.getDefaultPermissions());
        assertTrue(command.getContexts().contains(InteractionContextType.GUILD));
    }

    private static MapHistory listener(String... maps) {
        Queue<String> responses = new ArrayDeque<>(Arrays.asList(maps));
        MapManager mapManager = mock(MapManager.class);
        when(mapManager.getCurrentMap())
                .thenAnswer(ignored -> responses.isEmpty() ? null : responses.remove());
        MapHistoryService historyService = new MapHistoryService(mapManager);
        for (int i = 0; i < maps.length; i++) {
            historyService.poll();
        }
        return new MapHistory(historyService, ALLOWED_GUILD_ID);
    }
}

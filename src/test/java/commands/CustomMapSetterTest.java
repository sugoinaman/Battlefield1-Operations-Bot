package commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
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
import rotation.CustomMapRotationService;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

class CustomMapSetterTest {

    private static final String ALLOWED_GUILD_ID = "allowed-guild";

    @Test
    void ignoresCommandsOwnedByOtherListeners() {
        CustomMapSetter listener = listener();
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
        when(event.getName()).thenReturn("map_history");

        listener.onSlashCommandInteraction(event);

        verify(event).getName();
        verifyNoMoreInteractions(event);
    }

    @Test
    void toggleOffDoesNotRunOrLogUntilDiscordAcknowledgesTheCommand() {
        CustomMapRotationService rotation = mock(CustomMapRotationService.class);
        CustomMapSetter listener = new CustomMapSetter(rotation, ALLOWED_GUILD_ID);

        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
        Guild guild = mock(Guild.class);
        ReplyCallbackAction deferAction = mock(ReplyCallbackAction.class);
        InteractionHook hook = mock(InteractionHook.class);
        @SuppressWarnings("unchecked")
        WebhookMessageEditAction<Message> editAction = mock(WebhookMessageEditAction.class);

        when(event.getName()).thenReturn("toggle_off");
        when(event.getGuild()).thenReturn(guild);
        when(guild.getId()).thenReturn(ALLOWED_GUILD_ID);
        when(event.deferReply()).thenReturn(deferAction);
        when(rotation.stop()).thenReturn("Cannot switch custom map loop because it wasn't ON");
        when(hook.editOriginal("Cannot switch custom map loop because it wasn't ON")).thenReturn(editAction);

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<Consumer<? super InteractionHook>> successCaptor =
                (ArgumentCaptor) ArgumentCaptor.forClass(Consumer.class);

        listener.onSlashCommandInteraction(event);

        verify(deferAction).queue(successCaptor.capture(), any());
        verifyNoInteractions(hook);
        verify(rotation, never()).stop();

        successCaptor.getValue().accept(hook);

        verify(hook).editOriginal("Cannot switch custom map loop because it wasn't ON");
        verify(editAction).queue();
        verify(rotation).stop();
    }

    @Test
    void rejectsCommandsFromAnUnconfiguredGuildWithoutDeferring() {
        CustomMapSetter listener = listener();
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
        Guild guild = mock(Guild.class);
        ReplyCallbackAction replyAction = mock(ReplyCallbackAction.class);

        when(event.getName()).thenReturn("custom_map_info");
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
    void registersInfoForEveryoneAndKeepsMutatingCommandsRestricted() {
        CustomMapSetter listener = listener();
        GuildReadyEvent event = mock(GuildReadyEvent.class);
        Guild guild = mock(Guild.class);
        @SuppressWarnings("unchecked")
        RestAction<Command> upsertAction = mock(RestAction.class);

        when(event.getGuild()).thenReturn(guild);
        when(guild.upsertCommand(any(CommandData.class))).thenReturn(upsertAction);

        listener.onGuildReady(event);

        ArgumentCaptor<CommandData> commandCaptor = ArgumentCaptor.forClass(CommandData.class);
        verify(guild, org.mockito.Mockito.times(3)).upsertCommand(commandCaptor.capture());
        verify(upsertAction, org.mockito.Mockito.times(3)).queue();

        Map<String, CommandData> commands = commandCaptor.getAllValues().stream()
                .collect(Collectors.toMap(CommandData::getName, command -> command));

        CommandData info = commands.get("custom_map_info");
        assertSame(DefaultMemberPermissions.ENABLED, info.getDefaultPermissions());
        assertTrue(info.getContexts().contains(InteractionContextType.GUILD));

        assertKickMembersPermission(commands.get("custom_map"));
        assertKickMembersPermission(commands.get("toggle_off"));
    }

    private static void assertKickMembersPermission(CommandData command) {
        assertEquals(
                Permission.KICK_MEMBERS.getRawValue(),
                command.getDefaultPermissions().getPermissionsRaw().longValue()
        );
    }

    private static CustomMapSetter listener() {
        return new CustomMapSetter(mock(CustomMapRotationService.class), ALLOWED_GUILD_ID);
    }
}

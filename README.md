# Battlefield 1 Operations Bot

A Discord bot for managing a custom Battlefield 1 server map rotation through the
[GameTools Network](https://gametools.network/) API. It also keeps a small in-memory map history
and sends rotation activity to a configured Discord log channel.

> [!IMPORTANT]
> The GameTools provider is the only working provider at the moment. The EA authentication and
> session lifecycle are scaffolded, but `EaMapManager` does not yet implement map lookup, player
> count lookup, or map changes.

This is an independent community project and is not affiliated with Electronic Arts or
GameTools Network.

## Features

- Configure a custom map loop from Discord.
- Wait for the map active at command time to finish before taking control of the rotation.
- Allow the next map in a Battlefield 1 Operation to continue instead of replacing it.
- Stop custom rotation automatically when the server drops below 10 players.
- Report rotation decisions to a Discord log channel.
- Keep recent map history in memory without writing an ever-growing history file.
- Select GameTools or EA as mutually exclusive providers through JSON configuration.
- Test rotation, Discord command, configuration, history, authentication, and HTTP behavior
  without contacting live services.

## How rotation works

1. An administrator submits at least three maps with `/custom_map`.
2. The bot records the current map and waits for it to finish.
3. Rotation state is checked every four seconds.
4. When a new map appears, the bot allows it to continue if it is the second map of the current
   Operation.
5. Otherwise, the bot waits 12 seconds and submits the configured map index to GameTools.
6. A 60-second guard prevents another immediate rotation decision.

The Operation map relationships are defined in
[`MapManager`](src/main/java/tools/MapManager.java), while the rotation state machine and map
indexes are defined in
[`CustomMapRotationService`](src/main/java/rotation/CustomMapRotationService.java).

![Example Operations rotation](https://github.com/user-attachments/assets/6945e76c-8b2f-4ba8-98da-a80ce9499ecb)

_Operations chart originally created by Publii_u on Discord._

## Discord commands

| Command | Purpose | Default permission |
| --- | --- | --- |
| `/custom_map` | Sets a loop of 3–15 maps. | Kick Members |
| `/toggle_off` | Stops the custom map loop. | Kick Members |
| `/custom_map_info` | Shows whether the loop is running, its maps, and the next map. | Everyone |
| `/map_history` | Shows the recently observed maps. | Everyone |

Commands are registered for guild use. `discord.serverId` defines the primary Discord server in
which commands are handled; the current command listeners also contain a legacy development-guild
exception in code.

## Requirements

- JDK 23
- Maven 3.9 or newer
- A Discord bot token
- A Discord server and text channel for logs
- For the working provider: a GameTools manager token, group ID, server ID, and server lookup URL

The Discord bot must be able to register slash commands, view the configured log channel, and
send messages and embeds in that channel.

## Configuration

Configuration is read from `config.json` by default. This file is ignored by Git and should never
be committed because it contains credentials.

Create it from the GameTools example:

```bash
cp config.example.json config.json
```

Then replace every placeholder:

```json
{
  "provider": "GAME_TOOLS",
  "discord": {
    "token": "your-discord-bot-token",
    "serverId": "your-discord-server-id",
    "logChannelId": "your-log-channel-id"
  },
  "gameTools": {
    "token": "your-gametools-token",
    "serverUrl": "https://example.invalid/servers",
    "groupId": "your-gametools-group-id",
    "serverId": "your-gametools-server-id"
  }
}
```

| Property | Description |
| --- | --- |
| `provider` | Must be `GAME_TOOLS` for the currently working implementation. |
| `discord.token` | Discord bot token. |
| `discord.serverId` | Discord guild in which commands should be handled. |
| `discord.logChannelId` | Text channel that receives rotation logs. |
| `gameTools.token` | Token used by the GameTools manager API. |
| `gameTools.serverUrl` | Server lookup endpoint returning `servers[0].currentMap` and `playerAmount`. |
| `gameTools.groupId` | GameTools group identifier used for map changes. |
| `gameTools.serverId` | GameTools server identifier used for map changes. |

Only the selected provider may appear in a configuration. A `GAME_TOOLS` configuration must not
contain an `ea` object, and an `EA` configuration must not contain a `gameTools` object.

### EA configuration status

[`config.ea.example.json`](config.ea.example.json) documents the intended persistent EA
credentials:

- `remid` is retained so an expired SID can eventually be refreshed.
- `sid` is optional on first startup and is intended to be persisted after refresh.
- The shorter-lived session ID is held only in memory.

Selecting `EA` currently starts with an unimplemented `EaMapManager`, so it is not usable for live
rotation yet.

## Build, test, and run

Run the test suite:

```bash
mvn test
```

Build the runnable shaded JAR:

```bash
mvn clean package
```

Run it using `config.json` in the current directory:

```bash
java -jar target/NewA-1.0-SNAPSHOT.jar
```

To use a configuration at another location, pass its path as the only argument:

```bash
java -jar target/NewA-1.0-SNAPSHOT.jar /absolute/path/to/config.json
```

## Project structure

```text
src/main/java/
├── commands/   Discord/JDA command and logging adapters
├── config/     JSON configuration model and persistent configuration store
├── ea/         EA credential/session lifecycle and unfinished EA map provider
├── history/    Bounded in-memory history and its polling task
├── rotation/   Custom rotation state machine and scheduler
└── tools/      MapManager provider contract and GameTools HTTP implementation
```

The main test seams are `MapManager`, `RotationScheduler`, and the package-private
`GameToolsTransport`. This keeps network calls and background threads out of unit tests without
adding pass-through gateway layers.

## Known limitations

- Map changes are detected by comparing map names. A server transition from a map back to the same
  map cannot be detected reliably.
- GameTools can return HTTP 200 even when a map change is not applied. The current rotation logic
  treats a successful 2xx response as acceptance and advances to the next configured map without
  confirming the live result.
- Map numbers used by the change-level endpoint may depend on the server rotation. Verify the
  `MAP_INDEXES` values in `CustomMapRotationService` against your server before running the bot.
- Map history is intentionally in memory. It is bounded to fit inside a Discord message and is
  cleared whenever the bot restarts.
- The EA map provider is not implemented.

## Security

- Never commit `config.json`, Discord tokens, GameTools tokens, REMID, SID, or EA session IDs.
- The old dotenv-based setup is no longer used; runtime configuration comes from JSON.
- If a credential has ever been committed or shared, remove it from use and rotate it immediately.

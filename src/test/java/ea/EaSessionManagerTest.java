package ea;

import config.Configuration;
import config.ConfigurationStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EaSessionManagerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void keepsSessionIdOnlyInMemoryAndRecreatesItFromSid() throws IOException {
        ConfigurationStore store = createStore("remid", "sid");
        FakeAuthenticator authenticator = new FakeAuthenticator();
        authenticator.sessionIds.add("session-one");
        authenticator.sessionIds.add("session-two");
        EaSessionManager sessions = new EaSessionManager(
                store.load().ea(),
                authenticator,
                store
        );

        assertEquals("session-one", sessions.getSessionId());
        assertEquals("session-one", sessions.getSessionId());
        assertEquals(1, authenticator.createSessionCalls);

        sessions.invalidateSession();

        assertEquals("session-two", sessions.getSessionId());
        assertEquals(2, authenticator.createSessionCalls);
    }

    @Test
    void refreshesAndPersistsCookiesWhenSidExpires() throws IOException {
        ConfigurationStore store = createStore("old-remid", "old-sid");
        FakeAuthenticator authenticator = new FakeAuthenticator();
        authenticator.expireNextSid = true;
        authenticator.refreshedCredentials = new Configuration.Ea("new-remid", "new-sid");
        authenticator.sessionIds.add("new-session");
        EaSessionManager sessions = new EaSessionManager(
                store.load().ea(),
                authenticator,
                store
        );

        assertEquals("new-session", sessions.getSessionId());
        assertEquals("old-remid", authenticator.refreshedFromRemid);
        assertEquals("new-remid", store.load().ea().remid());
        assertEquals("new-sid", store.load().ea().sid());
    }

    @Test
    void generatesAndPersistsSidWhenInitialConfigurationHasNone() throws IOException {
        ConfigurationStore store = createStore("remid", null);
        FakeAuthenticator authenticator = new FakeAuthenticator();
        authenticator.refreshedCredentials = new Configuration.Ea("remid", "generated-sid");
        authenticator.sessionIds.add("generated-session");
        EaSessionManager sessions = new EaSessionManager(
                store.load().ea(),
                authenticator,
                store
        );

        assertEquals("generated-session", sessions.getSessionId());
        assertEquals("generated-sid", store.load().ea().sid());
    }

    private ConfigurationStore createStore(String remid, String sid) throws IOException {
        Path path = temporaryDirectory.resolve("config.json");
        String sidJson = sid == null ? "null" : "\"" + sid + "\"";
        Files.writeString(path, """
                {
                  "provider": "EA",
                  "discord": {
                    "token": "discord-token",
                    "serverId": "discord-server",
                    "logChannelId": "log-channel"
                  },
                  "ea": {
                    "remid": "%s",
                    "sid": %s
                  }
                }
                """.formatted(remid, sidJson));
        return new ConfigurationStore(path);
    }

    private static final class FakeAuthenticator implements EaAuthenticator {
        private final Deque<String> sessionIds = new ArrayDeque<>();
        private Configuration.Ea refreshedCredentials;
        private boolean expireNextSid;
        private int createSessionCalls;
        private String refreshedFromRemid;

        @Override
        public String createSessionId(String sid) {
            createSessionCalls++;
            if (expireNextSid) {
                expireNextSid = false;
                throw new EaSidExpiredException("SID expired");
            }
            return sessionIds.removeFirst();
        }

        @Override
        public Configuration.Ea refreshSid(String remid) {
            refreshedFromRemid = remid;
            return refreshedCredentials;
        }
    }
}

package ea;

import config.Configuration;
import config.ConfigurationStore;

import java.io.IOException;
import java.util.Objects;

/**
 * Keeps the short-lived EA gateway session in memory and refreshes persisted cookies as needed.
 */
public final class EaSessionManager {

    private final EaAuthenticator authenticator;
    private final ConfigurationStore configurationStore;
    private Configuration.Ea credentials;
    private String sessionId;

    public EaSessionManager(
            Configuration.Ea credentials,
            EaAuthenticator authenticator,
            ConfigurationStore configurationStore
    ) {
        this.credentials = Objects.requireNonNull(credentials);
        this.authenticator = Objects.requireNonNull(authenticator);
        this.configurationStore = Objects.requireNonNull(configurationStore);
    }

    public synchronized String getSessionId() {
        if (sessionId == null) {
            sessionId = createSessionId();
        }
        return sessionId;
    }

    /**
     * Marks the in-memory gateway session as expired. The next request recreates it from SID.
     */
    public synchronized void invalidateSession() {
        sessionId = null;
    }

    private String createSessionId() {
        if (!credentials.hasSid()) {
            refreshSid();
        }

        try {
            return requireSessionId(authenticator.createSessionId(credentials.sid()));
        } catch (EaSidExpiredException expiredSid) {
            refreshSid();
            return requireSessionId(authenticator.createSessionId(credentials.sid()));
        }
    }

    private void refreshSid() {
        Configuration.Ea refreshed = Objects.requireNonNull(
                authenticator.refreshSid(credentials.remid()),
                "EA authenticator returned no refreshed credentials"
        );
        if (!refreshed.hasSid()) {
            throw new EaAuthenticationException("EA authenticator returned no SID");
        }

        try {
            credentials = configurationStore.updateEaCredentials(refreshed).ea();
        } catch (IOException e) {
            throw new EaAuthenticationException("Could not persist refreshed EA cookies", e);
        }
    }

    private static String requireSessionId(String value) {
        if (value == null || value.isBlank()) {
            throw new EaAuthenticationException("EA authenticator returned no session ID");
        }
        return value;
    }
}

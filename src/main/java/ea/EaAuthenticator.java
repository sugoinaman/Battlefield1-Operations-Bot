package ea;

import config.Configuration;

/**
 * Boundary around the EA calls that create gateway sessions and rotate cookies.
 */
public interface EaAuthenticator {

    String createSessionId(String sid) throws EaSidExpiredException;

    Configuration.Ea refreshSid(String remid);
}

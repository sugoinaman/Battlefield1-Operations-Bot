package ea;

public final class EaSidExpiredException extends EaAuthenticationException {

    public EaSidExpiredException(String message) {
        super(message);
    }

    public EaSidExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}

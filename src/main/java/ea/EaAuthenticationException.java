package ea;

public class EaAuthenticationException extends RuntimeException {

    public EaAuthenticationException(String message) {
        super(message);
    }

    public EaAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

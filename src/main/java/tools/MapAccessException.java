package tools;

/**
 * Signals that a map-provider read or change operation failed.
 */
public final class MapAccessException extends RuntimeException {

    public MapAccessException(String message) {
        super(message);
    }

    public MapAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

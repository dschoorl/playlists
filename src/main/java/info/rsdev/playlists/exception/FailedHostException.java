package info.rsdev.playlists.exception;

/**
 * Exception indicates that a downstream host was approached for data, but it
 * was unavailable to provide service
 */
public class FailedHostException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FailedHostException(String message) {
        super(message);
    }

    public FailedHostException(String message, Exception cause) {
        super(message, cause);
    }

}

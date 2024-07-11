package info.rsdev.playlists.spotify;

import org.slf4j.Logger;

import se.michaelthelin.spotify.exceptions.detailed.TooManyRequestsException;

public class TooManyRequestsExceptionHandler {

    public static void handle(Logger log, String requestName, TooManyRequestsException e) {
        var sleepSecs = e.getRetryAfter() + 1;
        log.warn("{}: API Rate limit exceeded. Going to sleep for {} seconds", requestName, sleepSecs);
        sleep(sleepSecs);
        log.warn("Waking up and going on");
    }

    static private void sleep(int sleepSecs) {
        try {
            Thread.sleep(sleepSecs * 1000L);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}

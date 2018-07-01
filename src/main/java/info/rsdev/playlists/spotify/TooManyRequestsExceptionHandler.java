package info.rsdev.playlists.spotify;

import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import org.slf4j.Logger;

public abstract class TooManyRequestsExceptionHandler {
	
	private TooManyRequestsExceptionHandler() {};
	
    public static void handle(Logger log, String requestName, TooManyRequestsException e) {
    	int sleepSecs = e.getRetryAfter() + 1;
    	log.warn(String.format("%s: API Rate limit exceeded. Going to sleep for %d seconds", requestName, sleepSecs));
		sleep(e.getRetryAfter() + 1);
		log.warn("Waking up and going on");
    }

    public static void handle(TooManyRequestsException e) {
		sleep(e.getRetryAfter() + 1);
    }

    private static void sleep(int sleepSecs) {
		try {
			Thread.sleep(sleepSecs * 1000L);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
    }

}

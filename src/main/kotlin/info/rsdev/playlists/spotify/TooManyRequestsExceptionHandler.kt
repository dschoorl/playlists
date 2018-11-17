package info.rsdev.playlists.spotify

import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException
import org.slf4j.Logger

object TooManyRequestsExceptionHandler {

    fun handle(log: Logger, requestName: String, e: TooManyRequestsException) {
        val sleepSecs = e.retryAfter + 1
        log.warn("$requestName: API Rate limit exceeded. Going to sleep for $sleepSecs seconds")
        sleep(sleepSecs)
        log.warn("Waking up and going on")
    }

    private fun sleep(sleepSecs: Int) {
        try {
            Thread.sleep(sleepSecs * 1000L)
        } catch (ie: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

}

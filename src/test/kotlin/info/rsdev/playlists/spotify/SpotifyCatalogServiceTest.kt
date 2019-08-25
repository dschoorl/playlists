package info.rsdev.playlists.spotify

import info.rsdev.playlists.domain.Song
import info.rsdev.playlists.ioc.SpringCommonConfig
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertNotNull

@Disabled("This test is meant for manual execution, because it needs a valid Spotify Token")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [SpringCommonConfig::class])
class SpotifyCatalogServiceTest(@Autowired val subjectUnderTest: SpotifyCatalogService) {

    private val songsToTest = HashSet<Song>()
    init {
        with(songsToTest) {
            add(Song("Bruno Mars & Cardi B", "Finesse"))
            add(Song("atb", "don't stop!"))
            add(Song("Jay-Z featuring Amil (Of Major Coinz) and Ja Rule", "Can I Get A ..."))
        }
    }

    @Test
    fun findSong() {
        LOGGER.info("Start test")
        val song = Song("Bruno Mars & Cardi B", "Finesse")
        val searchHit = subjectUnderTest.findSong(song)
        assertNotNull(searchHit)
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(SpotifyCatalogServiceTest::class.java)
    }
}

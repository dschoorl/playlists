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

    @Test
    fun findSong() {
        LOGGER.info("Start test")
        val song = Song("Bruno Mars & Cardi B", "Finesse")
//        val song = Song("atb", "don't stop!")
        val searchHit = subjectUnderTest.findSong(song)
        assertNotNull(searchHit)
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(SpotifyCatalogServiceTest::class.java)
    }
}

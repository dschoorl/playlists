package info.rsdev.playlists.services

import info.rsdev.playlists.domain.Song
import info.rsdev.playlists.ioc.SpringCommonConfig
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertNotNull

@Disabled("This test is meant for manual execution, because it needs a valid Spotify Token")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes= [ SpringCommonConfig::class ])
class SpotifyCatalogServiceTest(@Autowired val subjectUnderTest: SpotifyCatalogService) {

    @Test
    fun findSong() {
        val song = Song("Bruno Mars & Cardi B", "Finesse")
        val searchHit = subjectUnderTest.searchSpotifyForSong(song, "b bruno cardi mars track:finesse")
        assertNotNull(searchHit)
    }

}

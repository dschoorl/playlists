package info.rsdev.playlists.services

import info.rsdev.playlists.domain.Song
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import javax.inject.Inject
import kotlin.test.assertNotNull

@Ignore
@RunWith(SpringRunner::class)
@SpringBootTest
class SpotifyCatalogServiceTest(@Inject val subjectUnderTest: SpotifyCatalogService) {

    @Test
    fun findSong() {
        val song = Song("Bruno Mars & Cardi B", "Finesse")
        val searchHit = subjectUnderTest.findSong(song)
        assertNotNull(searchHit)
    }
}

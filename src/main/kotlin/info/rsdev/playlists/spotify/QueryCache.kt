package info.rsdev.playlists.spotify

import info.rsdev.playlists.domain.Song
import info.rsdev.playlists.domain.SongFromCatalog

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache search results for a Song
 */
class QueryCache {

    private val cache: File = File(File(File(System.getProperty("user.home")), ".playlists"), "playlist.cache")

    private val cacheItems: ConcurrentHashMap<Song, SongFromCatalog>

    init {
        cacheItems = deserializeOrCreateCache()
    }

    fun cache(song: Song, result: SongFromCatalog) {
        this.cacheItems[song] = result
    }

    fun getFromCache(song: Song) : SongFromCatalog? = this.cacheItems[song]

    private fun deserializeOrCreateCache(): ConcurrentHashMap<Song, SongFromCatalog> {
        if (!cache.isFile) {
            return ConcurrentHashMap()
        }

        ObjectInputStream(BufferedInputStream(FileInputStream(cache)))
                .use { reader -> return reader.readObject() as ConcurrentHashMap<Song, SongFromCatalog> }
    }

    fun writeCache() {
        ObjectOutputStream(BufferedOutputStream(FileOutputStream(cache))).use { writer -> writer.writeObject(cacheItems) }
    }

    fun size() = cacheItems.size

}

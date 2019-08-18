package info.rsdev.playlists.spotify

import info.rsdev.playlists.domain.SongFromCatalog

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

class QueryCache {

    private val cache: File = File(File(File(System.getProperty("user.home")), ".playlists"), "playlist.cache")

    private val cacheItems: ConcurrentHashMap<String, SongFromCatalog>

    init {
        cacheItems = deserializeOrCreateCache()
    }

    fun cache(searchCriteria: String, result: SongFromCatalog) {
        this.cacheItems[searchCriteria] = result
    }

    fun getFromCache(searchCriteria: String) : SongFromCatalog? = this.cacheItems[searchCriteria]

    private fun deserializeOrCreateCache(): ConcurrentHashMap<String, SongFromCatalog> {
        if (!cache.isFile) {
            return ConcurrentHashMap()
        }

        ObjectInputStream(BufferedInputStream(FileInputStream(cache)))
                .use { reader -> return reader.readObject() as ConcurrentHashMap<String, SongFromCatalog> }
    }

    fun writeCache() {
        ObjectOutputStream(BufferedOutputStream(FileOutputStream(cache))).use { writer -> writer.writeObject(cacheItems) }
    }

    fun size() = cacheItems.size

}

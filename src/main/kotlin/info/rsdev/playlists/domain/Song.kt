package info.rsdev.playlists.domain

import java.io.Serializable

data class Song(val artist: String, val title: String) : Comparable<Song>, Serializable {

    override fun compareTo(other: Song): Int {
        return if (artist == other.artist) {
            title.compareTo(other.title)
        } else artist.compareTo(other.artist)
    }
}

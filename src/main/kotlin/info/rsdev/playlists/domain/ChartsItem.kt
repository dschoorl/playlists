package info.rsdev.playlists.domain

class ChartsItem(val chartName: String,
                 val year: Short, val weekNumber: Byte,
                 val position: Byte, val isNewRelease: Boolean,
                 val song: Song) {

    override fun toString(): String {
        return "ChartsItem{" +
                "chartName='" + chartName + '\''.toString() +
                ", year=" + year +
                ", weekNumber=" + weekNumber +
                ", position=" + position +
                ", new=" + isNewRelease +
                ", artist='" + song.artist + '\''.toString() +
                ", title='" + song.title + '\''.toString() +
                '}'.toString()
    }
}

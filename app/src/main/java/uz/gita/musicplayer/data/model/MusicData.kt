package uz.gita.musicplayer.data.model

data class MusicData(
    val id: Int?,
    val title: String?,
    val artist: String?,
    val data: String?,
    val duration: Long
)/* {
    fun musicDataToMusicEntity(): MusicEntity =
        MusicEntity(id ?: 0, title ?: "", artist ?: "", data ?: "", duration)
}*/
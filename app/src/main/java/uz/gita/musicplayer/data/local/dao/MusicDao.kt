package uz.gita.musicplayer.data.local.dao

//@Dao
//interface MusicDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun saveAllMusic(musicList: List<MusicEntity>)
//
//    @Query("select * from music")
//     fun getAllMusic(): Cursor
//
//    @Query("select id,title, artist, data, duration from music where music.title like :query")
//    fun getAllMusicByQuery(query: String): Cursor
//
//}
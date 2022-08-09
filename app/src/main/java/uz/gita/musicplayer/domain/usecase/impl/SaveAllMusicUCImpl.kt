package uz.gita.musicplayer.domain.usecase.impl

/*
class SaveAllMusicUCImpl
@Inject constructor(
    private val repository: AppRepository,
    private val mySharedPreferences: MySharedPreferences
) : SaveAllMusicUC {

    override fun saveAllMusic(cursor: Cursor) = flow<Int> {
        val musicList = ArrayList<MusicEntity>()
        for (i in 0 until cursor.count) {
            musicList.add(cursor.getMusicDataByPosition(i).musicDataToMusicEntity())
        }
        repository.saveAllMusic(musicList)
        emit(mySharedPreferences.lastScreenPosition)
    }.flowOn(Dispatchers.Default)

}*/

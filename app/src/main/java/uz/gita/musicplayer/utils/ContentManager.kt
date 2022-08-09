package uz.gita.musicplayer.utils

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import uz.gita.musicplayer.data.model.MusicData

private val projection = arrayOf(
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.TITLE,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.DATA,
    MediaStore.Audio.Media.DURATION,
)

fun Context.getMusicListCursor(): Flow<Cursor> = flow {
    val cursor: Cursor = contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        MediaStore.Audio.Media.IS_MUSIC + "!=0",  /*or MediaStore.Audio.Media.IS_MUSIC + " == 1"*/
        null,
        null
    ) ?: return@flow
    delay(1000)
    emit(cursor)
}.flowOn(Dispatchers.IO)

/*fun Context.getMusicListCursorByQuery(query: String): Flow<Cursor> = flow {
    val cursor: Cursor = contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        MediaStore.Audio.Media.IS_MUSIC + "!=0 and ${MediaStore.Audio.Media.TITLE} LIKE $query",  *//*or MediaStore.Audio.Media.IS_MUSIC + " == 1"*//*
        null,
        null
    ) ?: return@flow
    delay(1000)
    emit(cursor)
}.flowOn(Dispatchers.IO)*/

fun Cursor.getMusicDataByPosition(position: Int): MusicData {
    this.moveToPosition(position)
    return MusicData(
        this.getInt(0),
        this.getString(1),
        this.getString(2),
        this.getString(3),
        this.getLong(4)
    )
}

/*fun Cursor.getMusicListDataByQuery(query: String): Flow<List<MusicData>> = flow {
    val result = ArrayList<MusicData>()
    var position = 0
    this@getMusicListDataByQuery.moveToFirst()
    do {
        if (this@getMusicListDataByQuery.getString(1).contains(query, true) ||
            this@getMusicListDataByQuery.getString(2).contains(query, true)
        ) result.add(this@getMusicListDataByQuery.getMusicDataByPosition(position))
        position++
    } while (this@getMusicListDataByQuery.moveToNext())
    delay(500)
    emit(result)
}.flowOn(Dispatchers.IO)*/

/*
fun Cursor.getMusicDataByQuery(query: String): List<MusicData> {
    val result = ArrayList<MusicData>()
    var position = 0
    this.moveToFirst()
    do {
        if (this.getString(1) == query || this.getString(2) == query) {
            result.add(getMusicDataByPosition(position))
        }
        position++
    } while (this.moveToNext())
    return result
}*/

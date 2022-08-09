package uz.gita.musicplayer.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.widget.SeekBar
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.*

fun SeekBar.setChangeProgress(block: (progress: Int, fromUser: Boolean) -> Unit) {
    this.setOnSeekBarChangeListener(object :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            /*Timber.d("onProgressChanged: ${seekBar?.progress}")
            Timber.d("onProgressChanged: $progress")
            Timber.d("onProgressChanged: $fromUser")*/
            seekBar?.let { block.invoke(it.progress, fromUser) }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            /*Timber.d("onStartTrackingTouch: ${seekBar?.progress}")*/
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
//            Timber.d("onStopTrackingTouch: ${seekBar?.progress}")
            seekBar?.let { block.invoke(it.progress, false) }
        }
    })
}

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("mm:ss", Locale.getDefault())
    return format.format(date)
}

fun convertLongToProgress(time: Long): Int = (time * 100 / MusicManager.duration).toInt()

fun getAlbumImage(path: String): Bitmap? {
    val mmr: MediaMetadataRetriever = MediaMetadataRetriever()
    mmr.setDataSource(path)
    val data: ByteArray? = mmr.embeddedPicture
    return when {
        data != null -> BitmapFactory.decodeByteArray(data, 0, data.size)
        else -> null
    }
}

fun getAlbumImageAsync(path: String): Flow<Bitmap?> = flow<Bitmap?> {
    val mmr: MediaMetadataRetriever = MediaMetadataRetriever()
    mmr.setDataSource(path)
    val data: ByteArray? = mmr.embeddedPicture
    when {
        data != null -> emit(BitmapFactory.decodeByteArray(data, 0, data.size))
        else -> emit(null)
    }
}.flowOn(Dispatchers.IO)

fun createPaletteSync(bitmap: Bitmap): Palette = Palette.from(bitmap).generate()

fun createPaletteAsync(bitmap: Bitmap) = callbackFlow<Result<Palette>> {
    Palette.from(bitmap).generate { palette ->
        if (palette != null) trySendBlocking(Result.success(palette))
    }
    awaitClose {}
}.flowOn(Dispatchers.Default)

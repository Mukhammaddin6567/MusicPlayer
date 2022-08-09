package uz.gita.musicplayer.utils

import android.database.Cursor
import androidx.lifecycle.MutableLiveData
import uz.gita.musicplayer.data.model.MusicData

object MusicManager {

    var cursor: Cursor? = null
    var selectedMusicPosition: Int = -1

    var currentTime: Long = 0L
    var duration: Long = 0L
    var progress: Int = 0
    var phoneState = false

    val selectedMusicPositionLD = MutableLiveData<Int>()
    val currentTimeLiveData = MutableLiveData<Long>()
    val playMusicLiveData = MutableLiveData<MusicData>()
    val isPlayingLiveData = MutableLiveData<Boolean>()

    /*val audioSessionIdLiveData = MutableLiveData<Int>()*/

}
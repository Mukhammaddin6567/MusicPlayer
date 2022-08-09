package uz.gita.musicplayer.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import uz.gita.musicplayer.utils.SharedPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MySharedPreferences
@Inject constructor(@ApplicationContext context: Context) : SharedPreference(context) {

    var isLaunch: Boolean by BooleanPreference(false)
    var lastScreenPosition: Int by IntPreference(2)

    //    var selectedMusicPosition: Int by IntPreference(-1)
    var isPlayingMusic: Boolean by BooleanPreference(false)

    var isShuffle: Boolean by BooleanPreference(false)
    var isReplay: Boolean by BooleanPreference(false)

}
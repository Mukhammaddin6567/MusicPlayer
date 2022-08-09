package uz.gita.musicplayer

import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import uz.gita.musicplayer.data.local.MySharedPreferences
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity
@Inject constructor(
) : AppCompatActivity(R.layout.activity_main) {

    @Inject
    lateinit var mySharedPreferences: MySharedPreferences

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mySharedPreferences.isLaunch = false
    }

}
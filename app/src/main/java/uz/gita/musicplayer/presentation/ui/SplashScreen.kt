package uz.gita.musicplayer.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import uz.gita.musicplayer.R
import uz.gita.musicplayer.data.local.MySharedPreferences
import uz.gita.musicplayer.utils.MusicManager
import uz.gita.musicplayer.utils.checkPermissions
import uz.gita.musicplayer.utils.getMusicListCursor
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreen : Fragment(R.layout.screen_splash) {

    //    private val viewModel: SplashVM by viewModels<SplashVMImpl>()
    private val navController: NavController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }

    @Inject
    lateinit var mySharedPreferences: MySharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) /*= with(viewModel)*/ {
        super.onCreate(savedInstanceState)
        /*navigateNextScreenLD.observe(this@SplashScreen) { lastScreenPosition ->
            if (lastScreenPosition == 2)
                navController.navigate(R.id.action_splashScreen_to_mainScreen)
            else navController.navigate(R.id.action_splashScreen_to_musicScreen)
        }*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().checkPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
            ),
            {
                requireContext().getMusicListCursor().onEach { cursor ->
                    MusicManager.cursor = cursor
                    if (mySharedPreferences.lastScreenPosition == 2)
                        navController.navigate(R.id.action_splashScreen_to_mainScreen)
                    else navController.navigate(R.id.action_splashScreen_to_musicScreen)
//                    viewModel.saveAllMusic(cursor)
                }.launchIn(lifecycleScope)
            },
            {
                requireActivity().finish()
                Timber.d("denied")
            }
        )
    }
}
package uz.gita.musicplayer.presentation.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
//import com.chibde.visualizer.CircleBarVisualizer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import uz.gita.musicplayer.R
import uz.gita.musicplayer.data.ActionEnum
import uz.gita.musicplayer.data.local.MySharedPreferences
import uz.gita.musicplayer.data.model.MusicData
import uz.gita.musicplayer.databinding.ScreenMusicBinding
import uz.gita.musicplayer.presentation.service.MyService
import uz.gita.musicplayer.utils.*
import javax.inject.Inject

@AndroidEntryPoint
class MusicScreen : Fragment(R.layout.screen_music) {

    private val navController: NavController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }

    @Inject
    lateinit var mySharedPreferences: MySharedPreferences

    private val viewBinding by viewBinding(ScreenMusicBinding::bind)

    /*private var circleBarVisualizer: CircleBarVisualizer? = null*/
    private val rotate = RotateAnimation(
        0F, 360F,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mySharedPreferences.isLaunch = true
        rotate.duration = MusicManager.duration / 50
        rotate.interpolator = LinearInterpolator()
        rotate.repeatCount = Animation.INFINITE
        rotate.fillAfter = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        registerOnBackPressed()
        subscribeViewBinding(viewBinding)
        subscribeViewModel()
    }

    private fun subscribeViewBinding(viewBinding: ScreenMusicBinding) = with(viewBinding) {
        /*circleBarVisualizer = blast
        circleBarVisualizer!!.setColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )*/
        textMusicName.isSelected = true
        buttonShuffle.apply {
            setImageResource(
                when (mySharedPreferences.isShuffle) {
                    true -> R.drawable.ic_shuffle_on
                    else -> R.drawable.ic_shuffle_off
                }
            )
            setOnClickListener {
                val status = mySharedPreferences.isShuffle
                mySharedPreferences.isShuffle = !status
                viewBinding.buttonShuffle.setImageResource(
                    when (status) {
                        true -> R.drawable.ic_shuffle_off
                        else -> R.drawable.ic_shuffle_on
                    }
                )
            }
        }
        buttonReplay.apply {
            setImageResource(
                when (mySharedPreferences.isReplay) {
                    true -> R.drawable.ic_replay_on
                    else -> R.drawable.ic_replay_off
                }
            )
            setOnClickListener {
                val status = mySharedPreferences.isReplay
                mySharedPreferences.isReplay = !status
                viewBinding.buttonReplay.setImageResource(
                    when (status) {
                        true -> R.drawable.ic_replay_off
                        else -> R.drawable.ic_replay_on
                    }
                )
            }
        }
        buttonBack.setOnClickListener {
            navController.navigate(R.id.action_musicScreen_to_mainScreen)
        }
        buttonPrevious.setOnClickListener { startMyService(ActionEnum.PREVIOUS) }
        buttonNext.setOnClickListener { startMyService(ActionEnum.NEXT) }
        buttonManage.setOnClickListener { startMyService(ActionEnum.MANAGE) }
        progressMusic.setChangeProgress { progress, fromUser ->
            if (fromUser) {
                viewBinding.progressMusic.progress = progress
                Timber.d("setChangeProgress: $progress, $fromUser")
                MusicManager.progress = progress
                startMyService(ActionEnum.SEEK)
            }
        }
    }

    private fun subscribeViewModel() {
        MusicManager.playMusicLiveData.observe(viewLifecycleOwner, playMusicLiveDataObserver)
        MusicManager.isPlayingLiveData.observe(viewLifecycleOwner, isPlayingLiveDataObserver)
        MusicManager.currentTimeLiveData.observe(
            viewLifecycleOwner,
            currentTimeLiveDataObserver
        )
        /*MusicManager.audioSessionIdLiveData.observe(
            viewLifecycleOwner,
            audioSessionIdLiveDataObserver
        )*/
    }

    private val playMusicLiveDataObserver = Observer<MusicData> { data ->
        var background: Int? = 0
        val bitmap: Bitmap? = data.data?.let { getAlbumImage(it) }

        if (bitmap != null) {
            createPaletteAsync(bitmap).onEach { result ->
                result.onSuccess {
                    background = it.darkMutedSwatch?.rgb
                    if (background != null && background != 0) {
                        viewBinding.containerMusic.setBackgroundColor(background!!)
                    }
                }
            }.launchIn(lifecycleScope)
        } else {
            background = R.color.color_screen_background
            viewBinding.containerMusic.background =
                AppCompatResources.getDrawable(requireContext(), background!!)
        }

        Timber.d("background: $background")
        Timber.d("bitmap: $bitmap")

        viewBinding.apply {
            if (bitmap != null) Glide
                .with(imageMusic)
                .load(bitmap)
                .into(imageMusic)
            else imageMusic.setImageResource(R.drawable.ic_music_item)
            textMusicTime.text = convertLongToTime(data.duration)
            textMusicName.text = data.title
            textMusicArtist.text = data.artist
        }
    }

    private val isPlayingLiveDataObserver = Observer<Boolean> { status ->
        viewBinding.apply {
            when (status) {
                true -> {
                    buttonManage.setImageResource(R.drawable.ic_pause)
                    imageMusic.startAnimation(rotate)
                }
                else -> {
                    buttonManage.setImageResource(R.drawable.ic_play)
                    imageMusic.clearAnimation()
                }
            }
        }
    }

    private val currentTimeLiveDataObserver = Observer<Long> {
        viewBinding.textMusicCurrentTime.text = convertLongToTime(it)
        viewBinding.progressMusic.progress = convertLongToProgress(it)
    }

    /*private val audioSessionIdLiveDataObserver = Observer<Int> { audioSessionId ->
        Timber.d("audioSessionIdLiveDataObserver: $audioSessionId")
        if (audioSessionId != -1) {
            circleBarVisualizer = viewBinding.blast
            circleBarVisualizer!!.setColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_green
                )
            )
            viewBinding.blast.setPlayer(audioSessionId)
        } else if (audioSessionId == -1) {
            circleBarVisualizer!!.setColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )
            )
            circleBarVisualizer?.release()
            circleBarVisualizer = null
        }
    }*/

    private fun startMyService(command: ActionEnum) {
        val intent = Intent(requireContext(), MyService::class.java)
        intent.putExtra("COMMAND", command)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            requireActivity().startForegroundService(intent)
        else requireActivity().startService(intent)
    }

    private fun registerOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navController.navigate(R.id.action_musicScreen_to_mainScreen)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (MusicManager.isPlayingLiveData.value == true) mySharedPreferences.lastScreenPosition = 3
    }

}
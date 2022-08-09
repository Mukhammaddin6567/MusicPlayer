package uz.gita.musicplayer.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import uz.gita.musicplayer.MainActivity
import uz.gita.musicplayer.R
import uz.gita.musicplayer.data.ActionEnum
import uz.gita.musicplayer.data.local.MySharedPreferences
import uz.gita.musicplayer.data.model.MusicData
import uz.gita.musicplayer.presentation.broadcast.BroadcastReceiver
import uz.gita.musicplayer.utils.MusicManager
import uz.gita.musicplayer.utils.getAlbumImage
import uz.gita.musicplayer.utils.getMusicDataByPosition
import java.io.File
import java.util.*
import javax.inject.Inject


private const val CHANNEL = "Music Player"

@AndroidEntryPoint
class MyService : Service() {

    @Inject
    lateinit var mySharedPreferences: MySharedPreferences
    private val receiver = BroadcastReceiver()

    private var _mediaPlayer: MediaPlayer? = null
    private val mediaPlayer get() = _mediaPlayer!!
    private var musicData: MusicData? = null
    private val musicScope = CoroutineScope(Dispatchers.IO + Job())
    private val commandScope = CoroutineScope(Dispatchers.Main + Job())
    private var musicJob: Job? = null
    private var commandJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
//        mySharedPreferences = MySharedPreferences(this)
        registerReceiver(receiver, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
        _mediaPlayer = MediaPlayer()
        createChannel()
        startMyService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("flags: $flags, startId: $startId")
        val command = intent?.extras?.getSerializable("COMMAND") as ActionEnum
        commandJob?.cancel()
        commandJob = commandScope.launch {
            delay(500)
            doCommand(command)
        }
        return START_NOT_STICKY
    }

    private fun doCommand(command: ActionEnum) {
        musicData = MusicManager.cursor!!.getMusicDataByPosition(MusicManager.selectedMusicPosition)
        when (command) {
            ActionEnum.CREATE -> {
                Timber.d("doCommand: manage")
                if (_mediaPlayer != null) {
                    if (mediaPlayer.isPlaying) mediaPlayer.stop()
                    mediaPlayer.reset()
                    mediaPlayer.release()
                    _mediaPlayer = null
                }
                _mediaPlayer =
                    MediaPlayer.create(this, Uri.fromFile(File(musicData?.data ?: "")))
                mediaPlayer.setOnCompletionListener {
                    doCommand(ActionEnum.NEXT)
                }
                /*MusicManager.audioSessionIdLiveData.value = mediaPlayer.audioSessionId*/
                MusicManager.selectedMusicPositionLD.value = MusicManager.selectedMusicPosition
                MusicManager.playMusicLiveData.value = musicData
                MusicManager.currentTime = 0
                MusicManager.currentTimeLiveData.value = 0
                MusicManager.duration = musicData?.duration ?: 0
                musicJob?.cancel()
                doCommand(ActionEnum.PLAY)
            }
            ActionEnum.MANAGE -> {
                Timber.d("doCommand: manage")
                if (mediaPlayer.isPlaying) doCommand(ActionEnum.PAUSE)
                else doCommand(ActionEnum.PLAY)
            }
            ActionEnum.PLAY -> {
                mySharedPreferences.isPlayingMusic = true
                Timber.d("doCommand: play")
                MusicManager.isPlayingLiveData.value = true
                mediaPlayer.start()
                musicJob?.cancel()
                musicJob = musicScope.launch {
                    changeSeekBarProgress().collectLatest {
                        MusicManager.currentTime = it
                        MusicManager.currentTimeLiveData.postValue(it)
                    }
                }
                startMyService()
            }
            ActionEnum.PAUSE -> {
                mySharedPreferences.isPlayingMusic = false
                Timber.d("doCommand: pause")
                mediaPlayer.pause()
                musicJob?.cancel()
                MusicManager.isPlayingLiveData.value = false
                startMyService()
            }
            ActionEnum.NEXT -> {
                Timber.d("next before current position changed: ${MusicManager.selectedMusicPosition}")
                Timber.d("doCommand: next")
                MusicManager.currentTime = 0
                if (mySharedPreferences.isReplay) {
                    doCommand(ActionEnum.CREATE)
                    return
                }
                if (mySharedPreferences.isShuffle) {
                    MusicManager.selectedMusicPosition =
                        Random().nextInt(MusicManager.cursor!!.count)
                    doCommand(ActionEnum.CREATE)
                    return
                }
                if (MusicManager.selectedMusicPosition + 1 == MusicManager.cursor?.count)
                    MusicManager.selectedMusicPosition = 0
                else MusicManager.selectedMusicPosition++
                Timber.d("next after current position changed: ${MusicManager.selectedMusicPosition}")
                doCommand(ActionEnum.CREATE)
            }
            ActionEnum.PREVIOUS -> {
                Timber.d("previous before current position changed: ${MusicManager.selectedMusicPosition}")
                Timber.d("doCommand: previous")
                MusicManager.currentTime = 0
                if (mySharedPreferences.isReplay) {
                    doCommand(ActionEnum.CREATE)
                    return
                }
                if (mySharedPreferences.isShuffle) {
                    MusicManager.selectedMusicPosition =
                        Random().nextInt(MusicManager.cursor!!.count)
                    doCommand(ActionEnum.CREATE)
                    return
                }
                if (MusicManager.selectedMusicPosition == 0)
                    MusicManager.selectedMusicPosition = MusicManager.cursor!!.count - 1
                else MusicManager.selectedMusicPosition--
                Timber.d("previous after current position changed: ${MusicManager.selectedMusicPosition}")
                doCommand(ActionEnum.CREATE)
            }
            ActionEnum.SEEK -> {
                Timber.d("doCommand: seek")
                val time = MusicManager.progress * (musicData?.duration ?: 0) / 100
                mediaPlayer.seekTo(time.toInt())
                MusicManager.currentTime = time
                MusicManager.currentTimeLiveData.postValue(time)
                musicJob?.cancel()
                musicJob = musicScope.launch {
                    changeSeekBarProgress().collectLatest {
                        MusicManager.currentTime = it
                        MusicManager.currentTimeLiveData.postValue(it)
                    }
                }
            }
            ActionEnum.CANCEL -> {
                Timber.d("doCommand: cancel")
                mySharedPreferences.lastScreenPosition = 2
                stopSelf()
            }
        }
    }

    private fun changeSeekBarProgress() = flow<Long> {
        for (i in MusicManager.currentTime until MusicManager.duration step 1000) {
            emit(i)
            delay(1000)
        }
    }

    // version.codes.o == 26
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    "Music Player",
                    CHANNEL,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            channel.enableVibration(false)
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startMyService() {
        val notifyIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val notifyPendingIntent = PendingIntent.getActivity(
            this,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat
            .Builder(this, CHANNEL)
            .setContentIntent(notifyPendingIntent)
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_music)
            .setContentTitle("Music Player")
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(createNotificationLayout())
            .setCustomBigContentView(createNotificationLayoutExpanded())
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationLayoutExpanded(): RemoteViews {
        musicData = MusicManager.cursor!!.getMusicDataByPosition(MusicManager.selectedMusicPosition)
        val notificationLayoutExpanded =
            RemoteViews(this.packageName, R.layout.remote_view_expanded)
        notificationLayoutExpanded.setTextViewText(
            R.id.textMusicNameRemote,
            musicData?.title ?: ""
        )
        notificationLayoutExpanded.setTextViewText(
            R.id.textArtistNameRemote,
            musicData?.artist ?: ""
        )
        if (mediaPlayer.isPlaying) notificationLayoutExpanded.setImageViewResource(
            R.id.buttonManageRemote,
            R.drawable.ic_pause
        )
        else notificationLayoutExpanded.setImageViewResource(
            R.id.buttonManageRemote,
            R.drawable.ic_play
        )

        if (musicData?.data != null && musicData?.data != null) {
            val image = getAlbumImage(musicData!!.data!!)
            if (image != null) notificationLayoutExpanded.setImageViewBitmap(
                R.id.imageMusicRemote,
                image
            )
            else notificationLayoutExpanded.setImageViewResource(
                R.id.imageMusicRemote,
                R.drawable.ic_music
            )
        }

        notificationLayoutExpanded.setOnClickPendingIntent(
            R.id.buttonManageRemote,
            createPendingIntent(ActionEnum.MANAGE)
        )

        notificationLayoutExpanded.setOnClickPendingIntent(
            R.id.buttonNextRemote,
            createPendingIntent(ActionEnum.NEXT)
        )

        notificationLayoutExpanded.setOnClickPendingIntent(
            R.id.buttonPreviousRemote,
            createPendingIntent(ActionEnum.PREVIOUS)
        )

        notificationLayoutExpanded.setOnClickPendingIntent(
            R.id.buttonCancelRemote,
            createPendingIntent(ActionEnum.CANCEL)
        )

        return notificationLayoutExpanded
    }

    private fun createNotificationLayout(): RemoteViews {
        val musicData =
            MusicManager.cursor?.getMusicDataByPosition(MusicManager.selectedMusicPosition)!!

        val notificationLayout = RemoteViews(this.packageName, R.layout.remote_view)

        notificationLayout.setTextViewText(R.id.textMusicNameRemote, musicData.title)
        notificationLayout.setTextViewText(R.id.textArtistNameRemote, musicData.artist)

        return notificationLayout
    }

    private fun createPendingIntent(action: ActionEnum): PendingIntent {
        val intent = Intent(this, MyService::class.java)
        intent.putExtra("COMMAND", action)
        return PendingIntent.getService(
            this,
            action.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        /*telephonyManager = null*/
        receiver.clearAbortBroadcast()
        unregisterReceiver(receiver)
        musicJob?.cancel()
        commandJob?.cancel()
        if (_mediaPlayer != null) {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
            _mediaPlayer = null
        }
        MusicManager.selectedMusicPositionLD.value = -1
    }
}
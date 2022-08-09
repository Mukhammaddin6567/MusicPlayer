package uz.gita.musicplayer.presentation.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import uz.gita.musicplayer.R
import uz.gita.musicplayer.data.ActionEnum
import uz.gita.musicplayer.data.local.MySharedPreferences
import uz.gita.musicplayer.data.model.MusicData
import uz.gita.musicplayer.databinding.ScreenMainBinding
import uz.gita.musicplayer.presentation.service.MyService
import uz.gita.musicplayer.presentation.ui.adapter.MusicAdapter
import uz.gita.musicplayer.utils.MusicManager
import uz.gita.musicplayer.utils.getAlbumImage
import javax.inject.Inject

@AndroidEntryPoint
class MainScreen : Fragment(R.layout.screen_main)/*, SearchView.OnQueryTextListener*/ {

    private val viewBinding by viewBinding(ScreenMainBinding::bind)
    private val adapter: MusicAdapter by lazy { MusicAdapter() }
    /*private val viewModel: MainVM by viewModels<MainVMImpl>()*/

    @Inject
    lateinit var mySharedPreferences: MySharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mySharedPreferences.isLaunch = true
        /*setHasOptionsMenu(true)*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscribeViewBinding(viewBinding)
        subscribeViewModel()
    }

    private fun subscribeViewBinding(viewBinding: ScreenMainBinding) = with(viewBinding) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.inflateMenu(R.menu.menu_search)
        adapter.cursor = MusicManager.cursor
        musicList.adapter = adapter
        musicList.layoutManager = LinearLayoutManager(requireContext())
        adapter.setSelectedMusicPositionListener { position ->
            Timber.d("position: $position")
            MusicManager.selectedMusicPosition = position
            startMyService(ActionEnum.CREATE)
        }
        textMusicName.isSelected = true
        buttonPrevious.setOnClickListener { startMyService(ActionEnum.PREVIOUS) }
        buttonNext.setOnClickListener { startMyService(ActionEnum.NEXT) }
        buttonManage.setOnClickListener { startMyService(ActionEnum.MANAGE) }

        bottomPart.setOnClickListener {
            findNavController().navigate(R.id.action_mainScreen_to_musicScreen)
        }
    }

    private fun subscribeViewModel() {
        MusicManager.playMusicLiveData.observe(viewLifecycleOwner, playMusicLiveDataObserver)
        MusicManager.isPlayingLiveData.observe(viewLifecycleOwner, isPlayingLiveDataObserver)
        MusicManager.selectedMusicPositionLD.observe(
            viewLifecycleOwner,
            selectedMusicPositionLiveDataObserver
        )
        /*viewModel.cursorLD.observe(viewLifecycleOwner, cursorLDObserver)*/
    }

    private val playMusicLiveDataObserver = Observer<MusicData> { data ->
        val image = data.data?.let { getAlbumImage(it) }
        viewBinding.apply {
            if (image != null)
                Glide
                    .with(imageMusic)
                    .load(image)
                    .into(imageMusic)
            else imageMusic.setImageResource(R.drawable.ic_music_item)
            textMusicName.text = data.title
        }
    }

    private val isPlayingLiveDataObserver = Observer<Boolean> { status ->
        viewBinding.buttonManage.apply {
            when (status) {
                true -> setImageResource(R.drawable.ic_pause)
                else -> setImageResource(R.drawable.ic_play)
            }
        }
    }

    private val selectedMusicPositionLiveDataObserver = Observer<Int> { position ->
        Timber.d("selectedMusicPositionLiveDataObserver: $position")
        if (position >= 0) viewBinding.bottomPart.visibility = VISIBLE
        else viewBinding.bottomPart.visibility = GONE
    }

    /*private val cursorLDObserver = Observer<Cursor> {
        adapter.cursor = it
        adapter.notifyDataSetChanged()
    }*/

    private fun startMyService(command: ActionEnum) {
        val intent = Intent(requireContext(), MyService::class.java)
        intent.putExtra("COMMAND", command)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            requireActivity().startForegroundService(intent)
        else requireActivity().startService(intent)
    }

    /*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.menuSearch)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search Music"
        configureCloseButton(searchView)
        searchView.isIconified = true
        searchView.setOnQueryTextListener(this)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun configureCloseButton(searchView: SearchView) {
        val searchClose = searchView.javaClass.getDeclaredField("mCloseButton")
        searchClose.isAccessible = true
        val closeImage = searchClose.get(searchView) as ImageView
        closeImage.setImageResource(R.drawable.ic_close_white) // your image here
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        mySharedPreferences.lastScreenPosition = 2
    }

    /*override fun onQueryTextSubmit(query: String): Boolean {
        Timber.d("search text: $query")
        viewModel.onSearch(query)
        return true
    }

    override fun onQueryTextChange(query: String): Boolean {
        Timber.d("search text: $query")
        viewModel.onSearch(query)
        return true
    }*/
}
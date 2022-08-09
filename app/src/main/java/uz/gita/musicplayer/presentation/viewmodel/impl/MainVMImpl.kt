package uz.gita.musicplayer.presentation.viewmodel.impl

import android.database.Cursor
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

/*
@HiltViewModel
class MainVMImpl
@Inject constructor(
    private val musicUC: MusicUC
) : ViewModel(), MainVM {

    override val cursorLD: MutableLiveData<Cursor> by lazy { MutableLiveData<Cursor>() }
    private var job: Job? = null

    override fun onSearch(query: String) {
        Timber.d("onSearch: $query")
        if (query.isEmpty()) {
            Timber.d("onSearch: isEmpty")
            job?.cancel()
            job = musicUC
                .allMusic()
                .onEach {
                    cursorLD.value = it
                    Timber.d("onSearch isEmpty cursor: $it")
                }
                .launchIn(viewModelScope)
            return
        }
        Timber.d("onSearch: isNotEmpty")
        job?.cancel()
        job = musicUC
            .searchByQuery(query)
            .onEach {
                cursorLD.value = it
                Timber.d("onSearch isNotEmpty cursor: $it")
            }
            .launchIn(viewModelScope)
    }
}*/

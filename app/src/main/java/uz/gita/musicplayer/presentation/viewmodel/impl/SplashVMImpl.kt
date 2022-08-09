package uz.gita.musicplayer.presentation.viewmodel.impl

import android.database.Cursor
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/*
@HiltViewModel
class SplashVMImpl
@Inject constructor(
    private val saveAllMusicUC: SaveAllMusicUC,
) : ViewModel(), SplashVM {

    override val navigateNextScreenLD: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    override fun saveAllMusic(cursor: Cursor) {
        saveAllMusicUC
            .saveAllMusic(cursor)
            .onEach { navigateNextScreenLD.value = it }
            .launchIn(viewModelScope)

    }
}*/

package uz.gita.musicplayer.presentation.ui.adapter

import android.database.Cursor
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import uz.gita.musicplayer.R
import uz.gita.musicplayer.databinding.ItemMusicBinding
import uz.gita.musicplayer.utils.getAlbumImageAsync
import uz.gita.musicplayer.utils.getMusicDataByPosition

class MusicAdapter : RecyclerView.Adapter<MusicAdapter.Holder>() {

    var cursor: Cursor? = null
    private var selectedMusicPositionListener: ((position: Int) -> Unit)? = null
    private val bitmapScope = CoroutineScope(Dispatchers.IO + Job())

    init {
        Timber.d("cursor: ${cursor?.count}")
    }

    inner class Holder(private val viewBinding: ItemMusicBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {

        init {
            viewBinding.textMusicNameItem.isSelected = true
            viewBinding.musicContainer.setOnClickListener {
                selectedMusicPositionListener?.invoke(absoluteAdapterPosition)
            }
        }

        fun bind() = with(viewBinding) {
            Timber.d("data: ${cursor?.getMusicDataByPosition(absoluteAdapterPosition)}")
            cursor?.getMusicDataByPosition(absoluteAdapterPosition)?.let { data ->
                var image: Bitmap? = null
                if (data.data != null) {
                    bitmapScope.launch {
                        getAlbumImageAsync(data.data).collectLatest { bitmap -> image = bitmap }
                        withContext(Dispatchers.Main) {
                            if (image != null) {
                                Glide
                                    .with(imageMusicItem)
                                    .load(image)
                                    .into(imageMusicItem)
                            } else {
                                imageMusicItem.setImageResource(R.drawable.ic_music_item)
                            }
                        }
                    }
                }
                textMusicPositionItem.text = itemView.context.getString(
                    R.string.text_item_music_current_position,
                    absoluteAdapterPosition + 1
                )
                textMusicNameItem.isSelected = true
                textMusicNameItem.text = data.title
                textMusicArtistItem.text = data.artist
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(
            ItemMusicBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_music, parent, false)
            )
        )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = cursor?.count ?: 0

    fun setSelectedMusicPositionListener(block: (position: Int) -> Unit) {
        selectedMusicPositionListener = block
    }


}
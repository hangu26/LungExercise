package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class VideoViewModel(application: Application) : AndroidViewModel(application) {
    val player: ExoPlayer by lazy {
        ExoPlayer.Builder(application).build()
    }

    fun prepareAndPlay(uri: Uri) {
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}

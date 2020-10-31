package com.khoben.autotitle.huawei.mvp.presenter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.khoben.autotitle.huawei.App.Companion.LIMIT_DURATION_MS
import com.khoben.autotitle.huawei.common.FileState
import com.khoben.autotitle.huawei.mvp.view.MainActivityView
import moxy.InjectViewState
import moxy.MvpPresenter


@InjectViewState
class MainActivityPresenter : MvpPresenter<MainActivityView>() {

    /**
     *
     * @param context Context
     * @param uri Uri
     * @return FileState
     */
    fun verifyMedia(context: Context, uri: Uri): FileState {
        val mediaPlayer = MediaMetadataRetriever()
        try {
            mediaPlayer.setDataSource(context, uri)
        } catch (e: Exception) {
            mediaPlayer.release()
            return FileState.FAILED
        }
        try {
            val duration =
                mediaPlayer.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
            if (duration >= LIMIT_DURATION_MS) {
                mediaPlayer.release()
                return FileState.LIMIT
            }
        } catch (e: Exception) {
            mediaPlayer.release()
            return FileState.FAILED
        }
        return FileState.SUCCESS
    }
}
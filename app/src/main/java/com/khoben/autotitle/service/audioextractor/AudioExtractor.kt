package com.khoben.autotitle.service.audioextractor

import android.content.Context
import android.net.Uri
import io.reactivex.rxjava3.core.Observable

interface AudioExtractor {
    fun extractAudio(context: Context, uri: Uri, outAudioPath: String): Observable<String>
    fun extractAudio(videoFile: String, outAudioPath: String): Observable<String>
}
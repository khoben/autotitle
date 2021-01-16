package com.khoben.autotitle.service.audioextractor

import android.content.Context
import android.media.*
import android.net.Uri
import io.reactivex.rxjava3.core.Observable
import timber.log.Timber
import java.io.IOException
import java.nio.ByteBuffer


// Copyright (c) 2018 ArsalImam
// All Rights Reserved
//
class AudioExtractorImpl : AudioExtractor {

    override fun extractAudio(
        context: Context,
        uri: Uri,
        outAudioPath: String
    ): Observable<String> {
        return Observable.create { emitter ->
            var status: ResultType = ResultType.NONE
            try {
                status = genVideoUsingMuxer(
                    context,
                    uri,
                    outAudioPath,
                    -1,
                    -1,
                    useAudio = true,
                    useVideo = false
                )
            } catch (e: Exception) {
                emitter.onError(e)
            }
            if (status == ResultType.NO_AUDIO) {
                emitter.onError(
                    AudioExtractorNoAudioException("Video source doesn't have audio track")
                )
            }
            emitter.onNext(outAudioPath)
            emitter.onComplete()
        }
    }

    override fun extractAudio(videoFile: String, outAudioPath: String): Observable<String> {
        return Observable.create { emitter ->
            var status: ResultType = ResultType.NONE
            try {
                status = genVideoUsingMuxer(
                    videoFile,
                    outAudioPath,
                    -1,
                    -1,
                    useAudio = true,
                    useVideo = false
                )
            } catch (e: Exception) {
                emitter.onError(e)
            }
            if (status == ResultType.NO_AUDIO) {
                emitter.onError(
                    AudioExtractorNoAudioException("Video source doesn't have audio track")
                )
            }
            emitter.onNext(outAudioPath)
            emitter.onComplete()
        }
    }

    private fun genVideoUsingMuxer(
        srcPath: String?,
        dstPath: String?,
        startMs: Int,
        endMs: Int,
        useAudio: Boolean,
        useVideo: Boolean
    ): ResultType {
        // Set up MediaExtractor to read from the source.
        val extractor = MediaExtractor()
        extractor.setDataSource(srcPath!!)
        val retrieverSrc = MediaMetadataRetriever()
        retrieverSrc.setDataSource(srcPath)
        return genVideoUsingMuxer(
            extractor,
            retrieverSrc,
            dstPath,
            startMs,
            endMs,
            useAudio,
            useVideo
        )
    }

    private fun genVideoUsingMuxer(
        context: Context,
        uri: Uri,
        dstPath: String?,
        startMs: Int,
        endMs: Int,
        useAudio: Boolean,
        useVideo: Boolean
    ): ResultType {
        // Set up MediaExtractor to read from the source.
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)
        val retrieverSrc = MediaMetadataRetriever()
        retrieverSrc.setDataSource(context, uri)
        return genVideoUsingMuxer(
            extractor,
            retrieverSrc,
            dstPath,
            startMs,
            endMs,
            useAudio,
            useVideo
        )
    }

    /**
     * @param srcPath  the path of source video file.
     * @param dstPath  the path of destination video file.
     * @param startMs  starting time in milliseconds for trimming. Set to
     * negative if starting from beginning.
     * @param endMs    end time for trimming in milliseconds. Set to negative if
     * no trimming at the end.
     * @param useAudio true if keep the audio track from the source.
     * @param useVideo true if keep the video track from the source.
     * @throws IOException
     */
    private fun genVideoUsingMuxer(
        extractor: MediaExtractor,
        retrieverSrc: MediaMetadataRetriever,
        dstPath: String?,
        startMs: Int,
        endMs: Int,
        useAudio: Boolean,
        useVideo: Boolean
    ): ResultType {

        val trackCount = extractor.trackCount
        // Set up MediaMuxer for the destination.
        val muxer = MediaMuxer(dstPath!!, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        // Set up the tracks and retrieve the max buffer size for selected
        // tracks.
        val indexMap = HashMap<Int, Int>(trackCount)
        var bufferSize = -1
        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            var selectCurrentTrack = false
            if (mime!!.startsWith("audio/") && useAudio) {
                selectCurrentTrack = true
            } else if (mime.startsWith("video/") && useVideo) {
                selectCurrentTrack = true
            }
            if (selectCurrentTrack) {
                extractor.selectTrack(i)
                indexMap[i] = muxer.addTrack(format)
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    val newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    bufferSize = if (newSize > bufferSize) newSize else bufferSize
                }
            }
        }
        if (indexMap.isEmpty()) return ResultType.NO_AUDIO
        if (bufferSize < 0) {
            bufferSize = DEFAULT_BUFFER_SIZE
        }
        // Set up the orientation and starting time for extractor.
        val degreesString =
            retrieverSrc.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        if (degreesString != null) {
            val degrees = degreesString.toInt()
            if (degrees >= 0) {
                muxer.setOrientationHint(degrees)
            }
        }
        if (startMs > 0) {
            extractor.seekTo(startMs * 1000.toLong(), MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        }
        // Copy the samples from MediaExtractor to MediaMuxer. We will loop
        // for copying each sample and stop when we get to the end of the source
        // file or exceed the end time of the trimming.
        val offset = 0
        var trackIndex = -1
        val dstBuf: ByteBuffer = ByteBuffer.allocate(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()
        muxer.start()
        while (true) {
            bufferInfo.offset = offset
            bufferInfo.size = extractor.readSampleData(dstBuf, offset)
            if (bufferInfo.size < 0) {
                Timber.d("Saw input EOS.")
                bufferInfo.size = 0
                break
            } else {
                bufferInfo.presentationTimeUs = extractor.sampleTime
                if (endMs > 0 && bufferInfo.presentationTimeUs > endMs * 1000) {
                    Timber.d("The current sample is over the trim end time.")
                    break
                } else {
                    bufferInfo.flags = extractor.sampleFlags
                    trackIndex = extractor.sampleTrackIndex
                    muxer.writeSampleData(indexMap[trackIndex]!!, dstBuf, bufferInfo)
                    extractor.advance()
                }
            }
        }
        muxer.stop()
        muxer.release()
        return ResultType.SUCCESS
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024
    }
}
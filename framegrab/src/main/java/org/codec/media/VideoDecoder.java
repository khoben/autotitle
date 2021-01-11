package org.codec.media;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

class VideoDecoder {
    private String TAG = "VideoDecoder";
    private boolean DEBUG = FrameGrab.DEBUG;

    private MediaExtractor extractor = null;
    private MediaFormat format = null;
    private MediaCodec decoder = null;
    private Surface surface = null;
    private BufferInfo info = null;

    private boolean EOS = false;

    void setSource(String path) {
        if (DEBUG) Log.d(TAG, "Initializing Extractor");
        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setSource(Context context, Uri uri) {
        if (DEBUG) Log.d(TAG, "Initializing Extractor");
        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(context, uri, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setSurface(Surface surface) {
        this.surface = surface;
    }

    boolean isEOS() {
        return EOS;
    }

    int getFrameRate() {
        int fps = getFPS();
        return (int) ((1f / fps) * 1000 * 1000);
    }


    int getFPS() {
        return format.getInteger(MediaFormat.KEY_FRAME_RATE);
    }

    int getWidth() {
        return format.getInteger(MediaFormat.KEY_WIDTH);
    }

    int getHeight() {
        return format.getInteger(MediaFormat.KEY_HEIGHT);
    }

    long getDuration() {
        return format.getLong(MediaFormat.KEY_DURATION);
    }
    // END CALLABLE AFTER

    void release() {
        if (decoder != null) {
            decoder.stop();
            decoder.release();
        }

        if (extractor != null)
            extractor.release();

        decoder = null;
        extractor = null;
        format = null;
    }

    // Source has to be set
    // Create Extractor and Format
    void init() {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                extractor.selectTrack(i);
                break;
            }
        }
    }

    // Surface has to be set, and after init()
    // Create Decoder and Start
    void startDecoder() {
        if (DEBUG) Log.d(TAG, "Initializing Decoder");
        String mime = format.getString(MediaFormat.KEY_MIME);
        try {
            decoder = MediaCodec.createDecoderByType(mime);
        } catch (IOException e) {
            e.printStackTrace();
        }
        decoder.configure(format, surface, null, 0);

        if (DEBUG) Log.d(TAG, "Starting Decoder");
        decoder.start();
    }

    // Flush Decoder
    void flushDecoder() {
        if (decoder != null) {
            decoder.flush();
            EOS = false;
        }
    }

    // Go to last intra frame of frameNumber
    void seekTo(long frame) {
        if (DEBUG) Log.d(TAG, "Seek To Frame " + frame);
        info = new BufferInfo();
        long time = frame * getFrameRate();
        extractor.seekTo(time, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
    }

    // Get one frame at frameNumber
    void getFrameAt(long frame) {
        // Support for < API 21
        ByteBuffer[] inputBuffers = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            inputBuffers = decoder.getInputBuffers();
        }


        long time = frame * getFrameRate();
        boolean render = false;
        while (!render) {
            if (DEBUG) Log.d(TAG, "not render");
            int timeout = 10000;
            int inputId = decoder.dequeueInputBuffer(timeout);
            if (inputId >= 0) {
                ByteBuffer buffer = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    buffer = decoder.getInputBuffer(inputId);
                } else {
                    // Support for < API 21
                    buffer = inputBuffers[inputId];
                }

                if (buffer == null) {
                    throw new IllegalStateException("Buffer is null");
                }

                int sample = extractor.readSampleData(buffer, 0);
                long presentationTime = extractor.getSampleTime();

                if (sample < 0) {
                    decoder.queueInputBuffer(inputId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                } else {
                    decoder.queueInputBuffer(inputId, 0, sample, presentationTime, 0);
                    render = !extractor.advance();
                }

            }

            int outputId = decoder.dequeueOutputBuffer(info, timeout);
            boolean endOfStream = (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
            if (outputId >= 0) {
                if (info.presentationTimeUs >= time) render = true;
                decoder.releaseOutputBuffer(outputId, render);
                if (DEBUG && render) Log.d(TAG, "Rendering Output Time " + info.presentationTimeUs);
            }

            if (endOfStream) {
                if (DEBUG) Log.d(TAG, "End of stream");
                EOS = true;
                break;
            }
        }
    }

}


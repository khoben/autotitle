package org.codec.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public class FrameGrab {
    static boolean DEBUG = false;
    private final String TAG = "FrameGrab";
    private final HandlerThread mGLThread;
    private final Handler mGLHandler;
    VideoDecoder codec;
    CodecOutput output;

    int width = -1;
    int height = -1;

    public FrameGrab() {
        // Create Handler Thread
        if (DEBUG) Log.d(TAG, "Create Handler Thread");
        mGLThread = new HandlerThread("FrameGrab");
        mGLThread.start();
        mGLHandler = new Handler(mGLThread.getLooper());

        output = new CodecOutput();
        codec = new VideoDecoder();
    }

    // Call when FrameGrab is not needed anymore
    public void release() {
        if (DEBUG) Log.d(TAG, "Releasing FrameGrab");
        codec.release();
        output.release();
        mGLThread.quit();
    }

    // Call when you want to seek back
    public void flushDecoder() {
        if (DEBUG) Log.d(TAG, "Reset Decoder");
        mGLHandler.post(new Runnable() {
            @Override
            public void run() {
                codec.flushDecoder();
            }
        });
    }

    // Call before init()
    public void setSource(String path) {
        codec.setSource(path);
    }

    public void setSource(Context context, Uri uri) {
        codec.setSource(context, uri);
    }

    public void setTargetSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    // Call before seekToFrame() and getFrameAt()
    // Create Decoder
    public void init() {
        codec.init();

        // Run OnFrameAvailable on different thread
        mGLHandler.post(new Runnable() {
            @Override
            public void run() {
                // Set custom frame size if set, else original size
                if (width != -1) {
                    output.setWidth(width);
                    output.setHeight(height);
                } else {
                    output.setWidth(codec.getWidth());
                    output.setHeight(codec.getHeight());
                }

                output.init();
                codec.setSurface(output.getSurface());
                codec.startDecoder();
            }
        });
    }

    // Call before getFrameAt() and after init()
    // Go to previous frame of framenumber
    // Only works forward, if you want to go back call resetDecoder()
    public void seekToFrame(long frame) {
        codec.seekTo(frame);
    }

    // Decode Frame and wait for frame to be processed
    public void getFrameAt(final long frame) {
        if (DEBUG) Log.d(TAG, "Get Frame at " + frame);

        // Return if reached End of Stream
        if (codec.isEOS()) {
            if (DEBUG) Log.d(TAG, "Reached End-Of-Stream");
            return;
        }

        mGLHandler.post(new Runnable() {
            @Override
            public void run() {
                codec.getFrameAt(frame); // Has to be in a different thread than framelistener
            }
        });
        output.awaitFrame(); // Wait for Frame to be available
    }


    /**
     * Seek to desired time
     *
     * @param time In secs
     */
    public void seekToTime(long time) {
        long frame = time * getFrameRateSec();
        seekToFrame(frame);
    }


    /**
     * Get frame at desired time
     *
     * @param time In secs
     */
    public void getFrameAtTime(long time) {
        long frame = time * getFrameRateSec();
        getFrameAt(frame);
    }


    /**
     * End of stream
     */
    public boolean isEOS() {
        return codec.isEOS();
    }

    /**
     * Return Frame as Bitmap, except when reached EOS then only returning null
     */
    public Bitmap getBitmap() {
        if (DEBUG) Log.d(TAG, "Returning Bitmap");
        return output.getBitmap();
    }

    /**
     * Save current result frame as JPEG with high quality
     *
     * @param location Saving path
     */
    public void saveBitmap(String location) {
        saveBitmap(location, Bitmap.CompressFormat.JPEG, 100);
    }

    /**
     * Save current result frame
     *
     * @param location Saving path
     * @param format   Compress format
     * @param quality  Quality [0-100]
     */
    public void saveBitmap(String location, Bitmap.CompressFormat format, int quality) {
        Bitmap frame = getBitmap();
        if (frame == null) {
            if (DEBUG) Log.d(TAG, "Bitmap is null. Saving Bitmap not possible");
            return;
        }
        bmToFile(frame, location, format, quality);
    }

    private void bmToFile(Bitmap bm, String location, Bitmap.CompressFormat format, int quality) {
        if (DEBUG) Log.d(TAG, "Frame saved");
        try {
            FileOutputStream out = new FileOutputStream(location);
            bm.compress(format, quality, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get amount frames per seconds
     */
    public int getFrameRateSec() {
        return codec.getFPS();
    }

    /**
     * Get duration of loaded source video in ms
     */
    public long getDurationMs() {
        return codec.getDuration() / 1000L;
    }
}

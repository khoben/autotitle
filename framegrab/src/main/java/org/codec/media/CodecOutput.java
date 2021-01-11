package org.codec.media;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import org.codec.gl.GLHelper;

class CodecOutput implements SurfaceTexture.OnFrameAvailableListener {
    private final Object mWaitFrame = new Object();
    private String TAG = "CodecOutput";
    private boolean DEBUG = FrameGrab.DEBUG;
    private GLHelper mGLHelper = null;
    private int mWidth = 640;
    private int mHeight = 360;
    private SurfaceTexture sTexture = null;
    private Surface surface = null;
    private int textureID;

    private Bitmap frame = null;

    // Create GLHelper and Surface
    void init() {
        if (DEBUG) Log.d(TAG, "Creating GlHelper and Surface");
        mGLHelper = new GLHelper();
        int mDefaultTextureID = 10001;
        SurfaceTexture st = new SurfaceTexture(mDefaultTextureID);
        st.setDefaultBufferSize(mWidth, mHeight);
        mGLHelper.init(st);

        textureID = mGLHelper.createOESTexture();
        sTexture = new SurfaceTexture(textureID);
        sTexture.setOnFrameAvailableListener(this);
        surface = new Surface(sTexture);
    }

    void setWidth(int width) {
        mWidth = width;
    }

    void setHeight(int height) {
        mHeight = height;
    }

    Surface getSurface() {
        return surface;
    }

    // Get Bitmap, only once
    Bitmap getBitmap() {
        Bitmap bm = frame;
        frame = null;
        return bm;
    }

    // Wait for FrameProcessed()
    void awaitFrame() {
        if (DEBUG) Log.d(TAG, "Waiting for FrameAvailable");
        synchronized (mWaitFrame) {
            try {
                mWaitFrame.wait();
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            }
        }
    }

    // On Codec Frame Available, save Frame as Bitmap
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (DEBUG) Log.d(TAG, "Frame available");

        mGLHelper.drawFrame(sTexture, textureID);
        frame = mGLHelper.readPixels(mWidth, mHeight);
        frameProcessed();
    }

    // Notify awaitFrame() to continue
    private void frameProcessed() {
        if (DEBUG) Log.d(TAG, "Frame Processed");
        synchronized (mWaitFrame) {
            mWaitFrame.notifyAll();
        }
    }

    void release() {
        if (sTexture != null)
            sTexture.release();

        if (surface != null)
            surface.release();

        if (mGLHelper != null)
            mGLHelper.release();

        sTexture = null;
        surface = null;
        mGLHelper = null;
    }

}


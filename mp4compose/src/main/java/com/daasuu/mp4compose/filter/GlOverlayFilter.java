package com.daasuu.mp4compose.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Size;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;

/**
 * Created by sudamasayuki on 2018/01/07.
 */

public abstract class GlOverlayFilter extends GlFilter {

    private int[] textures = new int[1];

    private Bitmap bitmap = null;

    protected Size inputResolution = new Size(1280, 720);

    public GlOverlayFilter() {
        super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);
    }

    private final static String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D sTexture;\n" +
                    "uniform lowp sampler2D oTexture;\n" +
                    "void main() {\n" +
                    "   lowp vec4 textureColor = texture2D(sTexture, vTextureCoord);\n" +
                    "   lowp vec4 textureColor2 = texture2D(oTexture, vTextureCoord);\n" +
                    "   \n" +
                    "   gl_FragColor = mix(textureColor, textureColor2, textureColor2.a);\n" +
                    "}\n";

    public void setResolution(Size resolution) {
        this.inputResolution = resolution;
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        setResolution(new Size(width, height));
    }

    private void createBitmap() {
        releaseBitmap(bitmap);
        bitmap = Bitmap.createBitmap(inputResolution.getWidth(), inputResolution.getHeight(), Bitmap.Config.ARGB_8888);
    }

    @Override
    public void setup() {
        super.setup();// 1
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        createBitmap();
    }

    @Override
    public void onDraw(long currentTimeUs) {
        if (bitmap == null) {
            createBitmap();
        }
        if (bitmap.getWidth() != inputResolution.getWidth() || bitmap.getHeight() != inputResolution.getHeight()) {
            createBitmap();
        }

        bitmap.eraseColor(Color.argb(0F, 1F, 1F, 1F));
        Canvas bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.scale(1, -1, bitmapCanvas.getWidth() / 2F, bitmapCanvas.getHeight() / 2F);
        drawCanvas(bitmapCanvas, currentTimeUs);

        int offsetDepthMapTextureUniform = getHandle("oTexture");// 3

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GL_TEXTURE_2D, textures[0]);

        if (bitmap != null && !bitmap.isRecycled()) {
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
        }

        GLES20.glUniform1i(offsetDepthMapTextureUniform, 3);
    }

    protected abstract void drawCanvas(Canvas canvas, long currentTime);

    public static void releaseBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

}

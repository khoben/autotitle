package com.khoben.autotitle.service.mediaplayer

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import android.view.Surface
import timber.log.Timber
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SimpleVideoRender : VideoRender(),
        SurfaceTexture.OnFrameAvailableListener {

    private val mTriangleVertices: FloatBuffer
    private val mMVPMatrix = FloatArray(16)
    private val mSTMatrix = FloatArray(16)

    private var mProgram = 0
    private var mTextureID = 0
    private var muMVPMatrixHandle = 0
    private var muSTMatrixHandle = 0
    private var maPositionHandle = 0
    private var maTextureHandle = 0

    private var updateSurface = false

    private var surfaceTexture: SurfaceTexture? = null
    private var mediaPlayer: MediaSurfacePlayer? = null

    override fun setMediaPlayer(player: MediaSurfacePlayer?) {
        mediaPlayer = player
    }

    @Synchronized
    override fun onFrameAvailable(surface: SurfaceTexture) {
        updateSurface = true
    }

    override fun onDrawFrame(glUnused: GL10) {
        synchronized(this) {
            if (updateSurface) {
                surfaceTexture!!.updateTexImage()
                surfaceTexture!!.getTransformMatrix(mSTMatrix)
                updateSurface = false
            }
        }
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)
        checkGlError("glUseProgram")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES20.glVertexAttribPointer(
                maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices
        )
        checkGlError("glVertexAttribPointer maPosition")
        GLES20.glEnableVertexAttribArray(maPositionHandle)
        checkGlError("glEnableVertexAttribArray maPositionHandle")
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
        GLES20.glVertexAttribPointer(
                maTextureHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices
        )
        checkGlError("glVertexAttribPointer maTextureHandle")
        GLES20.glEnableVertexAttribArray(maTextureHandle)
        checkGlError("glEnableVertexAttribArray maTextureHandle")
        Matrix.setIdentityM(mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("glDrawArrays")
        GLES20.glFinish()
    }

    override fun onSurfaceChanged(glUnused: GL10, width: Int, height: Int) {}
    override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {
        mProgram = createProgram(mVertexShader, mFragmentShader)
        if (mProgram == 0) {
            return
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        checkGlError("glGetAttribLocation aPosition")
        if (maPositionHandle == -1) {
            throw RuntimeException("Could not get attrib location for aPosition")
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        checkGlError("glGetAttribLocation aTextureCoord")
        if (maTextureHandle == -1) {
            throw RuntimeException("Could not get attrib location for aTextureCoord")
        }
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        checkGlError("glGetUniformLocation uMVPMatrix")
        if (muMVPMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uMVPMatrix")
        }
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix")
        checkGlError("glGetUniformLocation uSTMatrix")
        if (muSTMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uSTMatrix")
        }
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        mTextureID = textures[0]
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)
        checkGlError("glBindTexture mTextureID")
        GLES20.glTexParameterf(
                GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
                GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR.toFloat()
        )

        /*
         * Create the SurfaceTexture that will feed this textureID,
         * and pass it to the MediaPlayer
         */
        surfaceTexture = SurfaceTexture(mTextureID)
        surfaceTexture!!.setOnFrameAvailableListener(this)

        Handler(Looper.getMainLooper()).post {
            mediaPlayer!!.setSurface(Surface(surfaceTexture))
            try {
                mediaPlayer!!.prepare()
            } catch (t: IOException) {
                Timber.e("Error while MediaPlayer preparing")
            }
            mediaPlayer!!.initSurface()
        }
        synchronized(this) { updateSurface = false }
    }

    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Timber.e("Could not compile shader $shaderType:")
                Timber.e(GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader)
            checkGlError("glAttachShader")
            GLES20.glAttachShader(program, pixelShader)
            checkGlError("glAttachShader")
            GLES20.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Timber.e("Could not link program: ")
                Timber.e(GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program
    }

    private fun checkGlError(op: String) {
        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            Timber.e("$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }

    companion object {
        private const val FLOAT_SIZE_BYTES = 4
        private const val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES
        private const val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
        private const val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3
        private const val GL_TEXTURE_EXTERNAL_OES = 0x8D65

        private val mTriangleVerticesData = floatArrayOf( // X, Y, Z, U, V
                -1.0f, -1.0f, 0f, 0f, 0f,
                1.0f, -1.0f, 0f, 1f, 0f,
                -1.0f, 1.0f, 0f, 0f, 1f,
                1.0f, 1.0f, 0f, 1f, 1f
        )

        private const val mVertexShader = """uniform mat4 uMVPMatrix;
                                        uniform mat4 uSTMatrix;
                                        attribute vec4 aPosition;
                                        attribute vec4 aTextureCoord;
                                        varying vec2 vTextureCoord;
                                        void main() {
                                          gl_Position = uMVPMatrix * aPosition;
                                          vTextureCoord = (uSTMatrix * aTextureCoord).xy;
                                        }
                                        """
        private const val mFragmentShader = """#extension GL_OES_EGL_image_external : require
                                        precision mediump float;
                                        varying vec2 vTextureCoord;
                                        uniform samplerExternalOES sTexture;
                                        void main() {
                                          gl_FragColor = texture2D(sTexture, vTextureCoord);
                                        }
                                        """

    }

    init {
        mTriangleVertices = ByteBuffer.allocateDirect(
                mTriangleVerticesData.size * FLOAT_SIZE_BYTES
        )
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTriangleVertices.put(mTriangleVerticesData).position(0)
        Matrix.setIdentityM(mSTMatrix, 0)
    }
}
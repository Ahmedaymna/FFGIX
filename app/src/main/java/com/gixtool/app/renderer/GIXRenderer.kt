package com.gixtool.app.renderer

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sin

class GIXRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private var program = 0
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var colorBuffer: FloatBuffer
    private var angle = 0f
    private var time = 0f

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    private val cubeVertices = floatArrayOf(
        // Front face
        -0.5f,-0.5f, 0.5f,  0.5f,-0.5f, 0.5f,  0.5f, 0.5f, 0.5f,
        -0.5f,-0.5f, 0.5f,  0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
        // Back face
        -0.5f,-0.5f,-0.5f, -0.5f, 0.5f,-0.5f,  0.5f, 0.5f,-0.5f,
        -0.5f,-0.5f,-0.5f,  0.5f, 0.5f,-0.5f,  0.5f,-0.5f,-0.5f,
        // Top face
        -0.5f, 0.5f,-0.5f, -0.5f, 0.5f, 0.5f,  0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f,-0.5f,  0.5f, 0.5f, 0.5f,  0.5f, 0.5f,-0.5f,
        // Bottom face
        -0.5f,-0.5f,-0.5f,  0.5f,-0.5f,-0.5f,  0.5f,-0.5f, 0.5f,
        -0.5f,-0.5f,-0.5f,  0.5f,-0.5f, 0.5f, -0.5f,-0.5f, 0.5f,
        // Right face
         0.5f,-0.5f,-0.5f,  0.5f, 0.5f,-0.5f,  0.5f, 0.5f, 0.5f,
         0.5f,-0.5f,-0.5f,  0.5f, 0.5f, 0.5f,  0.5f,-0.5f, 0.5f,
        // Left face
        -0.5f,-0.5f,-0.5f, -0.5f,-0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
        -0.5f,-0.5f,-0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f,-0.5f
    )

    private val cubeColors = floatArrayOf(
        // Front - Cyan
        0f,1f,1f,0.9f, 0f,1f,1f,0.9f, 0f,1f,1f,0.9f,
        0f,1f,1f,0.9f, 0f,1f,1f,0.9f, 0f,1f,1f,0.9f,
        // Back - Purple
        0.8f,0f,1f,0.9f, 0.8f,0f,1f,0.9f, 0.8f,0f,1f,0.9f,
        0.8f,0f,1f,0.9f, 0.8f,0f,1f,0.9f, 0.8f,0f,1f,0.9f,
        // Top - Orange
        1f,0.4f,0f,0.9f, 1f,0.4f,0f,0.9f, 1f,0.4f,0f,0.9f,
        1f,0.4f,0f,0.9f, 1f,0.4f,0f,0.9f, 1f,0.4f,0f,0.9f,
        // Bottom - Blue
        0f,0.4f,1f,0.9f, 0f,0.4f,1f,0.9f, 0f,0.4f,1f,0.9f,
        0f,0.4f,1f,0.9f, 0f,0.4f,1f,0.9f, 0f,0.4f,1f,0.9f,
        // Right - Green
        0f,1f,0.4f,0.9f, 0f,1f,0.4f,0.9f, 0f,1f,0.4f,0.9f,
        0f,1f,0.4f,0.9f, 0f,1f,0.4f,0.9f, 0f,1f,0.4f,0.9f,
        // Left - Pink
        1f,0f,0.6f,0.9f, 1f,0f,0.6f,0.9f, 1f,0f,0.6f,0.9f,
        1f,0f,0.6f,0.9f, 1f,0f,0.6f,0.9f, 1f,0f,0.6f,0.9f
    )

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec4 vColor;
        varying vec4 fColor;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            fColor = vColor;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec4 fColor;
        void main() {
            gl_FragColor = fColor;
        }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.04f, 0.04f, 0.12f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        vertexBuffer = ByteBuffer.allocateDirect(cubeVertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
            .apply { put(cubeVertices); position(0) }

        colorBuffer = ByteBuffer.allocateDirect(cubeColors.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
            .apply { put(cubeColors); position(0) }

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        time += 0.02f
        angle += 0.8f

        GLES20.glUseProgram(program)

        val posHandle = GLES20.glGetAttribLocation(program, "vPosition")
        val colorHandle = GLES20.glGetAttribLocation(program, "vColor")
        val mvpHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")

        GLES20.glEnableVertexAttribArray(posHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)

        // Draw main cube
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, angle, 1f, 1f, 0.5f)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)

        // Draw satellite cube
        Matrix.setIdentityM(modelMatrix, 0)
        val orbitX = (sin(time.toDouble()) * 1.5f).toFloat()
        val orbitY = (sin(time * 0.7).toDouble() * 0.8f).toFloat()
        Matrix.translateM(modelMatrix, 0, orbitX, orbitY, 0f)
        Matrix.scaleM(modelMatrix, 0, 0.3f, 0.3f, 0.3f)
        Matrix.rotateM(modelMatrix, 0, -angle * 2, 0f, 1f, 1f)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)

        GLES20.glDisableVertexAttribArray(posHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 20f)
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 5f,
            0f, 0f, 0f,
            0f, 1f, 0f)
    }

    private fun loadShader(type: Int, code: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, code)
            GLES20.glCompileShader(shader)
        }
    }
}

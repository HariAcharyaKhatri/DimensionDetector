package com.dimensiondetector.ar

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Renders the Android camera background quad using OpenGL ES 2.0 and ARCore frame transformations.
 */
class BackgroundRenderer {

    companion object {
        private const val TAG = "BackgroundRenderer"
        private const val COORDS_PER_VERTEX = 2
        private const val FLOAT_SIZE_BYTES = 4

        // 2D Normalized Device Coordinates (NDC) quad for 4 vertices:
        // Vertex 0: (-1.0f, -1.0f) -> Bottom-Left
        // Vertex 1: (+1.0f, -1.0f) -> Bottom-Right
        // Vertex 2: (-1.0f, +1.0f) -> Top-Left
        // Vertex 3: (+1.0f, +1.0f) -> Top-Right
        // Total: 4 vertices * 2 floats = 8 floats (32 bytes).
        private val QUAD_COORDS = floatArrayOf(
            -1.0f, -1.0f,
            +1.0f, -1.0f,
            -1.0f, +1.0f,
            +1.0f, +1.0f
        )

        // Default 2D normalized texture coordinates (UV):
        // Total: 4 vertices * 2 floats = 8 floats (32 bytes).
        private val QUAD_TEXCOORDS = floatArrayOf(
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
        )
    }

    private val vertexShaderCode = """
        attribute vec4 a_Position;
        attribute vec2 a_TexCoord;
        varying vec2 v_TexCoord;
        void main() {
            gl_Position = a_Position;
            v_TexCoord = a_TexCoord;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 v_TexCoord;
        uniform samplerExternalOES u_Texture;
        void main() {
            gl_FragColor = texture2D(u_Texture, v_TexCoord);
        }
    """.trimIndent()

    private var program = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureHandle = 0
    var textureId = -1
        private set

    // Coordinate buffers - both strictly 8 floats (4 vertices * 2 floats)
    private val quadCoords: FloatBuffer
    private val transformedTexCoords: FloatBuffer
    private var hasTransformedCoords = false

    init {
        quadCoords = ByteBuffer.allocateDirect(QUAD_COORDS.size * FLOAT_SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(QUAD_COORDS)
                position(0)
            }

        transformedTexCoords = ByteBuffer.allocateDirect(QUAD_TEXCOORDS.size * FLOAT_SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(QUAD_TEXCOORDS)
                position(0)
            }
    }

    fun createOnGlThread() {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        val vShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        if (vShader == 0 || fShader == 0) {
            Log.e(TAG, "Shader compilation failed; cannot link program")
            return
        }

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vShader)
            GLES20.glAttachShader(it, fShader)
            GLES20.glLinkProgram(it)
        }

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES20.glGetProgramInfoLog(program)
            GLES20.glDeleteProgram(program)
            program = 0
            Log.e(TAG, "Error linking shader program: $error")
            return
        }

        positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        texCoordHandle = GLES20.glGetAttribLocation(program, "a_TexCoord")
        textureHandle = GLES20.glGetUniformLocation(program, "u_Texture")
    }

    fun draw(frame: Frame) {
        if (frame.timestamp == 0L) {
            // Camera frame is not yet ready; suppress draw
            return
        }

        if (program == 0 || textureId == -1) {
            return
        }

        // Coordinate transformation for aspect ratio and device rotation
        if (!hasTransformedCoords || frame.hasDisplayGeometryChanged()) {
            try {
                quadCoords.position(0)
                transformedTexCoords.position(0)
                frame.transformCoordinates2d(
                    Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                    quadCoords,
                    Coordinates2d.TEXTURE_NORMALIZED,
                    transformedTexCoords
                )
                transformedTexCoords.position(0)
                quadCoords.position(0)
                hasTransformedCoords = true
            } catch (e: Exception) {
                Log.e(TAG, "Coordinate transformation failed: ${e.message}", e)
            }
        }

        try {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
            GLES20.glDepthMask(false)

            GLES20.glUseProgram(program)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
            GLES20.glUniform1i(textureHandle, 0)

            quadCoords.position(0)
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadCoords)

            transformedTexCoords.position(0)
            GLES20.glEnableVertexAttribArray(texCoordHandle)
            GLES20.glVertexAttribPointer(texCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, transformedTexCoords)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

            GLES20.glDisableVertexAttribArray(positionHandle)
            GLES20.glDisableVertexAttribArray(texCoordHandle)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during background draw: ${e.message}", e)
        } finally {
            GLES20.glDepthMask(true)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) return 0
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            Log.e(TAG, "Error compiling shader (type $type): $error")
            return 0
        }
        return shader
    }
}

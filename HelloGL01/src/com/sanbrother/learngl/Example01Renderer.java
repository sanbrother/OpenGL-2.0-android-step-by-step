package com.sanbrother.learngl;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

import com.learnopengles.android.common.RawResourceReader;
import com.learnopengles.android.common.ShaderHelper;
import com.sanbrother.opengl.common.Util;

public class Example01Renderer implements Renderer {

	public Example01Renderer(Context context) {
		this.context = context;

		// This triangle is red, green, and blue.
		final float[] triangle1VerticesData = {
			// X, Y, Z,
			// R, G, B, A
			-0.5f, -0.25f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
			0.5f, -0.25f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.559016994f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f
		};

		mTriangle1Vertices = Util.getNativeBuffer(triangle1VerticesData);
	}

	@Override
	public void onDrawFrame(GL10 glUnused) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		drawTriangle(mTriangle1Vertices);
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		Util.setupOrthoProjectionMatrix(mProjectionMatrix, width, height, 1.0f, 10.0f, true);
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig eglConfig) {
		// Set the background clear color to white.
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

		initProgram();

		initViewMatrix();
	}

	/**
	 * Draws a triangle from the given vertex data.
	 * 
	 * @param aTriangleBuffer
	 *            The buffer containing the vertex data.
	 */
	private void drawTriangle(final ByteBuffer aTriangleBuffer) {
		// Pass in the position information
		aTriangleBuffer.position(POSITION_DATA_OFFSET);
		GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE_BYTES, aTriangleBuffer);

		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Pass in the color information
		aTriangleBuffer.position(COLOR_DATA_OFFSET * 4);
		GLES20.glVertexAttribPointer(mColorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE_BYTES, aTriangleBuffer);

		GLES20.glEnableVertexAttribArray(mColorHandle);

		Matrix.setIdentityM(mModelMatrix, 0);

		// This multiplies the view matrix by the model matrix, and stores the
		// result in the MVP matrix
		// (which currently contains model * view).
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

		// This multiplies the modelview matrix by the projection matrix, and
		// stores the result in the MVP matrix
		// (which now contains model * view * projection).
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
	}

	private void initProgram() {
		final String vertexShader = RawResourceReader.readTextFileFromRawResource(this.context, R.raw.simple_vertex_shader);
		final String fragmentShader = RawResourceReader.readTextFileFromRawResource(this.context, R.raw.simple_fragment_shader);
		final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
		final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

		// create empty OpenGL ES Program
		mProgramHandle = GLES20.glCreateProgram();

		// add the vertex shader to program
		GLES20.glAttachShader(mProgramHandle, vertexShaderHandle);

		// add the fragment shader to program
		GLES20.glAttachShader(mProgramHandle, fragmentShaderHandle);

		// creates OpenGL ES program executables
		GLES20.glLinkProgram(mProgramHandle);

		// Tell OpenGL to use this program when rendering.
		GLES20.glUseProgram(mProgramHandle);

		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "vPosition");
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
		mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
	}

	private void initViewMatrix() {
		// Position the eye behind the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 1.5f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		// Set our up vector. This is where our head would be pointing were we
		// holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera
		// position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination
		// of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices
		// separately if we choose.
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
	}

	private final Context context;

	/**
	 * Store the model matrix. This matrix is used to move models from object
	 * space (where each model can be thought of being located at the center of
	 * the universe) to world space.
	 */
	private float[] mModelMatrix = new float[16];

	/**
	 * Store the view matrix. This can be thought of as our camera. This matrix
	 * transforms world space to eye space; it positions things relative to our
	 * eye.
	 */
	private float[] mViewMatrix = new float[16];

	/**
	 * Store the projection matrix. This is used to project the scene onto a 2D
	 * viewport.
	 */
	private float[] mProjectionMatrix = new float[16];

	/**
	 * Allocate storage for the final combined matrix. This will be passed into
	 * the shader program.
	 */
	private float[] mMVPMatrix = new float[16];

	/** Store our model data in a float buffer. */
	private final ByteBuffer mTriangle1Vertices;

	private int mMVPMatrixHandle;
	private int mProgramHandle;
	private int mPositionHandle;
	private int mColorHandle;

	/** How many bytes per float. */
	private final static int BYTES_PER_FLOAT = 4;
	/** Offset of the position data. */
	private final static int POSITION_DATA_OFFSET = 0;
	/** Size of the position data in elements. */
	private final static int POSITION_DATA_SIZE = 3;
	/** Offset of the color data. */
	private final static int COLOR_DATA_OFFSET = 3;
	/** Size of the color data in elements. */
	private final static int COLOR_DATA_SIZE = 4;

	/** How many elements per vertex. */
	private final static int STRIDE_BYTES = (POSITION_DATA_SIZE + COLOR_DATA_SIZE) * BYTES_PER_FLOAT;
}

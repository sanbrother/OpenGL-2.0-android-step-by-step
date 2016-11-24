package com.sanbrother.opengl.common;

import android.opengl.Matrix;

public final class MatrixUtil {
	/**
	 * Singleton
	 */
	private MatrixUtil() {
		
	}

    /**
     * Orthographic projection
     */
    public static void setupOrthoProjectionMatrix(float[] projectionMatrix, int width, int height, float near, float far, boolean portraitMode) {
        if (portraitMode) {
            final float ratio = (float) height / (float) width;
            final float left = -1.0f;
            final float right = 1.0f;
            final float top = ratio;
            final float bottom = -ratio;
            
            Matrix.orthoM(projectionMatrix, 0, left, right, bottom, top, near, far);
        } else {
            final float ratio = (float) width / (float) height;
            final float left = -ratio;
            final float right = ratio;
            final float top = 1.0f;
            final float bottom = -1.0f;
            
            Matrix.orthoM(projectionMatrix, 0, left, right, bottom, top, near, far);
        }
    }
}

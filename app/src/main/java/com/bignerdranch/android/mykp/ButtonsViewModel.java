package com.bignerdranch.android.mykp;

import android.util.Log;
import android.widget.SeekBar;

public class ButtonsViewModel {
    private static final String TAG = "ButtonsViewModel";
    OpenGLRenderer mOpenGLRenderer;
    public ButtonsViewModel(OpenGLRenderer openGLRenderer) {
        mOpenGLRenderer = openGLRenderer;
    }

    public void clickZoomPlus() {
        mOpenGLRenderer.zoomPlus();
        Log.i(TAG, "clickZoomPlus");
    }
    public void clickZoomMinus() {
        mOpenGLRenderer.zoomMinus();
        Log.i(TAG, "clickZoomPlus");
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.speed_cube_changer) {
            Log.d(TAG, "Got progress change of cube: " + progress);
            mOpenGLRenderer.setTetrahedronAngleMultiplier(((float)progress)/100);

        }
        else if (seekBar.getId() == R.id.speed_sphere_changer) {
            Log.d(TAG, "Got progress change of sphere: " + progress);
            mOpenGLRenderer.setSphereAngleMultiplier(((float)progress)/100);
        }
    }
}

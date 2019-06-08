package com.bignerdranch.android.mykp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class FrameFragment extends Fragment implements View.OnTouchListener {
    private static final String TAG = "FrameFragment";
    private Callbacks mCallbacks;
    private GLSurfaceView glSurfaceView;
    private OpenGLRenderer openGLRenderer;

    private float prevX;
    private float prevY;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!supportES2()) {
            Toast.makeText(getActivity(), "OpenGL ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            getActivity().finish();
            return null;
        }
        glSurfaceView = new GLSurfaceView(getActivity());
        glSurfaceView.setOnTouchListener(this);
        glSurfaceView.setEGLContextClientVersion(2);
        openGLRenderer = new OpenGLRenderer(getActivity());
        glSurfaceView.setRenderer(openGLRenderer);
        mCallbacks.rendererCreated(openGLRenderer);
        return glSurfaceView;

    }

    private boolean supportES2() {
        ActivityManager activityManager =
                (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // нажатие
//                sDown = "Down: " + x + "," + y;
//                sMove = ""; sUp = "";
                prevX = x;
                prevY = y;
                break;
            case MotionEvent.ACTION_MOVE: // движение
                openGLRenderer.setEye(prevX - x, prevY - y);
                Log.i(TAG, "Prev: " + prevX );
                Log.i(TAG, "Moved: " + (prevX - x) );
//                sMove = "Move: " + x + "," + y;
                break;
            case MotionEvent.ACTION_UP: // отпускание
            case MotionEvent.ACTION_CANCEL:
//                sMove = "";
//                sUp = "Up: " + x + "," + y;
                break;
        }
//        tv.setText(sDown + "\n" + sMove + "\n" + sUp);
        return true;    }

    public interface Callbacks {
        void rendererCreated(OpenGLRenderer openGLRenderer);
    }
}

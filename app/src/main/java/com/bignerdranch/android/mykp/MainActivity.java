package com.bignerdranch.android.mykp;

import android.support.v4.app.Fragment;

public class MainActivity extends SingleFragmentActivity implements FrameFragment.Callbacks{

    @Override
    protected Fragment createFragment() {
        return new FrameFragment();
    }

    @Override
    public void rendererCreated(OpenGLRenderer openGLRenderer) {
        Fragment fragmentButtons = ButtonsFragment.newInstance(openGLRenderer);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.buttons_fragment_container,fragmentButtons)
                .commit();
    }
}

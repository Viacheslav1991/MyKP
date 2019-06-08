package com.bignerdranch.android.mykp;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.android.mykp.databinding.FragmentButtonsBinding;

public class ButtonsFragment extends Fragment {
    private static final String ARG_RENDERER = "renderer";

    public static ButtonsFragment newInstance(OpenGLRenderer renderer) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_RENDERER, renderer);

        ButtonsFragment fragment = new ButtonsFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentButtonsBinding fragmentButtonsBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_buttons, container, false);
        OpenGLRenderer openGLRenderer = (OpenGLRenderer) getArguments().getSerializable(ARG_RENDERER);
        fragmentButtonsBinding.setViewModel(new ButtonsViewModel(openGLRenderer));
        return fragmentButtonsBinding.getRoot();
    }
}

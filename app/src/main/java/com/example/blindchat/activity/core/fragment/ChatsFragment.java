package com.example.blindchat.activity.core.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.blindchat.R;

import java.util.Objects;

public class ChatsFragment extends Fragment {

    private View fragmentView;
    private Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_chats, container, false);
        context = Objects.requireNonNull(getActivity()).getApplicationContext();

        return fragmentView;
    }

}

package com.pgm.elemenestedscrolling.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pgm.elemenestedscrolling.R;


public class TabFragment extends Fragment {
    private String name;

    public static Fragment newInstance(String name) {
        Bundle args = new Bundle();
        args.putString("data",name);
        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments()!=null) {
            name=getArguments().getString("data");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text_layout, container, false);
        TextView textView = view.findViewById(R.id.tv_fragment_tab_text);
        textView.setText(name);
        return view;
    }
}

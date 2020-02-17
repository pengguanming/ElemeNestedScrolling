package com.pgm.elemenestedscrolling.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.pgm.elemenestedscrolling.MyElemeActivity;
import com.pgm.elemenestedscrolling.R;
import com.pgm.elemenestedscrolling.adapter.FoodAdater;

import java.util.ArrayList;

public class FoodFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private FoodAdater mAdater;
    private ArrayList<Integer> mDatas = new ArrayList<>();
    private View mFooterView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_layout, container, false);
        mFooterView = inflater.inflate(R.layout.item_food_footer_layout, container, false);
        initView(view);
        initEvent();
        return view;
    }

    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void initEvent() {
        mAdater.addFooterView(mFooterView);
        mRecyclerView.setAdapter(mAdater);
        mAdater.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ((MyElemeActivity) getActivity()).showFoodLayout();
            }
        });
    }

    private void initData() {
        for (int i = 0; i < 32; i++) {
            mDatas.add(i);
        }
        mAdater = new FoodAdater(mDatas);
    }
}

package com.pgm.elemenestedscrolling;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.flyco.tablayout.SlidingTabLayout;
import com.jaeger.library.StatusBarUtil;
import com.pgm.elemenestedscrolling.adapter.ElemeDetailAdapter;
import com.pgm.elemenestedscrolling.fragment.FoodFragment;
import com.pgm.elemenestedscrolling.fragment.TabFragment;
import com.pgm.elemenestedscrolling.widget.ElemeFoodNestedScrollLayout;
import com.pgm.elemenestedscrolling.widget.ElemeNestedScrollLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class MyElemeActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private SlidingTabLayout mSl;
    private ViewPager mViewPager;
    private TextView mTvCouponCount;
    private ConstraintLayout mClShopBar;
    private ElemeNestedScrollLayout mElemeNSLayout;

    private ElemeFoodNestedScrollLayout mElemeFoodNsLayout;
    private View mVMask;
    private ImageView mIvClose;
    private ImageView mIvExpand;

    private ElemeDetailAdapter mAdapter;
    private final String[] mTitles = {
            "点餐", "评价", "商家"
    };
    private ArrayList<Fragment> mFragments=new ArrayList<>();
    private MyFragmentAdapter mFragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_eleme_layout);
        StatusBarUtil.setTranslucentForImageView(this,0,null);
        initData();
        initView();
        initEvent();
    }

    private void initEvent() {
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId()==R.id.iv_item_text_close){
                    mElemeNSLayout.restore(ElemeNestedScrollLayout.ANIM_DURATION_FRACTION);
                }
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mViewPager.setAdapter(mFragmentAdapter);
        mSl.setViewPager(mViewPager,mTitles);

        mTvCouponCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mElemeNSLayout.expand(ElemeNestedScrollLayout.ANIM_DURATION_FRACTION);
            }
        });
        mElemeNSLayout.setProgressUpdateListener(new ElemeNestedScrollLayout.ProgressUpdateListener() {
            @Override
            public void onUpCollapsedContentTransProUpdate(float pro) {

            }

            @Override
            public void onUpAlphaScaleProUpdate(float pro) {
            }

            @Override
            public void onUpAlphaGradientProUpdate(float pro) {
                if (pro>0.5f){
                    StatusBarUtil.setLightMode(MyElemeActivity.this);
                }else {
                    StatusBarUtil.setDarkMode(MyElemeActivity.this);
                }
            }

            @Override
            public void onDownCollapsedAlphaProUpdate(float pro) {

            }

            @Override
            public void onDownContentAlphaProUpdate(float pro) {

            }

            @Override
            public void onDownShopBarTransProUpdate(float pro) {

            }
        });

        mElemeFoodNsLayout.setProgressUpdateListener(new ElemeFoodNestedScrollLayout.ProgressUpdateListener() {
            @Override
            public void onDownConetntCloseProUpdate(float pro) {
                mElemeNSLayout.setScaleX(0.9f+(pro*0.1f));
                mElemeNSLayout.setScaleY(0.9f+(pro*0.1f));
                mVMask.setAlpha(1-pro);
                if (pro==1){
                    mVMask.setVisibility(View.GONE);
                }else {
                    mVMask.setVisibility(View.VISIBLE);
                }
            }
        });
        mIvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mElemeFoodNsLayout.close(ElemeFoodNestedScrollLayout.ANIM_DURATION_FRACTION);
            }
        });
        mVMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    mElemeFoodNsLayout.close(ElemeFoodNestedScrollLayout.ANIM_DURATION_FRACTION);
            }
        });
        mIvExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mElemeFoodNsLayout.expand(ElemeFoodNestedScrollLayout.ANIM_DURATION_FRACTION);
            }
        });

        mIvClose.setClickable(false);
        mVMask.setClickable(false);
        mIvExpand.setClickable(false);

        //反射修改最少滑动距离
        try {
            Field mTouchSlop = ViewPager.class.getDeclaredField("mTouchSlop");
            mTouchSlop.setAccessible(true);
            mTouchSlop.setInt(mViewPager,dp2px(50));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        mViewPager.setOffscreenPageLimit(mFragments.size());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                int visi = i == 0 ? View.VISIBLE : View.GONE;
                mClShopBar.setVisibility(visi);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void initData() {
        mAdapter = new ElemeDetailAdapter();
        mFragments.add(new FoodFragment());
        mFragments.add(TabFragment.newInstance("我是评价页面"));
        mFragments.add(TabFragment.newInstance("我是商家页面"));
        mFragmentAdapter=new MyFragmentAdapter(getSupportFragmentManager());
    }

    private void initView() {
        mRecyclerView =  findViewById(R.id.rv_collasped);
        mSl=findViewById(R.id.stl);
        mViewPager=findViewById(R.id.vp);
        mElemeNSLayout = findViewById(R.id.eleme_nested_scroll_layout);
        mElemeFoodNsLayout = findViewById(R.id.eleme_food_ns_layout);
        mTvCouponCount = findViewById(R.id.tv_coupon_count);
        mClShopBar = findViewById(R.id.cl_shop_bar);
        mVMask = findViewById(R.id.v_mask);

        mIvClose = findViewById(R.id.iv_small_close);
        mIvExpand = findViewById(R.id.iv_food_expand);
    }

    public void showFoodLayout(){
        mElemeFoodNsLayout.restore(ElemeFoodNestedScrollLayout.ANIM_DURATION_FRACTION);
    }

    public int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal,getResources().getDisplayMetrics());
    }

    private class MyFragmentAdapter extends FragmentPagerAdapter{

        MyFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return mFragments.get(i);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }
}
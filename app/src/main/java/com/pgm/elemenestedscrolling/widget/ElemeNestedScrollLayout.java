package com.pgm.elemenestedscrolling.widget;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.math.MathUtils;
import android.support.v4.view.NestedScrollingParent2;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.pgm.elemenestedscrolling.R;


public class ElemeNestedScrollLayout extends FrameLayout implements NestedScrollingParent2 {
    public static final long ANIM_DURATION_FRACTION = 200L;

    //Header部分
    private View mIvLogo;
    private View mVCollect;

    //Collaps Content部分
    private View mClCollapsedHeader;
    private View mCollapsedContent;
    private View mRvCollapsed;

    //TopBar部分
    private View mIvSearch;
    private View mIvShare;
    private View mTvSearch;
    private View mTopBar;
    private ImageView mIvBack;
    private ImageView mIvAssemble;

    //Content部分
    private View mLlContent;
    private View mIvClose;
    private View mViewPager;
    private View mStl;

    //ShopBar部分
    private View mShopBar;

    private NestedScrollingParentHelper mParentHelper;
    private ProgressUpdateListener mProgressUpdateListener;

    private ArgbEvaluator iconArgbEvaluator;//返回icon、拼团icon颜色渐变的Evaluator
    private ArgbEvaluator topBarArgbEvaluator;//topbar颜色渐变的Evaluator
    private ValueAnimator restoreOrExpandAnimator;//收起或展开折叠内容时执行的动画
    private ValueAnimator reboundedAnim;//回弹动画

    private float topBarHeight;//topBar高度
    private float shopBarHeight;//shopBar高度
    private float contentTransY;//滑动内容初始化TransY
    private float upAlphaScaleY;//上滑时logo，收藏icon缩放、搜索icon、分享icon透明度临界值
    private float upAlphaGradientY;//上滑时搜索框、topBar背景，返回icon、拼团icon颜色渐变临界值
    private float downFlingCutOffY;//从折叠状态下滑产生fling时回弹到初始状态的临界值
    private float downCollapsedAlphaY;//下滑时折叠内容透明度临界值
    private float downShopBarTransY;//下滑时购物内容位移临界值
    private float downContentAlphaY;//下滑时收起按钮和滑动内容透明度临界值
    private float downEndY;//下滑时终点值


    public ElemeNestedScrollLayout(@NonNull Context context) {
        this(context, null);
    }

    public ElemeNestedScrollLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ElemeNestedScrollLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mParentHelper = new NestedScrollingParentHelper(this);

        iconArgbEvaluator = new ArgbEvaluator();
        topBarArgbEvaluator = new ArgbEvaluator();
        reboundedAnim = new ValueAnimator();
        reboundedAnim.setInterpolator(new DecelerateInterpolator());
        reboundedAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                translation(mLlContent, (float) animation.getAnimatedValue());

                //根据upAlphaScalePro,设置logo、收藏icon缩放，搜索icon、分享icon透明度
                float upAlphaScalePro = getUpAlphaScalePro();
                alphaScaleByPro(upAlphaScalePro);

                //根据upAlphaGradientPro,设置topBar背景、返回icon、拼团icon颜色渐变值，搜索框透明度
                float upAlphaGradientPro = getUpAlphaGradientPro();
                alphaGradientByPro(upAlphaGradientPro);

                //根据downCollapsedAlphaPro,设置折叠内容透明度
                float downCollapsedAlphaPro = getDownCollapsedAlphaPro();
                alphaCollapsedContentByPro(downCollapsedAlphaPro);

                //根据downShopBarTransPro,设置购物内容内容位移
                float downShopBarTransPro = getDownShopBarTransPro();
                transShopBarByPro(downShopBarTransPro);

                //根据upCollapsedContentTransPro,设置折叠内容位移
                float upCollapsedContentTransPro = getUpCollapsedContentTransPro();
                transCollapsedContentByPro(upCollapsedContentTransPro);

                if (mProgressUpdateListener!=null){
                    mProgressUpdateListener.onUpAlphaScaleProUpdate(upAlphaScalePro);
                    mProgressUpdateListener.onUpAlphaGradientProUpdate(upAlphaGradientPro);
                    mProgressUpdateListener.onDownCollapsedAlphaProUpdate(downCollapsedAlphaPro);
                    mProgressUpdateListener.onDownShopBarTransProUpdate(downShopBarTransPro);
                    mProgressUpdateListener.onUpCollapsedContentTransProUpdate(upCollapsedContentTransPro);
                }
            }
        });
        reboundedAnim.setDuration(ANIM_DURATION_FRACTION);

        restoreOrExpandAnimator = new ValueAnimator();
        restoreOrExpandAnimator.setInterpolator(new AccelerateInterpolator());
        restoreOrExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                translation(mLlContent, (float) animation.getAnimatedValue());

                //根据downShopBarTransPro,设置购物内容内容位移
                float downShopBarTransPro = getDownShopBarTransPro();
                transShopBarByPro(downShopBarTransPro);

                //根据downCollapsedAlphaPro,设置折叠内容透明度
                float downCollapsedAlphaPro = getDownCollapsedAlphaPro();
                alphaCollapsedContentByPro(downCollapsedAlphaPro);

                //根据downContentAlphaPro,设置滑动内容、收起按钮的透明度
                float downContentAlphaPro = getDownContentAlphaPro();
                alphaContentByPro(downContentAlphaPro);

                if (mProgressUpdateListener!=null){
                    mProgressUpdateListener.onDownCollapsedAlphaProUpdate(downCollapsedAlphaPro);
                    mProgressUpdateListener.onDownContentAlphaProUpdate(downContentAlphaPro);
                    mProgressUpdateListener.onDownShopBarTransProUpdate(downShopBarTransPro);
                }
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLlContent = findViewById(R.id.cl_content);
        mCollapsedContent = findViewById(R.id.cl_collapsed_content);
        mIvSearch = findViewById(R.id.iv_search);
        mIvShare = findViewById(R.id.iv_share);
        mIvBack = findViewById(R.id.iv_back);
        mIvAssemble = findViewById(R.id.iv_assemble);
        mIvLogo = findViewById(R.id.iv_logo);
        mVCollect = findViewById(R.id.iv_collect);
        mTvSearch = findViewById(R.id.tv_search);
        mTopBar = findViewById(R.id.cl_top_bar);
        mClCollapsedHeader = findViewById(R.id.cl_collapsed_header);
        mRvCollapsed = findViewById(R.id.rv_collasped);
        mIvClose = findViewById(R.id.iv_close);
        mViewPager = findViewById(R.id.vp);
        mStl = findViewById(R.id.stl);
        mShopBar = findViewById(R.id.cl_shop_bar);

        mIvClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLlContent.getTranslationY() == downEndY) {
                    setAlpha(mIvClose,0);
                    mIvClose.setVisibility(GONE);
                    restore(ANIM_DURATION_FRACTION);
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //设置滑动内容View的高度
        topBarHeight= mTopBar.getMeasuredHeight();
        ViewGroup.LayoutParams params = mLlContent.getLayoutParams();
        params.height = (int) (getMeasuredHeight() - topBarHeight);
        //重新测量
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        shopBarHeight = getResources().getDimension(R.dimen.shop_bar_height);
        contentTransY = getResources().getDimension(R.dimen.content_trans_y);
        downShopBarTransY = contentTransY+ shopBarHeight;
        upAlphaScaleY = contentTransY - dp2px(32);
        upAlphaGradientY = contentTransY - dp2px(64);
        downFlingCutOffY = contentTransY + dp2px(28);
        downCollapsedAlphaY = contentTransY + dp2px(32);
        downContentAlphaY = getResources().getDimension(R.dimen.donw_content_alpha_y);
        downEndY = getHeight() - getResources().getDimension(R.dimen.iv_close_height);
    }

    /**
     * 释放资源
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (restoreOrExpandAnimator.isStarted()) {
            restoreOrExpandAnimator.cancel();
            restoreOrExpandAnimator.removeAllUpdateListeners();
            restoreOrExpandAnimator = null;
        }

        if(reboundedAnim.isStarted()){
            reboundedAnim.cancel();
            reboundedAnim.removeAllUpdateListeners();
            reboundedAnim = null;
        }

        if (mProgressUpdateListener!=null){
            mProgressUpdateListener=null;
        }
    }

    //---NestedScrollingParent---//
    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        onNestedScrollAccepted(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target) {
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        if (velocityY<0){
            float translationY = mLlContent.getTranslationY();
            float dy = translationY - velocityY;
            if (translationY >topBarHeight && translationY<= downFlingCutOffY) {
                if (dy<contentTransY){
                    reboundedAnim.setFloatValues(translationY,dy);
                }else if (dy>contentTransY&&dy<downFlingCutOffY){
                    reboundedAnim.setFloatValues(translationY,dy,contentTransY);
                }else {
                    reboundedAnim.setFloatValues(translationY,downFlingCutOffY,contentTransY);
                }
                reboundedAnim.start();
                return true;
            }
        }
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }

    //---NestedScrollingParent2---//
    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        //只接受内容View的垂直滑动
        return child.getId() == mLlContent.getId()&&axes== ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type);

        if (restoreOrExpandAnimator.isStarted()) {
            restoreOrExpandAnimator.cancel();
        }
        checkIvCloseVisi();
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mParentHelper.onStopNestedScroll(target, type);
        //如果不是从初始状态转换到展开状态过程触发返回
        if (mLlContent.getTranslationY() <= contentTransY) {
            return;
        }

        //根据百分比计算动画执行的时长
        float downCollapsedAlphaPro = getDownCollapsedAlphaPro();
        float downContentAlphaYPro = getDownContentAlphaPro();
        if (downCollapsedAlphaPro == 0) {
            expand((long) (downContentAlphaYPro * ANIM_DURATION_FRACTION));
        } else {
            restore((long) (downCollapsedAlphaPro * ANIM_DURATION_FRACTION));
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {

    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        float contentTransY = mLlContent.getTranslationY() - dy;

        //处理上滑
        if (dy > 0) {
            if (contentTransY >= topBarHeight) {
                translationByConsume(mLlContent, contentTransY, consumed, dy);
            } else {
                translationByConsume(mLlContent, topBarHeight, consumed, (mLlContent.getTranslationY() - topBarHeight));
            }
        }

        if (dy < 0 && !target.canScrollVertically(-1)) {
            //下滑时处理Fling,完全折叠时，下滑Recycler(或NestedScrollView) Fling滚动到列表顶部（或视图顶部）停止Fling
            if (type == ViewCompat.TYPE_NON_TOUCH&&mLlContent.getTranslationY() == topBarHeight) {
                return;
            }

            //处理下滑
            if (contentTransY >= topBarHeight && contentTransY <= downEndY) {
                translationByConsume(mLlContent, contentTransY, consumed, dy);
            } else {
                translationByConsume(mLlContent, downEndY, consumed, downEndY - mLlContent.getTranslationY());
                if (target instanceof RecyclerView) {
                    ((RecyclerView) target).stopScroll();
                }
                if (target instanceof NestedScrollView) {
                    //模拟DONW事件停止滚动，注意会触发onNestedScrollAccepted()
                    MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0);
                    target.onTouchEvent(motionEvent);
                }
            }
        }

        //根据upAlphaScalePro,设置logo、收藏icon缩放，搜索icon、分享icon透明度
        float upAlphaScalePro = getUpAlphaScalePro();
        alphaScaleByPro(upAlphaScalePro);

        //根据upAlphaGradientPro,设置topBar背景、返回icon、拼团icon颜色渐变值，搜索框透明度
        float upAlphaGradientPro = getUpAlphaGradientPro();
        alphaGradientByPro(upAlphaGradientPro);

        //根据downCollapsedAlphaPro,设置折叠内容透明度
        float downCollapsedAlphaPro = getDownCollapsedAlphaPro();
        alphaCollapsedContentByPro(downCollapsedAlphaPro);

        //根据downContentAlphaPro,设置滑动内容、收起按钮的透明度
        float downContentAlphaPro = getDownContentAlphaPro();
        alphaContentByPro(downContentAlphaPro);

        //根据downShopBarTransPro,设置购物内容内容位移
        float downShopBarTransPro = getDownShopBarTransPro();
        transShopBarByPro(downShopBarTransPro);

        //根据upCollapsedContentTransPro,设置折叠内容位移
        float upCollapsedContentTransPro = getUpCollapsedContentTransPro();
        transCollapsedContentByPro(upCollapsedContentTransPro);

        if (mProgressUpdateListener!=null){
            mProgressUpdateListener.onUpAlphaScaleProUpdate(upAlphaScalePro);
            mProgressUpdateListener.onUpAlphaGradientProUpdate(upAlphaGradientPro);
            mProgressUpdateListener.onDownCollapsedAlphaProUpdate(downCollapsedAlphaPro);
            mProgressUpdateListener.onDownContentAlphaProUpdate(downContentAlphaPro);
            mProgressUpdateListener.onDownShopBarTransProUpdate(downShopBarTransPro);
            mProgressUpdateListener.onUpCollapsedContentTransProUpdate(upCollapsedContentTransPro);
        }

    }

    /**
     * 根据upCollapsedContentTransPro,设置折叠内容位移
     */
    private void transCollapsedContentByPro(float upCollapsedContentTransPro) {
        float collapsedContentTranY = - (upCollapsedContentTransPro * (contentTransY - topBarHeight));
        translation(mCollapsedContent,collapsedContentTranY);
    }

    /**
     * 根据downShopBarTransPro,设置购物内容内容位移
     */
    private void transShopBarByPro(float downShopBarTransPro) {
        float shopBarTransY = (1-downShopBarTransPro) * shopBarHeight;
        translation(mShopBar,shopBarTransY);
    }

    /**
     * 根据downContentAlphaPro,设置滑动内容、收起按钮的透明度
     */
    private void alphaContentByPro(float downContentAlphaPro) {
        setAlpha(mViewPager,downContentAlphaPro);
        setAlpha(mStl,downContentAlphaPro);
        setAlpha(mIvClose,1-downContentAlphaPro);
        if (mIvClose.getAlpha()==0){
            mIvClose.setVisibility(GONE);
        }else {
            mIvClose.setVisibility(VISIBLE);
        }
    }

    /**
     * 根据downCollapsedAlphaPro,设置折叠内容透明度
     */
    private void alphaCollapsedContentByPro(float downCollapsedAlphaPro) {
        setAlpha(mClCollapsedHeader,downCollapsedAlphaPro);
        setAlpha(mRvCollapsed,1 - downCollapsedAlphaPro);
    }

    /**
     * 根据upAlphaGradientPro,设置topBar背景、返回icon、拼团icon颜色渐变值，搜索框透明度
     */
    private void alphaGradientByPro(float upAlphaGradientPro) {
        setAlpha(mTvSearch, upAlphaGradientPro);
        int iconColor = (int) iconArgbEvaluator.evaluate(
                upAlphaGradientPro,
                getContext().getResources().getColor(R.color.white),
                getContext().getResources().getColor(R.color.black)
        );
        int topBarColor = (int) topBarArgbEvaluator.evaluate(
                upAlphaGradientPro,
                getContext().getResources().getColor(R.color.trans_white),
                getContext().getResources().getColor(R.color.white)
        );
        mTopBar.setBackgroundColor(topBarColor);
        mIvBack.getDrawable().mutate().setTint(iconColor);
        mIvAssemble.getDrawable().mutate().setTint(iconColor);
    }

    /**
     * 根据upAlphaScalePro,设置logo、收藏icon缩放，搜索icon、分享icon透明度
     */
    private void alphaScaleByPro(float upAlphaScalePro) {
        float alpha = 1 - upAlphaScalePro;
        float scale = 1 - upAlphaScalePro;
        setAlpha(mIvSearch, alpha);
        setAlpha(mIvShare, alpha);
        setScaleAlpha(mIvLogo, scale, scale, alpha);
        setScaleAlpha(mVCollect, scale, scale, alpha);
    }

    private float getDownContentAlphaPro() {
        return (downEndY - MathUtils.clamp(mLlContent.getTranslationY(), downContentAlphaY, downEndY)) / (downEndY - downContentAlphaY);
    }

    private float getDownCollapsedAlphaPro() {
        return (downCollapsedAlphaY - MathUtils.clamp(mLlContent.getTranslationY(), contentTransY, downCollapsedAlphaY)) / (downCollapsedAlphaY -contentTransY);
    }

    private float getDownShopBarTransPro() {
        return (downShopBarTransY - MathUtils.clamp(mLlContent.getTranslationY(), contentTransY, downShopBarTransY)) / (downShopBarTransY -contentTransY);
    }

    private float getUpAlphaGradientPro() {
        return (upAlphaScaleY - MathUtils.clamp(mLlContent.getTranslationY(), upAlphaGradientY, upAlphaScaleY)) / (upAlphaScaleY-upAlphaGradientY);
    }

    private float getUpAlphaScalePro() {
        return (contentTransY - MathUtils.clamp(mLlContent.getTranslationY(), upAlphaScaleY, contentTransY)) / (contentTransY-upAlphaScaleY);
    }

    private float getUpCollapsedContentTransPro() {
        return (contentTransY - MathUtils.clamp(mLlContent.getTranslationY(), topBarHeight, contentTransY)) / (contentTransY-topBarHeight);
    }

    private int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getContext().getResources().getDisplayMetrics());
    }

    private void setAlpha(View view, float alpha){
        view.setAlpha(alpha);
    }

    private void setScale(View view ,float scaleY,float scaleX){
        view.setScaleY(scaleY);
        view.setScaleX(scaleX);
    }
    private void setScaleAlpha(View view ,float scaleY,float scaleX,float alpha){
        setAlpha(view,alpha);
        setScale(view,scaleY,scaleX);
    }

    private void checkIvCloseVisi(){
        if (mLlContent.getTranslationY()< downContentAlphaY){
            mIvClose.setAlpha(0);
            mIvClose.setVisibility(GONE);
        }else {
            mIvClose.setAlpha(1);
            mIvClose.setVisibility(VISIBLE);
        }
    }

    private void translationByConsume(View view, float translationY, int[] consumed, float consumedDy) {
        consumed[1] = (int) consumedDy;
        view.setTranslationY(translationY);
    }

    private void translation(View view, float translationY) {
        view.setTranslationY(translationY);
    }

    public void restore(long dur){
        if (restoreOrExpandAnimator.isStarted()) {
            restoreOrExpandAnimator.cancel();
        }
        restoreOrExpandAnimator.setFloatValues(mLlContent.getTranslationY(), contentTransY);
        restoreOrExpandAnimator.setDuration(dur);
        restoreOrExpandAnimator.start();
    }

    public void expand(long dur){
        if (restoreOrExpandAnimator.isStarted()) {
            restoreOrExpandAnimator.cancel();
        }
        restoreOrExpandAnimator.setFloatValues(mLlContent.getTranslationY(), downEndY);
        restoreOrExpandAnimator.setDuration(dur);
        restoreOrExpandAnimator.start();
    }

    public void setProgressUpdateListener(ProgressUpdateListener progressUpdateListener) {
        this.mProgressUpdateListener = progressUpdateListener;
    }

    public interface ProgressUpdateListener{

        void onUpCollapsedContentTransProUpdate(float pro);

        void onUpAlphaScaleProUpdate(float pro);

        void onUpAlphaGradientProUpdate(float pro);

        void onDownCollapsedAlphaProUpdate(float pro);

        void onDownContentAlphaProUpdate(float pro);

        void onDownShopBarTransProUpdate(float pro);
    }
}

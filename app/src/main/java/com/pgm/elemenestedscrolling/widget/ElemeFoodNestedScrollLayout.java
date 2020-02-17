package com.pgm.elemenestedscrolling.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.math.MathUtils;
import android.support.v4.view.NestedScrollingParent2;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.pgm.elemenestedscrolling.R;


public class ElemeFoodNestedScrollLayout extends FrameLayout implements NestedScrollingParent2 {
    public static final long ANIM_DURATION_FRACTION = 200L;

    //shopBar部分
    private View mShopBar;

    //content部分
    private View mNestedScrollView;
    private View mTvComm;
    private View mTvGoodCommRate;
    private View mTvCommDetail;
    private View mTvCommCount;
    private View mVLine;
    private View mTvFoodDetail;

    //expand部分
    private View mIvExpand;

    //icon部分
    private View mIvShare;
    private View mIvClose;

    //mask部分
    private View mVMask;

    private NestedScrollingParentHelper mParentHelper;
    private ProgressUpdateListener mProgressUpdateListener;

    private ValueAnimator restoreOrExpandOrCloseAnimator;//收起或展开折叠内容时执行的动画
    private ValueAnimator reboundedAnim;//回弹动画

    private float shopBarHeight;//shopBar部分高度
    private float ivExpandHegiht;//ivExpand部分高度
    private float iconTransY;//分享、关闭icon初始化transY
    private float contentTransY;//滑动内容初始化TransY
    private float downFlingCutOffY;//下滑时fling上部分回弹临界值
    private float upEndIconTransY;//分享、关闭icon上滑最终transY

    public ElemeFoodNestedScrollLayout(@NonNull Context context) {
        this(context, null);
    }

    public ElemeFoodNestedScrollLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ElemeFoodNestedScrollLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mParentHelper = new NestedScrollingParentHelper(this);

        reboundedAnim = new ValueAnimator();
        reboundedAnim.setInterpolator(new DecelerateInterpolator());
        reboundedAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                translation(mNestedScrollView, (float) animation.getAnimatedValue());
                alphaTransView(mNestedScrollView.getTranslationY());
                if (mProgressUpdateListener!=null){
                    mProgressUpdateListener.onDownConetntCloseProUpdate(getDownConetntClosePro());
                }
            }
        });
        reboundedAnim.setDuration(ANIM_DURATION_FRACTION);

        restoreOrExpandOrCloseAnimator = new ValueAnimator();
        restoreOrExpandOrCloseAnimator.setInterpolator(new AccelerateInterpolator());
        restoreOrExpandOrCloseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                translation(mNestedScrollView, (float) animation.getAnimatedValue());
                alphaTransView(mNestedScrollView.getTranslationY());
                if (mProgressUpdateListener!=null){
                    mProgressUpdateListener.onDownConetntCloseProUpdate(getDownConetntClosePro());
                }
            }
        });
        restoreOrExpandOrCloseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                int alpha=mNestedScrollView.getTranslationY() >= getMeasuredHeight()?0:1;
                setAlpha(mIvClose,alpha);
                setAlpha(mIvShare,alpha);
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        shopBarHeight = getResources().getDimension(R.dimen.shop_bar_height);
        ivExpandHegiht = getResources().getDimension(R.dimen.iv_food_expand);
        contentTransY = getResources().getDimension(R.dimen.food_content_trans_y);
        iconTransY = getResources().getDimension(R.dimen.iv_food_icon_trans_y);
        //状态栏高度
        float statusBarHeight = getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"));
        downFlingCutOffY = contentTransY + dp2px(92);
        upEndIconTransY = statusBarHeight + dp2px(8);
        //因为开始就是关闭状态，设置Content部分的TransY为满屏高度
        mNestedScrollView.setTranslationY(getMeasuredHeight());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mNestedScrollView = findViewById(R.id.ns);
        mShopBar = findViewById(R.id.cl_food_shop_bar);

        mTvComm = findViewById(R.id.t_comm);
        mTvGoodCommRate = findViewById(R.id.t_good_comm_rate);
        mTvCommDetail = findViewById(R.id.t_comm_detail);
        mTvFoodDetail = findViewById(R.id.t_food_detail);

        mTvCommCount = findViewById(R.id.t_comm_count);
        mVLine = findViewById(R.id.view_line);

        mIvExpand = findViewById(R.id.iv_food_expand);
        mIvShare = findViewById(R.id.iv_small_share);
        mIvClose = findViewById(R.id.iv_small_close);
        mVMask = findViewById(R.id.v_mask);
    }

    /**
     * 释放资源
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (restoreOrExpandOrCloseAnimator.isStarted()) {
            restoreOrExpandOrCloseAnimator.cancel();
            restoreOrExpandOrCloseAnimator.removeAllUpdateListeners();
            restoreOrExpandOrCloseAnimator.removeAllListeners();
            restoreOrExpandOrCloseAnimator = null;
        }

        if (reboundedAnim.isStarted()) {
            reboundedAnim.cancel();
            reboundedAnim.removeAllUpdateListeners();
            restoreOrExpandOrCloseAnimator = null;
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
        if (velocityY<0) {
            float translationY = target.getTranslationY();
            float dy = translationY - velocityY;

            //从展开状态下滑时处理回弹Fling
            if (translationY >= 0 && translationY <= downFlingCutOffY){
                if (dy<contentTransY){
                    reboundedAnim.setFloatValues(translationY,dy);
                }else if (dy>contentTransY&&dy<downFlingCutOffY){
                    reboundedAnim.setFloatValues(translationY,dy,contentTransY);
                }else {
                    reboundedAnim.setFloatValues(translationY,downFlingCutOffY,contentTransY);
                }
                target.scrollTo(0,0);
                reboundedAnim.start();
                return true;
            }

            //从初始状态到关闭状态下滑百分比超过50%惯性滑动关闭
            float dur = (1- getDownConetntClosePro()) * ANIM_DURATION_FRACTION;
            if (translationY<=(getMeasuredHeight()/2f)&&translationY>downFlingCutOffY){
                restore((long) dur);
                return true;
            }else {
                close((long) dur);
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
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL && target.getId() == R.id.ns;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mParentHelper.onStopNestedScroll(target, type);
        float translationY = target.getTranslationY();
        if (translationY == contentTransY|| reboundedAnim.isStarted()|| restoreOrExpandOrCloseAnimator.isStarted()) {
            return;
        }

        long dur;
        if (translationY < contentTransY) {
            if (getUpExpandTransPro() <= 0.5f) {
                dur = (long) (getUpExpandTransPro() *  ANIM_DURATION_FRACTION);
                restore(dur);
            } else {
                dur = (long) ((1 - getUpExpandTransPro()) *  ANIM_DURATION_FRACTION);
                expand(dur);
            }
        } else {
            if (getDownConetntClosePro() >= 0.5f) {
                dur = (long) (getDownConetntClosePro() *  ANIM_DURATION_FRACTION);
                close(dur);
            } else {
                dur = (long) ((1 - getDownConetntClosePro()) *  ANIM_DURATION_FRACTION);
                restore(dur);
            }
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {

    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        float contentTransY = target.getTranslationY() - dy;
        //处理上滑
        if (dy > 0) {
            if (contentTransY >= 0) {
                translationByConsume(target, contentTransY, consumed, dy);
            } else {
                translationByConsume(target, 0, consumed, (target.getTranslationY() - 0));
            }
        }

        //处理下滑
        if (dy < 0 && !target.canScrollVertically(-1)) {
            if (contentTransY >= 0 && contentTransY < getMeasuredHeight()) {
                translationByConsume(target, contentTransY, consumed, dy);
            } else {
                translationByConsume(target, getMeasuredHeight(), consumed, getMeasuredHeight() - target.getTranslationY());
            }
        }

        alphaTransView(contentTransY);

        if (mProgressUpdateListener!=null){
            mProgressUpdateListener.onDownConetntCloseProUpdate(getDownConetntClosePro());
        }
    }

    private void alphaTransView(float transY) {
        float upCollapseTransPro = getUpExpandTransPro();
        //位移购物内容
        float shopBarTransY = (1 - upCollapseTransPro) * shopBarHeight;
        translation(mShopBar, shopBarTransY);

        //设置商品信息View的透明度变化
        setAlpha(mTvComm, upCollapseTransPro);
        setAlpha(mTvGoodCommRate, upCollapseTransPro);
        setAlpha(mTvCommDetail, upCollapseTransPro);
        setAlpha(mTvFoodDetail, upCollapseTransPro);
        setAlpha(mTvCommCount, 1 - upCollapseTransPro);
        setAlpha(mVLine, 1 - upCollapseTransPro);

        //位移share close两个Icon，设置展开icon透明度
        if (transY <= contentTransY) {
            float ivExpandUpTransY = upCollapseTransPro * -contentTransY;
            translation(mIvExpand, ivExpandUpTransY);
            setAlpha(mIvExpand, 1 - upCollapseTransPro);

            float iconTransY = upEndIconTransY + (1 - upCollapseTransPro) * (this.iconTransY - upEndIconTransY);
            translation(mIvShare, iconTransY);
            translation(mIvClose, iconTransY);

        } else if (transY > contentTransY && transY <= getMeasuredHeight()) {
            float ivExpandDowndTransY = (1 - getDownIvExpnadPro()) * ivExpandHegiht;
            translation(mIvExpand, ivExpandDowndTransY);

            float iconTransY = transY + dp2px(16);
            translation(mIvShare, iconTransY);
            translation(mIvClose, iconTransY);
        }
    }

    private void translationByConsume(View view, float translationY, int[] consumed, float consumedDy) {
        consumed[1] = (int) consumedDy;
        view.setTranslationY(translationY);
    }

    private void translation(View view, float translationY) {
        view.setTranslationY(translationY);
    }

    private void setAlpha(View view, float alpha) {
        view.setAlpha(alpha);
    }

    private float getDownConetntClosePro() {
        return (mNestedScrollView.getTranslationY() - contentTransY) / (getMeasuredHeight() - contentTransY);
    }

    private float getDownIvExpnadPro() {
        return ((contentTransY+ivExpandHegiht)- MathUtils.clamp(mNestedScrollView.getTranslationY(), contentTransY, contentTransY+ivExpandHegiht)) / ivExpandHegiht;
    }

    private float getUpExpandTransPro() {
        return (contentTransY - MathUtils.clamp(mNestedScrollView.getTranslationY(), 0, contentTransY)) / contentTransY;
    }

    public void restore(long dur) {
        mIvClose.setClickable(true);
        mVMask.setClickable(true);
        mIvExpand.setClickable(true);
        if (restoreOrExpandOrCloseAnimator.isStarted()) {
            restoreOrExpandOrCloseAnimator.cancel();
        }
        restoreOrExpandOrCloseAnimator.setFloatValues(mNestedScrollView.getTranslationY(), contentTransY);
        restoreOrExpandOrCloseAnimator.setDuration(dur);
        restoreOrExpandOrCloseAnimator.start();
    }

    public void expand(long dur) {
        if (restoreOrExpandOrCloseAnimator.isStarted()) {
            restoreOrExpandOrCloseAnimator.cancel();
        }
        restoreOrExpandOrCloseAnimator.setFloatValues(mNestedScrollView.getTranslationY(), 0);
        restoreOrExpandOrCloseAnimator.setDuration(dur);
        restoreOrExpandOrCloseAnimator.start();
    }

    public void close(long dur) {
        mNestedScrollView.scrollTo(0,0);
        mIvClose.setClickable(false);
        mVMask.setClickable(false);
        mIvExpand.setClickable(false);
        if (restoreOrExpandOrCloseAnimator.isStarted()) {
            restoreOrExpandOrCloseAnimator.cancel();
        }
        restoreOrExpandOrCloseAnimator.setFloatValues(mNestedScrollView.getTranslationY(), getMeasuredHeight());
        restoreOrExpandOrCloseAnimator.setDuration(dur);
        restoreOrExpandOrCloseAnimator.start();
    }


    private float dp2px(float dpVal) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getContext().getResources().getDisplayMetrics());
    }

    public void setProgressUpdateListener(ProgressUpdateListener progressUpdateListener) {
        this.mProgressUpdateListener = progressUpdateListener;
    }

    public interface ProgressUpdateListener{
        void onDownConetntCloseProUpdate(float pro);
    }

}

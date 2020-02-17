package com.pgm.elemenestedscrolling.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


public class CouponView extends View {
    private Paint mPaint;

    public CouponView(Context context) {
        this(context,null);
    }

    public CouponView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public CouponView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        int radius = getWidth() / 2;
        canvas.drawCircle(radius,0,radius,mPaint);
        canvas.drawCircle(radius,getHeight(),radius,mPaint);
        canvas.restore();
    }
}

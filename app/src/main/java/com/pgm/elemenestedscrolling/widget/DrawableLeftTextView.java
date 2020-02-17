package com.pgm.elemenestedscrolling.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;


public class DrawableLeftTextView extends AppCompatTextView {

    public DrawableLeftTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawableLeftTextView(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
    }

    public DrawableLeftTextView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable[] drawables = getCompoundDrawables();
        Drawable drawableLeft = drawables[0];
        if (drawableLeft != null) {
            float textWidth = getPaint().measureText(getText().toString());
            int drawablePadding = getCompoundDrawablePadding();
            int drawableWidth;
            drawableWidth = drawableLeft.getIntrinsicWidth();
            float bodyWidth = textWidth + drawableWidth + drawablePadding;
            canvas.translate((getWidth() - bodyWidth) / 2, 0);
        }
        super.onDraw(canvas);
    }
}

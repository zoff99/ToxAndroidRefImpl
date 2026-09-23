package com.zoffcc.applications.trifa;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * ScrollView that never grows taller than a max height.
 * This guarantees that content placed BELOW it (our button bar)
 * always stays on screen, even with huge OS font sizes.
 */
public class MaxHeightScrollView extends ScrollView
{
    private int mMaxHeight = 0;

    public MaxHeightScrollView(Context context) { super(context); }
    public MaxHeightScrollView(Context context, AttributeSet attrs) { super(context, attrs); }
    public MaxHeightScrollView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    public void setMaxHeight(int px) { mMaxHeight = px; }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        if (mMaxHeight > 0)
        {
            int mode = MeasureSpec.getMode(heightMeasureSpec);
            int size = MeasureSpec.getSize(heightMeasureSpec);
            int cap = (mode == MeasureSpec.UNSPECIFIED) ? mMaxHeight : Math.min(size, mMaxHeight);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(cap, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

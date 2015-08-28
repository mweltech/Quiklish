package com.example.maw.quiklish;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by maw on 17/08/2015.
 */
public class SizeableVideo extends VideoView {

    private int mForceHeight = 100;
    private int mForceWidth = 100;

    public SizeableVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDimensions(int w, int h) {
        this.mForceHeight = h;
        this.mForceWidth = w;
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        //setMeasuredDimension(720,460);
        setMeasuredDimension(mForceWidth,mForceHeight);
    }

}

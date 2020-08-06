package com.handySiddur.bracha;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

/**
 * Text view with pinch-to-zoom and double-tap zoom ability. For scrolling wrap
 * it with CustomScrollView.
 *
 * @author lecho
 *
 */
public class ZoomTextView extends TextView {

    private static final float MIN_SCALE_FACTOR = 1.0f;
    private static final float MID_SCALE_FACTOR = 1.2f;
    private static final float MID_MAX_SCALE_FACTOR = 1.4f;
    private static final float MAX_SCALE_FACTOR = 1.6f;
    private TVListener listener;


    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mTapDetector;
    private float mScaleFactor = MIN_SCALE_FACTOR;
    private boolean mFullZoom = false;
    private float mOriginalFontSize;
    public Context context;

    public ZoomTextView(Context context) {
        this(context, null, 0);
        this.context = context;

    }

    public ZoomTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public ZoomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mTapDetector = new GestureDetector(context, new TapListener());
        this.context = context;
        mOriginalFontSize = getTextSize();
        SharedPreferences prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        mScaleFactor = prefs.getFloat("text_zoom", 1.0f);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, mScaleFactor * mOriginalFontSize);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mTapDetector.onTouchEvent(event);
        return true;
    }

    public void setListener(TVListener listener) {
        this.listener = listener;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(mScaleFactor, MAX_SCALE_FACTOR));
            if (mScaleFactor == MAX_SCALE_FACTOR) {
                mFullZoom = true;
            } else {
                mFullZoom = false;
            }

            setTextSize(TypedValue.COMPLEX_UNIT_PX, mScaleFactor * mOriginalFontSize);
            invalidate();
            return true;
        }
    }

    private class TapListener extends SimpleOnGestureListener {


        @Override
        public void onLongPress(MotionEvent e) {
           if (listener != null)
               listener.longPressed();;
        }


        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mScaleFactor == MAX_SCALE_FACTOR) {
                mScaleFactor = MIN_SCALE_FACTOR;
                mFullZoom = false;
            } else if (mScaleFactor == MIN_SCALE_FACTOR){
                mScaleFactor = MID_SCALE_FACTOR;
            }
            else if (mScaleFactor == MID_SCALE_FACTOR) {
                mScaleFactor = MID_MAX_SCALE_FACTOR;
                //mFullZoom = true;
            }
            else {
                mScaleFactor = MAX_SCALE_FACTOR;
            }
            SharedPreferences prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
            prefs.edit().putFloat("text_zoom", mScaleFactor).apply();
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mScaleFactor * mOriginalFontSize);
            return true;
        }
    }

    private interface TVListener {
        void longPressed();
    }

}
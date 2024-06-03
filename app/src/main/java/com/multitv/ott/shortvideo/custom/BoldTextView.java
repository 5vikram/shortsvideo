package com.multitv.ott.shortvideo.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class BoldTextView extends AppCompatTextView {

    public BoldTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoldTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/bold.ttf");
        setTypeface(tf);
    }

}
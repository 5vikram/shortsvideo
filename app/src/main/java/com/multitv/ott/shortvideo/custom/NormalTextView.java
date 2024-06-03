package com.multitv.ott.shortvideo.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class NormalTextView extends AppCompatTextView {

    public NormalTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public NormalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NormalTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/book.ttf");
        setTypeface(tf);
    }

}
package com.multitv.ott.shortvideo.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class MediumEditText extends AppCompatEditText {

    public MediumEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MediumEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MediumEditText(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/medium.ttf");
        setTypeface(tf);
    }

}
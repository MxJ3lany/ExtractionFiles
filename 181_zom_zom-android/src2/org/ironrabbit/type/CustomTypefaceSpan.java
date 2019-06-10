package org.ironrabbit.type;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

public class CustomTypefaceSpan extends TypefaceSpan {

    private final Typeface newType;

    public CustomTypefaceSpan(String family, Context context) {
        super(family);
        newType = CustomTypefaceManager.getCurrentTypeface(context);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        applyCustomTypeFace(ds, newType);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        applyCustomTypeFace(paint, newType);
    }

    private static void applyCustomTypeFace(Paint paint, Typeface tf) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }

	if (tf != null)
	{
        	int fake = oldStyle & tf.getStyle();
        	if ((fake & Typeface.BOLD) != 0) {
            		paint.setFakeBoldText(true);
        	}

        	if ((fake & Typeface.ITALIC) != 0) {
            		paint.setTextSkewX(-0.25f);
        	}

        	paint.setTypeface(tf);
	}
    }
}

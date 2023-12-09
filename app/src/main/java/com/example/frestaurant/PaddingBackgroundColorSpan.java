package com.example.frestaurant;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PaddingBackgroundColorSpan extends ReplacementSpan {
    private int mBackgroundColor;
    private int mPadding;

    public PaddingBackgroundColorSpan(int backgroundColor, int padding) {
        this.mBackgroundColor = backgroundColor;
        this.mPadding = padding;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        RectF rect = new RectF(x, top, x + measureText(paint, text, start, end), bottom);
        paint.setColor(mBackgroundColor);
        canvas.drawRect(rect, paint);
        paint.setColor(Color.BLACK); // 设置文字颜色
        canvas.drawText(text, start, end, x + mPadding, y, paint); // 添加padding
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end)) + mPadding * 2;
    }

    private float measureText(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }
}

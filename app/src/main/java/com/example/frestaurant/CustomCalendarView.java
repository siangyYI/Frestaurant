package com.example.frestaurant;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.CalendarView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CustomCalendarView extends CalendarView {

    private Map<String, Boolean> dateHasDataMap = new HashMap<>();
    private Paint eventPaint;

    public CustomCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        setWillNotDraw(false);
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                return true;
            case MotionEvent.ACTION_DOWN:
                return false;
            case MotionEvent.ACTION_UP:
                performClick();
                return false;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void initPaint() {
        eventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eventPaint.setColor(Color.RED);
        eventPaint.setStyle(Paint.Style.FILL);
        invalidate();
    }

    public void setDateHasDataMap(Map<String, Boolean> dateHasDataMap) {
        this.dateHasDataMap.clear();
        this.dateHasDataMap.putAll(dateHasDataMap);
        invalidate();
    }

    public void clearEventsForNewMonth() {
        dateHasDataMap.clear();
        invalidate();
    }

    public void setCurrentMonth(int year, int month) {
        String monthKey = String.format("%04d-%02d", year, month + 1);
        boolean monthChanged = true;

        for (String dateKey : dateHasDataMap.keySet()) {
            if (dateKey.startsWith(monthKey)) {
                monthChanged = false;
                break;
            }
        }

        if (monthChanged) {
            dateHasDataMap.clear();
            invalidate();
        }
    }
    public void changeMonth(int delta) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getDate()); // 获取当前日历的时间
        calendar.add(Calendar.MONTH, delta); // 增加或减少月份
        setDate(calendar.getTimeInMillis(), true, true); // 设置新的时间并触发视图更新
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int headerHeight = getHeaderHeight();
        int height = getHeight() - headerHeight;
        int cellHeight = height / 6;
        int circleRadius = 10;
        float dotSpacing = 140;

        for (Map.Entry<String, Boolean> entry : dateHasDataMap.entrySet()) {
            String date = entry.getKey();
            Boolean hasEvent = entry.getValue();

            if (hasEvent != null && hasEvent) {
                int year = Integer.parseInt(date.substring(0, 4));
                int month = Integer.parseInt(date.substring(5, 7)) - 1;
                int dayOfMonth = Integer.parseInt(date.substring(8, 10));
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);

                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek();
                dayOfWeek = (dayOfWeek < 0) ? (dayOfWeek + 7) : dayOfWeek;
                int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH) - 1;

                int column = dayOfWeek;
                int row = weekOfMonth;
                float startX = getPaddingLeft() + dotSpacing / 2;

                float centerX = (startX + column * dotSpacing);
                centerX += 50;

                float additionalGap = 50;
                float centerY = (row * cellHeight + getHeaderHeight()) + cellHeight - circleRadius - additionalGap;

                if (row == 2) {
                    centerY -= 340;
                }
                if (row == 1) {
                    centerY -= 170;
                }
                if (row == 3) {
                    centerY -= 510;
                }
                if (row == 4) {
                    centerY -= 680;
                }

                eventPaint.setColor(Color.RED);
                canvas.drawCircle(centerX, centerY, circleRadius, eventPaint);
            }
        }
    }

    private int getHeaderHeight() {
        return getHeight() / 12;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
}

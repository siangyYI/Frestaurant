package com.example.frestaurant;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

public class CustomTimePicker extends LinearLayout {
    private NumberPicker hourPicker, minutePicker;

    private String[] hours;
    private String[] minutes;

    // 其他代码

    public CustomTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.custom_time_picker, this);

        hourPicker = findViewById(R.id.hourPicker);
        minutePicker = findViewById(R.id.minutePicker);

        hours = new String[]{"00", "01", "02", "03","04","05","06","07","08","09","10","12","13","14","15","16","17","18","19","20","21","22","23"};
        minutes = new String[]{"00", "30"};

        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(hours.length - 1);
        hourPicker.setDisplayedValues(hours);

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(minutes.length - 1);
        minutePicker.setDisplayedValues(minutes);
        hourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                triggerTimeChanged(hourPicker.getValue(), minutePicker.getValue());
            }
        });
        minutePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                triggerTimeChanged(hourPicker.getValue(), minutePicker.getValue());
            }
        });
    }

    private OnTimeChangedListener onTimeChangedListener;

    public interface OnTimeChangedListener {
        void onTimeChanged(CustomTimePicker view, int hourOfDay, int minute);
    }

    public void setOnTimeChangedListener(OnTimeChangedListener listener) {
        this.onTimeChangedListener = listener;
    }

    private void triggerTimeChanged(int hourOfDay, int minute) {
        if (onTimeChangedListener != null) {
            onTimeChangedListener.onTimeChanged(this, hourOfDay, minute);
        }
    }
}


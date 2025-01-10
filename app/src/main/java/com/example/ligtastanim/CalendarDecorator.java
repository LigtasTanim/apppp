package com.example.ligtastanim;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.CalendarView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarDecorator {
    private CalendarView calendarView;
    private List<Date> wateringDates;
    private Drawable decorator;

    public CalendarDecorator(CalendarView calendarView, List<Date> wateringDates) {
        this.calendarView = calendarView;
        this.wateringDates = wateringDates;
        this.decorator = new ColorDrawable(Color.GREEN);
    }

    public void decorate() {
        Calendar calendar = Calendar.getInstance();
        for (Date date : wateringDates) {
            calendar.setTime(date);
            // Add visual indicator for this date
            calendarView.setBackgroundColor(Color.GREEN);
        }
    }
}
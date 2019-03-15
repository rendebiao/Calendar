package com.rdb.calendar.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.rdb.calendar.CalendarEnableController;
import com.rdb.calendar.CalendarSelectListener;
import com.rdb.calendar.CalendarSelectMode;
import com.rdb.calendar.CalendarView;
import com.rdb.calendar.DefaultCalendarPainter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private CalendarSelectMode selectMode;
    private DefaultCalendarPainter calendarPainter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        getSupportActionBar().setElevation(0);
        selectMode = (CalendarSelectMode) getIntent().getSerializableExtra("selectMode");
        calendarView = findViewById(R.id.calendarView);
        float density = getResources().getDisplayMetrics().density;
        int themeColor = getResources().getColor(R.color.colorAccent);
        calendarPainter = new DefaultCalendarPainter(themeColor, 14 * density, 0xff333333, 14 * density, 0xff333333, 14 * density, 0xff333333, 14 * density, themeColor, 0xff333333, 0x80333333, 0x80666666, 14 * density, themeColor);
        calendarPainter.setScale(1);
        calendarView.setCalendarPainter(calendarPainter);
        calendarView.setSelectMode(selectMode);
        calendarView.setEnableController(new CalendarEnableController() {
            @Override
            public boolean isEnable(CalendarSelectMode selectMode, long time) {
                return time > System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS;
            }
        });
        int lineColor = 0x40808080;
        calendarView.setGridLineColor(lineColor);
        calendarView.setWeekLineColor(lineColor);
        calendarView.setHeadLineColor(lineColor);
        calendarView.setStartWeek(Calendar.MONDAY);
        calendarView.setSelectListener(new CalendarSelectListener() {
            @Override
            protected void onDaySelected(long dayTime) {
                super.onDaySelected(dayTime);
                Toast.makeText(CalendarActivity.this, "onDaySelected:" + dateFormat.format(new Date(dayTime)), Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onWeekSelected(long weekTime) {
                super.onWeekSelected(weekTime);
                Toast.makeText(CalendarActivity.this, "onWeekSelected:" + dateFormat.format(new Date(weekTime)), Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onRangeSelected(long startDay, long endDay) {
                super.onRangeSelected(startDay, endDay);
                Toast.makeText(CalendarActivity.this, "onRangeSelected:" + dateFormat.format(new Date(startDay)) + "->" + dateFormat.format(new Date(endDay)), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

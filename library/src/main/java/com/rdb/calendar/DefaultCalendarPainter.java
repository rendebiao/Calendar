package com.rdb.calendar;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DefaultCalendarPainter implements CalendarPainter {

    private final static String[] MONTHS = {"一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"};
    private Calendar calendar;
    private float scale = 1.0f;
    private int disenableDayColor;
    private int curMonthDayTextColor;
    private int otherMonthDayTextColor;
    private int curDayColor;
    private int yearTextColor;
    private int monthTextColor;
    private float headTextSize;
    private float weekNameTextSize;
    private float yearTextSize;
    private float monthTextSize;
    private float dayTextSize;
    private RectF clickRectF = new RectF();
    private Paint yearTextPaint = new Paint();
    private Paint dayTextPaint = new Paint();
    private Paint headTextPaint = new Paint();
    private Paint backgroundPaint = new Paint();
    private Paint weekNameTextPaint = new Paint();
    private SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy年MM月");
    private SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Paint monthTextPaint = new Paint();

    public DefaultCalendarPainter(int headTextColor, float headTextSize, int weekNameTextColor, float weekNameTextSize, int yearTextColor, float yearTextSize, int monthTextColor, float monthTextSize, int curDayColor, int curMonthDayTextColor, int otherMonthDayTextColor, int disenableDayColor, float dayTextSize, int selectBgColor) {
        yearTextPaint.setAntiAlias(true);
        monthTextPaint.setAntiAlias(true);
        dayTextPaint.setAntiAlias(true);
        weekNameTextPaint.setAntiAlias(true);
        weekNameTextPaint.setColor(weekNameTextColor);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(selectBgColor);
        headTextPaint.setAntiAlias(true);
        headTextPaint.setColor(headTextColor);
        this.yearTextColor = yearTextColor;
        this.monthTextColor = monthTextColor;
        this.curDayColor = curDayColor;
        this.disenableDayColor = disenableDayColor;
        this.curMonthDayTextColor = curMonthDayTextColor;
        this.otherMonthDayTextColor = otherMonthDayTextColor;
        this.headTextSize = headTextSize;
        this.yearTextSize = yearTextSize;
        this.monthTextSize = monthTextSize;
        this.dayTextSize = dayTextSize;
        this.weekNameTextSize = weekNameTextSize;
        calendar = Calendar.getInstance();
    }

    @Override
    public void onYearDraw(Canvas canvas, RectF rectF, int year, boolean selected) {
        yearTextPaint.setTextSize(yearTextSize * scale);
        yearTextPaint.setColor(selected ? Color.WHITE : yearTextColor);
        if (selected) {
            canvas.drawCircle(rectF.centerX(), rectF.centerY(), yearTextPaint.getTextSize() * 2, backgroundPaint);
        }
        drawTextInCenter(canvas, rectF, String.valueOf(year), yearTextPaint);
    }

    @Override
    public void onMonthDraw(Canvas canvas, RectF rectF, int month, boolean selected) {
        monthTextPaint.setTextSize(monthTextSize * scale);
        monthTextPaint.setColor(selected ? Color.WHITE : monthTextColor);
        if (selected) {
            canvas.drawCircle(rectF.centerX(), rectF.centerY(), monthTextPaint.getTextSize() * 2, backgroundPaint);
        }
        drawTextInCenter(canvas, rectF, MONTHS[month], monthTextPaint);
    }

    @Override
    public RectF onHeadDraw(Canvas canvas, RectF rectF, long curMonth, CalendarSelectMode selectMode, long selectTime1, long selectTime2) {
        calendar.setTimeInMillis(curMonth);
        headTextPaint.setTextSize(headTextSize * scale);
        StringBuffer selectText = new StringBuffer();
        String monthText = monthFormat.format(new Date(curMonth));
        Paint.FontMetrics fontMetrics = headTextPaint.getFontMetrics();
        float baseline = (rectF.height() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        float padding = rectF.width() / 21;
        if (selectMode == CalendarSelectMode.DAY) {
            if (selectTime1 > 0) {
                selectText.append("日：");
                selectText.append(dayFormat.format(new Date(selectTime1)));
            }
        } else if (selectMode == CalendarSelectMode.WEEK) {
            if (selectTime1 > 0) {
                selectText.append("周：");
                selectText.append(dayFormat.format(new Date(selectTime1)) + "/" + dayFormat.format(new Date(selectTime2)));
            }
        } else if (selectMode == CalendarSelectMode.RANGE) {
            if (selectTime1 > 0) {
                selectText.append("范围：");
                selectText.append(dayFormat.format(new Date(selectTime1)));
                selectText.append("/");
                if (selectTime2 > 0) {
                    selectText.append(dayFormat.format(new Date(selectTime2)));
                } else {
                    selectText.append("?");
                }
            }
        }
        if (selectText.length() > 0) {
            String text = selectText.toString();
            float monthWidth = headTextPaint.measureText(monthText);
            float selectWidth = headTextPaint.measureText(text);
            canvas.drawText(monthText, rectF.left + padding, rectF.top + baseline, headTextPaint);
            canvas.drawText(text, rectF.right - padding - selectWidth, rectF.top + baseline, headTextPaint);
            clickRectF.set(rectF.left, rectF.top, rectF.left + monthWidth + padding * 2, rectF.bottom);
        } else {
            float monthWidth = headTextPaint.measureText(monthText);
            float x = rectF.left + (rectF.width() - monthWidth) / 2;
            canvas.drawText(monthText, x, rectF.top + baseline, headTextPaint);
            clickRectF.set(x - padding, rectF.top, x + monthWidth + padding, rectF.bottom);
        }
        return clickRectF;
    }

    @Override
    public void onWeekNameDraw(Canvas canvas, RectF rectF, String[] weekdays) {
        float width = rectF.width() / weekdays.length;
        float textWidth;
        weekNameTextPaint.setTextSize(weekNameTextSize * scale);
        for (int i = 0; i < weekdays.length; i++) {
            String text = weekdays[i];
            textWidth = weekNameTextPaint.measureText(text);
            Paint.FontMetrics fontMetrics = weekNameTextPaint.getFontMetrics();
            float baseline = (rectF.height() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
            canvas.drawText(text, rectF.left + i * width + (width - textWidth) / 2, rectF.top + baseline, weekNameTextPaint);
        }
    }

    @Override
    public void onWeekDraw(Canvas canvas, long time, RectF rectF, boolean beforeDayDraw, CalendarWeekState weekState, boolean scroll) {

    }

    @Override
    public void onDayDraw(Canvas canvas, long time, RectF rectF, boolean isCurMonth, boolean isToday, CalendarDayState dayState, boolean scroll) {
        dayTextPaint.setTextSize(dayTextSize * scale);
        calendar.setTimeInMillis(time);
        String text = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if (dayState == CalendarDayState.DISENABLE) {
            dayTextPaint.setColor(disenableDayColor);
        } else if (dayState == CalendarDayState.ENABLE) {
            if (isToday) {
                dayTextPaint.setColor(curDayColor);
            } else if (isCurMonth) {
                dayTextPaint.setColor(curMonthDayTextColor);
            } else {
                dayTextPaint.setColor(otherMonthDayTextColor);
            }
        } else {
            if (dayState == CalendarDayState.SELECTED_RANGE_START || dayState == CalendarDayState.SELECTED_WEEK_START) {
                text = "始";
            } else if (dayState == CalendarDayState.SELECTED_RANGE_END || dayState == CalendarDayState.SELECTED_WEEK_END) {
                text = "终";
            } else if (dayState == CalendarDayState.SELECTED_RANGE_START_END) {
                text = "始终";
            }
            dayTextPaint.setColor(Color.WHITE);
            canvas.drawCircle(rectF.centerX(), rectF.centerY(), dayTextPaint.getTextSize(), backgroundPaint);
        }
        drawTextInCenter(canvas, rectF, text, dayTextPaint);
    }

    private void drawTextInCenter(Canvas canvas, RectF rectF, String text, Paint paint) {
        float textWidth = paint.measureText(text);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float baseline = (rectF.height() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        canvas.drawText(text, rectF.left + (rectF.width() - textWidth) / 2, rectF.top + baseline, paint);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}

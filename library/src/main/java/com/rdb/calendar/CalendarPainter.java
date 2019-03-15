package com.db.core.widget.calendar;

import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * Created by DB on 2018/3/29.
 */
public interface CalendarPainter {

    void onYearDraw(Canvas canvas, RectF rectF, int year, boolean selected);

    void onMonthDraw(Canvas canvas, RectF rectF, int month, boolean selected);

    RectF onHeadDraw(Canvas canvas, RectF rectF, long curMonth, CalendarSelectMode selectMode, long selectTime1, long selectTime2);

    void onWeekNameDraw(Canvas canvas, RectF rectF, String[] weekdays);

    void onWeekDraw(Canvas canvas, long time, RectF rectF, boolean beforeDayDraw, CalendarWeekState weekState, boolean scroll);

    void onDayDraw(Canvas canvas, long time, RectF rectF, boolean isCurMonth, boolean isCurDay, CalendarDayState dayState, boolean scroll);
}

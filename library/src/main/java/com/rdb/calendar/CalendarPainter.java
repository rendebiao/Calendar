package com.rdb.calendar;

import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.List;

public interface CalendarPainter {

    void onYearDraw(Canvas canvas, RectF rectF, int year, boolean selected);

    void onMonthDraw(Canvas canvas, RectF rectF, int month, boolean selected);

    RectF onHeadDraw(Canvas canvas, RectF rectF, long curMonth, CalendarSelectMode selectMode, List<Long> selectTimes);

    void onWeekNameDraw(Canvas canvas, RectF rectF, String[] weekdays);

    void onWeekDraw(Canvas canvas, long time, RectF rectF, boolean beforeDayDraw, CalendarWeekState weekState, boolean scroll);

    void onDayDraw(Canvas canvas, long time, RectF rectF, boolean isCurMonth, boolean isCurDay, CalendarDayState dayState, boolean scroll);
}

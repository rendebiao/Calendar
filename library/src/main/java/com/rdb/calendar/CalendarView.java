package com.db.core.widget.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.ViewAnimator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarView extends ViewAnimator {

    private static final int YEAR_COUNT = 100;
    private static final String[] WEEKS = new String[]{"日", "一", "二", "三", "四", "五", "六"};
    private int startWeek = 1;
    private int gridLineColor = Color.TRANSPARENT;
    private int weekLineColor = Color.TRANSPARENT;
    private int headLineColor = Color.TRANSPARENT;
    private long curDayTime;
    private long firstSelectTime;
    private long secondSelectTime;
    private Month curMonth;
    private Calendar calendar;
    private Paint linePaint = new Paint();
    private String[] curWeeks = WEEKS.clone();
    private DayGridAdapter dayGridAdapter;
    private LinearLayout dayLayout;
    private HeadView headView;
    private WeekNameView weekNameView;
    private DayGridView dayGridView;
    private YearGridView yearGridView;
    private MonthGridView monthGridView;
    private CalendarPainter calendarPainter;
    private CalendarSelectListener selectListener;
    private CalendarEnableController enableController;
    private OnMonthShowListener monthShowListener;
    private List<Long> selectDayTime = new ArrayList<>();
    private CalendarSelectMode selectMode = CalendarSelectMode.NONE;

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        linePaint.setAntiAlias(true);
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        curDayTime = calendar.getTimeInMillis();
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(200);
        alphaAnimation.setStartOffset(200);
        setInAnimation(alphaAnimation);
        alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(200);
        setOutAnimation(alphaAnimation);

        dayGridAdapter = new DayGridAdapter(calendar, YEAR_COUNT);
        curMonth = dayGridAdapter.getMonth(dayGridAdapter.initPosition);
        dayLayout = new LinearLayout(context);
        dayLayout.setOrientation(LinearLayout.VERTICAL);
        headView = new HeadView(context);
        weekNameView = new WeekNameView(context);
        dayGridView = new DayGridView(context);
        yearGridView = new YearGridView(context);
        monthGridView = new MonthGridView(context);
        int height = (int) (getResources().getDisplayMetrics().density * 36);
        dayLayout.addView(headView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        dayLayout.addView(weekNameView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        dayLayout.addView(dayGridView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(dayLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(yearGridView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(monthGridView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dayGridView.setAdapter(dayGridAdapter);
        dayGridView.scrollToPosition(curMonth.position);
        setDisplayedChild(0);
    }

    public void scrollToMonthByOffset(int monthOffset) {
        final int position = curMonth.position + monthOffset;
        if (position >= 0 && position < dayGridAdapter.getItemCount()) {
            if (Math.abs(monthOffset) == 1) {
                dayGridView.smoothScrollToPosition(position);
            } else {
                dayGridView.scrollToPositionEx(position);
            }
        }
    }

    public void scrollToMonth(int year, int month) {
        final int position = dayGridAdapter.getPosition(year, month);
        if (position != -1) {
            dayGridView.scrollToPositionEx(position);
        }
    }

    public void setSelectMode(@NonNull CalendarSelectMode selectMode) {
        this.selectMode = selectMode;
    }

    public void setCalendarPainter(CalendarPainter calendarPainter) {
        this.calendarPainter = calendarPainter;
    }

    public void setEnableController(CalendarEnableController enableController) {
        this.enableController = enableController;
    }

    public void setMonthShowListener(OnMonthShowListener monthShowListener) {
        this.monthShowListener = monthShowListener;
        if (monthShowListener != null) {
            monthShowListener.onMonthShow(curMonth.monthTime);
        }
    }

    public void setSelectListener(CalendarSelectListener selectListener) {
        this.selectListener = selectListener;
    }

    public void refresh() {
        dayGridAdapter.notifyDataSetChanged();
        weekNameView.postInvalidate();
    }

    public void setStartWeek(int startWeek) {
        this.startWeek = startWeek;
        for (int i = 0; i < curWeeks.length; i++) {
            curWeeks[i] = WEEKS[(startWeek + i + 6) % 7];
        }
        dayGridAdapter.clearCache();
        dayGridAdapter.notifyDataSetChanged();
        weekNameView.postInvalidate();
    }

    public void setHeadHeight(int headHeight) {
        setViewHeight(headView, headHeight);
    }

    public void setWeekNameHeight(int weekNameHeight) {
        setViewHeight(weekNameView, weekNameHeight);
    }

    private void setViewHeight(View view, int height) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        lp.height = height;
        view.setLayoutParams(lp);
    }

    public void setGridLineColor(int gridLineColor) {
        this.gridLineColor = gridLineColor;
        dayGridView.postInvalidate();
    }

    public void setWeekLineColor(int weekLineColor) {
        this.weekLineColor = weekLineColor;
        weekNameView.postInvalidate();
    }

    public void setHeadLineColor(int headLineColor) {
        this.headLineColor = headLineColor;
        headView.postInvalidate();
    }

    public interface OnMonthShowListener {
        void onMonthShow(long time);
    }

    class DrawView extends View implements View.OnClickListener {

        private float touchX;
        private float touchY;
        protected RectF rectF = new RectF();

        public DrawView(Context context, boolean click) {
            super(context);
            if (click) {
                setOnClickListener(this);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            rectF.set(0, 0, getWidth(), getHeight());
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            touchX = event.getX();
            touchY = event.getY();
            return super.onTouchEvent(event);
        }

        @Override
        public final void onClick(View v) {
            onClick(touchX, touchY);
        }

        protected void onClick(float x, float y) {

        }

    }

    class HeadView extends DrawView implements View.OnClickListener {

        RectF clickRectF;

        public HeadView(Context context) {
            super(context, true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (calendarPainter != null) {
                clickRectF = calendarPainter.onHeadDraw(canvas, rectF, curMonth.monthTime, selectMode, firstSelectTime, secondSelectTime);
            }
            linePaint.setColor(headLineColor);
            canvas.drawLine(rectF.left, rectF.bottom - 1, rectF.right, rectF.bottom - 1, linePaint);
        }

        @Override
        protected void onClick(float x, float y) {
            if (clickRectF != null && clickRectF.contains(x, y)) {
                setDisplayedChild(1);
                yearGridView.scrollToYear(calendar.get(Calendar.YEAR));
            }
        }
    }

    class WeekNameView extends DrawView {

        public WeekNameView(Context context) {
            super(context, false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (calendarPainter != null) {
                calendarPainter.onWeekNameDraw(canvas, rectF, curWeeks);
            }
            linePaint.setColor(weekLineColor);
            canvas.drawLine(rectF.left, rectF.bottom - 1, rectF.right, rectF.bottom - 1, linePaint);
        }
    }

    class RowRectF extends RectF {

        private RectF[] itemRectFs;

        public RowRectF(int columnCount) {
            itemRectFs = new RectF[columnCount];
        }
    }

    abstract class DrawGridView extends DrawView {

        protected int columnCount;
        protected int rowCount;
        private RowRectF[] rowRectFS;

        public DrawGridView(Context context, int columnCount, int rowCount) {
            super(context, true);
            this.columnCount = columnCount;
            this.rowCount = rowCount;
            rowRectFS = new RowRectF[rowCount];
            for (int row = 0; row < rowRectFS.length; row++) {
                rowRectFS[row] = new RowRectF(columnCount);
                for (int column = 0; column < columnCount; column++) {
                    rowRectFS[row].itemRectFs[column] = new RectF();
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            linePaint.setColor(gridLineColor);
            float[] x = new float[columnCount + 1];
            float[] y = new float[rowCount + 1];
            for (int i = 0; i <= columnCount; i++) {
                x[i] = rectF.left + rectF.width() * i / columnCount;
            }
            for (int i = 0; i <= rowCount; i++) {
                y[i] = rectF.top + rectF.height() * i / rowCount;
            }
            for (int row = 0; row < rowCount; row++) {
                RowRectF rowRectF = rowRectFS[row];
                rowRectF.set(rectF.left, y[row], rectF.right, y[row + 1]);
                for (int column = 0; column < columnCount; column++) {
                    rowRectF.itemRectFs[column].set(x[column], y[row], x[column + 1], y[row + 1]);
                }
                onRowDraw(canvas, rowRectF, row, true);
                for (int column = 0; column < columnCount; column++) {
                    int position = columnCount * row + column;
                    onItemDraw(canvas, rowRectF.itemRectFs[column], row, column, position);
                }
                onRowDraw(canvas, rowRectF, row, false);
            }
            for (int i = 1; i < columnCount; i++) {
                canvas.drawLine(x[i], rectF.top, x[i], rectF.bottom, linePaint);
            }
            for (int i = 1; i <= rowCount; i++) {
                canvas.drawLine(rectF.left, y[i] - 1, rectF.right, y[i] - 1, linePaint);
            }
        }

        protected void onRowDraw(Canvas canvas, RectF rectF, int row, boolean beforeItemDraw) {

        }

        protected abstract void onItemDraw(Canvas canvas, RectF rectF, int row, int column, int position);

        @Override
        protected void onClick(float x, float y) {
            for (int row = 0; row < rowCount; row++) {
                if (rowRectFS[row].contains(x, y)) {
                    if (!onRowClick(row)) {
                        for (int column = 0; column < columnCount; column++) {
                            if (rowRectFS[row].itemRectFs[column].contains(x, y)) {
                                int position = columnCount * row + column;
                                onItemClick(row, column, position);
                            }
                        }
                    }
                    break;
                }
            }
        }

        protected boolean onRowClick(int row) {
            return false;
        }

        protected abstract void onItemClick(int row, int column, int position);
    }

    class YearGridView extends RecyclerView {

        private YearGridAdapter yearGridAdapter;

        public YearGridView(Context context) {
            super(context);
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            setLayoutManager(layoutManager);
            CalendarSnapHelper snapHelper = new CalendarSnapHelper(context);
            snapHelper.attachToRecyclerView(this);
            yearGridAdapter = new YearGridAdapter(calendar.get(Calendar.YEAR), YEAR_COUNT, 5, 4);
            setAdapter(yearGridAdapter);
        }

        public void scrollToYear(int year) {
            int position = yearGridAdapter.getPosition(year);
            if (position != -1) {
                scrollToPosition(position);
            }
        }
    }

    class YearView extends DrawGridView {

        private int startYear;

        public YearView(Context context) {
            super(context, 4, 5);
        }

        public void update(int startYear) {
            this.startYear = startYear;
        }

        @Override
        protected void onItemDraw(Canvas canvas, RectF rectF, int row, int column, int position) {
            if (calendarPainter != null) {
                int year = startYear + position;
                calendarPainter.onYearDraw(canvas, rectF, year, year == calendar.get(Calendar.YEAR));
            }
        }

        @Override
        protected void onItemClick(int row, int column, int position) {
            calendar.set(Calendar.YEAR, startYear + position);
            setDisplayedChild(2);
        }
    }

    class MonthGridView extends DrawGridView {

        public MonthGridView(Context context) {
            super(context, 3, 4);
        }

        @Override
        protected void onItemDraw(Canvas canvas, RectF rectF, int row, int column, int position) {
            if (calendarPainter != null) {
                calendarPainter.onMonthDraw(canvas, rectF, position, position == calendar.get(Calendar.MONTH));
            }
        }

        @Override
        protected void onItemClick(int row, int column, int position) {
            calendar.set(Calendar.MONTH, position);
            scrollToMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
            setDisplayedChild(0);
        }
    }

    class DayGridView extends RecyclerView {

        private boolean scroll;
        private int scrollPosition = -1;
        private Runnable updateCurMonthRunnable = new Runnable() {
            @Override
            public void run() {
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    DayGroupView dayGroupView = (DayGroupView) getChildAt(i);
                    if (dayGroupView != null && dayGroupView.getBottom() > 0) {
                        if (dayGroupView.month != curMonth) {
                            curMonth = dayGroupView.month;
                            if (monthShowListener != null) {
                                monthShowListener.onMonthShow(curMonth.monthTime);
                            }
                            headView.postInvalidate();
                            calendar.setTimeInMillis(curMonth.monthTime);
                        }
                        dayGridAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        };

        private OnScrollListener scrollListener = new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                removeCallbacks(updateCurMonthRunnable);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    postDelayed(updateCurMonthRunnable, 50);
                    scroll = false;
                } else {
                    scroll = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                removeCallbacks(updateCurMonthRunnable);
                if (dy == 0) {
                    if (scrollPosition != -1) {
                        scrollToPosition(scrollPosition);
                        scrollPosition = -1;
                    }
                    postDelayed(updateCurMonthRunnable, 50);
                }
            }
        };

        public DayGridView(Context context) {
            super(context);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            setLayoutManager(linearLayoutManager);
            CalendarSnapHelper snapHelper = new CalendarSnapHelper(context);
            snapHelper.attachToRecyclerView(this);
            addOnScrollListener(scrollListener);
        }

        public boolean isScroll() {
            return scroll;
        }

        public void scrollToPositionEx(int position) {
            if (position > curMonth.position) {
                scrollPosition = position;
                scrollToPosition(dayGridAdapter.getItemCount() - 1);
            } else if (position < curMonth.position) {
                scrollToPosition(position);
            }
        }
    }

    class DayGroupView extends DrawGridView {

        private Month month;
        private Week[] weekArray;

        public DayGroupView(Context context, int rowCount) {
            super(context, 7, rowCount);
            weekArray = new Week[rowCount];
        }

        void update(Month month) {
            this.month = month;
            for (int i = 0; i < weekArray.length; i++) {
                if (weekArray[i] == null) {
                    weekArray[i] = new Week(i);
                } else {
                    weekArray[i].update();
                }
            }
        }

        @Override
        protected void onRowDraw(Canvas canvas, RectF rectF, int row, boolean beforeDrawItem) {
            Week week = weekArray[row];
            week.draw(canvas, rectF, beforeDrawItem);
        }

        @Override
        protected void onItemDraw(Canvas canvas, RectF rectF, int row, int column, int position) {
            weekArray[row].dayArray[column].draw(canvas, rectF);
        }

        @Override
        protected boolean onRowClick(int row) {
            if (selectMode == CalendarSelectMode.WEEK) {
                Week week = weekArray[row];
                week.onClick();
                return true;
            } else {
                return super.onRowClick(row);
            }
        }

        @Override
        protected void onItemClick(int row, int column, int position) {
            Day day = weekArray[row].dayArray[column];
            day.onClick();
        }

        class Week {

            private int position;
            private long weekTime;
            private boolean weekEnable;
            private Day[] dayArray = new Day[7];

            public Week(int position) {
                this.position = position;
                update();
            }

            private void update() {
                weekTime = month.monthTime - month.offset * DateUtils.DAY_IN_MILLIS + position * DateUtils.WEEK_IN_MILLIS;
                weekEnable = selectMode != CalendarSelectMode.WEEK || enableController == null || enableController.isEnable(selectMode, weekTime);
                for (int i = 0; i < dayArray.length; i++) {
                    if (dayArray[i] == null) {
                        dayArray[i] = new Day(position * 7 + i);
                    }
                    dayArray[i].update(weekEnable);
                }
            }

            public void onClick() {
                if (weekEnable) {
                    firstSelectTime = weekTime;
                    secondSelectTime = weekTime + DateUtils.DAY_IN_MILLIS * 6;
                    dayGridAdapter.notifyDataSetChanged();
                    if (selectListener != null) {
                        selectListener.onWeekSelected(weekTime);
                    }
                    headView.postInvalidate();
                }
            }

            public void draw(Canvas canvas, RectF rectF, boolean beforeDrawItem) {
                CalendarWeekState weekState = weekEnable ? CalendarWeekState.ENABLE : CalendarWeekState.DISENABLE;
                if (selectMode == CalendarSelectMode.WEEK && weekTime == firstSelectTime) {
                    weekState = CalendarWeekState.SELECTED;
                }
                calendarPainter.onWeekDraw(canvas, weekTime, rectF, beforeDrawItem, weekState, dayGridView.isScroll());
            }
        }

        class Day {

            private int position;
            private long dayTime;
            private boolean dayEnable;

            public Day(int position) {
                this.position = position;
            }

            private void update(boolean weekEnable) {
                dayTime = month.monthTime + (position - month.offset) * DateUtils.DAY_IN_MILLIS;
                dayEnable = weekEnable;
                if (dayEnable && selectMode != CalendarSelectMode.WEEK && enableController != null) {
                    dayEnable = enableController.isEnable(selectMode, dayTime);
                }
            }

            public void onClick() {
                if (dayEnable) {
                    if (selectMode == CalendarSelectMode.DAY) {
                        firstSelectTime = dayTime;
                        if (selectListener != null) {
                            selectListener.onDaySelected(dayTime);
                        }
                        headView.postInvalidate();
                    } else if (selectMode == CalendarSelectMode.RANGE) {
                        selectDayTime.add(dayTime);
                        if (selectDayTime.size() == 3) {
                            selectDayTime.remove(0);
                        }
                        if (selectDayTime.size() == 1) {
                            firstSelectTime = selectDayTime.get(0);
                        } else if (selectDayTime.size() == 2) {
                            firstSelectTime = Math.min(selectDayTime.get(0), selectDayTime.get(1));
                            secondSelectTime = Math.max(selectDayTime.get(0), selectDayTime.get(1));
                            if (selectListener != null) {
                                selectListener.onRangeSelected(firstSelectTime, secondSelectTime);
                            }
                        }
                        headView.postInvalidate();
                    }
                    dayGridAdapter.notifyDataSetChanged();
                }
            }

            public void draw(Canvas canvas, RectF rectF) {
                boolean isToday = curDayTime == dayTime;
                boolean isCurMonth = dayTime >= curMonth.monthTime && dayTime - curMonth.monthTime < DateUtils.DAY_IN_MILLIS * curMonth.dayCount;
                CalendarDayState dayState = dayEnable ? CalendarDayState.ENABLE : CalendarDayState.DISENABLE;
                if (selectMode != CalendarSelectMode.NONE) {
                    if (selectMode == CalendarSelectMode.WEEK) {
                        if (firstSelectTime == dayTime) {
                            dayState = CalendarDayState.SELECTED_WEEK_START;
                        } else if (secondSelectTime == dayTime) {
                            dayState = CalendarDayState.SELECTED_WEEK_END;
                        } else if (dayTime > firstSelectTime && dayTime < secondSelectTime) {
                            dayState = CalendarDayState.SELECTED_WEEK_CENTER;
                        }
                    } else if (selectMode == CalendarSelectMode.DAY) {
                        if (firstSelectTime == dayTime) {
                            dayState = CalendarDayState.SELECTED;
                        }
                    } else if (selectMode == CalendarSelectMode.RANGE) {
                        if (selectDayTime.size() == 1) {
                            if (selectDayTime.get(0) == dayTime) {
                                dayState = CalendarDayState.SELECTED_RANGE_START;
                            }
                        } else if (selectDayTime.size() == 2) {
                            if (firstSelectTime == secondSelectTime) {
                                if (dayTime == firstSelectTime) {
                                    dayState = CalendarDayState.SELECTED_RANGE_START_END;
                                }
                            } else {
                                if (firstSelectTime == dayTime) {
                                    dayState = CalendarDayState.SELECTED_RANGE_START;
                                } else if (secondSelectTime == dayTime) {
                                    dayState = CalendarDayState.SELECTED_RANGE_END;
                                } else if (dayTime > firstSelectTime && dayTime < secondSelectTime) {
                                    dayState = CalendarDayState.SELECTED_RANGE_CENTER;
                                }
                            }
                        }
                    }
                }
                calendarPainter.onDayDraw(canvas, dayTime, rectF, isCurMonth, isToday, dayState, dayGridView.isScroll());
            }
        }
    }

    class YearGridAdapter extends RecyclerView.Adapter<CommonViewHolder<YearView>> {

        int count;
        int startYear;
        int endYear;
        int itemYearCount;

        public YearGridAdapter(int curYear, int years, int itemRowCount, int itemColumnCount) {
            this.itemYearCount = itemRowCount * itemColumnCount;
            this.count = years * 2 / itemYearCount;
            this.startYear = curYear - years;
            this.endYear = curYear + years - 1;
        }

        public int getPosition(int year) {
            int position = year - startYear;
            if (year < startYear || year > endYear) {
                return -1;
            }
            if ((position + 1) % itemYearCount == 0) {
                return (position + 1) / itemYearCount - 1;
            }
            return (position + 1) / itemYearCount;
        }

        @NonNull
        @Override
        public CommonViewHolder<YearView> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            YearView yearView = new YearView(parent.getContext());
            yearView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, parent.getHeight() * 5 / 6));
            return new CommonViewHolder(yearView);
        }

        @Override
        public void onBindViewHolder(@NonNull CommonViewHolder<YearView> holder, int position) {
            holder.getView().update(startYear + position * itemYearCount);
        }

        @Override
        public int getItemCount() {
            return count;
        }
    }

    class DayGridAdapter extends RecyclerView.Adapter<CommonViewHolder<DayGroupView>> {

        private final int count;
        private final int initPosition;
        private final long initTime;
        private Calendar calendar;
        private final int startYear;
        private final int endYear;
        private SparseArray<Month> monthCache = new SparseArray<>();

        public DayGridAdapter(Calendar calendar, int years) {
            Calendar clone = (Calendar) calendar.clone();
            clone.set(Calendar.DAY_OF_MONTH, 1);
            clone.set(Calendar.HOUR_OF_DAY, 0);
            clone.set(Calendar.MINUTE, 0);
            clone.set(Calendar.SECOND, 0);
            clone.set(Calendar.MILLISECOND, 0);
            initTime = clone.getTimeInMillis();
            int curYear = clone.get(Calendar.YEAR);
            startYear = curYear - years;
            endYear = curYear + years - 1;
            count = years * 2 * 12 + 1;
            initPosition = years * 12 + clone.get(Calendar.MONTH);
            this.calendar = Calendar.getInstance();
        }

        public void clearCache() {
            monthCache.clear();
        }

        public int getPosition(int year, int month) {
            if (year >= startYear && year <= endYear) {
                return (year - startYear) * 12 + month;
            }
            return -1;
        }

        @NonNull
        @Override
        public CommonViewHolder<DayGroupView> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DayGroupView dayGroupView = new DayGroupView(parent.getContext(), viewType);
            dayGroupView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, parent.getHeight() * viewType / 6));
            return new CommonViewHolder(dayGroupView);
        }

        @Override
        public void onBindViewHolder(@NonNull CommonViewHolder<DayGroupView> holder, int position) {
            Month month = getMonth(position);
            holder.getView().update(month);
        }

        @Override
        public int getItemViewType(int position) {
            Month month = getMonth(position);
            return month.weekCount;
        }

        @Override
        public void onViewRecycled(@NonNull CommonViewHolder holder) {
            super.onViewRecycled(holder);
        }

        private Month getMonth(int position) {
            Month month = monthCache.get(position);
            if (month == null) {
                calendar.setTimeInMillis(initTime);
                calendar.add(Calendar.MONTH, position - initPosition);
                int dayOffset = calendar.get(Calendar.DAY_OF_WEEK) - startWeek;
                if (dayOffset < 0) {
                    dayOffset += 7;
                }
                month = new Month(position);
                month.monthTime = calendar.getTimeInMillis();
                month.offset = dayOffset;
                month.dayCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                month.weekCount = (dayOffset + month.dayCount) / 7;
                monthCache.put(position, month);
            }
            return month;
        }

        @Override
        public int getItemCount() {
            return count;
        }
    }

    class CommonViewHolder<T extends View> extends RecyclerView.ViewHolder {

        T view;

        public CommonViewHolder(T view) {
            super(view);
            this.view = view;
        }

        public T getView() {
            return view;
        }
    }

    class Month {
        int offset;
        int position;
        int dayCount;
        int weekCount;
        long monthTime;

        public Month(int position) {
            this.position = position;
        }
    }
}

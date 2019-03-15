package com.rdb.calendar.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.rdb.calendar.CalendarSelectMode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);
        findViewById(R.id.noneButton).setOnClickListener(this);
        findViewById(R.id.dayButton).setOnClickListener(this);
        findViewById(R.id.weekButton).setOnClickListener(this);
        findViewById(R.id.rangeButton).setOnClickListener(this);
        findViewById(R.id.daysButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.noneButton) {
            Intent intent = new Intent(this, CalendarActivity.class);
            intent.putExtra("selectMode", CalendarSelectMode.NONE);
            startActivity(intent);
        } else if (v.getId() == R.id.dayButton) {
            Intent intent = new Intent(this, CalendarActivity.class);
            intent.putExtra("selectMode", CalendarSelectMode.DAY);
            startActivity(intent);
        } else if (v.getId() == R.id.weekButton) {
            Intent intent = new Intent(this, CalendarActivity.class);
            intent.putExtra("selectMode", CalendarSelectMode.WEEK);
            startActivity(intent);
        } else if (v.getId() == R.id.rangeButton) {
            Intent intent = new Intent(this, CalendarActivity.class);
            intent.putExtra("selectMode", CalendarSelectMode.RANGE);
            startActivity(intent);
        } else if (v.getId() == R.id.daysButton) {
//            Intent intent = new Intent(this, CalendarActivity.class);
//            intent.putExtra("selectMode",CalendarSelectMode.DAYS);
//            startActivity(intent);
        }
    }
}

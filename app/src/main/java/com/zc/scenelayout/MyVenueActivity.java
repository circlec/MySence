package com.zc.scenelayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.zc.scenelayout.venue.MyVenue;

public class MyVenueActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_venue);

        MyVenue myVenue = findViewById(R.id.venue);
        TextView tvDelete = findViewById(R.id.tv_delete);
        TextView tvRotate = findViewById(R.id.tv_rotate);
        tvDelete.setOnClickListener(view -> myVenue.deleteSelectModel());
        tvRotate.setOnClickListener(view -> myVenue.setSelectRotate(30));
    }
}

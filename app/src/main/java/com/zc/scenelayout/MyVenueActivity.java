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
        TextView tvAlign = findViewById(R.id.tv_align);
        tvDelete.setOnClickListener(view -> myVenue.deleteSelectModel());
        tvRotate.setOnClickListener(view -> myVenue.setSelectRotate(30));
        tvAlign.setOnClickListener(view -> {
            if(tvAlign.getText().toString().contains("关")){
                tvAlign.setText("对齐（开）");
                myVenue.setAlign(true);
            }else{
                tvAlign.setText("对齐（关）");
                myVenue.setAlign(false);
            }
        });
    }

}

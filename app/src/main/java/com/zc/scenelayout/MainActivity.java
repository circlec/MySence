package com.zc.scenelayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.zc.scenelayout.secens.MyScene;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyScene myScene = findViewById(R.id.scene);
        TextView tvDelete = findViewById(R.id.tv_delete);
        TextView tvRotate = findViewById(R.id.tv_rotate);
        tvDelete.setOnClickListener(view -> myScene.deleteSelectModel());
        tvRotate.setOnClickListener(view -> myScene.setSelectRotate(30));
    }
}

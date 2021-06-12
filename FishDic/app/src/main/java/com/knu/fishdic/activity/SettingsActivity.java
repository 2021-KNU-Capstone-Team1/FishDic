package com.knu.fishdic.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.knu.fishdic.R;

// 옵션 액티비티 정의

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_settings);

    }



}

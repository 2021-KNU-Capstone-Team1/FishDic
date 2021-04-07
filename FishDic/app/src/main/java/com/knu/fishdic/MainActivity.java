package com.knu.fishdic;

import android.app.Activity;
import android.os.Bundle;

// 메인 화면 액티비티 정의

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main);
    }
}
package com.knu.fishdic.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.knu.fishdic.R;
import com.knu.fishdic.manager.InitManager;

// 스플래시 스크린 출력을 위한 액티비티 정의

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /*** 앱 로딩 시 필요한 작업 수행 후 MainActivity 실행 ***/
        InitManager.doDataBindingJob();
        startActivity(new Intent(getApplication(), MainActivity.class));
        this.finish(); //스플래시 액티비티를 스택에서 제거
    }

    @Override
    public void onBackPressed() {
        //스플래시 화면에서 뒤로가기 기능 제거
    }
}
package com.knu.fishdic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

// 앱 로딩 과정 중 스플래시 스크린 출력을 위한 SpashActivity 정의

public class SplashActivity extends Activity {
    private final int SPLASH_DISPLAY_TIME = 3000; //스플래시 디버그용 딜레이

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /*** 앱 로딩 시 필요한 작업 수행 후 MainActivity 실행 ***/

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplication(), MainActivity.class));
                SplashActivity.this.finish(); //스플래시 액티비티를 스택에서 제거
            }
        }, SPLASH_DISPLAY_TIME); //디버그 위해 딜레이 준 후 실행

        //startActivity(new Intent(getApplication(), MainActivity.class));
        //SplashActivity.this.finish();
    }

    @Override
    public void onBackPressed() {
        //스플래시 화면에서 뒤로가기 기능 제거
    }
}
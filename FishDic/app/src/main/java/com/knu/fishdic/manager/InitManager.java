package com.knu.fishdic.manager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.knu.fishdic.MainActivity;
import com.knu.fishdic.R;

// 앱 초기화와 스플래시 스크린 출력을 위한 InitManager 정의

public class InitManager extends Activity {
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
                InitManager.this.finish(); //스플래시 액티비티를 스택에서 제거
            }
        }, 3000); //디버그 위해 적당히 딜레이 준 후 실행

        //위에 초기화 작업 추가 및 디버그 코드 삭제하고 아래 코드 주석 해제 할 것
        //startActivity(new Intent(getApplication(), MainActivity.class));
        //InitManager.this.finish(); //스플래시 액티비티를 스택에서 제거
    }

    @Override
    public void onBackPressed() {
        //스플래시 화면에서 뒤로가기 기능 제거
    }

    private void doSomeInitProc(){}; //예시
}
package com.knu.fishdic.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import com.knu.fishdic.R;

// 정보 액티비티 정의

public class InfoActivity extends Activity {
    Button info_ok_button; //확인 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //타이틀바 제거
        setContentView(R.layout.activity_info);

        this.setComponentsInteraction();
    }

    private void setComponentsInteraction() { //내부 구성요소 상호작용 설정
        this.info_ok_button = findViewById(R.id.info_ok_button);

        this.info_ok_button.setOnClickListener(v -> { //확인 버튼에 대한 클릭 리스너
            onBackPressed();
        });
    }
}

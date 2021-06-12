package com.knu.fishdic.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Spinner;

import com.knu.fishdic.R;

// 옵션 액티비티 정의

public class SettingsActivity extends Activity {
    Spinner settings_fish_detail_font_size_spinner; //어류 상세 정보 폰트 크기 스피너
    Spinner settings_fish_identification_threshold_spinner; //어류 판별 결과 유사도 임계값 스피너

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //타이틀바 제거
        setContentView(R.layout.activity_settings);



    }
}

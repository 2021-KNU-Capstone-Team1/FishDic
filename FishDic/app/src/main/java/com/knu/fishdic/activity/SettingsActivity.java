package com.knu.fishdic.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;

// 설정 액티비티 정의

public class SettingsActivity extends Activity {
    Spinner settings_fish_detail_font_size_spinner; //어류 상세 정보 폰트 크기 스피너
    Spinner settings_fish_identification_threshold_spinner; //어류 판별 결과 유사도 임계값 스피너

    Button settings_init_button; //초기화 버튼
    Button settings_ok_button; //확인 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //타이틀바 제거
        setContentView(R.layout.activity_settings);

        this.setComponentsInteraction();
    }

    private void setComponentsInteraction() { //내부 구성요소 상호작용 설정
        this.settings_fish_detail_font_size_spinner = findViewById(R.id.settings_fish_detail_font_size_spinner);
        this.settings_fish_identification_threshold_spinner = findViewById(R.id.settings_fish_identification_threshold_spinner);

        this.settings_init_button = findViewById(R.id.settings_init_button);
        this.settings_ok_button = findViewById(R.id.settings_ok_button);

        this.initSpinner();
        this.settings_fish_detail_font_size_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //어류 상세 정보 폰트 크기 스피너에 대한 아이템 선택 리스너
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FishDic.globalSettingsManager.setFishDetailFontSize(Integer.parseInt(parent.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.settings_fish_identification_threshold_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //어류 판별 결과 유사도 임계값 스피너에 대한 아이템 선택 리스너
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FishDic.globalSettingsManager.setResultsScoreThreshold(Float.parseFloat(parent.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.settings_init_button.setOnClickListener(v -> { //초기화 버튼에 대한 클릭 리스너
            FishDic.globalSettingsManager.clearAllSettings();
            this.initSpinner();
        });

        this.settings_ok_button.setOnClickListener(v -> { //확인 버튼에 대한 클릭 리스너
            onBackPressed();
        });
    }

    private void initSpinner() { //스피너 초기화
        int fishDetailFontSizeIndex = getSpinnerItemIndex(this.settings_fish_detail_font_size_spinner,
                String.valueOf(FishDic.globalSettingsManager.getFishDetailFontSize())); //기존 폰트 크기의 인덱스
        int resultsScoreThresholdIndex = getSpinnerItemIndex(this.settings_fish_identification_threshold_spinner,
                String.valueOf(FishDic.globalSettingsManager.getResultsScoreThreshold())); //기존 판별 결과 임계값의 인덱스

        this.settings_fish_detail_font_size_spinner.setSelection(fishDetailFontSizeIndex);
        this.settings_fish_identification_threshold_spinner.setSelection(resultsScoreThresholdIndex);
    }

    private int getSpinnerItemIndex(Spinner spinner, String item) { //스피너 아이템 인덱스 반환
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(item)) { //해당 아이템과 일치 할 경우 해당 인덱스 반환
                return i;
            }
        }

        return 0;
    }
}

package com.knu.fishdic.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.fragment.MyFragment;
import com.knu.fishdic.manager.FishIdentificationManager;

// 옵션 액티비티 정의

public class SettingsActivity extends Activity {
    Spinner settings_fish_detail_font_size_spinner; //어류 상세 정보 폰트 크기 스피너
    Spinner settings_fish_identification_threshold_spinner; //어류 판별 결과 유사도 임계값 스피너

    Button settings_init_button; //초기화 버튼
    Button settings_ok_button; //확인 버튼

    private int fishDetailFontSize; //어류 상세 정보 폰트 크기
    private float resultsScoreThreshold; //어류 판별 결과 유사도 임계값

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //타이틀바 제거
        setContentView(R.layout.activity_settings);

        this.loadSharedPreferences(); //공유 설정 불러오기
        this.setComponentsInteraction();
    }

    private void setComponentsInteraction() { //내부 구성요소 상호작용 설정
        this.settings_fish_detail_font_size_spinner = findViewById(R.id.settings_fish_detail_font_size_spinner);
        this.settings_fish_identification_threshold_spinner = findViewById(R.id.settings_fish_identification_threshold_spinner);

        this.settings_init_button = findViewById(R.id.settings_init_button);
        this.settings_ok_button = findViewById(R.id.settings_ok_button);

        int fishDetailFontSizeIndex = getSpinnerItemIndex(this.settings_fish_detail_font_size_spinner, String.valueOf(this.fishDetailFontSize)); //기존 폰트 크기의 인덱스
        int resultsScoreThresholdIndex = getSpinnerItemIndex(this.settings_fish_identification_threshold_spinner, String.valueOf(this.resultsScoreThreshold)); //기존 임계값의 인덱스
        this.settings_fish_detail_font_size_spinner.setSelection(fishDetailFontSizeIndex);
        this.settings_fish_identification_threshold_spinner.setSelection(resultsScoreThresholdIndex);

        this.settings_fish_detail_font_size_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //어류 상세 정보 폰트 크기 스피너에 대한 아이템 선택 리스너
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fishDetailFontSize = Integer.parseInt(parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.settings_fish_identification_threshold_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //어류 판별 결과 유사도 임계값 스피너에 대한 아이템 선택 리스너
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                resultsScoreThreshold = Float.parseFloat(parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.settings_init_button.setOnClickListener(v -> { //초기화 버튼에 대한 클릭 리스너

        });

        this.settings_ok_button.setOnClickListener(v -> { //확인 버튼에 대한 클릭 리스너
            this.saveSharedPreferences(); //공유 설정 저장
            onBackPressed();
        });
    }

    private int getSpinnerItemIndex(Spinner spinner, String item) { //스피너 아이템 인덱스 반환
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(item)) { //해당 아이템과 일치 할 경우 해당 인덱스 반환
                return i;
            }
        }

        return 0;
    }

    private void loadSharedPreferences() { //공유 설정 불러오기
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(FishDic.globalContext); //공유 설정

        this.fishDetailFontSize = sharedPreferences.getInt(MyFragment.FISH_DETAIL_FONT_SIZE_KEY, 15);
        this.resultsScoreThreshold = sharedPreferences.getFloat(FishIdentificationManager.RESULTS_SCORE_THRESHOLD_KEY, 10.0f);
    }

    private void saveSharedPreferences() { //공유 설정 저장
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(FishDic.globalContext); //공유 설정
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit(); //공유 설정 편집기

        sharedPreferencesEditor.putInt(MyFragment.FISH_DETAIL_FONT_SIZE_KEY, this.fishDetailFontSize);
        sharedPreferencesEditor.putFloat(FishIdentificationManager.RESULTS_SCORE_THRESHOLD_KEY, this.resultsScoreThreshold);
        sharedPreferencesEditor.commit();
    }
}

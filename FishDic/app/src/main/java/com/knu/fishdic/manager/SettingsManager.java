package com.knu.fishdic.manager;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.knu.fishdic.FishDic;

// 설정 관리를 위한 SettingsManager 정의

public class SettingsManager {
    public static final String FISH_DETAIL_FONT_SIZE_KEY = "fishDetailFontSizeKey"; //어류 상세 정보 폰트 크기를 위한 키 값
    public static final String RESULTS_SCORE_THRESHOLD_KEY = "resultsScoreThresholdKey"; //판별 결과 가중치 임계값을 위한 키 값

    private SharedPreferences sharedPreferences; //공유 설정
    private SharedPreferences.Editor sharedPreferencesEditor; //공유 설정 편집기

    public SettingsManager() {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(FishDic.globalContext); //공유 설정
        this.sharedPreferencesEditor = sharedPreferences.edit();
    }

    public void clearAllSettings(){ //모든 설정 초기화
        this.sharedPreferencesEditor.clear();
        this.sharedPreferencesEditor.commit();
    }

    public int getFishDetailFontSize() { //어류 상세 정보 폰트 크기 반환
        return this.sharedPreferences.getInt(FISH_DETAIL_FONT_SIZE_KEY, 15); //초기 값 15
    }

    public void setFishDetailFontSize(int fishDetailFontSize) { //어류 상세 정보 폰트 크기 설정
        this.sharedPreferencesEditor.putInt(FISH_DETAIL_FONT_SIZE_KEY, fishDetailFontSize);
        this.sharedPreferencesEditor.commit();
    }

    public float getResultsScoreThreshold() { //어류 판별 결과 유사도 임계값 반환
        return this.sharedPreferences.getFloat(RESULTS_SCORE_THRESHOLD_KEY, 10.0f); //초기값 10.0f
    }

    public void setResultsScoreThreshold(float resultsScoreThreshold) { //어류 판별 결과 유사도 임계값 설정
        this.sharedPreferencesEditor.putFloat(RESULTS_SCORE_THRESHOLD_KEY, resultsScoreThreshold);
        this.sharedPreferencesEditor.commit();
    }
}

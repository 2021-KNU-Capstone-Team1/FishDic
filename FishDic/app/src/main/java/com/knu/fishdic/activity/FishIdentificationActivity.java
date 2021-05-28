package com.knu.fishdic.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.knu.fishdic.R;


public class FishIdentificationActivity extends AppCompatActivity {
    public enum FISH_IDENTIFICATION_TYPE { //어류 판별 타입 정의
        TAKE_PICTURE, //사진 찍기
        GET_FROM_GALLERY //갤러리로부터 가져오기
    }

    //TODO : 메인액티비티의 카메라 혹은 갤러리로부터의 사진 가져오는 것을 여기로 옮기기

    //h5 -.> pb -> tflite로 변환
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_fishidentification);
    }
}
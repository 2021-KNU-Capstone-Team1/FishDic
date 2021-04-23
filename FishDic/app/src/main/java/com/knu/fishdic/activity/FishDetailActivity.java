package com.knu.fishdic.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;

import com.knu.fishdic.R;

// 어류 상세정보 화면 액티비티 정의

public class FishDetailActivity extends Activity {
    ImageButton fishDetail_back_imageButton; //뒤로 가기 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_fishdetail);

        setComponentsInteraction();
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.fishDetail_back_imageButton = (ImageButton) findViewById(R.id.fishDetail_back_imageButton);

        this.fishDetail_back_imageButton.setOnClickListener(v -> //뒤로 가기 버튼에 대한 클릭 리스너
        {
            onBackPressed();
        });
    }
}

package com.knu.fishdic;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;

// 이달의 금어기 화면 액티비티 정의

public class HelpActivity extends Activity {
    ImageButton help_back_imageButton; //뒤로 가기 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_deniedfish);

        setComponentsInteraction();
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        help_back_imageButton = (ImageButton) findViewById(R.id.help_back_imageButton);

        help_back_imageButton.setOnClickListener(v ->
        {
            onBackPressed();
        });
    }
}
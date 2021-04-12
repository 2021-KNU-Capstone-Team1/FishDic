package com.knu.fishdic.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import com.knu.fishdic.R;

// 도감 화면 액티비티 정의

public class DicActivity extends Activity {
    ImageButton dic_back_imageButton; //뒤로 가기 버튼
    EditText dic_search_editText; //검색 창

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_dic);

        setComponentsInteraction();
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        dic_back_imageButton = (ImageButton) findViewById(R.id.dic_back_imageButton);
        dic_search_editText = (EditText) findViewById(R.id.dic_search_editText);

        dic_back_imageButton.setOnClickListener(v -> //뒤로 가기 버튼에 대한 클릭 리스너
        {
            onBackPressed();
        });

        dic_search_editText.addTextChangedListener(new TextWatcher() { //검색 창에 대한 텍스트 변경 리스너
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) { //텍스트 변경 시마다 호출
            }
        });
    }
}

package com.knu.fishdic.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.manager.DBManager;

// 도감 화면 액티비티 정의

public class DicActivity extends Activity {
    ImageButton dic_back_imageButton; //뒤로 가기 버튼
    EditText dic_search_editText; //검색 창

    RecyclerView dic_recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_dic);

        this.setComponentsInteraction();
    }

    @Override
    public void onBackPressed() { //하드웨어, 소프트웨어 back 키와 앱 내의 뒤로 가기 버튼을 위하여 현재 액티비티 종료 시 수행 할 작업 설정
        FishDic.globalDicRecyclerAdapter.deallocateRefItemList(); //참조 목록 초기화 및 가비지 컬렉션 요청
        super.onBackPressed();
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.dic_back_imageButton = findViewById(R.id.dic_back_imageButton);
        this.dic_search_editText = findViewById(R.id.dic_search_editText);

        this.dic_recyclerView = findViewById(R.id.dic_recyclerView);
        this.dic_recyclerView.setHasFixedSize(true); //최적화를 위해서 사이즈 고정
        this.dic_recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.layoutManager = new LinearLayoutManager(this);
        this.dic_recyclerView.setLayoutManager(layoutManager);
        this.dic_recyclerView.setAdapter(FishDic.globalDicRecyclerAdapter);

        this.dic_back_imageButton.setOnClickListener(v -> { //뒤로 가기 버튼에 대한 클릭 리스너
            onBackPressed();
        });

        this.dic_search_editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) { //사용자가 검색 창 누를 시
                dic_search_editText.setHint(""); //힌트 제거
            } else { //포커스 해제 시
                dic_search_editText.setHint(R.string.enter_name); //힌트 재 설정
            }
        });

        this.dic_search_editText.addTextChangedListener(new TextWatcher() { //검색 창에 대한 텍스트 감시 리스너
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FishDic.globalDicRecyclerAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        FishDic.globalDicRecyclerAdapter.setOnItemClickListener((v, title) -> { //새로운 클릭 리스너 객체 생성 하여 RecyclerAdapter 내부의 refItemClickListener가 참조
            /*** 커스텀 리스너 인터페이스내의 void onItemClick(View v, String title) 오버라이드 ***/
            Intent intent = new Intent(this, FishDetailActivity.class);
            intent.putExtra(DBManager.NAME, title); //어류 이름 전달
            startActivity(intent);
        });
    }
}
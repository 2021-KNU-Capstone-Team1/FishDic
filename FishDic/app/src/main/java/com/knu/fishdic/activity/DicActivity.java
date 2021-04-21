package com.knu.fishdic.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;

// 도감 화면 액티비티 정의

public class DicActivity extends Activity {
    ImageButton dic_back_imageButton; //뒤로 가기 버튼
    EditText dic_search_editText; //검색 창

    RecyclerView dic_recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.OnItemTouchListener onItemTouchListener; //RecyclerView 내부의 아이템에 대해 터치 이벤트 발생 시 처리 위한 리스너

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

        dic_recyclerView = (RecyclerView) findViewById(R.id.dic_recyclerView);
        dic_recyclerView.setHasFixedSize(true); //최적화를 위해서 사이즈 고정
        dic_recyclerView.setItemAnimator(new DefaultItemAnimator());
        layoutManager = new LinearLayoutManager(this);
        dic_recyclerView.setLayoutManager(layoutManager);
        dic_recyclerView.setAdapter(FishDic.globalDicRecyclerAdapter);

        dic_back_imageButton.setOnClickListener(v -> //뒤로 가기 버튼에 대한 클릭 리스너
        {
            onBackPressed();
        });

        dic_search_editText.setOnFocusChangeListener((v, hasFocus) -> dic_search_editText.setHint("")); //검색 창 클릭 시 힌트 제거
        dic_search_editText.addTextChangedListener(new TextWatcher() { //검색 창에 대한 텍스트 감시 리스너
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

        this.onItemTouchListener = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY()); //터치한 곳의 좌표를 토대로 해당 Item의 View를 가져옴
                if (childView != null && FishDic.globalGestureDetector.onTouchEvent(e)) { //터치한 곳의 View가 RecyclerView 안의 아이템이고, 터치 이벤트 발생 시
                    int currentPosition = rv.getChildAdapterPosition(childView); //현재 터치된 곳의 position을 가져오고
                    //어류 상세정보 페이지로 이동
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        };
        dic_recyclerView.addOnItemTouchListener(this.onItemTouchListener);
    }
}
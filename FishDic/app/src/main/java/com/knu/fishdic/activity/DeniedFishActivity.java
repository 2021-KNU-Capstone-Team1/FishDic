package com.knu.fishdic.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;

// 이달의 금어기 화면 액티비티 정의

public class DeniedFishActivity extends Activity {
    ImageButton deniedFish_back_imageButton; //뒤로 가기 버튼

    RecyclerView deniedFish_recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_deniedfish);

        setComponentsInteraction();
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.deniedFish_back_imageButton = (ImageButton) findViewById(R.id.deniedFish_back_imageButton);

        this.deniedFish_recyclerView = (RecyclerView)findViewById(R.id.deniedFish_recyclerView);
        this.deniedFish_recyclerView.setHasFixedSize(true); //최적화를 위해서 사이즈 고정
        this.deniedFish_recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.layoutManager = new LinearLayoutManager(this);
        this.deniedFish_recyclerView.setLayoutManager(layoutManager);
        this.deniedFish_recyclerView.setAdapter(FishDic.globalDeniedFishRecyclerAdapter);

        this.deniedFish_back_imageButton.setOnClickListener(v -> //뒤로 가기 버튼에 대한 클릭 리스너
        {
            onBackPressed();
        });

        FishDic.globalDeniedFishRecyclerAdapter.setOnItemClickListener((v, title) -> { //새로운 클릭 리스너 객체 생성 하여 RecyclerAdapter 내부의 refItemClickListener가 참조
            /*** 커스텀 리스너 인터페이스내의 void onItemClick(View v, String title) 오버라이드 ***/
            Intent intent = new Intent(FishDic.globalContext, FishDetailActivity.class);
            intent.putExtra("title", title); //어류 이름
            startActivity(intent);
        });
    }
}
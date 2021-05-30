package com.knu.fishdic.activity;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.fragment.MyFragment;
import com.knu.fishdic.fragment.MyFragmentPagerAdapter;

import java.util.Timer;

import me.relex.circleindicator.CircleIndicator;

// 이용가이드 액티비티 정의

public class HelpActivity extends AppCompatActivity {
    ImageButton help_back_imageButton; //뒤로 가기 버튼

    ViewPager viewPager;
    FragmentPagerAdapter viewPagerAdapter; //ViewPager 어댑터
    CircleIndicator indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.app_name);
        setContentView(R.layout.activity_help);

        this.setComponentsInteraction();
        this.initViewPager();
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.help_back_imageButton = findViewById(R.id.help_back_imageButton);
        this.viewPager = findViewById(R.id.help_viewPager);
        this.indicator = findViewById(R.id.help_circleIndicator);

        this.help_back_imageButton.setOnClickListener(v -> //뒤로 가기 버튼에 대한 클릭 리스너
                onBackPressed());
    }

    private void initViewPager() { //ViewPager 초기화
        Bundle args = new Bundle();
        args.putSerializable(MyFragment.FRAGMENT_TYPE_KEY_VALUE, MyFragment.FRAGMENT_TYPE.HELP);
        args.putParcelableArray(MyFragment.IMAGE_KEY_VALUE, FishDic.helpImages);

        /*** ViewPager 어댑터 생성 및 할당, 원형 인디케이터 ViewPager에 할당 ***/
        this.viewPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), args);
        this.viewPager.setAdapter(viewPagerAdapter);
        this.indicator.setViewPager(viewPager);
    }
}
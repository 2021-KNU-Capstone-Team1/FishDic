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

import me.relex.circleindicator.CircleIndicator;

// 이용가이드 액티비티 정의

public class HelpActivity extends AppCompatActivity {
    ImageButton help_back_imageButton; //뒤로 가기 버튼
    FragmentPagerAdapter viewPagerAdapter; //ViewPager 어댑터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.app_name);
        setContentView(R.layout.activity_help);

        this.setComponentsInteraction();

        /////코드 정리 예정
        ViewPager viewPager = findViewById(R.id.help_viewPager);
        viewPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), MyFragment.FRAGMENT_TYPE.HELP, FishDic.bannerImages);
        viewPager.setAdapter(viewPagerAdapter);

        CircleIndicator indicator = findViewById(R.id.help_circleIndicator);
        indicator.setViewPager(viewPager);
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.help_back_imageButton = (ImageButton) findViewById(R.id.help_back_imageButton);

        this.help_back_imageButton.setOnClickListener(v -> //뒤로 가기 버튼에 대한 클릭 리스너
                onBackPressed());
    }

    private void getHelpImages() {

    }
}
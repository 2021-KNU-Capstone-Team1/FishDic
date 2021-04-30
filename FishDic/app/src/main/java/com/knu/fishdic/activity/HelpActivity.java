package com.knu.fishdic.activity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.fragment.MyFragment;
import com.knu.fishdic.fragment.MyFragmentPagerAdapter;

import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

// 이용가이드 액티비티 정의

public class HelpActivity extends AppCompatActivity {
    ImageButton help_back_imageButton; //뒤로 가기 버튼
    FragmentPagerAdapter viewPagerAdapter; //ViewPager 어댑터

    Timer timer;
    int currentPage = 0;
    final long DELAY_MS = 500; //작업이 실행 되기 전 딜레이 (MS)
    final long PERIOD_MS = 3000; //작업 실행 간의 딜레이 (MS)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.app_name);
        setContentView(R.layout.activity_help);

        this.setComponentsInteraction();
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.help_back_imageButton = (ImageButton) findViewById(R.id.help_back_imageButton);

        this.help_back_imageButton.setOnClickListener(v -> //뒤로 가기 버튼에 대한 클릭 리스너
                onBackPressed());
    }

    private void set() {
        ViewPager viewPager = findViewById(R.id.help_viewPager);

        Bundle args = new Bundle();
        args.putSerializable(MyFragment.FRAGMENT_TYPE_KEY_VALUE, MyFragment.FRAGMENT_TYPE.HELP);
        args.putParcelableArray(MyFragment.IMAGE_KEY_VALUE, FishDic.bannerImages);

        viewPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), args);
        viewPager.setAdapter(viewPagerAdapter);

        CircleIndicator indicator = findViewById(R.id.help_circleIndicator);
        indicator.setViewPager(viewPager);

        final Handler handler = new Handler();
        final Runnable Update = () -> {
            if (currentPage == FishDic.helpImages.length) {
                currentPage = 0;
            }
            viewPager.setCurrentItem(currentPage++, true);
        };

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);
    }
}
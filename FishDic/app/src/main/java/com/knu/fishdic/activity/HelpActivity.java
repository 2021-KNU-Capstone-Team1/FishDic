package com.knu.fishdic.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.knu.fishdic.R;

import me.relex.circleindicator.CircleIndicator;

// 이달의 금어기 화면 액티비티 정의

public class HelpActivity extends AppCompatActivity {
    ImageButton help_back_imageButton; //뒤로 가기 버튼
    FragmentPagerAdapter viewPagerAdapter; //ViewPager 어댑터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_help);

        setComponentsInteraction();

        ViewPager viewPager = findViewById(R.id.help_viewPager);
        viewPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        CircleIndicator indicator = findViewById(R.id.help_circleIndicator);
        indicator.setViewPager(viewPager);
    }


    ////////////////////코드 정리 예정
    public static class MyPagerAdapter extends FragmentPagerAdapter {
        /***
         * 참고자료
         * https://github.com/ongakuer/CircleIndicator
         * https://webnautes.tistory.com/1013
         ***/
        private static int NUM_ITEMS = 10; //임시값

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return HelpFragment.newInstance(position);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.help_back_imageButton = (ImageButton) findViewById(R.id.help_back_imageButton);

        this.help_back_imageButton.setOnClickListener(v -> //뒤로 가기 버튼에 대한 클릭 리스너
        {
            onBackPressed();
        });
    }
}


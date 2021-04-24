package com.knu.fishdic.viewpager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

////////////////////코드 정리 예정
//배너에서 사용하기 위해 시간마다 자동으로 넘어가는 기능 추가해야함 https://salix97.tistory.com/90
public class MyPagerAdapter extends FragmentPagerAdapter {
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
        return MyFragment.newInstance(position);
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }
}
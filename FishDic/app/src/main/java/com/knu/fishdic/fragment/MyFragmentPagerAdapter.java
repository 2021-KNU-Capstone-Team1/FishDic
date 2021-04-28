package com.knu.fishdic.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.knu.fishdic.utils.BitmapUtility;

////////////////////코드 정리 예정
//배너에서 사용하기 위해 시간마다 자동으로 넘어가는 기능 추가해야함 https://salix97.tistory.com/90

// https://developer.android.com/reference/androidx/fragment/app/FragmentPagerAdapter

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    /***
     * 참고자료
     * https://github.com/ongakuer/CircleIndicator
     * https://webnautes.tistory.com/1013
     ***/
    private int totalItemCount; //전체 항목의 수
    private Bitmap[] refImages; //전체 이미지 참조 변수

    public MyFragmentPagerAdapter(FragmentManager fragmentManager, int totalItemCount, Bitmap[] images) {
        super(fragmentManager);

        if (totalItemCount < 0)
            this.totalItemCount = 0;
        else
            this.totalItemCount = totalItemCount;

        this.refImages = images;
    }

    @Override
    public int getCount() { //전체 항목의 수 반환
        return this.totalItemCount;
    }

    @Override
    public Fragment getItem(int position) { //해당 위치(position)를 위한 Fragment 반환
        Log.d("반환 된 Fragment 현재 pos : ", String.valueOf(position));

        Bundle args = new Bundle();
        args.putParcelable(MyFragment.IMAGE_KEY_VALUE, this.refImages[position]);
        return MyFragment.newInstance(MyFragment.FRAGMENT_TYPE.HELP, position, args); //해당 위치(position)의 이미지를 전달하여 새로운 Fragment 개체 생성
    }
}
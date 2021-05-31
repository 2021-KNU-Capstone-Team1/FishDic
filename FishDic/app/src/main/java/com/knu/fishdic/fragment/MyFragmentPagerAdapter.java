package com.knu.fishdic.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

// 메인화면의 배너, 이용가이드를 위한 MyFragmentPagerAdapter 정의
// https://developer.android.com/reference/androidx/fragment/app/FragmentPagerAdapter

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private final MyFragment.FRAGMENT_TYPE fragmentType; //현재 Fragment의 타입
    private Bitmap[] refImages; //전체 이미지 참조 변수

    public MyFragmentPagerAdapter(FragmentManager fragmentManager, Bundle args) {
        /***
         * fragmentManager : Activity 내부의 Fragment 개체와 상호작용 위한 인터페이스
         * args : 인스턴스 객체 생성을 위한 Fragment의 타입, 이미지들
         ***/

        super(fragmentManager);
        this.fragmentType = (MyFragment.FRAGMENT_TYPE) args.getSerializable(MyFragment.FRAGMENT_TYPE_KEY_VALUE);
        this.refImages = (Bitmap[]) args.getParcelableArray(MyFragment.IMAGE_KEY_VALUE);
    }

    @Override
    public int getCount() { //전체 항목의 수 반환
        return this.refImages.length;
    }

    @Override
    public Fragment getItem(int position) { //해당 위치(position)를 위한 Fragment 반환
        Bundle args = new Bundle();
        args.putSerializable(MyFragment.FRAGMENT_TYPE_KEY_VALUE, this.fragmentType);
        args.putParcelable(MyFragment.IMAGE_KEY_VALUE, this.refImages[position]);

        return MyFragment.newInstance(position, args); //Fragment의 타입, 해당 위치(position)의 이미지를 전달하여 새로운 Fragment 개체 생성
    }
}
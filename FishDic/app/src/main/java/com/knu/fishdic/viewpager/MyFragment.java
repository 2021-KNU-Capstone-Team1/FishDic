package com.knu.fishdic.viewpager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.knu.fishdic.R;

////////////////////코드 정리 예정
public class MyFragment extends Fragment {
    private int pageIndex;

    // newInstance constructor for creating fragment with arguments
    public static MyFragment newInstance(int pageIndex) {
        MyFragment fragment = new MyFragment();
        Bundle args = new Bundle();
        args.putInt("pageIndex", pageIndex);
        fragment.setArguments(args);
        return fragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageIndex = getArguments().getInt("pageIndex", 0);
        //페이지 인덱스에 따라 내부 이미지 뷰의 이미지 변경하도록 수정 예정
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewpager_fragment, container, false);
        return view;
    }
}

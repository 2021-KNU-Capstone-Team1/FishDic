package com.knu.fishdic;

import android.app.Application;
import android.content.Context;

import com.knu.fishdic.manager.DBManager;
import com.knu.fishdic.recyclerview.RecyclerAdapter;

// 전역 앱 상태 관리, 공용 컴포넌트 사용을 위한 FishDic 정의
// https://developer.android.com/reference/android/app/Application

public class FishDic extends Application {
    public static Context globalContext; //전역 앱 Context (앱 실행 후 종료 시 까지 유지)
    public static DBManager globalDBManager; //전역 DBManager

    //앱 로딩 시점에 DB로부터 바인딩 작업 수행 위한 도감, 이달의 금어기 RecyclerAdapter
    public static RecyclerAdapter globalDicRecyclerAdapter;
    public static RecyclerAdapter globalDeniedFishRecyclerAdapter;

    @Override
    public void onCreate() { //최초 앱 가동 시
        super.onCreate();
        globalContext = getApplicationContext();
        globalDBManager = null;
        globalDicRecyclerAdapter = globalDeniedFishRecyclerAdapter = null;
    }

    @Override
    public void onTerminate() { //앱 종료 시
        globalDBManager.close();
        super.onTerminate();
    }
}
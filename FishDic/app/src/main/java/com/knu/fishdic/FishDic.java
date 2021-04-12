package com.knu.fishdic;

import android.app.Application;
import android.content.Context;

// 전역 앱 상태 관리, 공용 컴포넌트 사용을 위한 FishDic 정의
// https://developer.android.com/reference/android/app/Application

public class FishDic extends Application {
    public static Context globalContext; //전역 앱 Context (앱 실행 후 종료 시 까지 유지)
    //public static ArrayList<RecyclerItem> dicItemArray = null; //도감에 사용 할 데이터 배열
    // 이달의 금어기는 도감에 사용 할 데이터 배열에서 시스템 현재 시간을 기준으로 뽑아서 출력
    
    ///도감의 컴포넌트에서 사용 할 DB 데이터들 넣을 예정
    //해당 데이터들은 initmanager에 의해 최초 앱 실행 시 dbmanager를 통한 초기화 수행

    @Override
    public void onCreate() { //최초 앱 가동 시
        super.onCreate();
        this.globalContext = getApplicationContext();
    }

    @Override
    public void onTerminate() { //앱 종료 시
        super.onTerminate();
    }
}
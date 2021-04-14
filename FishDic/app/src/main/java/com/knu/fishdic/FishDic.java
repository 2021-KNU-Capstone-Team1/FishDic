package com.knu.fishdic;

import android.app.Application;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.knu.fishdic.manager.DBManager;

// 전역 앱 상태 관리, 공용 컴포넌트 사용을 위한 FishDic 정의
// https://developer.android.com/reference/android/app/Application

public class FishDic extends Application {
    public static Context globalContext; //전역 앱 Context (앱 실행 후 종료 시 까지 유지)
    public static DBManager globalDBManager; //전역 DBManager
    public static GestureDetector globalGestureDetector; //전역 제스처 및 이벤트 감지위한 개체

    @Override
    public void onCreate() { //최초 앱 가동 시
        super.onCreate();
        globalContext = getApplicationContext();
        globalDBManager = null;
        globalGestureDetector = new GestureDetector(globalContext, new GestureDetector.SimpleOnGestureListener() { //누르고 뗄 때, 단일 터치 인식 수행
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public void onTerminate() { //앱 종료 시
        super.onTerminate();
        globalDBManager.close();
    }
}
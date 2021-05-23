package com.knu.fishdic;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.StrictMode;

import androidx.core.app.NotificationManagerCompat;

import com.androidnetworking.AndroidNetworking;
import com.knu.fishdic.manager.DBManager;
import com.knu.fishdic.recyclerview.RecyclerAdapter;

// 전역 앱 상태 관리, 공용 컴포넌트 사용을 위한 FishDic 정의
// https://developer.android.com/reference/android/app/Application

public class FishDic extends Application {
    public static String CACHE_PATH = ""; //임시폴더 경로

    public static final String PUBLIC_BANNER_SERVER = "http://fishdic.asuscomm.com/Banner/";
    public static final String PUBLIC_DB_SERVER = "http://fishdic.asuscomm.com/DB/";
    public static final String VERSION_FILE_NAME = "version"; //버전 관리 파일 이름

    public static String BANNER_IMAGES_PATH; //배너 이미지 경로
    public static String HELP_IMAGES_PATH; //이용가이드 이미지 경로

    public static Context globalContext; //전역 앱 Context (앱 실행 후 종료 시 까지 유지)
    public static DBManager globalDBManager; //전역 DBManager

    //앱 로딩 시점에 DB로부터 바인딩 작업 수행 위한 도감, 이달의 금어기 RecyclerAdapter
    public static RecyclerAdapter globalDicRecyclerAdapter;
    public static RecyclerAdapter globalDeniedFishRecyclerAdapter;

    public static Bitmap[] bannerImages; //배너 이미지
    public static Bitmap[] helpImages; //이용가이드 이미지

    //TODO : 다운로드 진행상황 표시 (상단 알림쪽에)
    NotificationManagerCompat notificationManager; //알림 관리자
    public enum NOTIFICATION_TYPE { //알림 타입
        DOWNLOAD
    }


    @Override
    public void onCreate() { //최초 앱 가동 시
        super.onCreate();
        globalContext = getApplicationContext();
        globalDBManager = null;
        globalDicRecyclerAdapter = globalDeniedFishRecyclerAdapter = null;

        CACHE_PATH = globalContext.getCacheDir().toString() + "/";
        BANNER_IMAGES_PATH = "/data/data/" + globalContext.getPackageName() + "/banner/"; //배너 이미지 경로 "/data/data/앱 이름/banner/"
        HELP_IMAGES_PATH = "/data/data/" + globalContext.getPackageName() + "/help/"; //이용가이드 이미지 경로 "/data/data/앱 이름/help/"
        bannerImages = helpImages = null;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); //UI 스레드에서 동기 작업을 위한 네트워크 연결 허용하도록 설정
        AndroidNetworking.initialize(globalContext); //네트워킹 작업을 위한 Fast-Android-Networking 초기화

        NotificationManagerCompat.from(globalContext);
    }

    @Override
    public void onTerminate() { //앱 종료 시
        globalDBManager.close();
        super.onTerminate();
    }
}
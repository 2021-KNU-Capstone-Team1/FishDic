package com.knu.fishdic;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.StrictMode;

import androidx.core.app.NotificationManagerCompat;

import com.androidnetworking.AndroidNetworking;
import com.knu.fishdic.manager.DBManager;
import com.knu.fishdic.manager.FishIdentificationManager;
import com.knu.fishdic.recyclerview.RecyclerAdapter;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

// 전역 앱 상태 관리, 공용 컴포넌트 사용을 위한 FishDic 정의
// https://developer.android.com/reference/android/app/Application

public class FishDic extends Application {
    public static String CACHE_PATH = ""; //임시 폴더 경로

    public static final String PUBLIC_BANNER_SERVER = "http://fishdic.asuscomm.com/banner/";
    public static final String PUBLIC_DB_SERVER = "http://fishdic.asuscomm.com/DB/";
    public static final String PUBLIC_MODEL_SERVER = "http://fishdic.asuscomm.com/Model/";
    public static final String VERSION_FILE_NAME = "version"; //버전 관리 파일 이름

    public static String BANNER_IMAGES_PATH; //배너 이미지 경로
    public static String HELP_IMAGES_PATH; //이용가이드 이미지 경로

    public static Context globalContext; //전역 앱 Context (앱 실행 후 종료 시 까지 유지)
    public static DBManager globalDBManager; //전역 데이터베이스 관리를 위한 DBManager
    public static FishIdentificationManager globalFishIdentificationManager; //전역 어류 판별 관리를 위한 FishIdentificationManager

    /*** 앱 로딩 시점에 DB로부터 바인딩 작업 수행 위한 도감, 이달의 금어기 RecyclerAdapter ***/
    public static RecyclerAdapter globalDicRecyclerAdapter;
    public static RecyclerAdapter globalDeniedFishRecyclerAdapter;
    public static RecyclerAdapter globalFishIdentificationRecyclerAdapter; //어류 판별 결과를 보여주기 위한 RecyclerAdapter

    public static Bitmap[] bannerImages; //배너 이미지
    public static Bitmap[] helpImages; //이용가이드 이미지

   // public static String NOTIFICATION_CHANNEL_ID = "FishDicNotificationChannel"; //알림 채널 아이디

    @Override
    public void onCreate() { //최초 앱 가동 시
        super.onCreate();

        globalContext = getApplicationContext();

        globalDBManager = null;
        globalFishIdentificationManager = null;

        globalDicRecyclerAdapter = globalDeniedFishRecyclerAdapter = globalFishIdentificationRecyclerAdapter = null;

        CACHE_PATH = globalContext.getCacheDir().toString() + "/";
        BANNER_IMAGES_PATH = "/data/data/" + globalContext.getPackageName() + "/banner/"; //배너 이미지 경로 "/data/data/앱 이름/banner/"
        HELP_IMAGES_PATH = "/data/data/" + globalContext.getPackageName() + "/help/"; //이용가이드 이미지 경로 "/data/data/앱 이름/help/"

        bannerImages = helpImages = null;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); //UI 스레드에서 동기 작업을 위한 네트워크 연결 허용하도록 설정

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(2, TimeUnit.SECONDS) //서버 연결 제한시간 설정
                .build();
        AndroidNetworking.initialize(globalContext, okHttpClient); //네트워킹 작업을 위한 Fast-Android-Networking 초기화

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //알림 채널 생성
            CharSequence name = getString(R.string.app_name);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        */
    }

    @Override
    public void onTerminate() { //앱 종료 시
        globalDBManager.close();

        super.onTerminate();
    }
}
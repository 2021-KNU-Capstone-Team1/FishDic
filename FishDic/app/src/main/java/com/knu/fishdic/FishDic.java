package com.knu.fishdic;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.StrictMode;

import com.androidnetworking.AndroidNetworking;
import com.knu.fishdic.manager.DBManager;
import com.knu.fishdic.manager.FishIdentificationManager;
import com.knu.fishdic.manager.SettingsManager;
import com.knu.fishdic.recyclerview.RecyclerAdapter;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

// 전역 앱 상태 관리, 공용 컴포넌트 사용을 위한 FishDic 정의
// https://developer.android.com/reference/android/app/Application
// https://developer.android.com/reference/android/content/SharedPreferences

public class FishDic extends Application {
    public static final String VERSION_FILE_NAME = "version"; //버전 관리 파일 이름
    public static String CACHE_PATH = ""; //임시 폴더 경로
    public static String DB_PATH = ""; //DB 경로
    public static final String DB_NAME = "FishDicDB.db"; //DB 이름
    public static String MODEL_PATH; //판별 위한 모델 경로
    public static final String MODEL_NAME = "model.tflite"; //판별 위한 모델 이름

    public static Context globalContext; //전역 앱 Context (앱 실행 후 종료 시 까지 유지)
    public static SettingsManager globalSettingsManager; //전역 설정 관리를 위한 SettingsManager
    public static DBManager globalDBManager; //전역 데이터베이스 관리를 위한 DBManager
    public static FishIdentificationManager globalFishIdentificationManager; //전역 어류 판별 관리를 위한 FishIdentificationManager

    /*** 앱 로딩 시점에 DB로부터 바인딩 작업 수행 위한 도감, 이달의 금어기 RecyclerAdapter ***/
    public static RecyclerAdapter globalDicRecyclerAdapter;
    public static RecyclerAdapter globalDeniedFishRecyclerAdapter;
    public static RecyclerAdapter globalFishIdentificationRecyclerAdapter; //어류 판별 결과를 보여주기 위한 RecyclerAdapter

    public static String BANNER_IMAGES_PATH; //배너 이미지 경로
    public static String HELP_IMAGES_PATH; //이용가이드 이미지 경로

    public static Bitmap[] bannerImages; //배너 이미지
    public static Bitmap[] helpImages; //이용가이드 이미지

    // public static String NOTIFICATION_CHANNEL_ID = "FishDicNotificationChannel"; //알림 채널 아이디

    @Override
    public void onCreate() { //최초 앱 가동 시
        super.onCreate();

        globalContext = getApplicationContext();

        globalSettingsManager = null;
        globalDBManager = null;
        globalFishIdentificationManager = null;

        globalDicRecyclerAdapter = globalDeniedFishRecyclerAdapter = globalFishIdentificationRecyclerAdapter = null;

        CACHE_PATH = globalContext.getCacheDir().toString() + "/";
        DB_PATH = "/data/data/" + FishDic.globalContext.getPackageName() + "/databases/"; //안드로이드의 DB 저장 경로는 "/data/data/앱 이름/databases/"
        MODEL_PATH = "/data/data/" + FishDic.globalContext.getPackageName() + "/model/"; //판별용 모델 경로 "/data/data/앱 이름/model/"
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

    public static String getAppVersion() { //앱 버전 반환
        String version = null;

        try {
            PackageInfo i = globalContext.getPackageManager().getPackageInfo(globalContext.getPackageName(), 0);
            version = i.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }
}
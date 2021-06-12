package com.knu.fishdic.manager;

import android.graphics.Bitmap;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.recyclerview.RecyclerAdapter;
import com.knu.fishdic.utils.ImageUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

// 앱 초기화를 위한 InitManager 정의

public class InitManager {
    private final int NOTIFICATION_ID = 2; //알림 아이디
    public static boolean isAllComponentsInitialized = false; //모든 구성요소 초기화 작업 수행여부

    public static void initAllComponents() { //모든 구성요소 초기화
        if (!isAllComponentsInitialized) {
            UpdateManager.updateAll(); //모두 업데이트

            initGlobalManager(); //전역 Manager 초기화
            initGlobalRecyclerAdapter();//전역 RecyclerAdapter 초기화

            doDataBindJobForDic(); //도감을 위한 데이터 바인딩 작업 수행
            doDataBindJobForDeniedFish(); //이달의 금어기를 위한 데이터 바인딩 작업 수행

            initBannerImages(); //배너 이미지 초기 작업 수행
            initHelpImages(); //이용가이드 초기 작업 수행

            isAllComponentsInitialized = true;
        }
    }

    private static void initGlobalRecyclerAdapter() { //전역 RecyclerAdapter 초기화
        FishDic.globalDicRecyclerAdapter = new RecyclerAdapter();
        FishDic.globalDeniedFishRecyclerAdapter = new RecyclerAdapter();
        FishDic.globalFishIdentificationRecyclerAdapter = new RecyclerAdapter();
    }

    private static void initGlobalManager() { //전역 Manager 초기화
        FishDic.globalDBManager = new DBManager();
        FishDic.globalFishIdentificationManager = new FishIdentificationManager();
    }

    private static void doDataBindJobForDic() { //도감을 위한 데이터 바인딩 작업 수행
        if (FishDic.globalDicRecyclerAdapter == null || FishDic.globalDBManager == null)
            try {
                throw new Exception("Not Initialized globalDicRecyclerAdapter or globalDBManager");
            } catch (Exception e) {
                e.printStackTrace();
            }

        FishDic.globalDicRecyclerAdapter.addItemFromBundle(FishDic.globalDBManager.getSimpleFishBundle(DBManager.FISH_DATA_TYPE.ALL_FISH, null));
    }

    private static void doDataBindJobForDeniedFish() { //이달의 금어기를 위한 데이터 바인딩 작업 수행
        if (FishDic.globalDeniedFishRecyclerAdapter == null || FishDic.globalDBManager == null)
            try {
                throw new Exception("Not Initialized globalDeniedFishRecyclerAdapter or globalDBManager");
            } catch (Exception e) {
                e.printStackTrace();
            }

        FishDic.globalDeniedFishRecyclerAdapter.addItemFromBundle(FishDic.globalDBManager.getSimpleFishBundle(DBManager.FISH_DATA_TYPE.DENIED_FISH, null));
    }

    private static void initBannerImages() { //배너 이미지 초기화 작업 수행
        File dir = new File(FishDic.BANNER_IMAGES_PATH);
        if (!dir.exists()) { //위에서 이미 배너 이미지 디렉토리가 할당되었어야 함
            try {
                throw new Exception("Banner Integrity ERR");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File[] bannerImagesList = dir.listFiles((dir1, name) -> {
            //버전 관리 파일 제외한 모든 파일만 허용
            return !name.matches(FishDic.VERSION_FILE_NAME);
        }); //배너 이미지 목록

        int bannerImagesCount = bannerImagesList.length; //배너 이미지 수
        if (bannerImagesCount > 0) { //배너 이미지 존재 시
            FishDic.bannerImages = new Bitmap[bannerImagesCount];
            for (int index = 0; index < bannerImagesCount; index++) {

                FileInputStream fileInputStream;
                try {
                    fileInputStream = new FileInputStream(bannerImagesList[index]);
                    byte[] buffer = new byte[fileInputStream.available()];
                    fileInputStream.read(buffer);

                    FishDic.bannerImages[index] = ImageUtility.decodeFromByteArray(buffer); //비트맵 이미지 할당
                    fileInputStream.close();

                } catch (IOException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
            }
        } else { //배너 이미지가 없을 경우
            debugBannerImages(); //테스트용 배너 이미지 초기화 작업 수행
        }
    }

    private static void debugBannerImages() { //테스트용 배너 이미지 초기화 작업 수행 (대체 흐름)
        InputStream inputStream;

        try {
            String[] bannerImagesList = FishDic.globalContext.getAssets().list("banner/"); //테스트용 배너 이미지 리스트
            int bannerImagesCount = bannerImagesList.length; //배너 이미지 수

            FishDic.bannerImages = new Bitmap[bannerImagesCount];

            for (int index = 0; index < bannerImagesCount; index++) {
                inputStream = FishDic.globalContext.getAssets().open("banner/" + bannerImagesList[index]);

                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);

                FishDic.bannerImages[index] = ImageUtility.decodeFromByteArray(buffer); //비트맵 이미지 할당
                inputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initHelpImages() { //이용가이드 초기화 작업 수행
        InputStream inputStream;

        try {
            String[] helpImagesList = FishDic.globalContext.getAssets().list("help/"); //이용가이드 이미지 리스트
            int helpImagesCount = helpImagesList.length; //이용가이드 이미지 수

            FishDic.helpImages = new Bitmap[helpImagesCount];

            for (int index = 0; index < helpImagesCount; index++) {
                inputStream = FishDic.globalContext.getAssets().open("help/" + helpImagesList[index]);

                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);

                FishDic.helpImages[index] = ImageUtility.decodeFromByteArray(buffer); //비트맵 이미지 할당
                inputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
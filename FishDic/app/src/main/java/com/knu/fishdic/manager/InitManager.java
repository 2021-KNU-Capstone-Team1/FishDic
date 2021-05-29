package com.knu.fishdic.manager;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.knu.fishdic.FishDic;
import com.knu.fishdic.recyclerview.RecyclerAdapter;
import com.knu.fishdic.utils.ImageUtility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import okhttp3.Response;

// 앱 초기화를 위한 InitManager 정의

public class InitManager {
    private enum BANNER_STATE { //배너 상태 정의
        INIT, //초기 상태
        OUT_DATED, //구 버전
        UPDATED,//갱신 된 버전
        FAILURE //배너 상태 확인 실패 (테스트용 배너 이미지를 사용하는 대체 흐름 수행)
    }

    public static void initGlobalRecyclerAdapter() { //전역 RecyclerAdapter 초기화
        FishDic.globalDicRecyclerAdapter = new RecyclerAdapter();
        FishDic.globalDeniedFishRecyclerAdapter = new RecyclerAdapter();
        FishDic.globalFishIdentificationRecyclerAdapter = new RecyclerAdapter();
    }

    public static void doDataBindJobForDic() { //도감을 위한 데이터 바인딩 작업 수행
        if(FishDic.globalDicRecyclerAdapter == null)
            return;

        FishDic.globalDicRecyclerAdapter.addItemFromBundle(FishDic.globalDBManager.getSimpleFishBundle(DBManager.FISH_DATA_TYPE.ALL_FISH));
    }

    public static void doDataBindJobForDeniedFish() { //이달의 금어기를 위한 데이터 바인딩 작업 수행
        if(FishDic.globalDeniedFishRecyclerAdapter == null)
            return;

        FishDic.globalDeniedFishRecyclerAdapter.addItemFromBundle(FishDic.globalDBManager.getSimpleFishBundle(DBManager.FISH_DATA_TYPE.DENIED_FISH));
    }

    public static void initBannerImages() { //배너 이미지 초기화 작업 수행
        if (FishDic.bannerImages != null)
            return;

        switch (getCurrentBannerState()) { //기존 배너 상태 확인
            case INIT: //초기 상태일 경우
            case OUT_DATED: //구 버전일 경우
                updateBannerFromServer();
                break;

            case UPDATED: //최신 버전일 경우
                break;

            case FAILURE: //배너 상태 확인 실패 (테스트용 배너 이미지를 사용하는 대체 흐름 수행)
                debugBannerImages();
                return;
        }

        /*** 배너 이미지 할당 ***/
        File dir = new File(FishDic.BANNER_IMAGES_PATH);
        if (!dir.exists()) { //위에서 이미 배너 이미지 디렉토리가 할당 되었어야 함
            try {
                throw new Exception("Banner Integrity ERR");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File[] bannerImagesList = dir.listFiles((dir1, name) -> {
            if (name.matches("version")) //버전 관리 파일 제외한 모든 파일만 허용
                return false;
            return true;
        }); //배너 이미지 목록

        int bannerImagesCount = bannerImagesList.length; //배너 이미지 수
        FishDic.bannerImages = new Bitmap[bannerImagesCount];
        for (int index = 0; index < bannerImagesCount; index++) {

            FileInputStream fileInputStream = null;
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
    }

    private static BANNER_STATE getCurrentBannerState() { //현재 배너 상태 반환
        /***
         * 1) 로컬 배너 이미지와 로컬 배너 버전 관리 파일이 존재하지 않을 경우 서버로부터의 갱신을 위한 초기 상태 반환
         * 2) 로컬 배너 이미지와 로컬 배너 버전 관리 파일이 존재할 경우 서버와 로컬 배너 버전을 비교하여
         *  2-1) 로컬 배너 버전 < 서버 배너 버전일 경우 : 구 버전 상태 반환
         *  2-2) 로컬 배너 버전 == 서버 배너 버전일 경우 : 최신 버전 상태 반환
         *  2-3) 로컬 배너 버전 > 서버 배너 버전일 경우 : 무결성 오류
         ***/

        File dir = new File(FishDic.BANNER_IMAGES_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }

        final File currentBannerVersionFile = new File(FishDic.BANNER_IMAGES_PATH + FishDic.VERSION_FILE_NAME); //로컬 배너 버전 파일
        boolean currentBannerExists = dir.listFiles().length > 1 & currentBannerVersionFile.exists(); //로컬 배너 존재 여부 (디렉토리 내의 버전 파일을 제외한 파일 수가 1개 이상 혹은 버전 파일 존재 모두 만족 않을 시 무결성이 깨진 걸로 간주)

        int currentBannerVersion = -1; //로컬 배너 버전
        int serverBannerVersion = -1; //서버 배너 버전

        /*** 서버의 배너 버전 확인 ***/
        ANRequest request = AndroidNetworking
                .download(FishDic.PUBLIC_BANNER_SERVER + FishDic.VERSION_FILE_NAME, FishDic.CACHE_PATH, FishDic.VERSION_FILE_NAME)
                .doNotCacheResponse()
                .build()
                .setDownloadProgressListener((bytesDownloaded, totalBytes) -> {
                });
        ANResponse<String> response = request.executeForDownload();

        if (response.isSuccess()) {
            Response okHttpResponse = response.getOkHttpResponse();
            Log.d("Server Banner Version Check", "headers : " + okHttpResponse.headers().toString());
            Log.d("Server Banner Version Check", "body : " + okHttpResponse.body().toString());
            Log.d("Server Banner Version Check", "HTTP Status Code : " + okHttpResponse.code());

            File serverBannerVersionFile = new File(FishDic.CACHE_PATH + FishDic.VERSION_FILE_NAME);

            try {
                BufferedReader serverBannerVersionReader = new BufferedReader(new FileReader(serverBannerVersionFile));
                serverBannerVersion = Integer.parseInt(serverBannerVersionReader.readLine());
                serverBannerVersionReader.close();

                if (currentBannerExists) { //로컬 Banner 존재 시 버전 읽어오기
                    BufferedReader currentBannerVersionReader = new BufferedReader(new FileReader(currentBannerVersionFile));
                    currentBannerVersion = Integer.parseInt(currentBannerVersionReader.readLine());
                    currentBannerVersionReader.close();
                } else { //로컬 Banner가 존재하지 않을 시 다운로드 받은 서버의 Banner 버전 파일을 로컬 Banner의 버전 파일로 이동 및 초기 상태 반환
                    Files.move(serverBannerVersionFile.toPath(), Paths.get(FishDic.BANNER_IMAGES_PATH + FishDic.VERSION_FILE_NAME));
                    return BANNER_STATE.INIT;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.i("로컬 Banner 버전", String.valueOf(currentBannerVersion));
            Log.i("서버 Banner 버전", String.valueOf(serverBannerVersion));

        } else { //서버 접속 오류 시
            ANError error = response.getError();
            Log.d("Server Banner Version Check ERR", error.getMessage());

            return BANNER_STATE.FAILURE;
        }

        if (currentBannerVersion < serverBannerVersion) { //로컬 Banner 버전 < 서버 Banner 버전일 경우 : 구 버전 상태 반환
            return BANNER_STATE.OUT_DATED;
        } else if (currentBannerVersion == serverBannerVersion) { //로컬 Banner 버전 == 서버 Banner 버전일 경우 : 최신 버전 상태 반환
            return BANNER_STATE.UPDATED;
        } else { //로컬 Banner 버전 > 서버 Banner 버전일 경우 : 무결성 오류
            try {
                throw new Exception("Banner Integrity ERR");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return BANNER_STATE.FAILURE;
    }

    private static void updateBannerFromServer() { //서버로부터 최신 배너 이미지 갱신
        File dir = new File(FishDic.BANNER_IMAGES_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }

        ANRequest request = AndroidNetworking.get(FishDic.PUBLIC_BANNER_SERVER + "request_bannerlist.php") //서버의 최신 배너 이미지 리스트 확인
                .doNotCacheResponse()
                .build();

        ANResponse<String> response = request.executeForString();
        if (response.isSuccess()) {
            String[] bannerImagesList = response.getResult().split("\r\n"); //줄바꿈 문자로 분리

            for (int index = 0; index < bannerImagesList.length; index++) {
                Log.d("bannerImageList[" + index + "]", bannerImagesList[index]);

                ANRequest subRequest = AndroidNetworking //서버로투버 최신 배너 이미지들 다운로드
                        .download(FishDic.PUBLIC_BANNER_SERVER + bannerImagesList[index], FishDic.BANNER_IMAGES_PATH, bannerImagesList[index])
                        .doNotCacheResponse()
                        .build()
                        .setDownloadProgressListener((bytesDownloaded, totalBytes) -> {
                        });
                ANResponse<String> subResponse = subRequest.executeForDownload();

                if (subResponse.isSuccess()) {
                    Response okHttpResponse = subResponse.getOkHttpResponse();
                    Log.d("Server Banner Download", "headers : " + okHttpResponse.headers().toString());
                    Log.d("Server Banner Download", "body : " + okHttpResponse.body().toString());
                    Log.d("Server Banner Download", "HTTP Status Code : " + okHttpResponse.code());

                } else { //다운로드 오류 시
                    ANError error = subResponse.getError();
                    Log.e("Server Banner Download ERR", error.getMessage());
                }
            }
        } else {
            ANError error = response.getError();
            Log.e("Request Banner List ERR", error.getMessage());
        }
    }

    private static void debugBannerImages() { //테스트용 배너 이미지 초기화 작업 수행 (대체 흐름)
        AssetManager assetManager = FishDic.globalContext.getAssets();
        InputStream inputStream;

        try {
            String[] bannerImagesList = assetManager.list("banner/"); //테스트용 배너 이미지 리스트
            int bannerImagesCount = bannerImagesList.length; //배너 이미지 수

            FishDic.bannerImages = new Bitmap[bannerImagesCount];

            for (int index = 0; index < bannerImagesCount; index++) {
                inputStream = assetManager.open("banner/" + bannerImagesList[index]);

                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);

                FishDic.bannerImages[index] = ImageUtility.decodeFromByteArray(buffer); //비트맵 이미지 할당
                inputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initHelpImages() { //이용가이드 초기 작업 수행
        AssetManager assetManager = FishDic.globalContext.getAssets();
        InputStream inputStream;

        try {
            String[] helpImagesList = assetManager.list("help/"); //이용가이드 이미지 리스트
            int helpImagesCount = helpImagesList.length; //이용가이드 이미지 수

            FishDic.helpImages = new Bitmap[helpImagesCount];

            for (int index = 0; index < helpImagesCount; index++) {
                inputStream = assetManager.open("help/" + helpImagesList[index]);

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
package com.knu.fishdic.manager;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.knu.fishdic.FishDic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import okhttp3.Response;

// 서버로부터의 업데이트 기능을 위한 UpdateManager 정의

public class UpdateManager {
    private static final String PUBLIC_DB_SERVER = "http://fishdic.asuscomm.com/DB/";
    private static final String PUBLIC_MODEL_SERVER = "http://fishdic.asuscomm.com/Model/";
    private static final String PUBLIC_BANNER_SERVER = "http://fishdic.asuscomm.com/banner/";
    private static final String REQUEST_BANNERLIST = "request_bannerlist.php"; //배너 목록 요청

    private enum UPDATE_TARGET { //업데이트 타겟 대상 정의
        DB, //어류 데이터베이스
        MODEL, //어류 판별 위한 모델
        BANNER //배너
    }

    private enum VERSION_STATE { //버전 상태 정의
        INIT, //초기 상태
        OUT_DATED, //구 버전
        UPDATED, //갱신 된 버전
        DELAYED_FAILURE, //버전 상태 확인 실패 (지연 된 갱신 수행)
        IMMEDIATE_FAILURE //버전 상태 확인 실패 (assets으로부터 복사하는 대체 흐름 수행)
    }

    public static void updateAll() { //모두 업데이트
        UPDATE_TARGET[] updateTargets = UPDATE_TARGET.values(); //업데이트 타겟 대상 할당

        for (int i = 0; i < updateTargets.length; i++) {
            switch (getCurrentVersionState(updateTargets[i])) { //현재 버전 상태 확인
                case INIT: //초기 상태일 경우
                case OUT_DATED: //구 버전일 경우
                    updateFromServer(updateTargets[i]);
                    break;

                case UPDATED: //최신 버전일 경우
                case DELAYED_FAILURE: //버전 상태 확인 실패 (지연 된 갱신 수행)
                    break;

                case IMMEDIATE_FAILURE: //버전 상태 확인 실패
                    if (updateTargets[i] != UPDATE_TARGET.BANNER) //배너 이미지에 대해 InitManager에서 할당 수행
                        copyFromAssets(updateTargets[i]);
                    break;
            }
        }
    }

    private static VERSION_STATE getCurrentVersionState(UPDATE_TARGET currentUpdateTarget) { //현재 버전 상태 반환
        /***
         * 1) 로컬 업데이트 타겟 대상과 로컬 타겟 대상 버전 관리 파일이 존재하지 않을 경우 서버로부터의 갱신을 위한 초기 상태 반환
         * 2) 로컬 업데이트 타겟 대상과 로컬 타겟 대상 버전 관리 파일이 존재할 경우 서버와 로컬 버전을 비교하여
         *  2-1) 로컬 타겟 대상 버전 < 서버 타겟 대상 버전일 경우 : 구 버전 상태 반환
         *  2-2) 로컬 타겟 대상 버전 == 서버 타겟 대상 버전일 경우 : 최신 버전 상태 반환
         *  2-3) 로컬 타겟 대상 버전 > 서버 타겟 대상 버전일 경우 : 무결성 오류
         ***/

        File dir;
        File localTargetFile; //로컬 업데이트 타겟 대상 파일
        File localTargetVersionFile; //로컬 업데이트 타겟 대상 버전 관리 파일

        String localTargetPath; //로컬 업데이트 대상 경로
        String localTargetFileName = null; //로컬 업데이트 대상 파일 이름
        String updateServer; //업데이트 서버

        boolean localTargetExists; //로컬 타겟 대상 존재 여부

        int localTargetVersion = -1; //로컬 타겟 대상 버전
        int serverTargetVersion = -1; //서버 타겟 대상 버전

        switch (currentUpdateTarget) { //업데이트 대상 경로, 대상 이름, 업데이트 서버 설정
            case DB: //어류 데이터베이스
                localTargetPath = FishDic.DB_PATH;
                localTargetFileName = FishDic.DB_NAME;
                updateServer = PUBLIC_DB_SERVER;

                Log.d("Checking newest DB Version", "---");
                break;

            case MODEL: //어류 판별 위한 모델
                localTargetPath = FishDic.MODEL_PATH;
                localTargetFileName = FishDic.MODEL_NAME;
                updateServer = PUBLIC_MODEL_SERVER;

                Log.d("Checking newest Model Version", "---");
                break;

            case BANNER: //배너
                localTargetPath = FishDic.BANNER_IMAGES_PATH;
                updateServer = PUBLIC_BANNER_SERVER;

                Log.d("Checking newest Banner Version", "---");
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + currentUpdateTarget);
        }

        dir = new File(localTargetPath);

        if (!dir.exists()) {
            dir.mkdir();
        }

        localTargetFile = new File(localTargetPath + localTargetFileName);
        localTargetVersionFile = new File(localTargetPath + FishDic.VERSION_FILE_NAME);

        switch (currentUpdateTarget) { //로컬 업데이트 타겟 대상 존재 여부 판별
            case DB: //어류 데이터베이스
            case MODEL: //어류 판별 위한 모델
                localTargetExists = localTargetFile.exists() & localTargetVersionFile.exists(); //로컬 업데이트 타겟 대상 존재 여부 (타겟 대상 파일 혹은 버전 관리 파일 하나라도 존재 하지 않을 시 무결성이 깨진 걸로 간주)
                break;

            case BANNER: //배너
                localTargetExists = dir.listFiles().length > 1 & localTargetVersionFile.exists(); //로컬 업데이트 타겟 대상 존재 여부 (디렉토리 내의 버전 관리 파일을 제외한 파일 수가 1개 이상 혹은 버전 파일 존재 모두 만족 않을 시 무결성이 깨진 걸로 간주)
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + currentUpdateTarget);
        }

        /*** 서버 업데이트 타겟 대상과 로컬 업데이트 타겟 대상 버전 비교 ***/
        ANRequest request = AndroidNetworking
                .download(updateServer + FishDic.VERSION_FILE_NAME, FishDic.CACHE_PATH, FishDic.VERSION_FILE_NAME)
                .doNotCacheResponse()
                .build()
                .setDownloadProgressListener((bytesDownloaded, totalBytes) -> {
                });
        ANResponse<String> response = request.executeForDownload();

        if (response.isSuccess()) {
            Response okHttpResponse = response.getOkHttpResponse();

            Log.d("Server Version Check", "headers :" + okHttpResponse.headers().toString());
            Log.d("Server Version Check", "body :" + okHttpResponse.body().toString());
            Log.d("Server Version Check", "HTTP Status Code :" + okHttpResponse.code());

            File serverTargetVersionFile = new File(FishDic.CACHE_PATH + FishDic.VERSION_FILE_NAME); //서버 타겟 대상 버전 관리 파일

            try {
                BufferedReader serverTargetVersionReader = new BufferedReader(new FileReader(serverTargetVersionFile));
                serverTargetVersion = Integer.parseInt(serverTargetVersionReader.readLine());
                serverTargetVersionReader.close();

                if (localTargetExists) { //로컬 타겟 대상 존재 시 버전 읽어오기
                    BufferedReader localTargetVersionReader = new BufferedReader(new FileReader(localTargetVersionFile));
                    localTargetVersion = Integer.parseInt(localTargetVersionReader.readLine());
                    localTargetVersionReader.close();
                } else { //로컬 타겟 대상이 존재하지 않을 시 다운로드 받은 서버의 타겟 대상 버전 관리 파일을 로컬 타겟 대상의 버전 파일로 이동 및 초기 상태 반환
                    Files.move(serverTargetVersionFile.toPath(), Paths.get(localTargetPath + FishDic.VERSION_FILE_NAME));
                    return VERSION_STATE.INIT;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            switch (currentUpdateTarget) {
                case DB: //어류 데이터베이스
                    Log.i("로컬 DB 버전", String.valueOf(localTargetVersion));
                    Log.i("서버 DB 버전", String.valueOf(serverTargetVersion));
                    break;

                case MODEL: //어류 판별 위한 모델
                    Log.i("로컬 모델 버전", String.valueOf(localTargetVersion));
                    Log.i("서버 모델 버전", String.valueOf(serverTargetVersion));
                    break;

                case BANNER: //배너
                    Log.i("로컬 배너 버전", String.valueOf(localTargetVersion));
                    Log.i("서버 배너 버전", String.valueOf(serverTargetVersion));
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + currentUpdateTarget);
            }

        } else { //서버 접속 오류 시
            ANError error = response.getError();
            Log.d("Server Version Check ERR", error.getMessage());

            if (localTargetExists) //현재 업데이트 타겟 대상이 존재하면
                return VERSION_STATE.DELAYED_FAILURE;

            return VERSION_STATE.IMMEDIATE_FAILURE; //현재 업데이트 타겟 대상이 존재하지 않으면
        }

        if (localTargetVersion < serverTargetVersion) { //로컬 업데이트 타겟 대상 버전 < 서버 업데이트 타겟 대상 버전일 경우 : 구 버전 상태 반환
            return VERSION_STATE.OUT_DATED;
        } else if (localTargetVersion == serverTargetVersion) { //로컬 업데이트 타겟 대상 버전 == 서버 업데이트 타겟 대상 버전일 경우 : 최신 버전 상태 반환
            return VERSION_STATE.UPDATED;
        } else { //로컬 업데이트 타겟 대상 버전 > 서버 업데이트 타겟 대상 버전일 경우 : 무결성 오류
            try {
                throw new Exception("Integrity ERR");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return VERSION_STATE.IMMEDIATE_FAILURE;
    }

    private static void updateFromServer(UPDATE_TARGET currentUpdateTarget) { //서버로부터 최신 버전 갱신
        String localTargetPath; //로컬 업데이트 대상 경로
        String localTargetFileName = null; //로컬 업데이트 대상 파일 이름
        String updateServer; //업데이트 서버

        switch (currentUpdateTarget) { //업데이트 대상 경로, 대상 이름, 업데이트 서버 설정
            case DB: //어류 데이터베이스
                localTargetPath = FishDic.DB_PATH;
                localTargetFileName = FishDic.DB_NAME;
                updateServer = PUBLIC_DB_SERVER;

                Log.d("Downloading newest DB From Server", "---");
                break;

            case MODEL: //어류 판별 위한 모델
                localTargetPath = FishDic.MODEL_PATH;
                localTargetFileName = FishDic.MODEL_NAME;
                updateServer = PUBLIC_MODEL_SERVER;

                Log.d("Downloading newest Model From Server", "---");
                break;

            case BANNER: //배너
                localTargetPath = FishDic.BANNER_IMAGES_PATH;
                updateServer = PUBLIC_BANNER_SERVER;

                Log.d("Downloading newest Banner From Server", "---");
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + currentUpdateTarget);
        }

        File dir = new File(localTargetPath);
        if (!dir.exists()) {
            dir.mkdir();
        }

        ANRequest request; //요청
        ANResponse<String> response; //결과
        switch (currentUpdateTarget) { //서버로부터 업데이트 수행
            case DB: //어류 데이터베이스
            case MODEL: //어류 판별 위한 모델
                request = AndroidNetworking
                        .download(updateServer + localTargetFileName, localTargetPath, localTargetFileName)
                        .doNotCacheResponse()
                        .build()
                        .setDownloadProgressListener((bytesDownloaded, totalBytes) -> {
                        });

                response = request.executeForDownload();

                if (response.isSuccess()) {
                    Response okHttpResponse = response.getOkHttpResponse();
                    Log.d("Server Download", "headers : " + okHttpResponse.headers().toString());
                    Log.d("Server Download", "body : " + okHttpResponse.body().toString());
                    Log.d("Server Download", "HTTP Status Code : " + okHttpResponse.code());

                    File serverTargetVersionFile = new File(localTargetPath + FishDic.VERSION_FILE_NAME); //서버로부터 업데이트 된 타겟 버전 파일
                    if (!serverTargetVersionFile.exists())
                        try {
                            throw new Exception("Integrity ERR");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                } else { //다운로드 오류 시
                    ANError error = response.getError();
                    Log.d("Server Download ERR", error.getMessage());

                    copyFromAssets(currentUpdateTarget);
                }
                break;

            case BANNER: //배너
                request = AndroidNetworking.get(updateServer + REQUEST_BANNERLIST) //서버의 최신 배너 이미지 리스트 확인
                        .doNotCacheResponse()
                        .build();

                response = request.executeForString();
                if (response.isSuccess()) {
                    String[] bannerImagesList = response.getResult().split("\r\n"); //서버의 배너 이미지 목록 (줄바꿈 문자로 분리)

                    for (int index = 0; index < bannerImagesList.length; index++) {
                        Log.d("bannerImageList[" + index + "]", bannerImagesList[index]);

                        ANRequest subRequest = AndroidNetworking //서버로부터 최신 배너 이미지들 다운로드
                                .download(updateServer + bannerImagesList[index], FishDic.BANNER_IMAGES_PATH, bannerImagesList[index])
                                .doNotCacheResponse()
                                .build()
                                .setDownloadProgressListener((bytesDownloaded, totalBytes) -> {
                                });
                        ANResponse<String> subResponse = subRequest.executeForDownload();

                        if (subResponse.isSuccess()) {
                            Response okHttpResponse = subResponse.getOkHttpResponse();
                            Log.d("Server Download", "headers : " + okHttpResponse.headers().toString());
                            Log.d("Server Download", "body : " + okHttpResponse.body().toString());
                            Log.d("Server Download", "HTTP Status Code : " + okHttpResponse.code());

                        } else { //다운로드 오류 시
                            ANError error = subResponse.getError();
                            Log.e("Server Download ERR", error.getMessage());
                        }
                    }
                } else {
                    ANError error = response.getError();
                    Log.e("Request Banner List ERR", error.getMessage());
                }
                break;
        }
    }

    private static void copyFromAssets(UPDATE_TARGET currentUpdateTarget) { //Assets으로부터 복사하는 대체 흐름
        String localTargetPath; //로컬 업데이트 대상 경로
        String localTargetFileName = null; //로컬 업데이트 대상 파일 이름
        String localTargetAssetsDirName; //로컬 업데이트 대상의 Assets 디렉토리명

        switch (currentUpdateTarget) { //업데이트 대상 경로, 대상 이름, 업데이트 서버 설정
            case DB: //어류 데이터베이스
                localTargetPath = FishDic.DB_PATH;
                localTargetFileName = FishDic.DB_NAME;
                localTargetAssetsDirName = "DB/";

                Log.d("Downloading newest DB From Server", "---");
                break;

            case MODEL: //어류 판별 위한 모델
                localTargetPath = FishDic.MODEL_PATH;
                localTargetFileName = FishDic.MODEL_NAME;
                localTargetAssetsDirName = "model/";

                Log.d("Downloading newest Model From Server", "---");
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + currentUpdateTarget);
        }

        try {
            File folder = new File(localTargetPath);
            if (!folder.exists()) {
                folder.mkdir();
            }

            InputStream inputStream = FishDic.globalContext.getAssets().open(localTargetAssetsDirName + localTargetFileName);
            String outFileName = localTargetPath + localTargetFileName; //출력 파일명

            OutputStream outputStream = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();

            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
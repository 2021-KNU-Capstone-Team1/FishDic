package com.knu.fishdic.manager;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.knu.fishdic.FishDic;
import com.knu.fishdic.utils.ImageUtility;
import com.knu.fishdic.utils.ZipUtility;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import okhttp3.Response;

// 어류 판별을 위한 FishIdentificationManager 정의

public class FishIdentificationManager {
    public static final String PUBLIC_MODEL_SERVER = "http://fishdic.asuscomm.com/Model/";
    public final String PUBLIC_FEEDBACK_SERVER = "http://fishdic.asuscomm.com/";
    public final String SEND_FEEDBACK = "send_feedback.php";
    public final String FEEDBACK_KEY = "feedback_data";

    private final int NOTIFICATION_ID = 1; //알림 아이디

    private enum MODEL_STATE { //모델 상태 정의
        INIT, //초기 상태
        OUT_DATED, //구 버전
        UPDATED, //갱신 된 버전
        DELAYED_FAILURE, //모델 상태 확인 실패 (지연 된 갱신 수행)
        IMMEDIATE_FAILURE //모델 상태 확인 실패 (assets으로부터 복사하는 대체 흐름 수행)
    }

    private static String MODEL_PATH; //판별 위한 모델 경로
    private static final String MODEL_NAME = "model.tflite"; //판별 위한 모델 이름

    private final float RESULTS_SCORE_THRESHOLD = 0.1f; //결과 가중치 임계값

    public FishIdentificationManager() {
        MODEL_PATH = "/data/data/" + FishDic.globalContext.getPackageName() + "/model/"; //판별용 모델 경로 "/data/data/앱 이름/model/"

        switch (this.getCurrentModelState()) { //기존 모델 상태 확인
            case INIT: //초기 상태일 경우
            case OUT_DATED: //구 버전일 경우
                this.updateModelFromServer();
                break;

            case UPDATED: //최신 버전일 경우
            case DELAYED_FAILURE:  //모델 상태 확인 실패 (지연 된 갱신 수행)
                break;

            case IMMEDIATE_FAILURE: //모델 상태 확인 실패 (assets으로부터 복사하는 대체 흐름 수행)
                this.copyModel();
        }
        copyModel(); //디버그용
    }

    private MODEL_STATE getCurrentModelState() {  //기존 모델 상태 반환
        /***
         * 1) 로컬 Model과 로컬 Model 버전 관리 파일이 존재하지 않을 경우 서버로부터의 갱신을 위한 초기 상태 반환
         * 2) 로컬 Model과 로컬 Model 버전 관리 파일이 존재할 경우 서버와 로컬 Model 버전을 비교하여
         *  2-1) 로컬 Model 버전 < 서버 Model 버전일 경우 : 구 버전 상태 반환
         *  2-2) 로컬 Model 버전 == 서버 Model 버전일 경우 : 최신 버전 상태 반환
         *  2-3) 로컬 Model 버전 > 서버 Model 버전일 경우 : 무결성 오류
         ***/

        File dir = new File(MODEL_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }

        final File currentModelFile = new File(MODEL_PATH + MODEL_NAME); //현재 로컬 Model 파일
        final File currentModelVersionFile = new File(MODEL_PATH + FishDic.VERSION_FILE_NAME); //로컬 Model 버전 관리 파일
        boolean currentModelExists = currentModelFile.exists() & currentModelVersionFile.exists(); //로컬 Model 존재 여부 (Model 파일 혹은 버전 관리 파일 하나라도 존재 하지 않을 시 무결성이 깨진 걸로 간주)

        int currentModelVersion = -1; //로컬 Model 버전
        int serverModelVersion = -1; //서버 Model 버전

        /*** 서버와 로컬 DB 버전 비교 ***/
        Log.d("Checking newest DB Version", "---");
        ANRequest request = AndroidNetworking
                .download(PUBLIC_MODEL_SERVER + FishDic.VERSION_FILE_NAME, FishDic.CACHE_PATH, FishDic.VERSION_FILE_NAME)
                .doNotCacheResponse()
                .build()
                .setAnalyticsListener((timeTakenInMillis, bytesSent, bytesReceived, isFromCache) -> {
                    Log.i("Model", " timeTakenInMillis : " + timeTakenInMillis);
                    Log.i("Model", " bytesSent : " + bytesSent);
                    Log.i("Model", " bytesReceived : " + bytesReceived);
                    Log.i("Model", " isFromCache : " + isFromCache);
                })
                .setDownloadProgressListener((bytesDownloaded, totalBytes) -> {
                });
        ANResponse<String> response = request.executeForDownload();

        if (response.isSuccess()) {
            Response okHttpResponse = response.getOkHttpResponse();
            Log.d("Server Model Version Check", "headers :" + okHttpResponse.headers().toString());
            Log.d("Server Model Version Check", "body :" + okHttpResponse.body().toString());
            Log.d("Server Model Version Check", "HTTP Status Code :" + okHttpResponse.code());

            File serverModelVersionFile = new File(FishDic.CACHE_PATH + FishDic.VERSION_FILE_NAME);

            try {
                BufferedReader serverModelVersionReader = new BufferedReader(new FileReader(serverModelVersionFile));
                serverModelVersion = Integer.parseInt(serverModelVersionReader.readLine());
                serverModelVersionReader.close();

                if (currentModelExists) { //로컬 Model 존재 시 버전 읽어오기
                    BufferedReader currentModelVersionReader = new BufferedReader(new FileReader(currentModelVersionFile));
                    currentModelVersion = Integer.parseInt(currentModelVersionReader.readLine());
                    currentModelVersionReader.close();
                } else { //로컬 Model이 존재하지 않을 시 다운로드 받은 서버의 Model 버전 파일을 로컬 Model의 버전 파일로 이동 및 초기 상태 반환
                    Files.move(serverModelVersionFile.toPath(), Paths.get(MODEL_PATH + FishDic.VERSION_FILE_NAME));
                    return MODEL_STATE.INIT;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.i("로컬 Model 버전", String.valueOf(currentModelVersion));
            Log.i("서버 Model 버전", String.valueOf(serverModelVersion));

        } else { //서버 접속 오류 시
            ANError error = response.getError();
            Log.d("Server Model Version Check ERR", error.getMessage());

            if (currentModelExists) //현재 Model이 존재하면
                return MODEL_STATE.DELAYED_FAILURE;

            return MODEL_STATE.IMMEDIATE_FAILURE; //현재 Model이 존재하지 않으면
        }

        if (currentModelVersion < serverModelVersion) { //로컬 Model 버전 < 서버 Model 버전일 경우 : 구 버전 상태 반환
            return MODEL_STATE.OUT_DATED;
        } else if (currentModelVersion == serverModelVersion) { //로컬 Model 버전 == 서버 Model 버전일 경우 : 최신 버전 상태 반환
            return MODEL_STATE.UPDATED;
        } else { //로컬 Model 버전 > 서버 Model 버전일 경우 : 무결성 오류
            try {
                throw new Exception("Model Integrity ERR");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return MODEL_STATE.IMMEDIATE_FAILURE;
    }

    private void updateModelFromServer() { //서버로부터 최신 Model 갱신
        File dir = new File(MODEL_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }

        /*
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(FishDic.globalContext);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(FishDic.globalContext, FishDic.NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Downloading newest Model From Server")
                .setPriority(NotificationCompat.PRIORITY_MAX);
        */
        Log.d("Downloading newest Model From Server", "---");
        ANRequest request = AndroidNetworking
                .download(PUBLIC_MODEL_SERVER + MODEL_NAME, MODEL_PATH, MODEL_NAME)
                .doNotCacheResponse()
                .build()
                /*.setAnalyticsListener((timeTakenInMillis, bytesSent, bytesReceived, isFromCache) -> {
                    Log.i("Model", " timeTakenInMillis : " + timeTakenInMillis);
                    Log.i("Model", " bytesSent : " + bytesSent);
                    Log.i("Model", " bytesReceived : " + bytesReceived);
                    Log.i("Model", " isFromCache : " + isFromCache);
                })*/
                .setDownloadProgressListener((bytesDownloaded, totalBytes) -> {
                });
        ANResponse<String> response = request.executeForDownload();

        if (response.isSuccess()) {
            Response okHttpResponse = response.getOkHttpResponse();
            Log.d("Server Model Download", "headers : " + okHttpResponse.headers().toString());
            Log.d("Server Model Download", "body : " + okHttpResponse.body().toString());
            Log.d("Server Model Download", "HTTP Status Code : " + okHttpResponse.code());

            File serverModelVersionFile = new File(MODEL_PATH + FishDic.VERSION_FILE_NAME);

            if (!serverModelVersionFile.exists())
                try {
                    throw new Exception("Model Integrity ERR");
                } catch (Exception e) {
                    e.printStackTrace();
                }
        } else { //다운로드 오류 시
            ANError error = response.getError();
            Log.d("Server Model Download ERR", error.getMessage());

            this.copyModel();
        }
    }

    private void copyModel() { //assets으로부터 시스템으로 Model 복사
        /*** 서버로부터 모델 다운로드 실패 시 내장 Model을 이용한 대체 흐름 수행 ***/
        try {
            File folder = new File(MODEL_PATH);
            if (!folder.exists()) {
                folder.mkdir();
            }

            InputStream inputStream = FishDic.globalContext.getAssets().open("model/" + MODEL_NAME);
            String outFileName = MODEL_PATH + MODEL_NAME;

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

    public Bundle getImageClassificationResult(Bitmap targetBitmap) { //이미지 분류 결과 반환
        Bundle result = null;
        List<Classifications> classificationsList = null;

        ImageClassifier.ImageClassifierOptions options = ImageClassifier
                .ImageClassifierOptions.builder()
                .setScoreThreshold(RESULTS_SCORE_THRESHOLD)
                .build();

        ImageClassifier imageClassifier = null;
        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(new File(MODEL_PATH + MODEL_NAME), options);
            classificationsList = imageClassifier.classify(TensorImage.fromBitmap(targetBitmap)); //분류 결과
            imageClassifier.close();

            if (classificationsList != null) { //분류 결과가 존재하면
                /***
                 * 1) org.tensorflow.lite.support.label.Category 참조
                 * 2) 분류 결과의 구조는 다음과 같으며 score가 높은 순으로 정렬되어 있음
                 * 3) 학명 및 score 분리
                 * ---
                 * [Classifications
                 *        {
                 * 		    categories=
                 * 		    [
                 * 			    <Category "Pleuronectes yokohamae" (displayName= score=8.404746 index=35)>,
                 * 			    <Category "Hexagrammos otakii" (displayName= score=4.6259165 index=19)>,
                 * 			    <Category "Muraenesox cinereus" (displayName= score=3.373327 index=25)>,
                 * 			    <Category "Atrina" (displayName= score=0.7307081 index=3)>
                 * 		    ],
                 * 	        headIndex=0
                 *         }
                 * ]
                 ***/

                int totalCategoriesCount = classificationsList.get(0).getCategories().size(); //전체 카테고리의 수
                int fishIndex = 0; //어류 인덱스
                result = new Bundle(); //키(문자열), 값 쌍의 최종 결과

                for (int i = 0; i < totalCategoriesCount; i++) { //전체 카테고리의 수만큼 하위 결과에 추가 후 어류 인덱스를 기준으로 최종 결과에 추가
                    Log.d(classificationsList.get(0).getCategories().get(i).getLabel().trim(), String.valueOf(classificationsList.get(0).getCategories().get(i).getScore()));

                    Bundle subResult = new Bundle(); //result 내부에 판별 된 어류를 각각 추가하기 위한 키(문자열), 값 쌍의 하위 결과
                    subResult.putFloat(classificationsList.get(0).getCategories().get(i).getLabel().trim(), classificationsList.get(0).getCategories().get(i).getScore()); //학명, 가중치 쌍
                    result.putBundle(String.valueOf(fishIndex), subResult); //어류 인덱스를 키로 하여 최종 결과에 추가
                    fishIndex++;
                }

                result.putInt(DBManager.TOTAL_FISH_COUNT_KEY_VALUE, fishIndex); //인덱스로 각 판별 된 어류(학명 : 가중치 쌍)를 접근 위해 전체 어류 수를 추가
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (imageClassifier != null)
            imageClassifier.close();

        sendFeedbackData(targetBitmap, classificationsList);

        return result;
    }

    private void sendFeedbackData(Bitmap targetBitmap, List<Classifications> targetClassificationList) { //피드백 데이터 전송
        byte[] image = ImageUtility.encodeFromBitmap(targetBitmap, Bitmap.CompressFormat.JPEG, 50); //판별 시 사용 된 이미지
        String result = targetClassificationList.toString(); //판별 결과

        /***
         * < 전송 위한 피드백 데이터 예시 >
         * 2021-06-03T08:59:32_a5f815c6de14843a.jpeg
         * 2021-06-03T08:59:32_a5f815c6de14843a.txt
         * ---
         * 2021-06-03T08:59:32_a5f815c6de14843a.zip
         ***/

        String androidId = Settings.Secure.getString(FishDic.globalContext.getContentResolver(), Settings.Secure.ANDROID_ID); //고유 사용자 식별을 위한 안드로이드 ID

        final String currentDate = DBManager.getCurrentDate(DBManager.DATE_FORMAT_TYPE.DETAIL_WITHOUT_SEPARATOR);
        final String targetImageFileName = currentDate + "_" + androidId + ".jpeg";
        final String targetResultFileName = currentDate + "_" + androidId + ".txt";
        final String targetCompressedFileName = currentDate + "_" + androidId + ".zip";

        File targetImageFile = new File(FishDic.CACHE_PATH + targetImageFileName); //판별 시 사용 된 이미지 파일
        File targetResultFile = new File(FishDic.CACHE_PATH + targetResultFileName); //판별 결과 파일
        FileOutputStream fileOutputStream;

        try { //판별 시 사용 된 이미지 처리
            fileOutputStream = new FileOutputStream(targetImageFile);
            fileOutputStream.write(image, 0, image.length);
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try { //판별 결과 처리
            fileOutputStream = new FileOutputStream(targetResultFile);
            fileOutputStream.write(result.getBytes());
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        ZipUtility.zip(new String[]{FishDic.CACHE_PATH + targetImageFileName, FishDic.CACHE_PATH + targetResultFileName},
                FishDic.CACHE_PATH + targetCompressedFileName);

        targetImageFile.delete();
        targetResultFile.delete();

        File targetCompressedFile = new File(FishDic.CACHE_PATH + targetCompressedFileName); //전송 위해 압축 된 파일

        AndroidNetworking.upload(PUBLIC_FEEDBACK_SERVER + SEND_FEEDBACK)
                .addHeaders("enctype", "multipart/form-data")
                .addHeaders("Content-Length", String.valueOf(targetCompressedFile.length()))
                .addMultipartFile(FEEDBACK_KEY, targetCompressedFile)
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener((bytesUploaded, totalBytes) -> {
                    Log.i("Upload", " bytesUploaded, : " + bytesUploaded);
                    Log.i("Upload", " totalBytes : " + totalBytes);
                })
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        targetCompressedFile.delete();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("Response Error", anError.getMessage());
                        targetCompressedFile.delete();
                    }
                });
    }
}

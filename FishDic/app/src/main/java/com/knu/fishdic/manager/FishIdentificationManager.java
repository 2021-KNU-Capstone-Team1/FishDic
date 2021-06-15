package com.knu.fishdic.manager;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.knu.fishdic.FishDic;
import com.knu.fishdic.utils.DateUtility;
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
import java.util.List;

// 어류 판별을 위한 FishIdentificationManager 정의

public class FishIdentificationManager {
    public static String localModelVersion = null; //로컬 모델 버전

    public final String PUBLIC_FEEDBACK_SERVER = "http://fishdic.asuscomm.com/";
    public final String SEND_FEEDBACK = "send_feedback.php";
    public final String FEEDBACK_KEY = "Gh94K7572e503WjsiiV6dQZjQHea2126";

    //private final int NOTIFICATION_ID = 1; //알림 아이디

    public FishIdentificationManager() {
        this.allocateLocalModelVersion();
    }

    private void allocateLocalModelVersion() { //로컬 모델 버전 할당
        if (localModelVersion != null) //이미 할당 되었을 경우
            return;

        File localModelVersionFile = new File(FishDic.MODEL_PATH + FishDic.VERSION_FILE_NAME); //모델 버전 관리 파일

        if(!localModelVersionFile.exists()) { //로컬 모델 버전 관리 파일이 존재하지 않을 시
            localModelVersion = "BUILT-IN";
            return;
        }

        try {
            BufferedReader localModelVersionFileReader = new BufferedReader(new FileReader(localModelVersionFile));
            localModelVersion = "v" + localModelVersionFileReader.readLine();
            localModelVersionFileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bundle getImageClassificationResult(Bitmap targetBitmap) { //이미지 분류 결과 반환
        Bundle result = null;
        List<Classifications> classificationsList = null;

        ImageClassifier.ImageClassifierOptions options = ImageClassifier
                .ImageClassifierOptions.builder()
                .setScoreThreshold(FishDic.globalSettingsManager.getResultsScoreThreshold())
                .build();

        ImageClassifier imageClassifier = null;
        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(new File(FishDic.MODEL_PATH + FishDic.MODEL_NAME), options);
            classificationsList = imageClassifier.classify(TensorImage.fromBitmap(targetBitmap)); //분류 결과
            imageClassifier.close();

            //Log.d("분류 결과", classificationsList.toString());

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

            if (totalCategoriesCount > 0) { //분류 된 카테고리가 있을 경우
                int fishIndex = 0; //어류 인덱스
                result = new Bundle(); //키(문자열), 값 쌍의 최종 결과

                for (int i = 0; i < totalCategoriesCount; i++) { //전체 카테고리의 수만큼 하위 결과에 추가 후 어류 인덱스를 기준으로 최종 결과에 추가
                    Log.d(classificationsList.get(0).getCategories().get(i).getLabel().trim(), String.valueOf(classificationsList.get(0).getCategories().get(i).getScore()));

                    Bundle subResult = new Bundle(); //result 내부에 판별 된 어류를 각각 추가하기 위한 키(문자열), 값 쌍의 하위 결과
                    subResult.putFloat(classificationsList.get(0).getCategories().get(i).getLabel().trim(), classificationsList.get(0).getCategories().get(i).getScore()); //학명, 가중치 쌍
                    result.putBundle(String.valueOf(fishIndex), subResult); //어류 인덱스를 키로 하여 최종 결과에 추가
                    fishIndex++;
                }

                result.putInt(DBManager.TOTAL_FISH_COUNT_KEY, fishIndex); //인덱스로 각 판별 된 어류(학명 : 가중치 쌍)를 접근 위해 전체 어류 수를 추가
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

        final String currentDate = DateUtility.getCurrentDate(DateUtility.DATE_FORMAT_TYPE.DETAIL_WITHOUT_SEPARATOR);
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
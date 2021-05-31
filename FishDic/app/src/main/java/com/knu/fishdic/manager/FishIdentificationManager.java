package com.knu.fishdic.manager;

// 어류 판별을 위한 FishIdentificationManager 정의

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.knu.fishdic.FishDic;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class FishIdentificationManager {
    //TODO : 서버로부터의 업데이트 기능, FishIdentificationActivity 연동

    private static String MODEL_PATH; //판별 위한 모델 경로
    private static final String MODEL_NAME = "model.tflite"; //판별 위한 모델 이름

    private final float RESULTS_THRESHOLD = 0.1f; //결과 임계값
    private final int MAX_RESULTS_COUNT = 10; //최대 결과 수

    public FishIdentificationManager() {
        MODEL_PATH = "/data/data/" + FishDic.globalContext.getPackageName() + "/model/"; //판별용 모델 경로 "/data/data/앱 이름/model/"
        copyModel(); //디버그용
    }

    private void copyModel() { //assets으로부터 시스템으로 모델 복사
        /*** 서버로부터 모델 다운로드 실패 시 내장 DB를 이용한 대체 흐름 수행 ***/
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

    public Bundle getImageClassificationResult(Bitmap target) { //이미지 분류 결과 반환
        Bundle result = null;

        ImageClassifier.ImageClassifierOptions options = ImageClassifier
                .ImageClassifierOptions.builder()
                .setScoreThreshold(RESULTS_THRESHOLD)
                .setMaxResults(MAX_RESULTS_COUNT)
                .build();

        ImageClassifier imageClassifier = null;
        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(new File(MODEL_PATH + MODEL_NAME), options);
            List<Classifications> classificationsList = imageClassifier.classify(TensorImage.fromBitmap(target)); //분류 결과
            imageClassifier.close();

            if (classificationsList != null) { //분류 결과가 존재하면
                /***
                 * 1) 분류 결과의 구조는 다음과 같으며 score가 높은 순으로 정렬되어 있음
                 * 2) 학명 및 score 분리
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
                result = new Bundle();

                for (int i = 0; i < totalCategoriesCount; i++) {
                    Log.d(classificationsList.get(0).getCategories().get(i).getLabel(), String.valueOf(classificationsList.get(0).getCategories().get(i).getScore()));
                    result.putFloat(classificationsList.get(0).getCategories().get(i).getLabel(), classificationsList.get(0).getCategories().get(i).getScore()); //학명, 가중치 쌍
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (imageClassifier != null)
            imageClassifier.close();

        return result;
    }
}

package com.knu.fishdic.manager;

// 어류 판별을 위한 FishIdentificationManager 정의

import android.content.res.AssetManager;
import android.graphics.Bitmap;
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

    private static final float RESULTS_THRESHOLD = 0.1f; //결과 임계깞
    private static final int MAX_RESULTS_COUNT = 10; //최대 결과 수

    public FishIdentificationManager() {
        MODEL_PATH = "/data/data/" + FishDic.globalContext.getPackageName() + "/model/"; //판별용 모델 경로 "/data/data/앱 이름/model/"
        copyModel(); //디버그용
    }

    private void copyModel() { //assets으로부터 시스템으로 모델 복사
        /*** 서버로부터 모델 다운로드 실패 시 내장 DB를 이용한 대체 흐름 수행 ***/

        AssetManager assetManager = FishDic.globalContext.getAssets();

        try {
            File folder = new File(MODEL_PATH);
            if (!folder.exists()) {
                folder.mkdir();
            }

            InputStream inputStream = assetManager.open("model/" + MODEL_NAME);
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

        assetManager.close();
    }

    public void getImageClassificationResults(Bitmap target) { //이미지 분류 결과 반환
        ImageClassifier.ImageClassifierOptions options = ImageClassifier
                        .ImageClassifierOptions.builder()
                        .setScoreThreshold(RESULTS_THRESHOLD)
                        .setMaxResults(MAX_RESULTS_COUNT)
                        .build();

        ImageClassifier imageClassifier = null;
        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(new File(MODEL_PATH + MODEL_NAME), options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Classifications> results = imageClassifier.classify(TensorImage.fromBitmap(target));
        for (int i = 0; i < results.size(); i++) {
            Log.i("result : ", results.get(i).toString());
        }
    }
}

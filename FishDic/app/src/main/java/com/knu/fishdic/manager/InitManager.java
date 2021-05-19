package com.knu.fishdic.manager;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.recyclerview.RecyclerAdapter;
import com.knu.fishdic.utils.ImageUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// 앱 초기화를 위한 InitManager 정의

public class InitManager {
    public static void doDataBindJobForRecylerAdapter() { //도감 및 이달의 금어기를 위한 데이터 바인딩 작업 수행
        if (FishDic.globalDBManager != null || FishDic.globalDicRecyclerAdapter != null || FishDic.globalDeniedFishRecyclerAdapter != null)
            return;

        FishDic.globalDBManager = new DBManager();
        FishDic.globalDicRecyclerAdapter = new RecyclerAdapter();
        FishDic.globalDeniedFishRecyclerAdapter = new RecyclerAdapter();

        FishDic.globalDicRecyclerAdapter.addItemFromBundle(FishDic.globalDBManager.getSimpleFishBundle(DBManager.FISH_DATA_TYPE.ALL_FISH));
        FishDic.globalDeniedFishRecyclerAdapter.addItemFromBundle(FishDic.globalDBManager.getSimpleFishBundle(DBManager.FISH_DATA_TYPE.DENIED_FISH));
    }

    public static void debugBannerTest() { //디버그용 서버로부터 받아와야함
        AssetManager assetManager = FishDic.globalContext.getAssets();
        FishDic.bannerImages = new Bitmap[3];

        try {
            File folder = new File(FishDic.BANNER_IMAGES_PATH);
            if (!folder.exists()) {
                folder.mkdir();
            }

            for (int index = 1; index <= 3; index++) {
                String fileName = "sample_" + index + ".jpg";
                InputStream inputStream = assetManager.open("debugbanner/" + fileName);
                String outFileName = FishDic.BANNER_IMAGES_PATH + fileName;
                OutputStream outputStream = new FileOutputStream(outFileName);

                byte[] buffer = new byte[1024];
                byte[] image = null;
                int length = 0;
                int totalLength = 0;

                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                    totalLength += length;
                }

                //배너 할당 테스트
                image = new byte[totalLength]; //해당 이미지의 크기
                Log.d("배너 크기", String.valueOf(totalLength));
                inputStream.reset();
                inputStream.read(image);
                FishDic.bannerImages[index - 1] = ImageUtility.decodeFromByteArray(image);

                outputStream.flush();
                outputStream.close();
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

                Log.d("이용가이드 크기", String.valueOf(inputStream.available()));
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
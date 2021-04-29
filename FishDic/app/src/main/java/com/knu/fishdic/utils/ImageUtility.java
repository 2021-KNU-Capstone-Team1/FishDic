package com.knu.fishdic.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class ImageUtility {
    public static Bitmap decodeFromByteArray(byte[] target) { //byte[] 이미지를 Bitmap 형식으로 변환
        if (target != null) { //이미지가 존재 할 경우
            int imageLength = target.length; //이미지 배열 길이

            if (imageLength > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(target, 0, imageLength); //byte[] 이미지를 Bitmap 형식으로 변환
                return bitmap;
            }
        }

        return null; //이미지가 존재하지 않거나, 크기가 0일 경우
    }

    public static byte[] encodeFromBitmap(Bitmap target, Bitmap.CompressFormat compressFormat, int compressQuality){ //Bitmap 이미지를 압축하여 byte[] 형식으로 변환
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        target.compress(compressFormat, compressQuality, byteArrayOutputStream); //압축 형식, 압축 품질에 따라 압축
        return byteArrayOutputStream.toByteArray();
    }
}
package com.knu.fishdic.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtility {
    public static Bitmap decodeFromByteArray(byte[] image) { //byte[] 이미지를 Bitmap 형식으로 변환
        if (image != null) { //이미지가 존재 할 경우
            int imageLength = image.length; //이미지 배열 길이

            if (imageLength > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, imageLength); //byte[] 이미지를 Bitmap 형식으로 변환
                return bitmap;
            }
        }

        return null; //이미지가 존재하지 않거나, 크기가 0일 경우
    }
}
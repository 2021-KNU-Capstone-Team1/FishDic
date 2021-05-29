package com.knu.fishdic.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.canhub.cropper.CropImage;
import com.knu.fishdic.R;
import com.knu.fishdic.fragment.MyDialogFragment;

import java.io.InputStream;

// 어류 판별 액티비티 정의

public class FishIdentificationActivity extends AppCompatActivity {
    // public static String FISH_IDENTIFICATION_METHOD_KEY_VALUE = "fishIdentificationMethodKey"; //어류 판별 방법 키 값
    // public static String FISH_IDENTIFICATION_IMAGE_KEY_VALUE = "fishIdentificationImageKey"; //판별 이미지 키 값
//  https://www.geeksforgeeks.org/how-to-crop-image-from-camera-and-gallery-in-android/
    // /*** 어류 판별 위한 방법 ***/
    // public static final int TAKE_PICTURE = 1; //카메라로부터 사진 찍기 위한 키 값 상수
    // public static final int GET_FROM_GALLERY = 101; //갤러리로부터 사진 가져오기 위한 키 값 상수
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    String cameraPermission[];
    String storagePermission[];
    Uri imageUri;
    //TODO : 메인액티비티의 카메라 혹은 갤러리로부터의 사진 가져오는 것을 여기로 옮기기
    //TODO : h5 -.> pb -> tflite로 변환

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_fishidentification);

        /*** 카메라 및 저장소 권한 확인 ***/
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


         pickFromGallery();

        this.showFishDetailErrDialog();
    }

    // checking storage permissions
    private Boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    // Requesting  gallery permission
    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }

    // checking camera permissions
    private Boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    // Requesting camera permission
    private void requestCameraPermission() {
        requestPermissions(cameraPermission, CAMERA_REQUEST);
    }

    private void pickFromGallery() {
        CropImage.activity().start(this);
    }

    @Override
    public void onBackPressed() { //하드웨어, 소프트웨어 back 키와 앱 내의 뒤로 가기 버튼을 위하여 현재 액티비티 종료 시 수행 할 작업 설정
        //    FishDic.globalDicRecyclerAdapter.resetRefItemList(); //참조 목록 초기화 및 가비지 컬렉션 요청
        super.onBackPressed();
    }


    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {        //카메라 결과 전송
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE: //카메라로 사진 찍은 경우
                if (resultCode == RESULT_OK) {       //결과랑 데이터가 있는지 확인하고
                    Uri imageUri = getPickImageResultUri(data);


                    this.doFishIdentificationJob();
                }
                break;

            case GET_FROM_GALLERY: //갤러리에서 가져 온 경우
                if (resultCode == RESULT_OK) {
                    try {
                        InputStream is = getContentResolver().openInputStream(data.getData());
                        Bitmap bm = BitmapFactory.decodeStream(is);
                        is.close();

                        //나중에 여기다가 갤러리에서 얻은 사진 서버로 전송하도록 작성.


                        this.doFishIdentificationJob();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "취소하였습니다.", Toast.LENGTH_LONG).show();
                }

                break;
        }
    }


    private void doFishIdentificationJob() { //어류 판별 작업 수행

    }

    private void showFishDetailErrDialog() { //어류 판별 오류 Dialog 보여주기
        Bundle dialogArgs = new Bundle();
        dialogArgs.putSerializable(MyDialogFragment.DIALOG_TYPE_KEY, MyDialogFragment.DIALOG_TYPE.FISH_IDENTIFICATION_ERR);
        DialogFragment dialogFragment = MyDialogFragment.newInstance(dialogArgs);
        dialogFragment.setCancelable(false); //사용자가 뒤로가기 혹은 다른 위치를 클릭해서 건너뛰는 것을 방지
        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }
}
package com.knu.fishdic.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageView;
import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.fragment.MyDialogFragment;

// 어류 판별 액티비티 정의

public class FishIdentificationActivity extends AppCompatActivity {
    // public static String FISH_IDENTIFICATION_METHOD_KEY_VALUE = "fishIdentificationMethodKey"; //어류 판별 방법 키 값
    // public static String FISH_IDENTIFICATION_IMAGE_KEY_VALUE = "fishIdentificationImageKey"; //판별 이미지 키 값

    // /*** 어류 판별 위한 방법 ***/
    // public static final int TAKE_PICTURE = 1; //카메라로부터 사진 찍기 위한 키 값 상수
    // public static final int GET_FROM_GALLERY = 101; //갤러리로부터 사진 가져오기 위한 키 값 상수
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    String cameraPermission[];
    String storagePermission[];

    //TODO : h5 -.> pb -> tflite로 변환

    RecyclerView fishIdentification_recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_fishidentification);

        /*** 카메라 및 저장소 권한 확인 ***/
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        startCropImageActivity(); //이미지 Crop 작업 위한 액티비티 시작
        this.showFishDetailErrDialog();
    }

    @Override
    public void onBackPressed() { //하드웨어, 소프트웨어 back 키와 앱 내의 뒤로 가기 버튼을 위하여 현재 액티비티 종료 시 수행 할 작업 설정
        FishDic.globalFishIdentificationRecyclerAdapter.deallocateAllItemList(); //할당 해제 된 모든 목록에 대하여 가비지 컬렉션 요청
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { //권한 요청에 대한 콜백
        switch (requestCode) {
            case CAMERA_REQUEST: //카메라 접근 권한 요청
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) { //카메라와 저장소 접근 권한 모두 허용되었으면
                        startCropImageActivity();
                    } else {
                        Toast.makeText(this, getString(R.string.fish_identification_camera_storage_permission_message), Toast.LENGTH_LONG).show();
                        this.onBackPressed();
                    }
                }
            break;

            case STORAGE_REQUEST: //저장소 접근 권한 요청
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) { //저장소 접근 권한 허용되었으면
                        startCropImageActivity();
                    } else {
                        Toast.makeText(this, getString(R.string.fish_identification_storage_permission_message), Toast.LENGTH_LONG).show();
                        this.onBackPressed();
                    }
                }
            break;
        }
    }
/*
    private Boolean checkStoragePermission() { //저장소 접근 권한 확인
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private Boolean checkCameraPermission() { //카메라 접근 권한 확인
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() { //저장소 접근 권한 요청
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }

    private void requestCameraPermission() { //카메라 접근 권한 요청
        requestPermissions(cameraPermission, CAMERA_REQUEST);
    }
*/
    private void startCropImageActivity() { //이미지 자르기 작업 위한 액티비티 시작
        CropImage.activity()
                .setActivityTitle(getString(R.string.fish_identification_process_message))
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) { //이미지 자르기 작업 완료 시
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUriContent();
                //TODO : 어류 판별 작업 및 화면에 뿌리기

                doFishIdentificationJob();
            }
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
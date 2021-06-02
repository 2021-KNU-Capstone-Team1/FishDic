package com.knu.fishdic.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.fragment.MyDialogFragment;
import com.knu.fishdic.manager.DBManager;
import com.knu.fishdic.manager.FishIdentificationManager;

import java.io.IOException;
import java.util.List;

// 어류 판별 액티비티 정의

public class FishIdentificationActivity extends AppCompatActivity {
    /* 삭제
        private static final int CAMERA_REQUEST = 100;
        private static final int STORAGE_REQUEST = 200;
        String[] cameraPermission;
        String[] storagePermission;
    */
    ImageButton fishIdentification_back_imageButton; //뒤로 가기 버튼
    TextView fishIdentification_message_textView; //판별 완료 개수 출력

    RecyclerView fishIdentification_recyclerView;
    RecyclerView.LayoutManager layoutManager;

    PermissionListener permissionlistener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_fishidentification);

        setComponentsInteraction();

        /*** 카메라 및 저장소 권한 확인 ***/
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage(getString(R.string.fish_identification_camera_storage_permission_message))
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

        startCropImageActivity(); //이미지 Crop 작업 위한 액티비티 시작
    }

    @Override
    public void onBackPressed() { //하드웨어, 소프트웨어 back 키와 앱 내의 뒤로 가기 버튼을 위하여 현재 액티비티 종료 시 수행 할 작업 설정
        FishDic.globalFishIdentificationRecyclerAdapter.clearItemItemList(); //원본 목록 초기화

        super.onBackPressed();
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.fishIdentification_back_imageButton = findViewById(R.id.fishIdentification_back_imageButton);
        this.fishIdentification_message_textView = findViewById(R.id.fishIdentification_message_textView);

        this.fishIdentification_recyclerView = findViewById(R.id.fishIdentification_recyclerView);
        this.fishIdentification_recyclerView.setHasFixedSize(true); //최적화를 위해서 사이즈 고정
        this.fishIdentification_recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.layoutManager = new LinearLayoutManager(this);
        this.fishIdentification_recyclerView.setLayoutManager(layoutManager);
        this.fishIdentification_recyclerView.setAdapter(FishDic.globalFishIdentificationRecyclerAdapter);

        this.permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(FishIdentificationActivity.this, getString(R.string.permission_denied) + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        this.fishIdentification_back_imageButton.setOnClickListener(v -> { //뒤로 가기 버튼에 대한 클릭 리스너
            onBackPressed();
        });

        FishDic.globalFishIdentificationRecyclerAdapter.setOnItemClickListener((v, title) -> { //새로운 클릭 리스너 객체 생성 하여 RecyclerAdapter 내부의 refItemClickListener가 참조
            /*** 커스텀 리스너 인터페이스내의 void onItemClick(View v, String title) 오버라이드 ***/
            Intent intent = new Intent(this, FishDetailActivity.class);
            intent.putExtra(DBManager.NAME, title); //어류 이름 전달
            startActivity(intent);
        });
    }

    /* 삭제
    private boolean checkStoragePermission() { //저장소 접근 권한 확인
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkCameraPermission() { //카메라 접근 권한 확인
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED) &
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() { //저장소 접근 권한 요청
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }

    private void requestCameraPermission() { //카메라 접근 권한 요청
        requestPermissions(cameraPermission, CAMERA_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { //권한 요청에 대한 콜백
        switch (requestCode) {
            case CAMERA_REQUEST: //카메라 접근 권한 요청
                if (grantResults.length > 0) {
                    boolean cameraAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                    boolean storageAccepted = (grantResults[1] == PackageManager.PERMISSION_GRANTED);

                    if (cameraAccepted && storageAccepted) { //카메라와 저장소 접근 권한 모두 허용되었으면
                        this.startCropImageActivity();
                    } else {
                        Toast.makeText(this, getString(R.string.fish_identification_camera_storage_permission_message), Toast.LENGTH_LONG).show();
                        this.onBackPressed();
                    }
                }
                break;

            case STORAGE_REQUEST: //저장소 접근 권한 요청
                if (grantResults.length > 0) {
                    boolean storageAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);

                    if (storageAccepted) { //저장소 접근 권한 허용되었으면
                        this.startCropImageActivity();
                    } else {
                        Toast.makeText(this, getString(R.string.fish_identification_storage_permission_message), Toast.LENGTH_LONG).show();
                        this.onBackPressed();
                    }
                }
                break;
        }
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
                ImageDecoder.Source src = ImageDecoder.createSource(getContentResolver(), resultUri);

                try {
                    Bitmap bitmap = ImageDecoder.decodeBitmap(src).copy(Bitmap.Config.ARGB_8888, true);
                    Bundle classificationResult = FishDic.globalFishIdentificationManager.getImageClassificationResult(bitmap); //분류 결과

                    if (classificationResult != null) {
                        Bundle queryResult = FishDic.globalDBManager.getSimpleFishBundle(DBManager.FISH_DATA_TYPE.FISH_IDENTIFICATION_RESULT, classificationResult);
                        final String infoMessage = String.format(getString(R.string.fish_identification_info_message), queryResult.getInt(DBManager.TOTAL_FISH_COUNT_KEY_VALUE));
                        this.fishIdentification_message_textView.setText(infoMessage); //판별 된 어류 개수 출력
                        FishDic.globalFishIdentificationRecyclerAdapter.addItemFromBundle(queryResult);
                    } else {
                        final String infoMessage = String.format(getString(R.string.fish_identification_info_message), 0);
                        this.fishIdentification_message_textView.setText(infoMessage);
                        this.showFishDetailErrDialog();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    this.showFishDetailErrDialog();
                }
            } else
                this.onBackPressed();
        }
    }

    private void showFishDetailErrDialog() { //어류 판별 오류 Dialog 보여주기
        Bundle dialogArgs = new Bundle();
        dialogArgs.putSerializable(MyDialogFragment.DIALOG_TYPE_KEY, MyDialogFragment.DIALOG_TYPE.FISH_IDENTIFICATION_ERR);
        DialogFragment dialogFragment = MyDialogFragment.newInstance(dialogArgs);
        dialogFragment.setCancelable(false); //사용자가 뒤로가기 혹은 다른 위치를 클릭해서 건너뛰는 것을 방지
        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }
}
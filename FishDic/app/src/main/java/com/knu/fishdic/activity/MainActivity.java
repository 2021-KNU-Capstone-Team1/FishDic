package com.knu.fishdic.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.manager.InitManager;

import java.io.InputStream;

// 메인 화면 액티비티 정의

public class MainActivity extends Activity {
    ImageButton main_dic_imageButton;                       //메인화면 하단부 도감 버튼
    ImageButton main_deniedFish_imageButton;                //메인화면 하단부 금어기 버튼
    ImageButton main_fishIdentification_imageButton;        //메인화면 하단부 카메라 버튼
    ImageButton main_gallery_imageButton;                  //메인화면 하단부 갤러리 버튼
    ImageButton main_help_imageButton;                      //메인화면 하단부 도움 버튼

    private static final int GET_FROM_GALLERY = 101;
    final static int TAKE_PICTURE = 1;      //카메라 어플 열 때 전달 될 키값 상수
    // ImageView camera_picture; 나중에 찍은 사진 저장할 용도

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*** 초기화 작업 수행 ***/
        InitManager.doDataBindingJob(); //도감 및 이달의 금어기를 위한 바인딩 작업 수행

        setTitle(R.string.app_name);
        setTheme(R.style.AppTheme); //초기화 적업 완료 후 스플래시 테마에서 기존 앱 테마로 변경
        setContentView(R.layout.activity_main);
        setComponentsInteraction();

        //checkSelfPermission();

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //권한 모두 승인 되어 있음
        }
        else {
            //권한 다시 승인 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.main_dic_imageButton = findViewById(R.id.main_dic_imageButton);
        this.main_deniedFish_imageButton = findViewById(R.id.main_deniedFish_imageButton);
        this.main_fishIdentification_imageButton = findViewById(R.id.main_fishIdentification_imageButton);
        this.main_gallery_imageButton = findViewById(R.id.main_gallery_imageButton);
        this.main_help_imageButton = findViewById(R.id.main_help_imageButton);

        //도감 화면으로 넘어가는 클릭 리스너
        this.main_dic_imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(FishDic.globalContext, DicActivity.class);
            startActivity(intent);
        });

        //금어기 화면으로 넘어가는 클릭 리스너
        this.main_deniedFish_imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(FishDic.globalContext, DeniedFishActivity.class);
            startActivity(intent);
        });

        //카메라 촬영으로 넘어가는 클릭 리스너
        this.main_fishIdentification_imageButton.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, TAKE_PICTURE);
        });

        //갤러리 사진 선택으로 넘어가는 클릭 리스너
        this.main_gallery_imageButton.setOnClickListener(v -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(galleryIntent, GET_FROM_GALLERY);

        });

        //도움 화면으로 넘어가는 클릭 리스너
        this.main_help_imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(FishDic.globalContext, HelpActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){        //카메라 결과 전송
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case TAKE_PICTURE:          // 카메라 사진 찍은 상황이면
                if(resultCode == RESULT_OK && data.hasExtra("data")){       //결과랑 데이터가 있는지 확인하고
                    Bitmap bm = (Bitmap) data.getExtras().get("data");              //비트맵에 저장 한다.
                    //나중에 여기다가 얻은 사진 조정해서 서버로 전송하도록 작성.
                }
                break;
            case GET_FROM_GALLERY:
                if(resultCode == RESULT_OK){
                    try{
                        InputStream is = getContentResolver().openInputStream(data.getData());
                        Bitmap bm = BitmapFactory.decodeStream(is);
                        is.close();
                        //나중에 여기다가 갤러리에서 얻은 사진 서버로 전송하도록 작성.
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else if(resultCode == RESULT_CANCELED){
                    Toast.makeText(this, "취소하였습니다.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
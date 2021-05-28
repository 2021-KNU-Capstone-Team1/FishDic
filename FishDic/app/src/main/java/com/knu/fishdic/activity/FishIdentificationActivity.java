package com.knu.fishdic.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.knu.fishdic.R;
import com.knu.fishdic.fragment.MyDialogFragment;

import java.io.InputStream;

// 어류 판별 액티비티 정의

public class FishIdentificationActivity extends AppCompatActivity {
    public static String FISH_IDENTIFICATION_METHOD_KEY_VALUE = "fishIdentificationMethodKey"; //어류 판별 방법 키 값

    /*** 어류 판별 위한 방법 ***/
    private final static int TAKE_PICTURE = 1; //사진 찍기 위한 키 값 상수
    private static final int GET_FROM_GALLERY = 101; //갤러리로부터 사진 가져오기 위한 키 값 상수

    //TODO : 메인액티비티의 카메라 혹은 갤러리로부터의 사진 가져오는 것을 여기로 옮기기
    //TODO : h5 -.> pb -> tflite로 변환

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_fishidentification);

        Bundle args = getIntent().getExtras(); //현재 액티비티 생성 시 전달받은 키(문자열), 값 쌍
        if(!args.containsKey(FISH_IDENTIFICATION_METHOD_KEY_VALUE)) //어류 판별 위한 방법을 전달 받지 않았을 시
            return;
        
        switch(args.getInt(FISH_IDENTIFICATION_METHOD_KEY_VALUE)){
            case TAKE_PICTURE: //사진 찍기
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, TAKE_PICTURE);
                break;

            case GET_FROM_GALLERY: //갤러리로부터 가져오기
                Intent galleryIntent = new Intent();
                galleryIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent, GET_FROM_GALLERY);
                break;
        }

        this.doFishIdentificationJob();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {        //카메라 결과 전송
        super.onActivityResult(requestCode, resultCode, data);
// https://mainia.tistory.com/m/1631
        switch (requestCode) {
            case TAKE_PICTURE:          // 카메라 사진 찍은 상황이면
                if (resultCode == RESULT_OK && data.hasExtra("data")) {       //결과랑 데이터가 있는지 확인하고
                    Bitmap bm = (Bitmap) data.getExtras().get("data");              //비트맵에 저장 한다.
                    //나중에 여기다가 얻은 사진 조정해서 서버로 전송하도록 작성.
                }
                break;

            case GET_FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    try {
                        InputStream is = getContentResolver().openInputStream(data.getData());
                        Bitmap bm = BitmapFactory.decodeStream(is);
                        is.close();
                        //나중에 여기다가 갤러리에서 얻은 사진 서버로 전송하도록 작성.
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
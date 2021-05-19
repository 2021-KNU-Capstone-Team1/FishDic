package com.knu.fishdic.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.fragment.MyFragment;
import com.knu.fishdic.fragment.MyFragmentPagerAdapter;
import com.knu.fishdic.manager.InitManager;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

// 메인 화면 액티비티 정의

public class MainActivity extends AppCompatActivity {
    ImageButton main_dic_imageButton;                       //메인화면 하단부 도감 버튼
    ImageButton main_deniedFish_imageButton;                //메인화면 하단부 금어기 버튼
    ImageButton main_fishIdentification_imageButton;        //메인화면 하단부 카메라 버튼
    ImageButton main_gallery_imageButton;                  //메인화면 하단부 갤러리 버튼
    ImageButton main_help_imageButton;                      //메인화면 하단부 도움 버튼

    ViewPager viewPager;
    FragmentPagerAdapter viewPagerAdapter; //ViewPager 어댑터
    CircleIndicator indicator;

  //  DrawerLayout drawerLayout;
    //NavigationView navigationView;
    
    private Timer timer;
    private int currentPosition = 0; //현재 이미지의 위치
    private final long DELAY_MS = 1000; //작업이 실행 되기 전 딜레이 (MS)
    private final long PERIOD_MS = 3000; //작업 실행 간의 딜레이 (MS)

    private static final int GET_FROM_GALLERY = 101;
    final static int TAKE_PICTURE = 1;      //카메라 어플 열 때 전달 될 키값 상수
    // ImageView camera_picture; 나중에 찍은 사진 저장할 용도

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*** 초기화 작업 수행 ***/
        InitManager.doDataBindJobForRecylerAdapter(); //도감 및 이달의 금어기를 위한 데이터 바인딩 작업 수행
        InitManager.debugBannerTest(); //배너 이미지 테스트
        InitManager.initHelpImages(); //이용가이드 초기 작업 수행

        setTitle(R.string.app_name);
        setTheme(R.style.AppTheme); //초기화 적업 완료 후 스플래시 테마에서 기존 앱 테마로 변경
        setContentView(R.layout.activity_main);
        this.setComponentsInteraction();
        this.initViewPager();
        //checkSelfPermission();

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //권한 모두 승인 되어 있음
        } else {
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
        this.viewPager = findViewById(R.id.banner_viewPager);
        this.indicator = findViewById(R.id.banner_circleIndicator);

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {        //카메라 결과 전송
        super.onActivityResult(requestCode, resultCode, data);

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

    private void initViewPager() { //ViewPager 초기화
        Bundle args = new Bundle();
        args.putSerializable(MyFragment.FRAGMENT_TYPE_KEY_VALUE, MyFragment.FRAGMENT_TYPE.BANNER);
        args.putParcelableArray(MyFragment.IMAGE_KEY_VALUE, FishDic.bannerImages);

        /*** ViewPager 어댑터 생성 및 할당, 원형 인디케이터 ViewPager에 할당 ***/
        this.viewPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), args);
        this.viewPager.setAdapter(viewPagerAdapter);
        this.indicator.setViewPager(viewPager);

        /*** 일정 시간 간격으로 자동으로 다음 이미지로 이동 ***/
        final Handler handler = new Handler();
        final Runnable Update = () -> { //다음 이미지로 이동
            if (currentPosition == FishDic.bannerImages.length) {
                currentPosition = 0;
            }
            viewPager.setCurrentItem(currentPosition++, true); //다음 이미지를 보여주기 위한 viewPager의 현재 아이템 위치 설정
        };

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);
    }
}
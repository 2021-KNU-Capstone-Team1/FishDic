package com.knu.fishdic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.fragment.MyFragment;
import com.knu.fishdic.fragment.MyFragmentPagerAdapter;
import com.knu.fishdic.manager.InitManager;

import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

// 메인 화면 액티비티 정의

public class MainActivity extends AppCompatActivity {
    ImageButton main_dic_imageButton;                       //메인화면 하단부 도감 버튼
    ImageButton main_deniedFish_imageButton;                //메인화면 하단부 금어기 버튼
    ImageButton main_fishIdentification_imageButton;        //메인화면 하단부 카메라 버튼
    ImageButton main_help_imageButton;                      //메인화면 하단부 도움 버튼

    ImageButton main_menu_imageButton; //메인화면 상단 메뉴 버튼
    DrawerLayout drawerLayout; //메인화면 최상위 루트 레이아웃
    View drawerView; //네비게이션 메뉴 뷰
    
    ViewPager viewPager;
    FragmentPagerAdapter viewPagerAdapter; //ViewPager 어댑터
    CircleIndicator indicator;

    private Timer timer;
    private int currentPosition = 0; //현재 배너 이미지의 위치
    private final long DELAY_MS = 1000; //작업이 실행 되기 전 딜레이 (MS)
    private final long PERIOD_MS = 3000; //작업 실행 간의 딜레이 (MS)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*** 초기화 작업 수행 ***/
        InitManager.initAllComponents();

        setTitle(R.string.app_name);
        setTheme(R.style.AppTheme); //초기화 적업 완료 후 스플래시 테마에서 기존 앱 테마로 변경
        setContentView(R.layout.activity_main);

        this.setComponentsInteraction();
        this.initViewPager();
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.main_dic_imageButton = findViewById(R.id.main_dic_imageButton);
        this.main_deniedFish_imageButton = findViewById(R.id.main_deniedFish_imageButton);
        this.main_fishIdentification_imageButton = findViewById(R.id.main_fishIdentification_imageButton);
        this.main_help_imageButton = findViewById(R.id.main_help_imageButton);

        this.main_menu_imageButton = findViewById(R.id.main_menu_imageButton);
        this.drawerLayout = findViewById(R.id.outerMain_drawerLayout);
        this.drawerView = (View) findViewById(R.id.outerNavigation_linearLayout);

        this.viewPager = findViewById(R.id.banner_viewPager);
        this.indicator = findViewById(R.id.banner_circleIndicator);

        //메인화면 상단 메뉴 버튼 클릭 리스너
        this.main_menu_imageButton.setOnClickListener(v -> {
            this.drawerLayout.openDrawer(drawerView); //네비게이션 메뉴 출력
        });

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

        //어류 판별 화면으로 넘어가는 클릭 리스너
        this.main_fishIdentification_imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(FishDic.globalContext, FishIdentificationActivity.class);
            startActivity(intent);
        });

        //도움 화면으로 넘어가는 클릭 리스너
        this.main_help_imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(FishDic.globalContext, HelpActivity.class);
            startActivity(intent);
        });
    }

    /*
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
    */
    private void initViewPager() { //ViewPager 초기화
        Bundle args = new Bundle();
        args.putSerializable(MyFragment.FRAGMENT_TYPE_KEY, MyFragment.FRAGMENT_TYPE.BANNER);
        args.putParcelableArray(MyFragment.IMAGE_KEY, FishDic.bannerImages);

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
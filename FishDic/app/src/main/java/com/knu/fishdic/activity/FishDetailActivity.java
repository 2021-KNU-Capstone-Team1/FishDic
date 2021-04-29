package com.knu.fishdic.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.fragment.MyFragment;
import com.knu.fishdic.manager.DBManager;

////////////////////코드 정리 예정


// 어류 상세정보 화면 액티비티 정의

//https://recipes4dev.tistory.com/58
//https://salix97.tistory.com/90
//https://devatom.tistory.com/3

public class FishDetailActivity extends AppCompatActivity {
    ImageButton fishDetail_back_imageButton; //뒤로 가기 버튼
    TextView fishDetail_title_textView; //어류 이름 출력 할 타이틀 텍스트 뷰

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_fishdetail);

        this.setComponentsInteraction();

        Bundle args = getIntent().getExtras(); //현재 액티비티 생성 시 전달받은 키(문자열), 값 쌍
        String fishName = args.getString(DBManager.NAME); //전달 받은 어류 이름
        Log.d("Target 어류 이름 : ", fishName);
        this.doDataBindingJob(fishName);
        this.fishDetail_title_textView.setText(fishName); //성공적으로 바인딩 완료 시 어류 이름으로 타이틀 텍스트 뷰 설정
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.fishDetail_back_imageButton = findViewById(R.id.fishDetail_back_imageButton);
        this.fishDetail_title_textView = findViewById(R.id.fishDetail_title_textView);

        this.fishDetail_back_imageButton.setOnClickListener(v -> //뒤로 가기 버튼에 대한 클릭 리스너
                onBackPressed());
    }

    private void doDataBindingJob(String fishName) {
        /*** 
         * 1) DB로부터 해당 어류의 상세정보를 받아온다. 
         * 2) 해당 어류의 상세정보가 존재 할 경우
         *      2-1)
         *
         * 3) 해당 어류의 금지행정이 존재할 경우
         *      3-1)
         ***/
        Bundle queryResult = FishDic.globalDBManager.getFishDetailBundle(fishName);
        if (queryResult == null) { //해당 어류가 존재하지 않을 경우
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false); //사용자가 뒤로가기 혹은 다른 위치를 클릭해서 건너뛰는 것을 방지
            builder.setIcon(R.drawable.error_64x64);
            builder.setTitle(R.string.not_exist_fish_title);
            builder.setMessage(R.string.not_exist_fish_message);
            builder.setPositiveButton(R.string.not_exist_fish_button, (dialog, which) -> onBackPressed()); //현재 액티비티 종료
            builder.create().show();
        }

        /***
         * 해당 어류의 전체 금지행정의 수만큼 queryResult 내부에 0부터 순차적으로 Key값을 가지므로
         * 각 상세 금지행정인 subQueryResult를 해당 Key값으로 분리한다.
         ***/
        int specialProhibitAdminCount = queryResult.getInt(DBManager.TOTAL_SPECIAL_PROHIBIT_ADMIN_COUNT_KEY_VALUE); //해당 어류의 전체 금지행정의 수

        /*** Fragment를 Activity의 ViewGroup(innerFishDetail_linearLayout)에 추가 ***/
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.innerFishDetail_linearLayout, MyFragment.newInstance(MyFragment.FRAGMENT_TYPE.BASIC_INFO, 0, queryResult)); //기본 정보에 해당하는 Fragment 인스턴스 생성 및 추가

        for (int specialProhibitAdminIndex = 0; specialProhibitAdminIndex < specialProhibitAdminCount; specialProhibitAdminIndex++) { //전체 금지행정의 수만큼 금지행정 정보 추가
            Bundle subQueryResult = queryResult.getBundle(String.valueOf(specialProhibitAdminIndex)); //특별 금지행정의 인덱스를 키로하는 각 금지행정 정보
            fragmentTransaction.add(R.id.innerFishDetail_linearLayout, MyFragment.newInstance(MyFragment.FRAGMENT_TYPE.DENIED_INFO, specialProhibitAdminIndex, subQueryResult)); //금지 행정정보에 해당하는 Fragment 인스턴스 생성 및 추가
        }

        fragmentTransaction.commit();
    }
}
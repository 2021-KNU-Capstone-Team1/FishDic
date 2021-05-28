package com.knu.fishdic.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.fragment.MyDialogFragment;
import com.knu.fishdic.fragment.MyFragment;
import com.knu.fishdic.manager.DBManager;

// 어류 상세정보 화면 액티비티 정의

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
        this.doDataBindJob(args);
    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.fishDetail_back_imageButton = findViewById(R.id.fishDetail_back_imageButton);
        this.fishDetail_title_textView = findViewById(R.id.fishDetail_title_textView);

        this.fishDetail_back_imageButton.setOnClickListener(v -> //뒤로 가기 버튼에 대한 클릭 리스너
                onBackPressed());
    }

    private void doDataBindJob(Bundle args) {
        /*** 
         * 1) DB로부터 해당 어류의 상세정보를 받아온다. 
         * 2) 해당 어류의 상세정보가 존재 할 경우
         *      2-1) 어류의 기본 정보에 해당하는 Fragment 인스턴스 생성 및 추가 (Activity의 ViewGroup(innerFishDetail_linearLayout))
         *
         * 3) 해당 어류의 금지행정이 존재할 경우
         *      3-1) 어류의 금지 행정정보에 해당하는 Fragment 인스턴스 생성 및 추가 (쿼리 된 전체 금지행정 수만큼)
         ***/

        Bundle queryResult = FishDic.globalDBManager.getFishDetailBundle(args);

        if (queryResult == null) { //해당 어류가 존재하지 않을 경우
            this.showFishDetailErrDialog();
        }

        this.fishDetail_title_textView.setText(queryResult.getString(DBManager.NAME)); //성공적으로 쿼리 완료 시 어류 이름으로 타이틀 텍스트 뷰 설정

        /***
         * 해당 어류의 전체 금지행정의 수만큼 queryResult 내부에 0부터 순차적으로 Key값을 가지므로
         * 각 상세 금지행정인 subQueryResult를 해당 Key값으로 분리한다.
         ***/
        int specialProhibitAdminCount = queryResult.getInt(DBManager.TOTAL_SPECIAL_PROHIBIT_ADMIN_COUNT_KEY_VALUE); //해당 어류의 전체 금지행정의 수

        /*** Fragment를 Activity의 ViewGroup(innerFishDetail_linearLayout)에 추가 ***/
        // https://developer.android.com/reference/android/support/v4/app/FragmentTransaction.html
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle fragmentTransactionArgs = new Bundle(); //Fragment 타입 및 쿼리 결과 재 전달을 위한 키(문자열), 값 쌍
        fragmentTransactionArgs.putSerializable(MyFragment.FRAGMENT_TYPE_KEY_VALUE, MyFragment.FRAGMENT_TYPE.BASIC_INFO);
        fragmentTransactionArgs.putBundle(DBManager.QUERY_RESULT_KEY_VALUE, queryResult);
        fragmentTransaction.add(R.id.innerFishDetail_linearLayout, MyFragment.newInstance(0, fragmentTransactionArgs)); //기본 정보에 해당하는 Fragment 인스턴스 생성 및 추가

        for (int specialProhibitAdminIndex = 0; specialProhibitAdminIndex < specialProhibitAdminCount; specialProhibitAdminIndex++) { //전체 금지행정의 수만큼 금지행정 정보 추가
            Bundle subQueryResult = queryResult.getBundle(String.valueOf(specialProhibitAdminIndex)); //특별 금지행정의 인덱스를 키로하는 각 금지행정 정보
            Bundle subFragmentTransactionArgs = new Bundle(); //Fragment 타입 및 하위 쿼리 결과 (금지행정) 재 전달을 위한 키(문자열), 값 쌍

            subFragmentTransactionArgs.putSerializable(MyFragment.FRAGMENT_TYPE_KEY_VALUE, MyFragment.FRAGMENT_TYPE.DENIED_INFO);
            subFragmentTransactionArgs.putBundle(DBManager.QUERY_RESULT_KEY_VALUE, subQueryResult); //금지행정 정보를 전달 위해 추가
            fragmentTransaction.add(R.id.innerFishDetail_linearLayout, MyFragment.newInstance(specialProhibitAdminIndex, subFragmentTransactionArgs)); //금지 행정정보에 해당하는 Fragment 인스턴스 생성 및 추가
        }

        fragmentTransaction.commit(); //변경사항 반영
    }

    private void showFishDetailErrDialog() { //어류 상세정보 오류 Dialog 보여주기
        Bundle dialogArgs = new Bundle();
        dialogArgs.putSerializable(MyDialogFragment.DIALOG_TYPE_KEY, MyDialogFragment.DIALOG_TYPE.FISH_DETAIL_ERR);
        DialogFragment dialogFragment = MyDialogFragment.newInstance(dialogArgs);
        dialogFragment.setCancelable(false); //사용자가 뒤로가기 혹은 다른 위치를 클릭해서 건너뛰는 것을 방지
        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }
}
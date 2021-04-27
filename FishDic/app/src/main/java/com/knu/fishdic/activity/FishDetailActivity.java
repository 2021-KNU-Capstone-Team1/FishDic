package com.knu.fishdic.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.fragment.MyFragment;
import com.knu.fishdic.manager.DBManager;

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

        setComponentsInteraction();

        Bundle args = getIntent().getExtras(); //현재 액티비티 생성 시 전달받은 키(문자열), 값 쌍
        String fishName = args.getString(DBManager.NAME); //전달 받은 어류 이름
        Log.d("어류 이름 : ", fishName);
        this.fishDetail_title_textView.setText(fishName); //어류 이름으로 타이틀 텍스트 뷰 설정

    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        this.fishDetail_back_imageButton = findViewById(R.id.fishDetail_back_imageButton);
        this.fishDetail_title_textView = findViewById(R.id.fishDetail_title_textView);

        this.fishDetail_back_imageButton.setOnClickListener(v -> //뒤로 가기 버튼에 대한 클릭 리스너
                onBackPressed());
    }

    private void temp(String fishName){
        /*** DB로부터 상세정보를 받아온다. ***/
        Bundle result = FishDic.globalDBManager.getFishDetailBundle(fishName);
        int specialProhibitAdminCount = result.getInt(DBManager.SPECIAL_PROHIBIT_ADMIN_COUNT_KEY_VALUE); //해당 어류의 전체 금지행정의 수

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        /*** Fragment를 Activity의 ViewGroup(innerFishDetail_scrollView)에 추가 ***/
        ///////임시 DB로부터 받아와서 바인딩해야함
        fragmentTransaction.add(R.id.innerFishDetail_linearLayout, MyFragment.newInstance(MyFragment.FRAGMENT_TYPE.BASIC_INFO, 0, null));
        fragmentTransaction.add(R.id.innerFishDetail_linearLayout, MyFragment.newInstance(MyFragment.FRAGMENT_TYPE.DENIED_INFO, 0, null));
        fragmentTransaction.add(R.id.innerFishDetail_linearLayout, MyFragment.newInstance(MyFragment.FRAGMENT_TYPE.DENIED_INFO, 0, null));

        for (int i = 0; i < specialProhibitAdminCount; i++) { //전체 금지행정의 수만큼 금지행정 정보 추가

        }

        fragmentTransaction.commit();
    }
}
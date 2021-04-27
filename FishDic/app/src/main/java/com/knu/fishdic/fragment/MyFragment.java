package com.knu.fishdic.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.knu.fishdic.R;

// 어류 상세 정보 페이지의 동적 화면(기본 정보 및 금지 행정 표) 추가 및 메인화면의 배너, 이용가이드를 위한 MyFragment 정의
// https://developer.android.com/reference/androidx/fragment/app/Fragment.html

/***
 * 1) FragmentManager에 의해 프래그먼트 추가와 삭제는 트랜잭션 단위로 수행
 * 2) 프래그먼트 추가는 ViewGroup에 수행
 * 3) 액티비티와 마찬가지로 Back Stack 존재
 ***/

public class MyFragment extends Fragment {
    public static String POSITION_KEY_VALUE = "position"; //position 키 값
    public static String FRAGMENT_TYPE_KEY_VALUE = "fragment"; //Fragment의 타입 키 값
    public static String IMAGE_KEY_VALUE = "image"; //이미지의 키 값
    public static String FISH_DETAIL_DATA_KEY_VALUE = "fishDetailData"; //어류 상세정보의 키 값

    public enum FRAGMENT_TYPE { //Fragment의 타입 정의
        BASIC_INFO, //어류 상세 정보 페이지의 기본 정보
        DENIED_INFO, //어류 상세 정보 페이지의 금지행정 정보
        BANNER, //메인화면의 배너
        HELP //이용가이드
    }

    private int position; //Fragment의 위치 (금지 행정 표, 배너 혹은 이용가이드를 위함)
    private FRAGMENT_TYPE fragmentType; //현재 Fragment의 타입

    private byte[] image; //이미지
    //private Bundle refOptionalFishDetailData; //어류 상세 정보 페이지의 기본 정보 및 어류 상세 정보 페이지의 금지 행정 정보를 Fragment에 바인딩 위해 DB로부터 읽어들인 데이터의 참조 변수

    public static MyFragment newInstance(FRAGMENT_TYPE fragmentType, int position, Bundle args) { //Fragment의 인스턴스 객체 생성
        /***
         * fragmentType : Fragment의 타입
         * position : Fragment의 위치
         * args : 이미지, 어류 상세정보를 포함하고 있는 키(문자열), 값 쌍의 데이터
         ***/

        MyFragment fragment = new MyFragment();

        /////수정예정 args 분리
        Bundle newArgs = new Bundle();
        newArgs.putInt(POSITION_KEY_VALUE, position);
        newArgs.putSerializable(FRAGMENT_TYPE_KEY_VALUE, fragmentType);
        fragment.setArguments(newArgs);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { //Fragment의 초기 생성 시 호출
        /***
         * Called to do initial creation of a fragment.
         * This is called after onAttach(Activity) and before onCreateView(LayoutInflater, ViewGroup, Bundle).
         * ---
         * savedInstanceState : If the fragment is being re-created from a previous saved state, this is the state.
         ***/
        super.onCreate(savedInstanceState);
        this.position = getArguments().getInt(POSITION_KEY_VALUE, 0);
        this.fragmentType = (FRAGMENT_TYPE) getArguments().getSerializable(FRAGMENT_TYPE_KEY_VALUE);

        this.image = null;
       // this.refOptionalFishDetailData = null;

        //페이지 인덱스에 따라 내부 이미지 뷰의 이미지 변경하도록 수정 예정
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { //현재 Fragment를 위해 뷰 생성
        /***
         * Called to have the fragment instantiate its user interface view.
         * This is optional, and non-graphical fragments can return null.
         * This will be called between onCreate(Bundle) and onViewCreated(View, Bundle).
         ***/

        View view;
        Log.d("currentFragmentType :", this.fragmentType.toString());

        switch (this.fragmentType) {
            case BASIC_INFO:
                view = inflater.inflate(R.layout.fishdetail_basicinfo_fragment, container, false);
                break;

            case DENIED_INFO:
                view = inflater.inflate(R.layout.fishdetail_deniedinfo_fragment, container, false);
                break;

            case BANNER:
            case HELP:
                view = inflater.inflate(R.layout.viewpager_fragment, container, false);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + this.fragmentType);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { //onCreateView이후 즉시 호출
        /***
         * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned,
         * but before any saved state has been restored in to the view.
         * ---
         * view : The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle)
         ***/
        super.onViewCreated(view, savedInstanceState);

        switch (this.fragmentType) {
            case BASIC_INFO:
                TextView fishDetail_name_textView = view.findViewById(R.id.fishDetail_name_textView); //이름(국명)
                ImageView fishDetail_imageView = view.findViewById(R.id.fishDetail_imageView); //이미지
                TextView fishDetail_scientific_name_textView = view.findViewById(R.id.fishDetail_scientific_name_textView); //학명
                TextView fishDetail_bio_class_textView = view.findViewById(R.id.fishDetail_bio_class_textView); //생물분류
                TextView fishDetail_shape_textView = view.findViewById(R.id.fishDetail_shape_textView); //형태
                TextView fishDetail_distribution_textView = view.findViewById(R.id.fishDetail_distribution_textView); //분포
                TextView fishDetail_body_length_textView = view.findViewById(R.id.fishDetail_body_length_textView); //몸길이
                TextView fishDetail_habitat_textView = view.findViewById(R.id.fishDetail_habitat_textView); //서식지
                TextView fishDetail_warnings_textView = view.findViewById(R.id.fishDetail_warnings_textView); //주의사항
                break;

            case DENIED_INFO:
                TextView fishDetail_denied_length_textView = view.findViewById(R.id.fishDetail_denied_length_textView); //금지체장
                TextView fishDetail_denied_weight_textView = view.findViewById(R.id.fishDetail_denied_weight_textView); //금지체중
                TextView fishDetail_denied_water_depth_textView = view.findViewById(R.id.fishDetail_denied_water_depth_textView); //수심
                TextView fishDetail_special_prohibit_admin_area_textView = view.findViewById(R.id.fishDetail_special_prohibit_admin_area_textView); //특별 금지구역
                TextView fishDetail_special_prohibit_admin_start_date_textView = view.findViewById(R.id.fishDetail_special_prohibit_admin_start_date_textView); //금지시작기간
                TextView fishDetail_special_prohibit_admin_end_date_textView = view.findViewById(R.id.fishDetail_special_prohibit_admin_end_date_textView); //금지종료기간

                break;

            case BANNER:
            case HELP:
                ImageView viewPager_imageView = view.findViewById(R.id.viewPager_imageView);

                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this.fragmentType);
        }
    }
}
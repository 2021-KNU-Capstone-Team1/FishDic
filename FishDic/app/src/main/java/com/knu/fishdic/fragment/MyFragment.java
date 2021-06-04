package com.knu.fishdic.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.knu.fishdic.R;
import com.knu.fishdic.manager.DBManager;
import com.knu.fishdic.utils.ImageUtility;

// 어류 상세 정보 페이지의 동적 화면(기본 정보 및 금지 행정 표) 추가 및 메인화면의 배너, 이용가이드를 위한 MyFragment 정의
// https://developer.android.com/reference/androidx/fragment/app/Fragment.html

/***
 * 1) FragmentManager에 의해 프래그먼트 추가와 삭제는 트랜잭션 단위로 수행
 * 2) 프래그먼트 추가는 ViewGroup에 수행
 * 3) 액티비티와 마찬가지로 Back Stack 존재
 ***/

public class MyFragment extends Fragment {
    public static final String FRAGMENT_TYPE_KEY = "fragmentTypeKey"; //Fragment의 타입 키 값
    public static final String POSITION_KEY = "positionKey"; //position 키 값
    public static final String IMAGE_KEY = "imageKey"; //이미지의 키 값

    public enum FRAGMENT_TYPE { //Fragment의 타입 정의
        BASIC_INFO, //어류 상세 정보 페이지의 기본 정보
        DENIED_INFO, //어류 상세 정보 페이지의 금지행정 정보
        BANNER, //메인화면의 배너
        HELP //이용가이드
    }

    private int position; //Fragment의 위치
    private FRAGMENT_TYPE fragmentType; //현재 Fragment의 타입

    private Bitmap refImage; //이미지 참조 변수
    private Bundle refQueryResult; //어류 상세 정보 페이지의 기본 정보 및 금지 행정 정보를 Fragment에 바인딩 위해 DB로부터 읽어들인 결과의 참조 변수

    public static MyFragment newInstance(int position, Bundle args) { //Fragment의 인스턴스 객체 생성
        /***
         * fragmentType : 인스턴스 객체 생성을 위한 Fragment의 타입
         * position : 인스턴스 객체 생성을 위한 Fragment의 위치
         * args : 인스턴스 객체 생성을 위한 타입, 이미지, 어류 상세정보를 포함하고 있는 키(문자열), 값 쌍의 데이터 등
         ***/

        MyFragment fragment = new MyFragment();

        //디버그용 DBManager.doParseQueryResultBundle(args, 0, true);
        FRAGMENT_TYPE fragmentType = (FRAGMENT_TYPE) args.getSerializable(FRAGMENT_TYPE_KEY);
        switch (fragmentType) { //Fragment의 타입에 따라 Fragment의 인스턴스 객체 생성을 위한 데이터 설정
            case BASIC_INFO: //어류 상세 정보 페이지의 기본 정보
            case DENIED_INFO: //어류 상세 정보 페이지의 금지행정 정보
                /***
                 * 어류 테이블 : 이름, 학명, 이미지, 형태, 분포, 몸길이, 서식지, 주의사항
                 * 생물분류 테이블 : 생물분류
                 * 금어기 테이블 : 금지체장, 금지체중, 수심
                 * 특별 금지행정 테이블 : 특별 금지행정 ID, 특별 금지구역, 금지시작기간, 금지종료기간
                 ***/
                fragment.refQueryResult = args.getBundle(DBManager.QUERY_RESULT_KEY); //파라미터로 받은 쿼리 결과 참조
                break;

            case BANNER: //메인화면의 배너
            case HELP: //이용가이드
                fragment.refImage = args.getParcelable(IMAGE_KEY); //파라미터로 받은 이미지 참조
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + fragmentType);
        }

        Bundle subArgs = new Bundle();
        subArgs.putInt(POSITION_KEY, position);
        subArgs.putSerializable(FRAGMENT_TYPE_KEY, fragmentType);
        fragment.setArguments(subArgs);

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
        this.position = getArguments().getInt(POSITION_KEY, 0);
        this.fragmentType = (FRAGMENT_TYPE) getArguments().getSerializable(FRAGMENT_TYPE_KEY);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { //현재 Fragment를 위해 뷰 생성
        /***
         * Called to have the fragment instantiate its user interface view.
         * This is optional, and non-graphical fragments can return null.
         * This will be called between onCreate(Bundle) and onViewCreated(View, Bundle).
         ***/

        View view;
        //디버그용 Log.d("currentFragmentType", this.fragmentType.toString());

        switch (this.fragmentType) { //뷰 설정
            case BASIC_INFO: //어류 상세 정보 페이지의 기본 정보
                view = inflater.inflate(R.layout.fishdetail_basicinfo_fragment, container, false);
                break;

            case DENIED_INFO: //어류 상세 정보 페이지의 금지행정 정보
                view = inflater.inflate(R.layout.fishdetail_deniedinfo_fragment, container, false);
                break;

            case BANNER: //메인화면의 배너
            case HELP: //이용가이드
                view = inflater.inflate(R.layout.viewpager_fragment, container, false);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + this.fragmentType);
        }

        switch (this.fragmentType) { //데이터 바인딩
            case BASIC_INFO: //어류 상세 정보 페이지의 기본 정보
                TextView fishDetail_name_textView = view.findViewById(R.id.fishDetail_name_textView); //이름(국명)
                ImageView fishDetail_imageView = view.findViewById(R.id.fishDetail_imageView); //이미지
                TextView fishDetail_scientific_name_textView = view.findViewById(R.id.fishDetail_scientific_name_textView); //학명
                TextView fishDetail_bio_class_textView = view.findViewById(R.id.fishDetail_bio_class_textView); //생물분류
                TextView fishDetail_shape_textView = view.findViewById(R.id.fishDetail_shape_textView); //형태
                TextView fishDetail_distribution_textView = view.findViewById(R.id.fishDetail_distribution_textView); //분포
                TextView fishDetail_body_length_textView = view.findViewById(R.id.fishDetail_body_length_textView); //몸길이
                TextView fishDetail_habitat_textView = view.findViewById(R.id.fishDetail_habitat_textView); //서식지
                TextView fishDetail_warnings_textView = view.findViewById(R.id.fishDetail_warnings_textView); //주의사항

                fishDetail_name_textView.setText(this.refQueryResult.getString(DBManager.NAME));

                /*** 이미지가 존재 할 경우 해당 이미지로 설정, 존재하지 않을 경우 대체 이미지 설정 ***/
                Bitmap bitmap = ImageUtility.decodeFromByteArray(this.refQueryResult.getByteArray(DBManager.IMAGE));
                if (bitmap != null)
                    fishDetail_imageView.setImageBitmap(bitmap);
                else
                    fishDetail_imageView.setImageResource(R.drawable.photo_coming_soon_600x600);

                fishDetail_scientific_name_textView.setText(this.refQueryResult.getString(DBManager.SCIENTIFIC_NAME));
                fishDetail_bio_class_textView.setText(this.refQueryResult.getString(DBManager.BIO_CLASS));
                fishDetail_shape_textView.setText(this.refQueryResult.getString(DBManager.SHAPE));
                fishDetail_distribution_textView.setText(this.refQueryResult.getString(DBManager.DISTRIBUTION));
                fishDetail_body_length_textView.setText(this.refQueryResult.getString(DBManager.BODY_LENGTH));
                fishDetail_habitat_textView.setText(this.refQueryResult.getString(DBManager.HABITAT));
                fishDetail_warnings_textView.setText(this.refQueryResult.getString(DBManager.WARNINGS));
                break;

            case DENIED_INFO: //어류 상세 정보 페이지의 금지행정 정보
                TextView fishDetail_denied_length_textView = view.findViewById(R.id.fishDetail_denied_length_textView); //금지체장
                TextView fishDetail_denied_weight_textView = view.findViewById(R.id.fishDetail_denied_weight_textView); //금지체중
                TextView fishDetail_denied_water_depth_textView = view.findViewById(R.id.fishDetail_denied_water_depth_textView); //수심
                TextView fishDetail_special_prohibit_admin_area_textView = view.findViewById(R.id.fishDetail_special_prohibit_admin_area_textView); //특별 금지구역
                TextView fishDetail_special_prohibit_admin_start_date_textView = view.findViewById(R.id.fishDetail_special_prohibit_admin_start_date_textView); //금지시작기간
                TextView fishDetail_special_prohibit_admin_end_date_textView = view.findViewById(R.id.fishDetail_special_prohibit_admin_end_date_textView); //금지종료기간

                fishDetail_denied_length_textView.setText(this.refQueryResult.getString(DBManager.DENIED_LENGTH));
                fishDetail_denied_weight_textView.setText(this.refQueryResult.getString(DBManager.DENIED_WEIGHT));
                fishDetail_denied_water_depth_textView.setText(this.refQueryResult.getString(DBManager.DENIED_WATER_DEPTH));
                fishDetail_special_prohibit_admin_area_textView.setText(this.refQueryResult.getString(DBManager.SPECIAL_PROHIBIT_ADMIN_AREA));
                fishDetail_special_prohibit_admin_start_date_textView.setText(this.refQueryResult.getString(DBManager.SPECIAL_PROHIBIT_ADMIN_START_DATE));
                fishDetail_special_prohibit_admin_end_date_textView.setText(this.refQueryResult.getString(DBManager.SPECIAL_PROHIBIT_ADMIN_END_DATE));
                break;

            case BANNER: //메인화면의 배너
            case HELP: //이용가이드
                ImageView viewPager_imageView = view.findViewById(R.id.viewPager_imageView);
                viewPager_imageView.setImageBitmap(this.refImage);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + this.fragmentType);
        }


        return view;
    }
}
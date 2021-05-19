package com.knu.fishdic.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.knu.fishdic.R;

// 사용자 정의 대화상자를 위한 MyDialogFragment 정의
// https://developer.android.com/reference/android/app/DialogFragment

public class MyDialogFragment extends DialogFragment {
    public static final String DIALOG_TYPE_KEY = "dialogTypeKey"; //다이얼로그 타입 키
    public enum DIALOG_TYPE { //다이얼로그 타입 정의
        FISH_DETAIL_ERR, //어류 상세정보 오류 메시지 창
    }

    public static MyDialogFragment newInstance(Bundle args) { //MyDialogFramgent 인스턴스 객체 생성
        MyDialogFragment myDialogFragment = new MyDialogFragment();
        myDialogFragment.setArguments(args);
        return myDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) { //Dialog 생성 시 호출
        /***
         * This method will be called after onCreate(android.os.Bundle) and
         * before Fragment.onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle).
         * The default implementation simply instantiates and returns a Dialog class.
         ***/

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); //getContext() : 현재 Fragment와 얀관 된 Context 이용
        DIALOG_TYPE currentDialogType = (DIALOG_TYPE) getArguments().getSerializable(DIALOG_TYPE_KEY);
        switch (currentDialogType) {
            case FISH_DETAIL_ERR: //어류 상세정보 오류 메시지 창

                builder.setIcon(R.drawable.error_64x64);
                builder.setTitle(R.string.not_exist_fish_title);
                builder.setMessage(R.string.not_exist_fish_message);
                builder.setPositiveButton(R.string.not_exist_fish_button, (dialog, which) -> getActivity().onBackPressed()); //현재 액티비티 종료
                return builder.create();

            default:
                throw new IllegalStateException("Unexpected value: " + currentDialogType);
        }

    }
}

package com.knu.fishdic.recyclerview;

// 이달의 금어기, 도감에 사용되는 RecyclerView를 위한 RecyclerViewItem 정의

import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.knu.fishdic.FishDic;

public class RecyclerViewItem {
    public enum DIC_TYPE //도감 타입 정의
    {
        /***
         * 도감의 경우 내용에 생물분류 출력
         * 이달의 금어기의 경우 금지체장, 금지체중, 수심, 특별금지구역, 금지시작기간, 금지종료기간 출력
         ***/

        FISH_DIC, //어류 도감
        DENIED_FISH //이달의 금어기
    }

    private int resId; //이미지 아이디
    private String title; //제목
    private String content; //내용

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content, DIC_TYPE dic_type) {
        //수정예정 : 도감 타입에 따라 내용 변경할것

        switch (dic_type) {
            case FISH_DIC:
                break;
            case DENIED_FISH:
                break;
        }

        this.content = content;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }
}

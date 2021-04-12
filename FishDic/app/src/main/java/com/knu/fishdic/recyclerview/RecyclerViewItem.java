package com.knu.fishdic.recyclerview;

// 이달의 금어기, 도감에 사용되는 RecyclerView를 위한 RecyclerViewItem 정의

public class RecyclerViewItem {
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

    public void setContent(String content) {
        this.content = content;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }
}

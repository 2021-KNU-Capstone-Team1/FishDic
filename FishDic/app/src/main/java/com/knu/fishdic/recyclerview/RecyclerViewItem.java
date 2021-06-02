package com.knu.fishdic.recyclerview;

// 이달의 금어기, 도감, 판별 결과에 사용되는 RecyclerView를 위한 RecyclerViewItem 정의

public class RecyclerViewItem {
    private float comparableValue; //정렬 하기 위해 비교 가능 한 값 (기본값 0)
    private byte[] image; //이미지
    private String title; //제목
    private String content; //내용

    public RecyclerViewItem() {
        this.comparableValue = 0;
        this.image = null;
        this.title = null;
        this.content = null;
    }

    public float getComparableValue() {
        return comparableValue;
    }

    public void setComparableValue(float comparableValue) {
        this.comparableValue = comparableValue;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;

    }

    public byte[] getImage() {
        return this.image;
    }

    public void setImage(byte[] image) {
           this.image = image;
    }
}

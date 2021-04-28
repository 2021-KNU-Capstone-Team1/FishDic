package com.knu.fishdic.recyclerview;

// 이달의 금어기, 도감에 사용되는 RecyclerView를 위한 RecyclerViewItem 정의

public class RecyclerViewItem {
    private byte[] image; //이미지
    private String title; //제목
    private String content; //내용

    public RecyclerViewItem() {
        this.image = null;
        this.title = null;
        this.content = null;
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

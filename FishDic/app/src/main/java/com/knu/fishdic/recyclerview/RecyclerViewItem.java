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
        /***
         * 도감의 경우 내용에 생물분류 출력
         * 이달의 금어기의 경우 금지체장, 금지체중, 수심, 특별금지구역, 금지시작기간, 금지종료기간 출력
         ***/

        this.content = content;
    }

    public byte[] getImage() {
        return this.image;
    }

    public void setImage(byte[] image) {
           this.image = image;
    }

    public int getImageLength() { //이미지 byte 배열 길이 반환
        if (this.image != null)
            return this.image.length;
        else //할당 된 이미지가 없으면
            return 0;
    }
}

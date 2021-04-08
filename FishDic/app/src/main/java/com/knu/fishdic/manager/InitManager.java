package com.knu.fishdic.manager;

// 앱 초기화를 위한 InitManager 정의

public class InitManager {
    public static void doSomeInitProc() {
        DBManager.test(); //인스턴스를 만들지 않고 정적 함수 실행하여 작업 수행
    }
}
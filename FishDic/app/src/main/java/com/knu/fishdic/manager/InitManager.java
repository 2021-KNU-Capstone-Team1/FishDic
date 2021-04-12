package com.knu.fishdic.manager;

// 앱 초기화를 위한 InitManager 정의

import static java.lang.System.gc;

public class InitManager {
    public static void doDataBindingJob() { //DBManager에 의해 데이터 받아와서 바로 사용 할 수 있게 바인딩 작업 수행
        DBManager dbManager;
        try { //DBManager 생성하고 데이터 받아와서 바로 사용 할 수 있게 바인딩 작업 수행
            dbManager = new DBManager();
            dbManager.getDataFromDB();

            //FishDic의 dicItemArray에 넣는다
            
            dbManager.close();
        } catch (Exception e) {
            throw e;
        } finally {
            dbManager = null;
            gc();
        }
    }
}
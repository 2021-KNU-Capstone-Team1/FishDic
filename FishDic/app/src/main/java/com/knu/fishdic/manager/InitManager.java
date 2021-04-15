package com.knu.fishdic.manager;

// 앱 초기화를 위한 InitManager 정의

import com.knu.fishdic.FishDic;
import com.knu.fishdic.recyclerview.RecyclerAdapter;

public class InitManager {
    public static void doDataBindingJob() { //DBManager에 의해 데이터 받아와서 바로 사용 할 수 있게 바인딩 작업 수행
        FishDic.globalDBManager = new DBManager();
        FishDic.global_Dic_RecyclerAdapter = new RecyclerAdapter();
        FishDic.global_DeniedFish_RecyclerAdapter = new RecyclerAdapter();
        FishDic.globalDBManager.doBindingAllFishData(FishDic.global_Dic_RecyclerAdapter);
        FishDic.globalDBManager.doBindingAllDeniedFishData(FishDic.global_DeniedFish_RecyclerAdapter);
    }
}
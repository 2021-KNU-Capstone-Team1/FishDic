package com.knu.fishdic.manager;

// 앱 초기화를 위한 InitManager 정의

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.recyclerview.RecyclerAdapter;

public class InitManager {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void doBindForRecylerAdapter() { //도감 및 이달의 금어기를 위한 데이터 바인딩 작업 수행
        if(FishDic.globalDBManager != null || FishDic.globalDicRecyclerAdapter != null || FishDic.globalDeniedFishRecyclerAdapter != null)
           return;

        FishDic.globalDBManager = new DBManager();
        FishDic.globalDicRecyclerAdapter = new RecyclerAdapter();
        FishDic.globalDeniedFishRecyclerAdapter = new RecyclerAdapter();

        FishDic.globalDicRecyclerAdapter.addItemFromBudle(FishDic.globalDBManager.getSimpleFishBundle(DBManager.FISH_DATA_TYPE.ALL_FISH));
        FishDic.globalDeniedFishRecyclerAdapter.addItemFromBudle(FishDic.globalDBManager.getSimpleFishBundle(DBManager.FISH_DATA_TYPE.DENIED_FISH));
    }

    public static void debugBannerTest(){

    }

    public static void debugHelpTest(){

    }
}
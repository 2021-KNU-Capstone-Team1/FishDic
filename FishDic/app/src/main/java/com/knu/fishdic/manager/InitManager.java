package com.knu.fishdic.manager;

// 앱 초기화를 위한 InitManager 정의

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.recyclerview.RecyclerAdapter;

public class InitManager {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void doDataBindingJob() { //DBManager에 의해 데이터 받아와서 바로 사용 할 수 있게 바인딩 작업 수행
        FishDic.globalDBManager = new DBManager();
        FishDic.globalDicRecyclerAdapter = new RecyclerAdapter();
        FishDic.globalDeniedFishRecyclerAdapter = new RecyclerAdapter();
        FishDic.globalDBManager.doBindingAllFishData(FishDic.globalDicRecyclerAdapter);
        FishDic.globalDBManager.doBindingAllDeniedFishData(FishDic.globalDeniedFishRecyclerAdapter);
    }
}
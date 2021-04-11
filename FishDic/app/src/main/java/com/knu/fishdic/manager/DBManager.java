package com.knu.fishdic.manager;

// 이달의 금어기, 도감관련 모든 기능을 위한 DBManager 정의

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBManager extends SQLiteOpenHelper {
    public SQLiteDatabase sqlDataBase;

    private static String DB_PATH = ""; //DB 경로
    private static final String DB_NAME = "FishDicDB.db"; //DB 이름
    private Context context;

    private enum DB_STATE { //DB 상태 정의
        INIT, //초기 상태
        OLD, //구 버전
        UPDATED //갱신 된 버전
    };

    public DBManager(Context context) {
        super(context, DB_NAME, null, 1); //SQLiteOpenHelper(context, name, factory, version)
        DB_PATH = "/data/data/" + context.getPackageName() + "/databases/"; //안드로이드의 DB 저장 경로는 "/data/data/앱 이름/databases/"
        this.context = context;

       switch(chkDataBase()) //DB 상태 확인
       {
           case INIT: //초기 상태일 경우
               copyDataBase(); //assets으로부터 시스템으로 DB 복사

           default:
               break;
       }

       this.sqlDataBase = this.getReadableDatabase(); //읽기 전용 DB 로드
    }
    @Override
    public synchronized void close() {
        if (this.sqlDataBase != null) {
            this.sqlDataBase.close();
        }

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private DB_STATE chkDataBase() {  //기존 DB 확인
        File dbFile = new File(DB_PATH + DB_NAME);

        //나중에 DB 버전 확인 한다면 코드 수정
        if(dbFile.exists())
            return DB_STATE.UPDATED;

        return DB_STATE.INIT;
    }

    private void copyDataBase() { //assets으로부터 시스템으로 DB 복사
        try {
            File folder = new File(DB_PATH);
            if (!folder.exists()) {
                folder.mkdir();
            }

            InputStream inputStream = context.getAssets().open(DB_NAME);
            String outFileName = DB_PATH + DB_NAME;
            OutputStream outputStream = new FileOutputStream(outFileName);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            ;
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getDataFromDataBase() {
    }
}
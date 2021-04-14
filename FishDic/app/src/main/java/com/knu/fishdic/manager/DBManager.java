package com.knu.fishdic.manager;

// 이달의 금어기, 도감 관련 모든 기능을 위한 DBManager 정의

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.recyclerview.RecyclerAdapter;
import com.knu.fishdic.recyclerview.RecyclerViewItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class DBManager extends SQLiteOpenHelper {
    private SQLiteDatabase sqlDB; //DB 접근 위한 SQLiteDatabase 객체
    private Cursor cursor; //데이터베이스 쿼리에서 반환 된 결과 집합에 대한 임의의 읽기-쓰기 액세스를 제공하는 인터페이스
    private static String DB_PATH = ""; //DB 경로
    private static final String DB_NAME = "FishDicDB.db"; //DB 이름

    private static final String FISH_DIC_TABLE = "어류_테이블";
    private static final String DENIED_FISH_TABLE = "금어기_테이블";
    private static final String BIO_CLASS_TABLE = "생물분류_테이블";
    private static final String SPECIAL_PROHIBIT_ADMIN_TABLE = "특별_금지행정_테이블";
    private static final String SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE = "특별_금지행정_관계_테이블";

    private static final String FISH_DIC_QUERY = "SELECT 어류_테이블.이름, 생물분류_테이블.생물분류 FROM 어류_테이블 INNER JOIN 생물분류_테이블 ON 어류_테이블.이름 = 생물분류_테이블.이름"; //도감 출력

    /***
     * 특별 금지행정의 특별 금지구역이 별도로 지정되지 않은 금어기는, 전 지역을 대상으로 포획을 금지한다.
     * 특별 금지행정의 금지기간이 별도로 지정되지 않은 금어기는, 별도의 행정명령 시까지 포획을 금지한다.
     ***/
    //이달의 금어기 출력 (금지기간에 속하는 것과 금지기간이 정해지지 않은 것 모두 출력)

    private enum DB_STATE { //DB 상태 정의
        INIT, //초기 상태
        OUT_DATED, //구 버전
        UPDATED //갱신 된 버전
    }

    public DBManager() {
        super(FishDic.globalContext, DB_NAME, null, 1); //SQLiteOpenHelper(context, name, factory, version)
        DB_PATH = "/data/data/" + FishDic.globalContext.getPackageName() + "/databases/"; //안드로이드의 DB 저장 경로는 "/data/data/앱 이름/databases/"

        switch (this.chkDB()) //DB 상태 확인
        {
            case INIT: //초기 상태일 경우
                this.copyDB(); //assets으로부터 시스템으로 DB 복사

                break;

            default:
                break;
        }

        this.sqlDB = this.getReadableDatabase(); //읽기 전용 DB 로드
    }

    @Override
    public synchronized void close() {
        if (this.sqlDB != null) {
            this.sqlDB.close();
        }

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqlDB) {
    }

    @Override
    public void onOpen(SQLiteDatabase sqlDB) {
        super.onOpen(sqlDB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDB, int oldVersion, int newVersion) {
    }

    private DB_STATE chkDB() {  //기존 DB 확인
        File dbFile = new File(DB_PATH + DB_NAME);

        /*
        //나중에 DB 버전 확인 한다면 코드 수정
        if (dbFile.exists())
            return DB_STATE.UPDATED;

        return DB_STATE.INIT;
        */
        return DB_STATE.INIT; //디버그 위해 매 번 새로 복사
    }

    private void copyDB() { //assets으로부터 시스템으로 DB 복사
        try {
            File folder = new File(DB_PATH);
            if (!folder.exists()) {
                folder.mkdir();
            }

            InputStream inputStream = FishDic.globalContext.getAssets().open(DB_NAME);
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

    public void addFishDicListFromDB(RecyclerAdapter recyclerAdapter) { //DB로부터 모든 어류 데이터 추가
       // this.cursor = this.sqlDB.query(FISH_DIC_TABLE, , null, null, null, null ,null);
        while(this.cursor.moveToNext()) { //행 데이터 수만큼 반복해서 전달
            //recyclerAdapter.addItem();    실행이 안되서 잠깐 주석처리하였음. -남진
        }

        //this.sqlDB.execSQL();
        this.cursor.close();
    }

    public void addDeniedFishListFromDB(RecyclerAdapter recyclerAdapter)
    {

    }

    public void getFishDetailFromDB(String fishName)
    {

    }

    public void searchFishNameFromDB()
    {

    }

    private int getCurrentMonth() //현재 달 반환
    {
        Calendar cal=Calendar.getInstance();
        return cal.get(Calendar.MONTH)+1;
    }
}
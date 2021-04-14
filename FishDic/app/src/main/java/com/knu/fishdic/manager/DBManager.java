package com.knu.fishdic.manager;

// 이달의 금어기, 도감 관련 모든 기능을 위한 DBManager 정의

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.CursorAdapter;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.recyclerview.RecyclerAdapter;

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

    private static final String FISH_DIC_QUERY = "SELECT 어류_테이블.이름, 어류_테이블.이미지, 생물분류_테이블.생물분류 FROM 어류_테이블 INNER JOIN 생물분류_테이블 ON 어류_테이블.이름 = 생물분류_테이블.이름"; //도감 출력

    //CursorAdapter
    /***
     * 특별 금지행정의 특별 금지구역이 별도로 지정되지 않은 금어기는, 전 지역을 대상으로 포획을 금지한다.
     * 특별 금지행정의 금지기간이 별도로 지정되지 않은 금어기는, 별도의 행정명령 시까지 포획을 금지한다.
     ***/
    //이달의 금어기 출력 (금지기간에 속하는 것과 금지기간이 정해지지 않은 것 모두 출력)
/*
SELECT 금어기_테이블.*, 특별_금지행정_테이블.특별_금지구역, 특별_금지행정_테이블.금지시작기간, 특별_금지행정_테이블.금지종료기간
FROM 금어기_테이블
	LEFT OUTER JOIN 특별_금지행정_관계_테이블 ON 금어기_테이블.이름 = 특별_금지행정_관계_테이블.이름
	LEFT OUTER JOIN 특별_금지행정_테이블 ON 특별_금지행정_관계_테이블.특별_금지행정_ID = 특별_금지행정_테이블.특별_금지행정_ID;
 */

    private enum DB_STATE { //DB 상태 정의
        INIT, //초기 상태
        OUT_DATED, //구 버전
        UPDATED //갱신 된 버전
    }

    public DBManager() {
        super(FishDic.globalContext, DB_NAME, null, 1); //SQLiteOpenHelper(context, name, factory, version)
        DB_PATH = "/data/data/" + FishDic.globalContext.getPackageName() + "/databases/"; //안드로이드의 DB 저장 경로는 "/data/data/앱 이름/databases/"

        switch (this.getCurrentDBState()) //기존 DB 상태 확인
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
        copyDB();
    }

    private DB_STATE getCurrentDBState() {  //기존 DB 상태 반환
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

    public Cursor getAllFishDataCursorFromDB(RecyclerAdapter recyclerAdapter) { //DB로부터 모든 어류 데이터 커서 반환
        //SELECT 어류_테이블.이름, 어류_테이블.이미지, 생물분류_테이블.생물분류 FROM 어류_테이블 INNER JOIN 생물분류_테이블 ON 어류_테이블.이름 = 생물분류_테이블.이름
        this.cursor = this.sqlDB.rawQuery(FISH_DIC_QUERY.toString(), null);
        return this.cursor;
        //this.cursor.close();
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

    private String getCurrentYearMonth() //현재 "년-달" 반환
    {
        Calendar cal = Calendar.getInstance();
        String result = String.valueOf(cal.get(Calendar.YEAR)) + "-" + String.valueOf(cal.get(Calendar.MONTH)+1); //현재 "년-달" 문자열
        return result;
    }
}
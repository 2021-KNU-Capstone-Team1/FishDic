package com.knu.fishdic.manager;

// 이달의 금어기, 도감 관련 모든 기능을 위한 DBManager 정의

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.recyclerview.RecyclerAdapter;
import com.knu.fishdic.recyclerview.RecyclerViewItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DBManager extends SQLiteOpenHelper {
    private enum DB_STATE { //DB 상태 정의
        INIT, //초기 상태
        OUT_DATED, //구 버전
        UPDATED //갱신 된 버전
    }
    
    private enum DATE_FORMAT_TYPE{ //날짜 형식 타입 정의
        WITH_SEPARATOR, //구분자 사용(YY-MM-dd)
        WITHOUT_SEPARATOR //구분자 사용하지 않음(YYMMdd)
    }

    private static final String DB_SERVER = "https://raw.githubusercontent.com/2021-KNU-Capstone-Team1/FishDic/master/DB/"; //DB 저장 된 서버 경로
    private static final String DB_VERSION_FILE_NAME = "version"; //DB 버전 관리 파일 이름
    private static final int DB_VERSION_FILE_SIZE = 8; //DB 버전 관리 파일 크기 (바이트 단위)

    private SQLiteDatabase sqlDB; //DB 접근 위한 SQLiteDatabase 객체
    private static String DB_PATH = ""; //DB 경로
    //private static String CACHE_PATH = ""; //임시폴더 경로
    private static final String DB_NAME = "FishDicDB.db"; //DB 이름

    //테이블명 정의
    private static final String FISH_DIC_TABLE = "어류_테이블";
    private static final String DENIED_FISH_TABLE = "금어기_테이블";
    private static final String BIO_CLASS_TABLE = "생물분류_테이블";
    private static final String SPECIAL_PROHIBIT_ADMIN_TABLE = "특별_금지행정_테이블";
    private static final String SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE = "특별_금지행정_관계_테이블";

    //필드명 정의
    private static final String ALL = "*";
    private static final String NAME = "이름";
    private static final String SCIENTIFIC_NAME = "학명";
    private static final String IMAGE = "이미지";
    private static final String SHAPE = "형태";
    private static final String DISTRIBUTION = "분포";
    private static final String BODY_LENGTH = "몸길이";
    private static final String HABITAT = "서식지";
    private static final String WARNINGS = "주의사항";

    private static final String BIO_CLASS = "생물분류";

    private static final String DENIED_LENGTH = "금지체장";
    private static final String DENIED_WEIGHT = "금지체중";
    private static final String DENIED_WATER_DEPTH = "수심";

    private static final String SPECIAL_PROHIBIT_ADMIN_ID = "특별_금지행정_ID";
    private static final String SPECIAL_PROHIBIT_ADMIN_AREA = "특별_금지구역";
    private static final String SPECIAL_PROHIBIT_ADMIN_START_DATE = "금지시작기간";
    private static final String SPECIAL_PROHIBIT_ADMIN_END_DATE = "금지종료기간";

    private static final String EMPTY_DATA = "등록 된 정보가 없습니다."; //입력되지 않은 데이터에 대하여 치환 할 문자열

    @RequiresApi(api = Build.VERSION_CODES.N)
    public DBManager() {
        super(FishDic.globalContext, DB_NAME, null, 1); //SQLiteOpenHelper(context, name, factory, version)
        DB_PATH = "/data/data/" + FishDic.globalContext.getPackageName() + "/databases/"; //안드로이드의 DB 저장 경로는 "/data/data/앱 이름/databases/"
        //CACHE_PATH = FishDic.globalContext.getCacheDir().toString() + "/";

        switch (this.getCurrentDBState()) //기존 DB 상태 확인
        {
            case INIT: //초기 상태일 경우
                this.copyDB();
                break;

            case OUT_DATED:
                break;

            case UPDATED:
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private DB_STATE getCurrentDBState() {  //기존 DB 상태 반환
        /***
         * 1) 로컬 DB와 로컬 DB 버전 관리 파일이 존재하지 않을 경우 서버로부터의 갱신을 위한 초기 상태 반환
         * 2) 로컬 DB와 로컬 DB 버전 관리 파일이 존재할 경우 서버와 로컬 DB 버전을 비교하여
         *  2-1) 로컬 DB 버전 >= 서버 DB 버전일 경우 : 구 버전 상태 반환
         *  2-2) 로컬 DB 버전 < 서버 DB 버전일 경우 : 최신 버전 상태 반환
         ***/
        byte[] buffer = new byte[DB_VERSION_FILE_SIZE];

        //수정예정, 로컬에 DB 버전 관리 파일 생성 및 현재 날짜 기록
        File folder = new File(DB_PATH);
        if (!folder.exists()) {
            folder.mkdir();
        }

        File currentDBFile = new File(DB_PATH + DB_NAME); //현재 로컬 DB
        File currentDBVersionFile = new File(DB_PATH + DB_VERSION_FILE_NAME); //로컬 DB 버전

        return DB_STATE.INIT;

        //if (!currentDBVersionFile.exists() || !currentDBFile.exists()) { //기존 DB가 존재하지 않거나, 버전 관리 파일이 존재하지 않을 경우
        //}

        //AsyncDownloader asyncDownloader = new AsyncDownloader(DB_SERVER + DB_VERSION_FILE_NAME, null, buffer); //(String targetUrl, String outputPath, byte[] outputBuffer)
        //asyncDownloader.execute();

        /*삭제 예정 : UI 스레드가 아닌 백그라운드 작업으로 돌려야 함
        try { //서버의 DB 버전과 로컬 DB 버전 비교
            String serverDBVersion;
            String currentDBVersion;
            byte[] buffer = new byte[DB_VERSION_FILE_SIZE];
            URL dbVersionUrl = new URL(DB_SERVER + DB_VERSION_FILE_NAME); //서버의 DB 버전
            HttpURLConnection urlConnection = (HttpURLConnection) dbVersionUrl.openConnection(); //서버 연결

            //서버 DB 버전 가져오기
            InputStream urlConnectionInputStream = urlConnection.getInputStream(); //연결로부터 입력 스트림 생성
            urlConnectionInputStream.read(buffer, 0, DB_VERSION_FILE_SIZE);
            serverDBVersion = buffer.toString();
            urlConnectionInputStream.close();
            urlConnection.disconnect();

            //로컬 DB 버전 가져오기
            InputStream inputStream = new FileInputStream(currentDBVersionFile);
            inputStream.read(buffer, 0, DB_VERSION_FILE_SIZE);
            currentDBVersion = buffer.toString();
            inputStream.close();

            if (currentDBVersion.compareTo(serverDBVersion) >= 0) { //로컬 DB 버전 >= 서버 DB 버전일 경우

            } else { //로컬 DB 버전 < 서버 DB 버전일 경우

            }

            Log.d("로컬 DB 버전 : ", currentDBVersion);
            Log.d("서버 DB 버전 : ", serverDBVersion);

        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }

    private void updateDBFromServer() { //서버로부터 최신 DB 갱신
        try {
            URL dbUrl = new URL(DB_SERVER + "/FishDicDB.db"); //서버의 DB 경로

            InputStream inputStream = FishDic.globalContext.getAssets().open(DB_NAME);
            String outFileName = DB_PATH + DB_NAME;
            OutputStream outputStream = new FileOutputStream(outFileName);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyDB() { //임시 : assets으로부터 시스템으로 DB 복사
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
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doBindingAllFishData(RecyclerAdapter recyclerAdapter) //모든 어류 정보 바인딩 작업 수행
    {
        String sqlQuery = "SELECT " + FISH_DIC_TABLE + "." + NAME + ", " + FISH_DIC_TABLE + "." + IMAGE + ", " + BIO_CLASS_TABLE + "." + BIO_CLASS +
                " FROM " + FISH_DIC_TABLE +
                " INNER JOIN " + BIO_CLASS_TABLE + " ON " + FISH_DIC_TABLE + "." + NAME + " = " + BIO_CLASS_TABLE + "." + NAME;

        Log.d("모든 어류 Query : ", sqlQuery);
        Cursor cursor = this.sqlDB.rawQuery(sqlQuery, null);

        int nameIndex = cursor.getColumnIndex(NAME);
        int imageIndex = cursor.getColumnIndex(IMAGE);
        int bioClassIndex = cursor.getColumnIndex(BIO_CLASS);

        while (cursor.moveToNext()) //쿼리 된 내용에 대하여 바인딩 작업 수행
        {
            RecyclerViewItem recyclerViewItem = new RecyclerViewItem();

            recyclerViewItem.setTitle(cursor.getString(nameIndex)); //어류 이름
            recyclerViewItem.setImage(cursor.getBlob(imageIndex)); //어류 이미지
            recyclerViewItem.setContent("생물분류 : " + cursor.getString(bioClassIndex)); //생물 분류

            recyclerAdapter.addItem(recyclerViewItem);
        }

        cursor.close();
    }

    public void doBindingAllDeniedFishData(RecyclerAdapter recyclerAdapter) //모든 이달의 금어기 정보 바인딩 작업 수행
    {
        String currentDate = this.getCurrentDate(DATE_FORMAT_TYPE.WITH_SEPARATOR); //현재 "년-달-일"

        /***
         * 특별 금지행정의 특별 금지구역이 별도로 지정되지 않은 금어기는, 전 지역을 대상으로 포획을 금지한다.
         * 특별 금지행정의 금지기간이 별도로 지정되지 않은 금어기는, 별도의 행정명령 시까지 포획을 금지한다.
         * ---
         * 금지시작기간은 현재 날짜보다 이전에서 시작해서, 금지종료기간은 현재 날짜 이후일 경우만 뽑는다.
         ***/
        String sqlQuery = "SELECT " + DENIED_FISH_TABLE + "." + ALL + ", " + FISH_DIC_TABLE + "." + IMAGE + ", " +
                SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_AREA + ", " + SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_START_DATE + ", " +
                SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_END_DATE +
                " FROM " + DENIED_FISH_TABLE +
                " INNER JOIN " + FISH_DIC_TABLE + " ON " + DENIED_FISH_TABLE + "." + NAME + "=" + FISH_DIC_TABLE + "." + NAME +
                " LEFT OUTER JOIN " + SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE + " ON " + DENIED_FISH_TABLE + "." + NAME + "=" + SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE + "." + NAME +
                " LEFT OUTER JOIN " + SPECIAL_PROHIBIT_ADMIN_TABLE + " ON " + SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_ID + "=" + SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_ID +
                " WHERE " + SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_START_DATE + " <= " + '"' + currentDate + '"' +
                " AND " + SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_END_DATE + " >= " + '"' + currentDate + '"';

        Log.d("금어기 Query : ", sqlQuery);
        Cursor cursor = this.sqlDB.rawQuery(sqlQuery, null);

        int nameIndex = cursor.getColumnIndex(NAME);
        int imageIndex = cursor.getColumnIndex(IMAGE);
        int deniedLengthIndex = cursor.getColumnIndex(DENIED_LENGTH);
        int deniedWeightIndex = cursor.getColumnIndex(DENIED_WEIGHT);
        int waterDepthIndex = cursor.getColumnIndex(DENIED_WATER_DEPTH);
        int specialProhibitAdminAreaIndex = cursor.getColumnIndex(SPECIAL_PROHIBIT_ADMIN_AREA);
        int specialProhibitAdminStartDateIndex = cursor.getColumnIndex(SPECIAL_PROHIBIT_ADMIN_START_DATE);
        int specialProhibitAdminEndDateIndex = cursor.getColumnIndex(SPECIAL_PROHIBIT_ADMIN_END_DATE);

        while (cursor.moveToNext()) //쿼리 된 내용에 대하여 바인딩 작업 수행
        {
            RecyclerViewItem recyclerViewItem = new RecyclerViewItem();

            recyclerViewItem.setTitle(cursor.getString(nameIndex)); //금어기 이름
            recyclerViewItem.setImage(cursor.getBlob(imageIndex)); //어류 이미지

            //금지체장, 금지체중, 수심, 특별 금지구역, 금지시작기간, 금지종료기간
            String content = "금지체장 : " + cursor.getString(deniedLengthIndex) +
                    "\n금지체중 : " + cursor.getString(deniedWeightIndex) +
                    "\n수심 : " + cursor.getString(waterDepthIndex) +
                    "\n특별 금지구역 : " + cursor.getString(specialProhibitAdminAreaIndex) +
                    "\n금지시작기간 : " + cursor.getString(specialProhibitAdminStartDateIndex) +
                    "\n금지종료기간 : " + cursor.getString(specialProhibitAdminEndDateIndex);

            recyclerViewItem.setContent(content.replaceAll("null", EMPTY_DATA)); //입력되지 않은 데이터에 대하여 문자열 치환하여 내용 설정
            recyclerAdapter.addItem(recyclerViewItem);
        }

        cursor.close();
    }

    public void getFishDetailDataFromDB(String fishName) {

    }

    private String getCurrentDate(DATE_FORMAT_TYPE dateFormatType) //현재 날짜 반환
    {
        SimpleDateFormat dateFormat;

        switch(dateFormatType)
        {
            case WITH_SEPARATOR: //구분자 사용(YY-MM-DD)
                dateFormat = new SimpleDateFormat("YYYY-MM-dd");
                break;

            case WITHOUT_SEPARATOR: //구분자 사용하지 않음(YYMMDD)
                dateFormat = new SimpleDateFormat("YYYYMMdd");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + dateFormatType);
        }

        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }
}
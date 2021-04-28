package com.knu.fishdic.manager;

// 이달의 금어기, 도감 관련 모든 기능을 위한 DBManager 정의

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
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

    private enum DATE_FORMAT_TYPE { //날짜 형식 타입 정의
        WITH_SEPARATOR, //구분자 사용 (YY-MM-dd)
        WITHOUT_SEPARATOR //구분자 사용하지 않음 (YYMMdd)
    }

    private enum EMPTY_DATA_TYPE { //빈 데이터 타입 정의
        /***
         * 이름, 학명, 생물분류는 NOT NULL이므로 공백을 허용하지 않는다.
         * 이름, 학명, 생물분류를 제외 한 형태, 분포, 서식지, 주의사항, 금지체장, 금지체중, 수심 등은 null일 경우 대체 문자열로 치환
         ***/

        SPECIAL_PROHIBIT_ADMIN_AREA, //특별 금지구역
        SPECIAL_PROHIBIT_ADMIN_DATE //금지시작기간, 금지종료기간
    }

    private static final String DB_SERVER = "https://raw.githubusercontent.com/2021-KNU-Capstone-Team1/FishDic/master/DB/"; //DB 저장 된 서버 경로
    private static final String DB_VERSION_FILE_NAME = "version"; //DB 버전 관리 파일 이름
    private static final int DB_VERSION_FILE_SIZE = 8; //DB 버전 관리 파일 크기 (바이트 단위)

    private SQLiteDatabase sqlDB; //DB 접근 위한 SQLiteDatabase 객체
    private static String DB_PATH = ""; //DB 경로
    //private static String CACHE_PATH = ""; //임시폴더 경로
    private static final String DB_NAME = "FishDicDB.db"; //DB 이름

    //테이블명 정의
    public static final String FISH_TABLE = "어류_테이블";
    public static final String DENIED_FISH_TABLE = "금어기_테이블";
    public static final String BIO_CLASS_TABLE = "생물분류_테이블";
    public static final String SPECIAL_PROHIBIT_ADMIN_TABLE = "특별_금지행정_테이블";
    public static final String SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE = "특별_금지행정_관계_테이블";

    //필드명 정의
    public static final String ALL = "*";
    public static final String NAME = "이름";
    public static final String SCIENTIFIC_NAME = "학명";
    public static final String IMAGE = "이미지";
    public static final String SHAPE = "형태";
    public static final String DISTRIBUTION = "분포";
    public static final String BODY_LENGTH = "몸길이";
    public static final String HABITAT = "서식지";
    public static final String WARNINGS = "주의사항";

    public static final String BIO_CLASS = "생물분류";

    public static final String DENIED_LENGTH = "금지체장";
    public static final String DENIED_WEIGHT = "금지체중";
    public static final String DENIED_WATER_DEPTH = "수심";

    public static final String SPECIAL_PROHIBIT_ADMIN_ID = "특별_금지행정_ID";
    public static final String SPECIAL_PROHIBIT_ADMIN_AREA = "특별_금지구역";
    public static final String SPECIAL_PROHIBIT_ADMIN_START_DATE = "금지시작기간";
    public static final String SPECIAL_PROHIBIT_ADMIN_END_DATE = "금지종료기간";

    ///!!!!!!!!!수정예정:DBManager와 RecyclerAdapter간의 클래스 결합도를 낮추기 위해 DBManager에서 Bundle반환 후 초기 모든 어류 혹은 이달의 금어기 바인딩을 RecyclerAdapter에서 수행
    public static final String QUERY_RESULT_COUNT_KEY_VALUE = "queryResultCountKey"; //쿼리 결과 수를 위한 키 값
    public static final String SPECIAL_PROHIBIT_ADMIN_COUNT_KEY_VALUE = "specialProhibitAdminCountKey"; //특별 금지행정의 수를 위한 키 값

    /***
     * 특별 금지행정의 특별 금지구역이 별도로 지정되지 않은 금어기는, 전 지역을 대상으로 포획을 금지한다.
     * 특별 금지행정의 금지기간이 별도로 지정되지 않은 금어기는, 별도의 행정명령 시까지 포획을 금지한다.
     * ---
     * 금지시작기간은 현재 날짜보다 이전에서 시작해서, 금지종료기간은 현재 날짜 이후일 경우만 뽑는다.
     ***/

    @RequiresApi(api = Build.VERSION_CODES.N)
    public DBManager() {
        super(FishDic.globalContext, DB_NAME, null, 1); //SQLiteOpenHelper(context, name, factory, version)
        DB_PATH = "/data/data/" + FishDic.globalContext.getPackageName() + "/databases/"; //안드로이드의 DB 저장 경로는 "/data/data/앱 이름/databases/"
        //CACHE_PATH = FishDic.globalContext.getCacheDir().toString() + "/";

        switch (this.getCurrentDBState()) { //기존 DB 상태 확인
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
        String sqlQuery = "SELECT " + FISH_TABLE + "." + NAME + ", " + FISH_TABLE + "." + IMAGE + ", " + BIO_CLASS_TABLE + "." + BIO_CLASS +
                " FROM " + FISH_TABLE +
                " INNER JOIN " + BIO_CLASS_TABLE +
                " ON " + FISH_TABLE + "." + NAME + " = " + BIO_CLASS_TABLE + "." + NAME;

        Log.d("모든 어류 Query : ", sqlQuery);
        Cursor cursor = this.sqlDB.rawQuery(sqlQuery, null);

        int nameIndex = cursor.getColumnIndex(NAME);
        int imageIndex = cursor.getColumnIndex(IMAGE);
        int bioClassIndex = cursor.getColumnIndex(BIO_CLASS);

        while (cursor.moveToNext()) { //쿼리 된 내용에 대하여 바인딩 작업 수행
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
        String sqlQuery = "SELECT DISTINCT " + DENIED_FISH_TABLE + "." + NAME + ", " + FISH_TABLE + "." + IMAGE + ", " + BIO_CLASS_TABLE + "." + BIO_CLASS +
                " FROM " + DENIED_FISH_TABLE +
                " INNER JOIN " + BIO_CLASS_TABLE +
                " ON " + DENIED_FISH_TABLE + "." + NAME + "=" + BIO_CLASS_TABLE + "." + NAME +
                " INNER JOIN " + FISH_TABLE +
                " ON " + DENIED_FISH_TABLE + "." + NAME + "=" + FISH_TABLE + "." + NAME +
                " LEFT OUTER JOIN " + SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE +
                " ON " + DENIED_FISH_TABLE + "." + NAME + "=" + SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE + "." + NAME +
                " LEFT OUTER JOIN " + SPECIAL_PROHIBIT_ADMIN_TABLE +
                " ON " + SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_ID + "=" + SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_ID +
                " WHERE " + SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_START_DATE + " <= " + '"' + currentDate + '"' +
                " AND " + SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_END_DATE + " >= " + '"' + currentDate + '"';

        Log.d("금어기 Query : ", sqlQuery);
        Cursor cursor = this.sqlDB.rawQuery(sqlQuery, null);

        int nameIndex = cursor.getColumnIndex(NAME);
        int imageIndex = cursor.getColumnIndex(IMAGE);
        int bioClassIndex = cursor.getColumnIndex(BIO_CLASS);

        while (cursor.moveToNext()) { //쿼리 된 내용에 대하여 바인딩 작업 수행
            RecyclerViewItem recyclerViewItem = new RecyclerViewItem();

            recyclerViewItem.setTitle(cursor.getString(nameIndex)); //금어기 이름
            recyclerViewItem.setImage(cursor.getBlob(imageIndex)); //어류 이미지
            recyclerViewItem.setContent("생물분류 : " + cursor.getString(bioClassIndex)); //생물 분류

            recyclerAdapter.addItem(recyclerViewItem);
        }

        cursor.close();
    }

    public Bundle getFishDetailBundle(String fishName) { //특정 어류의 상세정보 반환
        if (fishName.isEmpty()) //입력 받은 어류 이름이 없을 경우
            return null;

        String sqlQuery = "SELECT " + FISH_TABLE + "." + ALL + ", " + BIO_CLASS_TABLE + "." + BIO_CLASS + ", " +
                DENIED_FISH_TABLE + "." + DENIED_LENGTH + ", " + DENIED_FISH_TABLE + "." + DENIED_WEIGHT + ", " + DENIED_FISH_TABLE + "." + DENIED_WATER_DEPTH + ", " +
                SPECIAL_PROHIBIT_ADMIN_TABLE + "." + ALL +
                " FROM " + FISH_TABLE +
                " INNER JOIN " + BIO_CLASS_TABLE +
                " ON " + FISH_TABLE + "." + NAME + "=" + BIO_CLASS_TABLE + "." + NAME +
                " INNER JOIN " + DENIED_FISH_TABLE +
                " ON " + FISH_TABLE + "." + NAME + "=" + DENIED_FISH_TABLE + "." + NAME +
                " LEFT OUTER JOIN " + SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE +
                " ON " + FISH_TABLE + "." + NAME + "=" + SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE + "." + NAME +
                " LEFT OUTER JOIN " + SPECIAL_PROHIBIT_ADMIN_TABLE +
                " ON " + SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_ID + "=" + SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_ID +
                " WHERE " + FISH_TABLE + "." + NAME + "=" + '"' + fishName + '"';

        Log.d("어류 상세정보 Query : ", sqlQuery);
        Cursor cursor = this.sqlDB.rawQuery(sqlQuery, null);

        int nameIndex = cursor.getColumnIndex(NAME);
        int scientificNameIndex = cursor.getColumnIndex(SCIENTIFIC_NAME);
        int imageIndex = cursor.getColumnIndex(IMAGE);
        int bioClassIndex = cursor.getColumnIndex(BIO_CLASS);
        int shapeIndex = cursor.getColumnIndex(SHAPE);
        int distributionIndex = cursor.getColumnIndex(DISTRIBUTION);
        int bodyLengthIndex = cursor.getColumnIndex(BODY_LENGTH);
        int habitatIndex = cursor.getColumnIndex(HABITAT);
        int warningsIndex = cursor.getColumnIndex(WARNINGS);

        int deniedLengthIndex = cursor.getColumnIndex(DENIED_LENGTH);
        int deniedWeightIndex = cursor.getColumnIndex(DENIED_WEIGHT);
        int deniedWaterDepthIndex = cursor.getColumnIndex(DENIED_WATER_DEPTH);
        int specialProhibitAdminIdIndex = cursor.getColumnIndex(SPECIAL_PROHIBIT_ADMIN_ID);
        int specialProhibitAdminAreaIndex = cursor.getColumnIndex(SPECIAL_PROHIBIT_ADMIN_AREA);
        int specialProhibitAdminStartDateIndex = cursor.getColumnIndex(SPECIAL_PROHIBIT_ADMIN_START_DATE);
        int specialProhibitAdminEndDateIndex = cursor.getColumnIndex(SPECIAL_PROHIBIT_ADMIN_END_DATE);

        /***
         * 쿼리 결과에 대해 하나의 어류가 다수의 금지 행정정보를 가지고 있을 경우
         * 어류_테이블과 생물분류_테이블의 데이터는 중복되므로 최초 한 번만 결과에 추가
         * 어류 상세정보의 금지 행정정보를 동적으로 사용자에게 보여주기 위해 해당 어류의 모든 정보 반환
         * ---
         * 어류 테이블 : 이름, 학명, 이미지, 형태, 분포, 몸길이, 서식지, 주의사항
         * 생물분류 테이블 : 생물분류
         * 금어기 테이블 : 금지체장, 금지체중, 수심
         * 특별 금지행정 테이블 : 특별 금지행정 ID, 특별 금지구역, 금지시작기간, 금지종료기간
         ***/

        Bundle queryResult = new Bundle(); //키(문자열), 값 쌍의 최종 결과

        boolean duplicateDataAdded = false; //중복 데이터 추가 여부
        boolean queryResultExist = false; //쿼리 결과 존재 여부
        int specialProhibitAdminIndex = 0; //특별 금지행정의 인덱스

        while (cursor.moveToNext()) {
            if (!queryResultExist)
                queryResultExist = true;

            if (!duplicateDataAdded) { //중복 데이터가 추가되지 않았을 경우 최초 한 번만 추가
                queryResult.putString(NAME, cursor.getString(nameIndex));
                queryResult.putString(SCIENTIFIC_NAME, cursor.getString(scientificNameIndex));
                queryResult.putString(BIO_CLASS, cursor.getString(bioClassIndex));
                queryResult.putByteArray(IMAGE, cursor.getBlob(imageIndex));
                queryResult.putString(SHAPE, replaceEmptyData(cursor.getString(shapeIndex), null));
                queryResult.putString(DISTRIBUTION, replaceEmptyData(cursor.getString(distributionIndex), null));
                queryResult.putString(BODY_LENGTH, replaceEmptyData(cursor.getString(bodyLengthIndex), null));
                queryResult.putString(HABITAT, replaceEmptyData(cursor.getString(habitatIndex), null));
                queryResult.putString(WARNINGS, replaceEmptyData(cursor.getString(warningsIndex), null));

                duplicateDataAdded = true;
            }

            /*** 특별 금지 행정 ID가 존재할 경우만 하위 결과 생성 ***/
            String specialProhibitAdminID = cursor.getString(specialProhibitAdminIdIndex);

            if (specialProhibitAdminID == null) //특별 금지 행정 ID가 존재하지 않을 경우 건너뜀
                continue;

            Bundle subQueryResult = new Bundle(); //queryResult 내부에 특별 금지행정을 각각 추가하기 위한 키(문자열), 값 쌍의 하위 결과

            /***
             * 금어기 정보 중 금지체장, 금지체중, 수심은 각 금지행정마다 변동 될 수 있으므로,
             * 중복 데이터로 간주하여 처리하지 않고, 각각 추가한다.
             ***/
            subQueryResult.putString(DENIED_LENGTH, replaceEmptyData(cursor.getString(deniedLengthIndex), null));
            subQueryResult.putString(DENIED_WEIGHT, replaceEmptyData(cursor.getString(deniedWeightIndex), null));
            subQueryResult.putString(DENIED_WATER_DEPTH, replaceEmptyData(cursor.getString(deniedWaterDepthIndex), null));

            subQueryResult.putString(SPECIAL_PROHIBIT_ADMIN_ID, specialProhibitAdminID);
            subQueryResult.putString(SPECIAL_PROHIBIT_ADMIN_AREA, replaceEmptyData(cursor.getString(specialProhibitAdminAreaIndex), EMPTY_DATA_TYPE.SPECIAL_PROHIBIT_ADMIN_AREA));
            subQueryResult.putString(SPECIAL_PROHIBIT_ADMIN_START_DATE, replaceEmptyData(cursor.getString(specialProhibitAdminStartDateIndex), EMPTY_DATA_TYPE.SPECIAL_PROHIBIT_ADMIN_DATE));
            subQueryResult.putString(SPECIAL_PROHIBIT_ADMIN_END_DATE, replaceEmptyData(cursor.getString(specialProhibitAdminEndDateIndex), EMPTY_DATA_TYPE.SPECIAL_PROHIBIT_ADMIN_DATE));

            queryResult.putBundle(String.valueOf(specialProhibitAdminIndex), subQueryResult); //특별 금지행정의 인덱스를 키로하여 최종 결과에 추가
            specialProhibitAdminIndex++;
        }

        //Log.d("쿼리 된 금지행정의 수 :", String.valueOf(specialProhibitAdminIndex));
        queryResult.putInt(SPECIAL_PROHIBIT_ADMIN_COUNT_KEY_VALUE, specialProhibitAdminIndex); //전체 특별 금지행정의 수를 추가

        if (queryResultExist) //쿼리 결과가 존재하면 결과 반환
            return queryResult;
        else //쿼리 결과가 존재하지 않으면
            return null;
    }

    public void doParseFishDetailBundle(Bundle queryResult) { //디버그를 위해 어류 상세정보 Bundle 구조 파싱 수행
        Log.d("---------------------", "Parsing queryResult");
        for (String key : queryResult.keySet()) {
            Log.d("queryResult Key", key);
        }

        for (int i = 0; i < queryResult.getInt(SPECIAL_PROHIBIT_ADMIN_COUNT_KEY_VALUE); i++) {
            Bundle subQueryResult = queryResult.getBundle(String.valueOf(0));

            for (String key : subQueryResult.keySet()) {
                Log.d("subQueryResult Key", key);
            }
        }

        Log.d("---------------------", "queryResult Value");

        /*** 어류 테이블, 생물분류 테이블 ***/
        Log.d(NAME, queryResult.getString(NAME));
        Log.d(SCIENTIFIC_NAME, queryResult.getString(SCIENTIFIC_NAME));
        Log.d(BIO_CLASS, queryResult.getString(BIO_CLASS));

        byte[] image = queryResult.getByteArray(IMAGE);
        int imageLength = 0;
        if (image != null)
            imageLength = image.length;
        Log.d(IMAGE + " 크기", String.valueOf(imageLength));

        Log.d(SHAPE, replaceEmptyData(queryResult.getString(SHAPE), null));
        Log.d(DISTRIBUTION, replaceEmptyData(queryResult.getString(DISTRIBUTION), null));
        Log.d(BODY_LENGTH, replaceEmptyData(queryResult.getString(BODY_LENGTH), null));
        Log.d(HABITAT, replaceEmptyData(queryResult.getString(HABITAT), null));
        Log.d(WARNINGS, replaceEmptyData(queryResult.getString(WARNINGS), null));

        /*** 금어기 테이블, 특별 금지행정 테이블 ***/
        Log.d("---------------------", "subQueryResult Value");
        int specialProhibitAdminCount = queryResult.getInt(SPECIAL_PROHIBIT_ADMIN_COUNT_KEY_VALUE); //해당 어류의 전체 금지행정의 수
        for (int specialProhibitAdminIndex = 0; specialProhibitAdminIndex < specialProhibitAdminCount; specialProhibitAdminIndex++) { //전체 금지행정의 수만큼
            Bundle subQueryResult = queryResult.getBundle(String.valueOf(specialProhibitAdminIndex)); //특별 금지행정의 인덱스를 키로하는 각 금지행정 정보

            Log.d(DENIED_LENGTH, replaceEmptyData(subQueryResult.getString(DENIED_LENGTH), null));
            Log.d(DENIED_WEIGHT, replaceEmptyData(subQueryResult.getString(DENIED_WEIGHT), null));
            Log.d(DENIED_WATER_DEPTH, replaceEmptyData(subQueryResult.getString(DENIED_WATER_DEPTH), null));

            Log.d(SPECIAL_PROHIBIT_ADMIN_ID, replaceEmptyData(subQueryResult.getString(SPECIAL_PROHIBIT_ADMIN_ID), null));
            Log.d(SPECIAL_PROHIBIT_ADMIN_AREA, replaceEmptyData(subQueryResult.getString(SPECIAL_PROHIBIT_ADMIN_AREA), EMPTY_DATA_TYPE.SPECIAL_PROHIBIT_ADMIN_AREA));
            Log.d(SPECIAL_PROHIBIT_ADMIN_START_DATE, replaceEmptyData(subQueryResult.getString(SPECIAL_PROHIBIT_ADMIN_START_DATE), EMPTY_DATA_TYPE.SPECIAL_PROHIBIT_ADMIN_DATE));
            Log.d(SPECIAL_PROHIBIT_ADMIN_END_DATE, replaceEmptyData(subQueryResult.getString(SPECIAL_PROHIBIT_ADMIN_END_DATE), EMPTY_DATA_TYPE.SPECIAL_PROHIBIT_ADMIN_DATE));
        }
    }

    private String replaceEmptyData(String data, EMPTY_DATA_TYPE emptyDataType) { //공백 데이터 존재 시 대체 문자열로 치환
        if (data == null) { //공백 데이터일 경우 치환
            if (emptyDataType == null) //이름, 학명, 생물분류 제외 (NOT NULL) 한 형태, 분포, 서식지, 주의사항, 금지체장, 금지체중, 수심 등
                return FishDic.globalContext.getString(R.string.empty_info);

            switch (emptyDataType) {
                case SPECIAL_PROHIBIT_ADMIN_AREA: //특별 금지구역
                    return FishDic.globalContext.getString(R.string.empty_area);

                case SPECIAL_PROHIBIT_ADMIN_DATE: //금지시작기간, 금지종료기간
                    return FishDic.globalContext.getString(R.string.empty_date);

                default:
                    throw new IllegalStateException("Unexpected value: " + emptyDataType);
            }
        }

        return data;
    }

    private String getCurrentDate(DATE_FORMAT_TYPE dateFormatType) //현재 날짜 반환
    {
        SimpleDateFormat dateFormat;

        switch (dateFormatType) {
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
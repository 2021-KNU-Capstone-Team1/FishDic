package com.knu.fishdic.manager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;
import com.knu.fishdic.recyclerview.RecyclerAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import okhttp3.Response;

// 이달의 금어기, 도감 관련 모든 기능을 위한 DBManager 정의

public class DBManager extends SQLiteOpenHelper {
    public static final String TOTAL_FISH_COUNT_KEY = "totalFishCountKey"; //전체 어류 개수를 위한 키 값
    public static final String TOTAL_SPECIAL_PROHIBIT_ADMIN_COUNT_KEY = "totalSpecialProhibitAdminCountKey"; //전체 특별 금지행정의 수를 위한 키 값
    public static final String QUERY_RESULT_KEY = "queryResultKey"; //쿼리 결과를 위한 키 값

    public final String PUBLIC_DB_SERVER = "http://fishdic.asuscomm.com/DB/";

    private static String DB_PATH = ""; //DB 경로
    private static final String DB_NAME = "FishDicDB.db"; //DB 이름

    private final int NOTIFICATION_ID = 0; //알림 아이디

    public enum FISH_DATA_TYPE { //어류 데이터 타입 정의
        ALL_FISH, //모든 어류
        DENIED_FISH, //이달의 금어기
        FISH_IDENTIFICATION_RESULT //어류 판별 결과 (해당 어류에 대한 가중치를 포함하여 출력)
    }

    public enum DATE_FORMAT_TYPE { //날짜 형식 타입 정의
        SIMPLE_WITH_SEPARATOR, //구분자 사용 (yyyy-MM-dd)
        SIMPLE_WITHOUT_SEPARATOR, //구분자 사용하지 않음 (yyyyMMdd)
        DETAIL_WITHOUT_SEPARATOR //구분자 사용하지 않음 (yyyy-MM-dd'T'HHmmss)
    }

    private enum DB_STATE { //DB 상태 정의
        INIT, //초기 상태
        OUT_DATED, //구 버전
        UPDATED, //갱신 된 버전
        DELAYED_FAILURE, //DB 상태 확인 실패 (지연 된 갱신 수행)
        IMMEDIATE_FAILURE //DB 상태 확인 실패 (assets으로부터 복사하는 대체 흐름 수행)
    }

    private enum EMPTY_DATA_TYPE { //빈 데이터 타입 정의
        /***
         * 이름, 학명, 생물분류는 NOT NULL이므로 공백을 허용하지 않는다.
         * 이름, 학명, 생물분류를 제외 한 형태, 분포, 서식지, 주의사항, 금지체장, 금지체중, 수심 등은 null일 경우 대체 문자열로 치환
         ***/

        SPECIAL_PROHIBIT_ADMIN_AREA, //특별 금지구역
        SPECIAL_PROHIBIT_ADMIN_DATE //금지시작기간, 금지종료기간
    }

    private SQLiteDatabase sqlDB; //DB 접근 위한 SQLiteDatabase 객체

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

    public DBManager() {
        super(FishDic.globalContext, DB_NAME, null, 1); //SQLiteOpenHelper(context, name, factory, version)
        DB_PATH = "/data/data/" + FishDic.globalContext.getPackageName() + "/databases/"; //안드로이드의 DB 저장 경로는 "/data/data/앱 이름/databases/"

        switch (this.getCurrentDBState()) { //기존 DB 상태 확인
            case INIT: //초기 상태일 경우
            case OUT_DATED: //구 버전일 경우
                this.updateDBFromServer();
                break;

            case UPDATED: //최신 버전일 경우
            case DELAYED_FAILURE: //DB 상태 확인 실패 (지연 된 갱신 수행)
                break;

            case IMMEDIATE_FAILURE: //DB 상태 확인 실패 (assets으로부터 복사하는 대체 흐름 수행)
                this.copyDB();
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

    private DB_STATE getCurrentDBState() {  //기존 DB 상태 반환
        /***
         * 1) 로컬 DB와 로컬 DB 버전 관리 파일이 존재하지 않을 경우 서버로부터의 갱신을 위한 초기 상태 반환
         * 2) 로컬 DB와 로컬 DB 버전 관리 파일이 존재할 경우 서버와 로컬 DB 버전을 비교하여
         *  2-1) 로컬 DB 버전 < 서버 DB 버전일 경우 : 구 버전 상태 반환
         *  2-2) 로컬 DB 버전 == 서버 DB 버전일 경우 : 최신 버전 상태 반환
         *  2-3) 로컬 DB 버전 > 서버 DB 버전일 경우 : 무결성 오류
         ***/

        File dir = new File(DB_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }

        final File currentDBFile = new File(DB_PATH + DB_NAME); //현재 로컬 DB 파일
        final File currentDBVersionFile = new File(DB_PATH + FishDic.VERSION_FILE_NAME); //로컬 DB 버전 관리 파일
        boolean currentDBExists = currentDBFile.exists() & currentDBVersionFile.exists(); //로컬 DB 존재 여부 (DB 파일 혹은 버전 관리 파일 하나라도 존재 하지 않을 시 무결성이 깨진 걸로 간주)

        int currentDBVersion = -1; //로컬 DB 버전
        int serverDBVersion = -1; //서버 DB 버전

        /*** 서버와 로컬 DB 버전 비교 ***/
        Log.d("Checking newest DB Version", "---");
        ANRequest request = AndroidNetworking
                .download(PUBLIC_DB_SERVER + FishDic.VERSION_FILE_NAME, FishDic.CACHE_PATH, FishDic.VERSION_FILE_NAME)
                .doNotCacheResponse()
                .build()
                .setAnalyticsListener((timeTakenInMillis, bytesSent, bytesReceived, isFromCache) -> {
                    Log.i("DB", " timeTakenInMillis : " + timeTakenInMillis);
                    Log.i("DB", " bytesSent : " + bytesSent);
                    Log.i("DB", " bytesReceived : " + bytesReceived);
                    Log.i("DB", " isFromCache : " + isFromCache);
                })
                .setDownloadProgressListener((bytesDownloaded, totalBytes) -> {
                });
        ANResponse<String> response = request.executeForDownload();

        if (response.isSuccess()) {
            Response okHttpResponse = response.getOkHttpResponse();
            Log.d("Server DB Version Check", "headers :" + okHttpResponse.headers().toString());
            Log.d("Server DB Version Check", "body :" + okHttpResponse.body().toString());
            Log.d("Server DB Version Check", "HTTP Status Code :" + okHttpResponse.code());

            File serverDBVersionFile = new File(FishDic.CACHE_PATH + FishDic.VERSION_FILE_NAME);

            try {
                BufferedReader serverDBVersionReader = new BufferedReader(new FileReader(serverDBVersionFile));
                serverDBVersion = Integer.parseInt(serverDBVersionReader.readLine());
                serverDBVersionReader.close();

                if (currentDBExists) { //로컬 DB 존재 시 버전 읽어오기
                    BufferedReader currentDBVersionReader = new BufferedReader(new FileReader(currentDBVersionFile));
                    currentDBVersion = Integer.parseInt(currentDBVersionReader.readLine());
                    currentDBVersionReader.close();
                } else { //로컬 DB가 존재하지 않을 시 다운로드 받은 서버의 DB 버전 파일을 로컬 DB의 버전 파일로 이동 및 초기 상태 반환
                    Files.move(serverDBVersionFile.toPath(), Paths.get(DB_PATH + FishDic.VERSION_FILE_NAME));
                    return DB_STATE.INIT;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.i("로컬 DB 버전", String.valueOf(currentDBVersion));
            Log.i("서버 DB 버전", String.valueOf(serverDBVersion));

        } else { //서버 접속 오류 시
            ANError error = response.getError();
            Log.d("Server DB Version Check ERR", error.getMessage());

            if (currentDBExists) //현재 DB가 존재하면
                return DB_STATE.DELAYED_FAILURE;

            return DB_STATE.IMMEDIATE_FAILURE; //현재 DB가 존재하지 않으면
        }

        if (currentDBVersion < serverDBVersion) { //로컬 DB 버전 < 서버 DB 버전일 경우 : 구 버전 상태 반환
            return DB_STATE.OUT_DATED;
        } else if (currentDBVersion == serverDBVersion) { //로컬 DB 버전 == 서버 DB 버전일 경우 : 최신 버전 상태 반환
            return DB_STATE.UPDATED;
        } else { //로컬 DB 버전 > 서버 DB 버전일 경우 : 무결성 오류
            try {
                throw new Exception("DB Integrity ERR");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return DB_STATE.IMMEDIATE_FAILURE;
    }

    private void updateDBFromServer() { //서버로부터 최신 DB 갱신
        File dir = new File(DB_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }

        /*
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(FishDic.globalContext);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(FishDic.globalContext, FishDic.NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Downloading newest DB From Server")
                .setPriority(NotificationCompat.PRIORITY_MAX);
        */
        Log.d("Downloading newest DB From Server", "---");
        ANRequest request = AndroidNetworking
                .download(PUBLIC_DB_SERVER + DB_NAME, DB_PATH, DB_NAME)
                .doNotCacheResponse()
                .build()
                /*.setAnalyticsListener((timeTakenInMillis, bytesSent, bytesReceived, isFromCache) -> {
                    Log.i("DB", " timeTakenInMillis : " + timeTakenInMillis);
                    Log.i("DB", " bytesSent : " + bytesSent);
                    Log.i("DB", " bytesReceived : " + bytesReceived);
                    Log.i("DB", " isFromCache : " + isFromCache);
                })*/
                .setDownloadProgressListener((bytesDownloaded, totalBytes) -> {
                });
        ANResponse<String> response = request.executeForDownload();

        if (response.isSuccess()) {
            Response okHttpResponse = response.getOkHttpResponse();
            Log.d("Server DB Download", "headers : " + okHttpResponse.headers().toString());
            Log.d("Server DB Download", "body : " + okHttpResponse.body().toString());
            Log.d("Server DB Download", "HTTP Status Code : " + okHttpResponse.code());

            File serverDBVersionFile = new File(DB_PATH + FishDic.VERSION_FILE_NAME);

            if (!serverDBVersionFile.exists())
                try {
                    throw new Exception("DB Integrity ERR");
                } catch (Exception e) {
                    e.printStackTrace();
                }
        } else { //다운로드 오류 시
            ANError error = response.getError();
            Log.d("Server DB Download ERR", error.getMessage());

            this.copyDB();
        }
    }

    private void copyDB() { //assets으로부터 시스템으로 DB 복사
        /*** 서버로부터 DB 다운로드 실패 시 내장 DB를 이용한 대체 흐름 수행 ***/
        try {
            File folder = new File(DB_PATH);
            if (!folder.exists()) {
                folder.mkdir();
            }

            InputStream inputStream = FishDic.globalContext.getAssets().open("DB/" + DB_NAME);
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

    public Bundle getSimpleFishBundle(FISH_DATA_TYPE fishDataType, Bundle args) { //간략화된 어류 정보 반환
        String sqlQuery;
        String currentDate;
        Cursor cursor;
        Bundle scientificNameScoreMap = null; //학명 : 가중치 쌍의 맵

        switch (fishDataType) { //어류 데이터 타입에 따라서 쿼리문 설정
            case ALL_FISH: //모든 어류
                sqlQuery = "SELECT " + FISH_TABLE + "." + NAME + ", " + FISH_TABLE + "." + IMAGE + ", " + BIO_CLASS_TABLE + "." + BIO_CLASS +
                        " FROM " + FISH_TABLE +
                        " INNER JOIN " + BIO_CLASS_TABLE +
                        " ON " + FISH_TABLE + "." + NAME + " = " + BIO_CLASS_TABLE + "." + NAME + "";
                Log.d("모든 어류 Query", sqlQuery);
                break;

            case DENIED_FISH: //이달의 금어기
                /***
                 * 특별 금지행정의 특별 금지구역이 별도로 지정되지 않은 금어기는, 전 지역을 대상으로 포획을 금지한다.
                 * 특별 금지행정의 금지기간이 별도로 지정되지 않은 금어기는, 별도의 행정명령 시까지 포획을 금지한다.
                 * ---
                 * 금지시작기간은 현재 날짜보다 이전에서 시작해서, 금지종료기간은 현재 날짜 이후일 경우만 뽑는다.
                 ***/

                currentDate = this.getCurrentDate(DATE_FORMAT_TYPE.SIMPLE_WITH_SEPARATOR); //현재 "년-달-일"
                sqlQuery = "SELECT DISTINCT " + DENIED_FISH_TABLE + "." + NAME + ", " + FISH_TABLE + "." + IMAGE + ", " + BIO_CLASS_TABLE + "." + BIO_CLASS +
                        " FROM " + DENIED_FISH_TABLE +
                        " INNER JOIN " + BIO_CLASS_TABLE +
                        " ON " + DENIED_FISH_TABLE + "." + NAME + "=" + BIO_CLASS_TABLE + "." + NAME +
                        " INNER JOIN " + FISH_TABLE +
                        " ON " + DENIED_FISH_TABLE + "." + NAME + "=" + FISH_TABLE + "." + NAME +
                        " LEFT OUTER JOIN " + SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE +
                        " ON " + DENIED_FISH_TABLE + "." + NAME + "=" + SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE + "." + NAME +
                        " LEFT OUTER JOIN " + SPECIAL_PROHIBIT_ADMIN_TABLE +
                        " ON " + SPECIAL_PROHIBIT_ADMIN_RELATION_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_ID + "=" + SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_ID +
                        " WHERE " + SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_START_DATE + " <= " + "'" + currentDate + "'" +
                        " AND " + SPECIAL_PROHIBIT_ADMIN_TABLE + "." + SPECIAL_PROHIBIT_ADMIN_END_DATE + " >= " + "'" + currentDate + "'";
                Log.d("이달의 금어기 Query", sqlQuery);
                break;

            case FISH_IDENTIFICATION_RESULT: //어류 판별 결과 (해당 어류에 대한 가중치를 포함하여 출력)
                /***
                 * 판별 된 어류의 전체 수만큼 전달받은 args 내부에 0부터 순차적으로 Key값을 가지므로
                 * 각 어류에 대한 학명 : 가중치 쌍인 하위 결과 (Bundle)을 해당 Key값으로 분리한다. (가중치에 따라 높은 순으로 정렬되어 있음)
                 ***/

                if (args == null || args.isEmpty()) //어류 판별 시 출력 위한 결과가 존재하지 않을 경우
                    return null;

                int totalFishCount = args.getInt(TOTAL_FISH_COUNT_KEY); //판별 완료 된 전체 어류 수

                sqlQuery = "SELECT " + FISH_TABLE + "." + NAME + ", " + FISH_TABLE + "." + SCIENTIFIC_NAME + ", " + FISH_TABLE + "." + IMAGE + ", " + BIO_CLASS_TABLE + "." + BIO_CLASS +
                        " FROM " + FISH_TABLE +
                        " INNER JOIN " + BIO_CLASS_TABLE +
                        " ON " + FISH_TABLE + "." + NAME + " = " + BIO_CLASS_TABLE + "." + NAME +
                        " WHERE " + FISH_TABLE + "." + SCIENTIFIC_NAME;

                StringBuffer stringBuffer = new StringBuffer(sqlQuery);
                scientificNameScoreMap = new Bundle();

                for (int fishIndex = 0; fishIndex < totalFishCount; fishIndex++) {
                    Bundle subArgs = args.getBundle(String.valueOf(fishIndex)); //각 어류에 대한 학명 : 가중치 쌍인 하위 결과
                    Set<String> keySet = subArgs.keySet();
                    String scientificName = keySet.iterator().next(); //해당 어류의 학명 (하위 결과에는 학명인 키 값이 KeySet에서 하나만 존재)
                    float score = subArgs.getFloat(scientificName); //해당 어류의 가중치

                    if (fishIndex == 0) { //첫 번째로 쿼리문에 추가할 경우
                        stringBuffer.append(" IN (\'").append(scientificName).append("\'");
                    } else {
                        stringBuffer.append(", \'").append(scientificName).append("\'");
                    }
                    scientificNameScoreMap.putFloat(scientificName, score);
                }

                stringBuffer.append(")");
                sqlQuery = stringBuffer.toString();

                Log.d("어류 판별 결과 Query", sqlQuery);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + fishDataType);
        }

        cursor = this.sqlDB.rawQuery(sqlQuery, null);

        int nameIndex = cursor.getColumnIndex(NAME);
        int scientificNameIndex = cursor.getColumnIndex(SCIENTIFIC_NAME);
        int imageIndex = cursor.getColumnIndex(IMAGE);
        int bioClassIndex = cursor.getColumnIndex(BIO_CLASS);

        Bundle queryResult = new Bundle(); //키(문자열), 값 쌍의 최종 결과

        boolean queryResultExist = false; //쿼리 결과 존재 여부
        int fishIndex = 0; //어류 인덱스

        while (cursor.moveToNext()) {
            if (!queryResultExist)
                queryResultExist = true;

            Bundle subQueryResult = new Bundle(); //queryResult 내부에 각 어류 정보를 추가하기 위한 키(문자열), 값 쌍의 하위 결과

            subQueryResult.putString(NAME, cursor.getString(nameIndex));
            subQueryResult.putByteArray(IMAGE, cursor.getBlob(imageIndex));

            if (fishDataType == FISH_DATA_TYPE.FISH_IDENTIFICATION_RESULT) { //어류 판별 결과일 경우 유사도 출력 (높은 순으로)
                subQueryResult.putString(BIO_CLASS, FishDic.globalContext.getString(R.string.fish_identification_percentage_info) + String.format("%.2f", scientificNameScoreMap.getFloat(cursor.getString(scientificNameIndex))) + "%\n" +
                        FishDic.globalContext.getString(R.string.bio_class_info) + cursor.getString(bioClassIndex));
                subQueryResult.putFloat(RecyclerAdapter.COMPARABLE_VALUE_KEY, scientificNameScoreMap.getFloat(cursor.getString(scientificNameIndex))); //정렬을 위해 해당 어류의 가중치에 따른 순서
            } else {
                subQueryResult.putString(BIO_CLASS, FishDic.globalContext.getString(R.string.bio_class_info) + cursor.getString(bioClassIndex));
            }
            queryResult.putBundle(String.valueOf(fishIndex), subQueryResult); //각 어류의 인덱스를 키로하여 어류 정보를 queryResult 내부에 추가
            fishIndex++;
        }
        cursor.close();
        queryResult.putInt(TOTAL_FISH_COUNT_KEY, fishIndex); //인덱스로 각 어류 접근 위해 전체 어류 수를 추가

        if (queryResultExist) //쿼리 결과가 존재하면 결과 반환
            return queryResult;
        else //쿼리 결과가 존재하지 않으면
            return null;
    }

    public Bundle getFishDetailBundle(Bundle args) { //특정 어류의 상세정보 반환
        boolean argsContainsName = args.containsKey(NAME);
        boolean argsContainsScientificName = args.containsKey(SCIENTIFIC_NAME);

        if (!argsContainsName && !argsContainsScientificName) { //이름, 학명 키 값에 해당하는 데이터가 모두 없을경우
            return null;
        }

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
                " WHERE " + FISH_TABLE;

        if (argsContainsName) //이름으로 검색
            sqlQuery += "." + NAME + "=" + "'" + args.getString(NAME) + "'";
        else //학명으로 검색
            sqlQuery += "." + NAME + "=" + "'" + args.getString(SCIENTIFIC_NAME) + "'";

        Log.d("어류 상세정보 Query", sqlQuery);
        Cursor cursor = this.sqlDB.rawQuery(sqlQuery, null);

        //DatabaseUtils.dumpCursor(cursor);

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
             * 현재 DB에는 금지체장, 금지체중, 수심 정보가 각 금지행정마다 변동사항이 없는 것으로 판단하여
             * 금어기 테이블의 필드로 들어가있지만, 만약 어떤 한 어류에 대하여 금지행정이 새로 추가되었는데
             * 기존의 금지체장, 금지체중, 수심과 다르다면 금지체장, 금지체중, 수심을 특별 금지 행정 테이블로 옮길 것
             * ---
             * 금어기 정보 중 금지체장, 금지체중, 수심은 각 금지행정마다 사용자에게 보여주기 위해 각각 추가한다.
             ***/
            subQueryResult.putString(DENIED_LENGTH, replaceEmptyData(cursor.getString(deniedLengthIndex), null));
            subQueryResult.putString(DENIED_WEIGHT, replaceEmptyData(cursor.getString(deniedWeightIndex), null));
            subQueryResult.putString(DENIED_WATER_DEPTH, replaceEmptyData(cursor.getString(deniedWaterDepthIndex), null));

            subQueryResult.putString(SPECIAL_PROHIBIT_ADMIN_ID, specialProhibitAdminID);
            subQueryResult.putString(SPECIAL_PROHIBIT_ADMIN_AREA, replaceEmptyData(cursor.getString(specialProhibitAdminAreaIndex), EMPTY_DATA_TYPE.SPECIAL_PROHIBIT_ADMIN_AREA));
            subQueryResult.putString(SPECIAL_PROHIBIT_ADMIN_START_DATE, replaceEmptyData(cursor.getString(specialProhibitAdminStartDateIndex), EMPTY_DATA_TYPE.SPECIAL_PROHIBIT_ADMIN_DATE));
            subQueryResult.putString(SPECIAL_PROHIBIT_ADMIN_END_DATE, replaceEmptyData(cursor.getString(specialProhibitAdminEndDateIndex), EMPTY_DATA_TYPE.SPECIAL_PROHIBIT_ADMIN_DATE));

            queryResult.putBundle(String.valueOf(specialProhibitAdminIndex), subQueryResult); //특별 금지행정의 인덱스를 키로 하여 최종 결과에 추가
            specialProhibitAdminIndex++;
        }
        cursor.close();
        queryResult.putInt(TOTAL_SPECIAL_PROHIBIT_ADMIN_COUNT_KEY, specialProhibitAdminIndex); //인덱스로 각 특별 금지행정 접근 위해 전체 특별 금지행정의 수를 추가

        if (queryResultExist) //쿼리 결과가 존재하면 결과 반환
            return queryResult;
        else //쿼리 결과가 존재하지 않으면
            return null;
    }

    public static void doParseQueryResultBundle(Bundle queryResult, int subIndex, boolean isInitialCall) { //디버그를 위해 쿼리 결과 구조 파싱 수행
        int index = subIndex;

        /*
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        Log.d("Stack Trace Start", String.valueOf(stacks.length));
        for (StackTraceElement element : stacks) {
            Log.d("메소드 명", element.getMethodName());
        }
        */

        if (isInitialCall)
            Log.d("초기 탐색 시작", "---");

        for (String key : queryResult.keySet()) {
            Log.d("Index " + index, key);
            index++;

            if (queryResult.get(key) instanceof Bundle) { //하위 결과가 존재하면 (Bundle 타입일 경우만)
                Log.d("Index " + index, key + "의 내부 하위 결과");
                Bundle subQueryResult = queryResult.getBundle(key);

                Log.d("하위 결과 탐색 시작", key + " ---");
                doParseQueryResultBundle(subQueryResult, 0, false); //하위 결과에 대하여 계속 탐색
                Log.d("하위 결과 탐색 완료", key + " ---");
            } else { //해당 키가 가지고 있는 값 출력
                Log.d(key, queryResult.get(key).toString());
            }
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

    public static String getCurrentDate(DATE_FORMAT_TYPE dateFormatType) //현재 날짜 반환
    {
        SimpleDateFormat dateFormat;

        switch (dateFormatType) {
            case SIMPLE_WITH_SEPARATOR: //구분자 사용(yyyy-MM-DD)
                dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                break;

            case SIMPLE_WITHOUT_SEPARATOR: //구분자 사용하지 않음(yyyyMMDD)
                dateFormat = new SimpleDateFormat("yyyyMMdd");
                break;


            case DETAIL_WITHOUT_SEPARATOR: //구분자 사용하지 않음 (yyyy-MM-dd'T'HHmmss)
                dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + dateFormatType);
        }

        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }
}
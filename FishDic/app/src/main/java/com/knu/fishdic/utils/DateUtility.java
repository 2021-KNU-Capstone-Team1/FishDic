package com.knu.fishdic.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

// 날짜 관련 작업을 위한 DateUtility 정의

public class DateUtility {
    public enum DATE_FORMAT_TYPE { //날짜 형식 타입 정의
        SIMPLE_WITH_SEPARATOR, //구분자 사용 (yyyy-MM-dd)
        SIMPLE_WITHOUT_SEPARATOR, //구분자 사용하지 않음 (yyyyMMdd)
        DETAIL_WITHOUT_SEPARATOR //구분자 사용하지 않음 (yyyy-MM-dd'T'HHmmss)
    }

    public static String getCurrentDate(DATE_FORMAT_TYPE dateFormatType) { //현재 날짜 반환

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

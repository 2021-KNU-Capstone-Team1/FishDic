package com.knu.fishdic.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.knu.fishdic.FishDic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

// DB, 배너 갱신을 위한 AsyncDownloader 정의
// https://developer.android.com/reference/android/os/AsyncTask

public class AsyncDownloader extends AsyncTask<String, String, Long> { //비동기 다운로더
    private String outputPath = null; //출력 경로 (파일 시스템 경로)
    private PowerManager.WakeLock wakeLock; //디바이스의 전원 상태 제어

    public AsyncDownloader(String outputPath, byte[] outputBuffer) {
        super();

        if (outputPath != null && outputBuffer == null) { //파일 시스템으로만 출력
            this.outputPath = outputPath;
            this.currentDownloadMode = DOWNLOAD_MODE.FILE_SYS;
        } else if (outputPath == null && outputBuffer != null) { //버퍼로만 출력
            this.refOutputBuffer = outputBuffer; //출력 버퍼 참조
            this.currentDownloadMode = DOWNLOAD_MODE.BUFFER;
        } else { //둘 중에 하나만 되게 한다.
            try {
                throw new Exception("Illegal outputType");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPreExecute() { //Runs on the UI thread before doInBackground(Params...)
        super.onPreExecute();

        //사용자가 다운로드 중 전원 버튼을 누르더라도 CPU가 잠들지 않도록 해서 다운로드 계속 수행
        PowerManager pm = (PowerManager) FishDic.globalContext.getSystemService(Context.POWER_SERVICE); //전역 앱 콘텍스트의 시스템 서비스 사용
        this.wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        this.wakeLock.acquire();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected Long doInBackground(String... params) { //백그라운드 다운로드 작업
        //한 번에 다중 다운로드 작업을 수행하고자 한다면, params에 여러 타겟 주소를 넣어서 각 주소에 대하여 수행하도록 수정해야함
        long totalFileSize = -1; //타겟 파일의 크기
        URLConnection connection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        //TODO : 파일 다운로드 오류
        try {
            URL url = new URL(params[0]);

            connection = url.openConnection(); //서버 연결
            connection.connect();
            Log.d("targetUrl : ", String.valueOf(url));

            totalFileSize = connection.getContentLengthLong(); //타겟 파일 크기
            Log.d("totalFileSize : ", String.valueOf(totalFileSize));

            InputStream urlConnectionInputStream = new BufferedInputStream(url.openStream(), 1024); //연결로부터 입력 스트림 생성

            switch (this.currentDownloadMode) {
                case BUFFER: //버퍼로만 출력
                    inputStream.read(this.refOutputBuffer, 0, this.refOutputBuffer.length);
                    Log.d("refOutputBuffer : ", this.refOutputBuffer.toString());

                    inputStream.close();
                    break;

                case FILE_SYS: //파일 시스템으로만 출력
                    File outputFile = new File(this.outputPath);
                    outputStream = new FileOutputStream(outputFile);
                    Log.d("outputFile : ", outputPath);

                    byte[] buffer = new byte[1024];
                    int length = 0;

                    while ((length = urlConnectionInputStream.read(buffer)) >= 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    outputStream.flush();
                    outputStream.close();
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + this.currentDownloadMode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
            this.wakeLock.release();
        }

        return totalFileSize;
    }
}
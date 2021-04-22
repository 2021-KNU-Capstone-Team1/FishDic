package com.knu.fishdic;

// DB 갱신, 배너 다운로드를 위한 AsyncDownloader 정의
// https://developer.android.com/reference/android/os/AsyncTask

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AsyncDownloader extends AsyncTask<String, Integer, String> { //비동기 다운로더
    public enum DOWNLOAD_MODE { //비동기 다운로더 (AsyncDownloader)의 다운로드 모드 정의
        BUFFER, //버퍼로만 출력
        FILE_SYS, //파일 시스템으로만 출력
        ERR
    }

    private DOWNLOAD_MODE currentDownloadMode; //현재 다운로드 모드
    private URL targetUrl = null; //타겟 URL
    private String outputPath = null; //출력 경로 (파일 시스템 경로)
    private byte[] refOutputBuffer = null; //참조 출력 버퍼

    public AsyncDownloader(String targetUrl, String outputPath, byte[] outputBuffer) {
        super();

        try {
            this.targetUrl = new URL(targetUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (outputPath != null && outputBuffer == null) { //파일 시스템으로만 출력
            this.outputPath = outputPath;
            this.currentDownloadMode = DOWNLOAD_MODE.FILE_SYS;
        } else if (outputPath == null && outputBuffer != null) { //버퍼로만 출력
            this.refOutputBuffer = outputBuffer; //출력 버퍼 참조
            this.currentDownloadMode = DOWNLOAD_MODE.BUFFER;
        } else { //둘 중에 하나만 되게 한다.
            this.currentDownloadMode = DOWNLOAD_MODE.ERR;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected String doInBackground(String... params) { //백그라운드 다운로드 작업
        //한 번에 다중 다운로드 작업을 수행하고자 한다면, params에 여러 타겟 주소를 넣어서 각 주소에 대하여 수행하도록 수정해야함
        int totalFileSize = 0; //타겟 파일의 크기

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) this.targetUrl.openConnection(); //서버 연결
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            Log.d("targetUrl : ", String.valueOf(this.targetUrl));

            //////오류 다운로드가 안됨 파일 사이즈가 -1로 나옴
            totalFileSize = urlConnection.getContentLength();
            Log.d("totalFileSize : ", String.valueOf(totalFileSize));

            InputStream urlConnectionInputStream = urlConnection.getInputStream(); //연결로부터 입력 스트림 생성

            switch(this.currentDownloadMode)
            {
                case BUFFER: //버퍼로만 출력
                    urlConnectionInputStream.read(this.refOutputBuffer, 0, this.refOutputBuffer.length);
                    Log.d("refOutputBuffer : ", this.refOutputBuffer.toString());
                    break;

                case FILE_SYS: //파일 시스템으로만 출력
                    File outputFile = new File(this.outputPath);
                    OutputStream outputStream = new FileOutputStream(outputFile);
                    Log.d("outputFile : ", outputPath);

                    byte[] buffer = new byte[1024];
                    int length = 0;

                    while((length = urlConnectionInputStream.read(buffer)) >= 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    outputStream.flush();
                    outputStream.close();
                    break;

                case ERR: //오류
                    return null;
            }

            urlConnectionInputStream.close();
            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
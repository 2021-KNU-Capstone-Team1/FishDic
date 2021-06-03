package com.knu.fishdic.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtility {
    private static final int BUFFER_SIZE = 1024;

    public static void zip(String[] targetFiles, String outputZipFile) { //압축 수행
        /***
         * targetFiles : 압축 수행 할 파일 경로 리스트
         * outputZipFile : 압축 수행 후 저장 될 파일 이름
         ***/

        try {
            BufferedInputStream bufferedInputStream = null;
            FileOutputStream fileOutputStream = new FileOutputStream(outputZipFile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));

            byte buffer[] = new byte[BUFFER_SIZE];
            for (int i = 0; i < targetFiles.length; i++) { //각각을 zip 엔트리에 추가 후 압축 수행
                Log.d("Compress", targetFiles[i]);
                FileInputStream fileInputStream = new FileInputStream(targetFiles[i]);
                bufferedInputStream = new BufferedInputStream(fileInputStream);

                ZipEntry entry = new ZipEntry(targetFiles[i].substring(targetFiles[i].lastIndexOf("/") + 1));
                zipOutputStream.putNextEntry(entry);
                int length = 0;
                while ((length = bufferedInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    zipOutputStream.write(buffer, 0, length);
                }
                bufferedInputStream.close();
            }

            zipOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unzip(String targetZipFile, String targetLocation) { //압축 해제
        /***
         * targetZipFile : 압축 해제 할 파일
         * targetLocation : 압축 해제 할 경로
         ***/

        File file = new File(targetLocation);
        if (!file.isDirectory()) {
            file.mkdirs();
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(targetZipFile);
            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry = null;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) { //압축 파일 내의 모든 엔트리에 대하여
                if (zipEntry.isDirectory()) { //압축 해제 시 엔트리가 디렉토리일 경우 디렉토리 생성
                    File entryFile = new File(zipEntry.getName());
                    if (!entryFile.isDirectory()) {
                        entryFile.mkdirs();
                    }
                } else {
                    FileOutputStream fileOutputStream = new FileOutputStream(targetLocation + zipEntry.getName());
                    for (int length = zipInputStream.read(); length != -1; length = zipInputStream.read()) {
                        fileOutputStream.write(length);
                    }

                    zipInputStream.closeEntry();
                    fileOutputStream.close();
                }
            }
            zipInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

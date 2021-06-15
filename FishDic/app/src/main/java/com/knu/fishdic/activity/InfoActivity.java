package com.knu.fishdic.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.knu.fishdic.R;

// 정보 액티비티 정의

public class InfoActivity extends Activity {
    Button info_ok_button; //확인 버튼

    WebView info_nifs_webView; //국립수산과학원 웹 뷰
    WebView info_tflite_webView; //텐서플로우 라이트 웹 뷰

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //타이틀바 제거
        setContentView(R.layout.activity_info);

        this.setComponentsInteraction();
    }

    private void setComponentsInteraction() { //내부 구성요소 상호작용 설정
        this.info_ok_button = findViewById(R.id.info_ok_button);
        this.info_nifs_webView = findViewById(R.id.info_nifs_webView);
        this.info_tflite_webView = findViewById(R.id.info_tflite_webView);

        this.info_ok_button.setOnClickListener(v -> { //확인 버튼에 대한 클릭 리스너
            onBackPressed();
        });

        String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                        "<html><head>" +
                        "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />" +
                        "</head><body><div width=\"10\" style=\"text-align:justify\">";

        String nifsInfo = getString(R.string.nifs_info);
        String tfliteInfo = getString(R.string.tflite_info);

        WebSettings nifsWebSettings;
        nifsWebSettings = this.info_nifs_webView.getSettings();
        nifsWebSettings.setTextZoom(50);
        nifsWebSettings.setUseWideViewPort(true);
        nifsWebSettings.setLoadWithOverviewMode(true);

        WebSettings tfliteWebSettings;
        tfliteWebSettings = this.info_tflite_webView.getSettings();
        tfliteWebSettings.setTextZoom(50);
        tfliteWebSettings.setUseWideViewPort(true);
        tfliteWebSettings.setLoadWithOverviewMode(true);

        this.info_nifs_webView.loadData(content + nifsInfo + "</div></body></html>","text/html; charset=utf-8", "UTF-8");
        this.info_tflite_webView.loadData(content + tfliteInfo + "</div></body></html>","text/html; charset=utf-8", "UTF-8");

    }
}

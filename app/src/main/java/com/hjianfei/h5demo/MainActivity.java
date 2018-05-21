package com.hjianfei.h5demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String TITLE_NAME = "TITLE_NAME";
    public static final String OPEN_URL = "OPEN_URL";
    private CommonWebView mCommonWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCommonWebView = (CommonWebView) findViewById(R.id.common_webview);
        mCommonWebView.loadUrl("http://192.168.3.14:8083/html/openApp.html");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCommonWebView != null) {
            mCommonWebView.removeAllViews();
        }
    }
}

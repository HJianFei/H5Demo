package com.hjianfei.h5demo;

import android.graphics.Bitmap;
import android.webkit.WebView;

/**
 *  on 16-10-25.
 */

public abstract class OnUrlLoadingListener {
    /**
     * 监听Url的加载过程，100%的时候加载完成
     */
    public void onLoadProgress(WebView view, int progress) {
    }

    public void onLoadStart(WebView view, String url, Bitmap favicon) {
    }

    public void onLoadFinished(WebView view, String url) {
    }

    public void onLoadError(WebView view, int errorCode, String description, String failingUrl) {
    }

}

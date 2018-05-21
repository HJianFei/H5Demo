package com.hjianfei.h5demo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;


/**
 * 自定义的WebView, 具备进度条，前景页和错误页的功能
 * on 16-10-28.
 */
public class CommonWebView extends WebView {
    private static final String TAG = CommonWebView.class.getSimpleName();

    public static String mCookieStr; //cookie值

    private static final int TIME_OUT = 10;

    private static final int PROGRESS_HEIGHT = 3; //进度条的高度

    private Context mContext;

    private ProgressBar mProgressBar;

    private FrameLayout mForeView; //一层处于WebView和进度条之间的布局，用于显示加载界面或者错误界面

    private OnUrlLoadingListener mLoadingListener;

    public CommonWebView(Context context) {
        super(context);
    }

    public CommonWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        //设置默认的加载页或错误页
        mForeView = new FrameLayout(mContext);
        FrameLayout.LayoutParams lp1 = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mForeView.setLayoutParams(lp1);
        addView(mForeView);
        mForeView.setVisibility(GONE);


        //设置webview的东西
        setWebChromeClient(new CommonWebChromeClient());
        initWebViewSetting();
    }

    /**
     * 监控网页加载进度
     */
    public class CommonWebChromeClient extends android.webkit.WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            if (mProgressBar != null) {
                if ((newProgress > 0 && newProgress < 100)) {
                    if (mProgressBar.getVisibility() == GONE) {
                        mProgressBar.setVisibility(VISIBLE);
                    }
                    mProgressBar.setProgress(newProgress);
                } else {
                    mProgressBar.setVisibility(GONE);
                }
            }

            //有无自定义的监听器
            if (mLoadingListener != null) {
                mLoadingListener.onLoadProgress(view, newProgress);
            }

            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }
    }

    /**
     * 初始化一些默认配置
     */
    private void initWebViewSetting() {
        WebSettings webSettings = getSettings();

        //根据网络状态执行不同的缓存策略
//        if (NetworkUtils.isNetworkAvailable()) {
//            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
//        } else {
//            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
//        }

        requestFocus(View.FOCUS_DOWN);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true); //文件访问权限
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        String appCachePath = mContext.getApplicationContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);

        setVerticalScrollBarEnabled(false);

        //支持缩放
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        //自动适应屏幕
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        //隐藏放大缩小按钮
        webSettings.setDisplayZoomControls(false);

        //5.0以上，设置自动同步cookie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        }

        //启用地理定位
        webSettings.setGeolocationEnabled(true);

        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (TextUtils.isEmpty(url)) {
                    return false;
                }
                try {
                    if (!url.startsWith("http") || !url.startsWith("https") || !url.startsWith("ftp")) {

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        Uri data = intent.getData();
                        String action = data.getQueryParameter("action");
                        if (url.startsWith("tgparent") && null != action && action.equals("home")) {//打开首页

                            getContext().startActivity(new Intent(mContext, HomeActivity.class));
                            return true;

                        } else if (url.startsWith("tgparent") && null != action && action.equals("children")) {//打开小孩页
                            return true;

                        } else if (url.startsWith("tgparent") && null != action && action.equals("shopHome")) {//商家主页

                            String ecode = data.getQueryParameter("ecode");
                            return true;

                        } else if (url.startsWith("tgparent") && null != action && action.equals("orderList")) {//订单列表

                            return true;

                        } else if (url.startsWith("tgparent") && null != action && action.equals("orderDetail")) {// 订单详情

                            String orderId = data.getQueryParameter("orderId");
                            return true;

                        } else if (url.startsWith("tgparent") && null != action && action.equals("wallet")) {//今钱包

                            return true;

                        } else if (url.startsWith("tgparent") && null != action && action.equals("messageList")) {//消息列表

                            return true;

                        } else if (url.startsWith("tgparent") && null != action && action.equals("chat")) {//聊天界面

                            return true;

                        } else if (url.startsWith("tgparent") && null != action && action.equals("chatGroup")) {//群聊天界面

                            return true;

                        } else {

                            return true;
                        }

                    } else {
                        view.loadUrl(url);
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                url = url.toLowerCase();
                //拦截广告url
                if (!ADFilterTool.hasAd(mContext, url)) {
                    return super.shouldInterceptRequest(view, url);
                } else {
                    return new WebResourceResponse(null, null, null);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (null != mLoadingListener) {
                    mLoadingListener.onLoadStart(view, url, favicon);
                }

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager cookieManager = CookieManager.getInstance();
                String cookieStr = cookieManager.getCookie(url);
                Log.d("Cookie --> ", url + "==> Cookies = " + cookieStr);

                if (mProgressBar != null) {
                    mProgressBar.setVisibility(GONE);
                }

                if (null != mLoadingListener) {
                    mLoadingListener.onLoadFinished(view, url);
                }
                super.onPageFinished(view, url);
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Handle the error
                if (mProgressBar != null) {
                    mProgressBar.setVisibility(GONE);
                }

                if (null != mLoadingListener) {
                    mLoadingListener.onLoadError(view, errorCode, description, failingUrl);
                }

            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }

        });
    }


    /**
     * 如果滑动，仍保持进度条的位置不变
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) mProgressBar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        mProgressBar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }


    /**
     * 设置进度条，若progressbar为null，则不显示
     *
     * @param progressBar
     */
    public void setProgressBar(ProgressBar progressBar) {
        this.mProgressBar = progressBar;
    }

    /**
     * 设置前置页面的内容
     *
     * @param view
     */
    public void setForeViewContent(View view) {
        mForeView.removeAllViews();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        view.setLayoutParams(lp);
        mForeView.addView(view);
    }

    /**
     * 显示前置页面与否
     *
     * @param isShow
     */
    public void showForeViewContent(boolean isShow) {
        mForeView.setVisibility(isShow ? VISIBLE : GONE);
    }

    /**
     * 设置错误页
     *
     * @param view
     */
    public void setErrorPage(View view) {
        setForeViewContent(view);
    }

    /**
     * 自定义的加载监听器
     *
     * @param loadingListener
     */
    public void setLoadingListener(OnUrlLoadingListener loadingListener) {
        this.mLoadingListener = loadingListener;
    }

    /**
     * 网络请求前，先做判断
     *
     * @param url
     */
    @Override
    public void loadUrl(String url) {

        Map<String, String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("device", "android");
        //syncCookie(mContext, url);
        super.loadUrl(url, extraHeaders);
    }


    /**
     * This method is designed to hide how Javascript is injected into
     * the WebView.
     * <p>
     * In KitKat the new evaluateJavascript method has the ability to
     * give you access to any return values via the ValueCallback object.
     * <p>
     * The String passed into onReceiveValue() is a JSON string, so if you
     * execute a javascript method which return a javascript object, you can
     * parse it as valid JSON. If the method returns a primitive value, it
     * will be a valid JSON object, but you should use the setLenient method
     * to true and then you can use peek() to test what kind of object it is,
     *
     * @param javascript
     */
    public void loadJavascript(String javascript) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // In KitKat+ you should use the evaluateJavascript method
            evaluateJavascript(javascript, new ValueCallback<String>() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onReceiveValue(String s) {
                    JsonReader reader = new JsonReader(new StringReader(s));

                    // Must set lenient to parse single values
                    reader.setLenient(true);

                    try {
                        if (reader.peek() != JsonToken.NULL) {
                            if (reader.peek() == JsonToken.STRING) {
                                String msg = reader.nextString();
                                if (msg != null) {
                                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } catch (IOException e) {
                        Log.e("TAG", "HomeActivity: IOException", e);
                    } finally {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                }
            });
        } else {
            /**
             * For pre-KitKat+ you should use loadUrl("javascript:<JS Code Here>");
             * To then call back to Java you would need to use addJavascriptInterface()
             * and have your JS call the interface
             **/
            loadUrl("javascript:" + javascript);
        }
    }

}

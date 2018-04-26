package com.example.ypp.sonic;

import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.tencent.sonic.sdk.SonicConfig;
import com.tencent.sonic.sdk.SonicEngine;
import com.tencent.sonic.sdk.SonicSession;
import com.tencent.sonic.sdk.SonicSessionClient;
import com.tencent.sonic.sdk.SonicSessionConfig;

public class BrowerActivity extends AppCompatActivity {
    private static final String TAG = "BrowerActivity";

    public static final String PARAM_URL = "param_url";
    public static final String PARAM_MODE = "param_mode";

    private String url = "https://baijia.baidu.com/";
    private SonicSession sonicSession;

    private ProgressBar progressBar;
    private SonicSessionClientImpl sonicSessionClient;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        if (!SonicEngine.isGetInstanceAllowed()){
            SonicEngine.createInstance(new HostRuntime(getApplication()),new SonicConfig.Builder().build());
        }

        SonicSessionConfig.Builder sessionConfigBuilder=new SonicSessionConfig.Builder();
        sonicSession=SonicEngine.getInstance().createSession(url,sessionConfigBuilder.build());
        if (null!=sonicSession){
            sonicSession.bindClient(sonicSessionClient=new SonicSessionClientImpl());
        }else {
            Log.d(TAG, "onCreate: ");
        }
        setContentView(R.layout.activity_brower);


        webView = findViewById(R.id.webView);
        progressBar=findViewById(R.id.progressbar);

        WebSettings webSettings=webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webView.canGoBack();
        if (sonicSessionClient!=null){
            sonicSessionClient.bindWebView(webView);
            sonicSessionClient.clientReady();
        }

                webView.loadUrl(url);

        Log.d(TAG, "onCreate: aaaa");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (sonicSession != null) {
                    sonicSession.getSessionClient().pageFinish(url);
                }
            }

            @TargetApi(21)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return shouldInterceptRequest(view, request.getUrl().toString());
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (sonicSession != null) {
                    return (WebResourceResponse) sonicSession.getSessionClient().requestResource(url);
                }
                return null;
            }

        });

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (webView.canGoBack()){
            webView.goBack();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (null!=sonicSession){
            sonicSession.destroy();
            sonicSession=null;
        }
        super.onDestroy();
    }
}


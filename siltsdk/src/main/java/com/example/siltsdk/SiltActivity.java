package com.example.siltsdk;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SiltActivity extends AppCompatActivity {
    private static final String TAG = "SiltActivity";
    private static final String SiltSignUpUrl = "https://signup.getsilt.com";
    private String CompanyAppId;
    //private WebView webview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Created web activity");
        CompanyAppId = getIntent().getStringExtra("companyAppId");
        Log.d(TAG, "Company App ID " + CompanyAppId);
        setContentView(R.layout.activity_silt);
        loadSiltSignUp(CompanyAppId);
    }

    public void loadSiltSignUp(String companyAppId) {
        final String url = SiltSignUpUrl + "?company_app_id=" + companyAppId;
        WebView web = (WebView) findViewById(R.id.silt_web);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            web.setWebContentsDebuggingEnabled(true);
        }
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                Log.d(TAG, url);
                Uri uri = Uri.parse(url);
                String path = uri.getPath();
                String user_id = uri.getQueryParameter("user_id");
                if(path.equals("/finishedVerification")) {
                    Log.d(TAG, "user finished verification");
                    Intent data = new Intent();
                    data.putExtra("user_id", user_id);
                    setResult(RESULT_OK, data);
                    finish();
                }
                super.doUpdateVisitedHistory(view, url, isReload);
            }

        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                Log.d(TAG, "onPermissionRequest");
                SiltActivity.this.runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                        Log.d(TAG, request.getResources().toString());
                        Log.d(TAG, "GRANTED");

                    }
                });
            }
        });

        web.loadUrl(url);
    }

/*    @Override
    public void onBackPressed(){
        if(webview.canGoBack()){
            // If web view have back history, then go to the web view back history
            webview.goBack();
        }else {
            finish();
        }
    }*/
}

package com.example.siltdemojava;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Created web activity");
        setContentView(R.layout.activity_silt);
        loadSiltSignUp();
    }

    public void loadSiltSignUp() {
        final String url = SiltSignUpUrl + "?customer_id=123asdf123asdf";
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
                String user_id = uri.getQueryParameter("customer_id");
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
                        Log.d(TAG, request.getOrigin().toString());
                        Log.d(TAG, "GRANTED");
                        request.grant(request.getResources());
                    }
                });
            }
        });

        web.loadUrl(url);
    }
}

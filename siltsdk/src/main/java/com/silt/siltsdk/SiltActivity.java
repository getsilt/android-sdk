package com.silt.siltsdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
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
    private static final String SiltSignUpUrl = "https://signup-stg.getsilt.com";
    private String CompanyAppId;
    private WebView webview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CompanyAppId = getIntent().getStringExtra("companyAppId");
        setContentView(R.layout.activity_silt);
        loadSiltSignUp(CompanyAppId);
    }

    public void grantPermission() {
        String permission = Manifest.permission.CAMERA;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
    }

    public void loadSiltSignUp(String companyAppId) {
        final String url = SiltSignUpUrl + "?company_app_id=" + companyAppId;
        webview = (WebView) findViewById(R.id.silt_web);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                Uri uri = Uri.parse(url);
                String path = uri.getPath();
                String silt_user_id = uri.getQueryParameter("silt_user_id");
                String company_app_token = uri.getQueryParameter("company_app_token");

                // UPDATE user_id and company_app token, set it to intent
                Intent data = getIntent();
                if(  silt_user_id != null && !silt_user_id.isEmpty() ) {
                    data.putExtra("silt_user_id", silt_user_id);
                }
                if( company_app_token != null && !company_app_token.isEmpty() ) {
                    data.putExtra("company_app_token", company_app_token);
                }
                setResult(RESULT_OK, data);

                // Aks for Camera permissions
                if (path.contains("/document-select")) {
                    grantPermission();
                }

                // Close web view after finished verification
                if(path.equals("/finished-verification")) {
                    finish();
                }
                super.doUpdateVisitedHistory(view, url, isReload);
            }
        });

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                grantPermission();
                SiltActivity.this.runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                    }
                });
            }
        });
        webview.loadUrl(url);
    }

    @Override
    public void onBackPressed(){
        if(webview.canGoBack()){
            // If web view have back history, then go to the web view back history
            webview.goBack();
        }else {
            finish();
        }
    }
}

package com.silt.siltsdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;



public class SiltActivity extends AppCompatActivity {
    private static final String TAG = "SiltActivity";
    private static final String SiltSignUpUrl = "https://signup.getsilt.com/";
    private String companyAppId;
    private String extraQuery;
    private String path;
    private WebView webview;

    //https://developpaper.com/android-webview¢-supports-input-file-to-enable-camera-select-photos/
    private android.webkit.ValueCallback mUploadCallbackAboveL;
    public String fileProviderAuthority = "";

    private static final int REQUEST_CODE = 1234;
    private static final int PERMISSION_REQUEST_CODE = 777;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        companyAppId = getIntent().getStringExtra("companyAppId");
        path = getIntent().getStringExtra("path");
        extraQuery = getIntent().getStringExtra("extraQuery");
        setContentView(R.layout.activity_silt);
        loadSiltSignUp(companyAppId, extraQuery);
        fileProviderAuthority = getApplicationContext().getPackageName() + ".siltsdk.CameraPictureProvider";
        Log.d(TAG, "setting context provider name: " + fileProviderAuthority);

    }

    public void loadSiltSignUp(String companyAppId, String extraQuery) {
        if (extraQuery == null) {
            extraQuery = "";
        }
        final String url = SiltSignUpUrl + path + "?company_app_id=" + companyAppId + extraQuery;
        webview = (WebView) findViewById(R.id.silt_web);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);

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
                if (silt_user_id != null && !silt_user_id.isEmpty()) {
                    data.putExtra("silt_user_id", silt_user_id);
                }
                if (company_app_token != null && !company_app_token.isEmpty()) {
                    data.putExtra("company_app_token", company_app_token);
                }
                setResult(RESULT_OK, data);

                // Aks for Camera permissions
                if (path.contains("/document-select/")) {
                    grantPermission();
                }
                if (path.contains("/biocheck")) {
                    grantPermission();
                }

                // Close web view after finished verification
                if (path.equals("/finished-verification")) {
                    finish();
                }
                super.doUpdateVisitedHistory(view, url, isReload);
            }
        });

        webview.setWebChromeClient(new WebChromeClient() {

            public boolean onShowFileChooser(WebView mWebView, ValueCallback valueCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                mUploadCallbackAboveL = valueCallback;
                takePhoto();
                return true;
            }

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
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode: " + requestCode +
                " Result Code: " + resultCode);
        if (requestCode == REQUEST_CODE) {
            if (mUploadCallbackAboveL != null) {
                chooseAbove(resultCode, data);
            } else {
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        }
        return;
    }

    private void chooseAbove(int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            File out = new File(getFilesDir(), CameraPictureProvider.FILENAME);
            if(!out.exists()) {
                Toast.makeText(getBaseContext(),
                        "Error while capturing image", Toast.LENGTH_LONG)
                        .show();
                mUploadCallbackAboveL.onReceiveValue(null);
                return;
            }
            Log.d(TAG, "Sending file to webview: " + "file://" + out.getAbsolutePath());
            mUploadCallbackAboveL.onReceiveValue(new Uri[]{Uri.parse("file://" + out.getAbsolutePath())});
        } else {
            mUploadCallbackAboveL.onReceiveValue(null);
        }
        mUploadCallbackAboveL = null;
    }

    private void takePhoto() {
        try {
            grantPermission();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Log.d(TAG, "Starting camera to save file in: " + CameraPictureProvider.content_uri);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, CameraPictureProvider.content_uri);
            startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception ex) {
            Log.e(TAG, "Found exception while taking photo: ", ex);
        }
    }


    public void grantPermission() {
        String permissionCamera = Manifest.permission.CAMERA;
        List<String> permissionsList = new ArrayList<String>();
        int grantCamera = ContextCompat.checkSelfPermission(this, permissionCamera);
        if (grantCamera != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permissionCamera);
        }

        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Camera permissions required", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
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
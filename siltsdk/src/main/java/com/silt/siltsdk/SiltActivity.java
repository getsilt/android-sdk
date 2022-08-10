package com.silt.siltsdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.util.Date;
import java.util.Objects;

public class SiltActivity extends AppCompatActivity {
    private static final String TAG = "SiltActivity";
    private static final String SiltSignUpUrl = "https://signup-stg.getsilt.com";
    private String CompanyAppId;
    private String extraQuery;
    private WebView webview;

    //https://developpaper.com/android-webview¢-supports-input-file-to-enable-camera-select-photos/
    private android.webkit.ValueCallback mUploadCallbackAboveL;
    private Uri imageUri;
    private int REQUEST_CODE = 1234;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CompanyAppId = getIntent().getStringExtra("companyAppId");
        extraQuery = getIntent().getStringExtra("extraQuery");
        setContentView(R.layout.activity_silt);
        loadSiltSignUp(CompanyAppId, extraQuery);
    }

    public void grantPermission() {
        String permissionCamera = Manifest.permission.CAMERA;
        int grantCamera = ContextCompat.checkSelfPermission(this, permissionCamera);
        if (grantCamera != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permissionCamera;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }

        String permissionFiles = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int grantFiles = ContextCompat.checkSelfPermission(this, permissionFiles);
        if (grantFiles != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permissionFiles;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
    }

    public void loadSiltSignUp(String companyAppId, String extraQuery) {
        if (extraQuery == null) {
            extraQuery = "";
        }
        final String url = SiltSignUpUrl + "?company_app_id=" + companyAppId + extraQuery;
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
                Log.d(TAG, "doUpdateVisitedHistory");

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
                if (path.contains("/document-select")) {
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
        if (requestCode == REQUEST_CODE) {
            if (mUploadCallbackAboveL != null) {
                chooseAbove(resultCode, data);
            } else {
                Toast.makeText(this, "error occurred", Toast.LENGTH_SHORT).show();
            }
        }
        return;
    }

    /*private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        imageFilePath = image.getAbsolutePath();
        Log.e(TAG, "createImageFile got image Uri: " + imageFilePath);
        return image;
    }*/

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filePath = Environment.getExternalStorageDirectory() + File.separator
                + Environment.DIRECTORY_PICTURES + File.separator;
        File image = new File(filePath + timeStamp + ".jpg");
        imageUri = FileProvider.getUriForFile(
                Objects.requireNonNull(getApplicationContext()),
                "com.silt.siltsdk.provider",
                image);
        return image;
    }

    private void chooseAbove(int resultCode, Intent data) {
        Log.e(TAG, "return call method -- chooseabove");

        if (RESULT_OK == resultCode) {
            updatePhotos();

            if (data != null) {
                //Here is the processing for selecting pictures from files
                Uri[] results;
                Uri uriData = data.getData();
                if (uriData != null) {
                    results = new Uri[]{uriData};
                    for (Uri uri : results) {
                        Log.e(TAG, "system return URI:" + uri.toString());
                    }
                    mUploadCallbackAboveL.onReceiveValue(results);
                } else {
                    mUploadCallbackAboveL.onReceiveValue(null);
                }
            } else {
                Log.e(TAG, "custom result:" + imageUri.toString());
                mUploadCallbackAboveL.onReceiveValue(new Uri[]{imageUri});
            }
        } else {
            mUploadCallbackAboveL.onReceiveValue(null);
        }
        mUploadCallbackAboveL = null;
    }

    private void updatePhotos() {
        //It doesn't matter if the broadcast is sent multiple times (i.e. when the photos are selected successfully), but it just wakes up the system to refresh the media files
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(imageUri);
        sendBroadcast(intent);
    }


    private void takePhoto() {
        try {
            createImageFile();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, REQUEST_CODE);
        } catch (IOException ex) {

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
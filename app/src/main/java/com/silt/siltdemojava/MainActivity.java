package com.silt.siltdemojava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.silt.siltsdk.SiltActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int VERIFY_CODE = 777;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void loadSiltSignUp(View v) {
        // ask for your companyAppId on hello@getsilt.com
        // and use it in the initializer as extra
        // siltActivity.putExtra("companyAppId", "{YOUR_CUSTOMER_APP_ID}")
        // demo companyAppId: 9f936bc0-328f-4985-95b1-2c562061711f
        Intent siltActivity = new Intent(this, SiltActivity.class);
        siltActivity.putExtra("companyAppId", "2022a022-a662-4c58-8865-a1fb904d2cde");
        String queryParams = "&user_email=test@getsilt.com";

        siltActivity.putExtra("extraQuery", queryParams);
        startActivityForResult(siltActivity, VERIFY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == VERIFY_CODE) {
            if (data.hasExtra("silt_user_id") && data.hasExtra("company_app_token")) {
                Log.d(TAG, "####### Got user Id from Silt: " + data.getStringExtra("silt_user_id"));
                Log.d(TAG, "####### Got Company App Token: " + data.getStringExtra("company_app_token"));
                /*
                * 1. Place here a function that calls to your backend with user_id
                * 2. Your backend should make a request to Silt API to /v1/users/{user_id}
                * 3. Retrieve the info that you want from that response
                * */

            }
        }
    }

}

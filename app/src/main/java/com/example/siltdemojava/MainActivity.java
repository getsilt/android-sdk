package com.example.siltdemojava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int VERIFY_CODE = 777;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btn = findViewById(R.id.silt_button);
        Typeface futura = Typeface.createFromAsset(getAssets(), "FuturaMedium.ttf");
        btn.setTypeface(futura);

        Button btn2 = findViewById(R.id.silt_button2);
        btn2.setTypeface(futura);

    }

    public void loadSiltSignUp(View v) {
        Intent siltActivity = new Intent(this, SiltActivity.class);
        startActivityForResult(siltActivity, VERIFY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Got activity result");
        if (resultCode == RESULT_OK && requestCode == VERIFY_CODE) {
            if (data.hasExtra("user_id")) {
                Log.d(TAG, "" + data.getStringExtra("user_id"));

                /*
                * 1. Place here a function that calls to your backend with user_id
                * 2. Your backend should make a request to Silt API to /v1/users/{user_id}
                * 3. Retrieve the info that want from that response
                * */

            }
        }
    }

}

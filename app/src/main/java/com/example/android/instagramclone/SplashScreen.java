package com.example.android.instagramclone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    int SPLASH_TIMEOUT = 2000;

    ImageView imageView;

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        imageView = findViewById(R.id.instagramTitleView);
        textView = findViewById(R.id.textView);
        textView.animate().alpha(10).setDuration(6000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent splashIntent = new Intent(SplashScreen.this, SignUp.class);
                startActivity(splashIntent);
                finish();

            }
        } , SPLASH_TIMEOUT);

    }
}

package com.example.android.instagramclone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import me.relex.circleindicator.CircleIndicator;

public class SlideShow extends AppCompatActivity {

    private ViewPager viewPager;
    private CircleIndicator circleIndicator;
    MyPager myPager;
    int currentPage = 0;
    Timer timer;
    TextView slideText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_show);

        slideText = findViewById(R.id.slideText);
        myPager = new MyPager(this);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(myPager);
        circleIndicator = findViewById(R.id.circle);
        circleIndicator.setViewPager(viewPager);

        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == 6-1) {
                   // currentPage = 0;
                    Intent intent = new Intent(getApplicationContext(), AccountSetup.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                if(currentPage == 0){
                    slideText.setText(R.string.slideShow1);
                }else if(currentPage == 1){
                    slideText.setText(R.string.slideShow2);
                }else if(currentPage == 2){
                    slideText.setText(R.string.slideShow3);
                }else if(currentPage == 3){
                    slideText.setText(R.string.slideShow4);
                }else if(currentPage == 4){
                    slideText.setText(R.string.slideShow5);
                }

                viewPager.setCurrentItem(currentPage++, true);
            }
        };

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 500, 4000);

    }
}

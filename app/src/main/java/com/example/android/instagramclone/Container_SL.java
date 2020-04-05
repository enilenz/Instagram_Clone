package com.example.android.instagramclone;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Container_SL extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container__sl);

        if (findViewById(R.id.fragment_containerSL) != null) {
            if (savedInstanceState != null) {
                return;
            }

            SignUp firstFragment = new SignUp();
            firstFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_containerSL, firstFragment).commit();
        }
    }
}


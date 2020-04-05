package com.example.android.instagramclone;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Container_Main extends AppCompatActivity {


    Button logOutButton;
    TextView textView;

    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container__main);

        textView = findViewById(R.id.userDataView);

        //user = auth.getCurrentUser();
        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

               user = firebaseAuth.getCurrentUser();

            }
        };

        logOutButton = findViewById(R.id.logOut);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (auth.getCurrentUser() != null){
                    auth.signOut();
                }
            }
        });


    }

    @Override
    protected void onStart() {

        auth.addAuthStateListener(authStateListener);
        super.onStart();
    }

    public void textClick(View view){
        if(auth.getCurrentUser() != null ){
        String txt = user.getEmail() + "\t" + user.getUid();
        textView.setText(txt);
        }
    }
}

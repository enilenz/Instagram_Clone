package com.example.android.instagramclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class SignUp extends Fragment {
    private Button signUpButton;
    private TextView signInTextView;
    private TextInputEditText userNameField;
    private TextInputEditText emailField;
    private TextInputEditText passwordField;
    private TextInputLayout usernameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReferenceSignUp;
    private ProgressDialog progressDialog;

    private String username;
    private String email;
    private String password;

    private boolean isEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.activity_sign_up, container , false);

        mAuth = FirebaseAuth.getInstance();
        databaseReferenceSignUp = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReferenceSignUp.keepSynced(true);
        progressDialog = new ProgressDialog(getContext());
        userNameField = view.findViewById(R.id.userName);
        emailField = view.findViewById(R.id.email);
        usernameLayout = view.findViewById(R.id.username_text_input);
        emailLayout = view.findViewById(R.id.email_text_input);
        passwordLayout = view.findViewById(R.id.password_text_input);
        passwordField = view.findViewById(R.id.password_edit_text);
        signInTextView = view.findViewById(R.id.signinTextView);
        signUpButton = view.findViewById(R.id.signUpButton);
        signInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LogIn newFragment = new LogIn();

                FragmentTransaction transaction = null;
                if (getFragmentManager() != null) {
                    transaction = getFragmentManager().beginTransaction();
                }

                transaction.replace(R.id.fragment_containerSL, newFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });

        passwordField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isPasswordValid(passwordField.getText())) {
                    passwordLayout.setError(null);
                }
                return false;
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startSignUp();

            }
        });


        return view;
    }

    private void startSignUp() {

        emailLayout.setErrorEnabled(false);
        passwordLayout.setErrorEnabled(false);

        username = userNameField.getText().toString().trim();
        email = emailField.getText().toString().trim();
        password = passwordField.getText().toString().trim();

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            if (isEmailValid(email) && isPasswordValid(passwordField.getText())) {

                // if (isPasswordValid(passwordField.getText())) {
                progressDialog.setMessage("Signing Up...");
                progressDialog.show();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful() ) {

                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference current_user = databaseReferenceSignUp.child(user_id);
                            current_user.child("username").setValue(username);
                            current_user.child("email").setValue(email);
                            current_user.child("password").setValue(password);


                            progressDialog.dismiss();

                            Intent intent = new Intent(getContext(), SlideShow.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getContext(), "Sign Up Failed", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });

//                else {
//                    passwordLayout.setError(getString(R.string.sign_up_PasswordError));
//                }
            } else if (!isEmailValid(email) && isPasswordValid(passwordField.getText())) {
                emailLayout.setError(getString(R.string.sign_up_EmailError));
            } else if (isEmailValid(email) && !isPasswordValid(passwordField.getText())) {
                passwordLayout.setError(getString(R.string.sign_up_PasswordError));
            }  else if (!isEmailValid(email) && !isPasswordValid(passwordField.getText())) {
                emailLayout.setError(getString(R.string.sign_up_EmailError));
                passwordLayout.setError(getString(R.string.sign_up_PasswordError));
            }
        }else {
            Toast.makeText(getActivity(), "One or more fields are empty", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

    }

    private boolean isPasswordValid(@Nullable Editable text) {
        return text != null && text.length() >= 8;
    }

    public static boolean isEmailValid(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }


    public boolean checkEmailExists(){
        //final String user_id  = mAuth.getCurrentUser().getUid();
         isEmail = true;
        final String user_email = mAuth.getCurrentUser().getEmail();

        databaseReferenceSignUp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(user_email)){
                    Toast.makeText(getContext(), "Email has already been used", Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(getContext(),"User does not exist",Toast.LENGTH_SHORT).show();
                    isEmail = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
     return isEmail;
    }

}

package com.example.android.instagramclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import static android.content.ContentValues.TAG;

public class LogIn extends Fragment {
    private Button loginButton;
    private TextView loginTextView;
    private TextInputEditText emailField;
    private TextInputEditText passwordField;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private SignInButton gSignInButton;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReferenceLogIn;

    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 58;

    private String email;
    private String password;

    boolean isEmail;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_log_in, container, false);

        mAuth = FirebaseAuth.getInstance();
        databaseReferenceLogIn = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReferenceLogIn.keepSynced(true);
        loginButton = view.findViewById(R.id.logInButton);
        loginTextView = view.findViewById(R.id.loginTextView);
        emailField = view.findViewById(R.id.email);
        passwordField = view.findViewById(R.id.password_edit_text);
        emailLayout = view.findViewById(R.id.email_text_input);
        passwordLayout = view.findViewById(R.id.password_text_input);
        gSignInButton = view.findViewById(R.id.googleSign_in_button);
        progressDialog = new ProgressDialog(getContext());

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                SignUp newFragment = new SignUp();
//                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                FragmentTransaction transaction = null;
                if (getFragmentManager() != null) {
                    transaction = getFragmentManager().beginTransaction();
                }

                transaction.replace(R.id.fragment_containerSL, newFragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
            }
        });

        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("844982095388-log6duco4okgbc1rcpvkbj26to8bdp62.apps.googleusercontent.com")
                .requestEmail()
                .build();

        gSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               signIn();
            }
        });

        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());


    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
//            progressDialog.setMessage("Signing In with Google...");
//            progressDialog.show();
            //checkUserExists();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
//                progressDialog.dismiss();
                Toast.makeText(getContext(),"Error Signing In",Toast.LENGTH_SHORT).show();
//                progressDialog.dismiss();

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
//                            progressDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Intent intent  = new Intent(getContext(),MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                            FirebaseUser user = mAuth.getCurrentUser();


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getContext(),"Google Sign In Failed", Toast.LENGTH_SHORT).show();


                        }


                    }
                });
    }
    public void logIn(){

        emailLayout.setErrorEnabled(false);
        passwordLayout.setErrorEnabled(false);

        email = emailField.getText().toString().trim();
        password = passwordField.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            if (isEmailValid(email) && isPasswordValid(passwordField.getText())) {
                // if (isPasswordValid(passwordField.getText())) {
                progressDialog.setMessage("Logging In...");
                progressDialog.show();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener( getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    progressDialog.dismiss();
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();


                                    checkUserExists();

                                } else {

                                    Toast.makeText(getContext(), "Log In Failed", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();

                                }

                            }
                        });

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

    private boolean isPasswordValid(@Nullable Editable text) {
        return text != null && text.length() >= 8;
    }

    public static boolean isEmailValid(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public boolean checkEmailExistsOrNot(){
        isEmail = true;
        mAuth.fetchSignInMethodsForEmail(emailField.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                Log.d(TAG,""+task.getResult().getSignInMethods().size());
                if (task.getResult().getSignInMethods().size() == 0){

                }else {
                    // email existed
                    isEmail = false;
                    Toast.makeText(getContext(),"This email has already been used",Toast.LENGTH_SHORT).show();
                }

            }
        });
        return isEmail;
    }

    public void checkUserExists() {

        if (mAuth.getCurrentUser() != null) {

            final String user_id = mAuth.getCurrentUser().getUid();

            databaseReferenceLogIn.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(user_id)) {
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "User does not exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }


}

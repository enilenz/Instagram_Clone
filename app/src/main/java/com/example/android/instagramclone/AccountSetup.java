package com.example.android.instagramclone;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AccountSetup extends AppCompatActivity {

    private ImageButton imageButton;
    private int GALLERY_REQUEST_CODE =8;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private DatabaseReference asDatabaseRef;
    private StorageReference storageReference;

    private static int WRITE_STORAGE_REQUEST_CODE = 0;
    private static int READ_STORAGE_REQUEST_CODE = 1;
    private static int CAMERA_REQUEST_CODE = 2;

    TextInputLayout fullNameLayout;
    TextInputLayout phoneNumberLayout;
    TextInputEditText fullName;
    TextInputEditText phoneNumber;
    Button setupButton;
    private Uri imageUri = null;

    ProgressDialog accountDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        fullName = findViewById(R.id.accountSetup_fullName_Text);
        fullNameLayout = findViewById(R.id.accountSetup_fullName_Layout);
        phoneNumber = findViewById(R.id.accountSetup_phoneNumber_Text);
        phoneNumberLayout = findViewById(R.id.accountSetup_phoneNumber_Layout);
        setupButton = findViewById(R.id.setupButton);

        asDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference().child("Profile_Images");

        accountDialog = new ProgressDialog(this);

        if (ContextCompat.checkSelfPermission(this , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(this , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }

                imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent ,GALLERY_REQUEST_CODE);
            }
        });

        //user = auth.getCurrentUser();
        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                user = firebaseAuth.getCurrentUser();

            }
        };

        fullName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                fullNameLayout.setError(null);
                return false;
            }
        });

        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finishSetUp();

            }


        });

    }
    @Override
    public void onStart() {
        auth.addAuthStateListener(authStateListener);
        super.onStart();
    }

    public void finishSetUp(){
        fullNameLayout.setErrorEnabled(false);

        final String fName = fullName.getText().toString().trim();
        final String pNumber = phoneNumber.getText().toString().trim();
        String user_id = user.getUid();

        if (imageUri != null && !TextUtils.isEmpty(fName)) {

                accountDialog.setMessage("Setting Up Account...");
                accountDialog.show();

                final DatabaseReference current = asDatabaseRef.child(user_id);

                final StorageReference filepath = storageReference.child(imageUri.getLastPathSegment());
                filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                     String downloadUri = filepath.getDownloadUrl().toString();
                        current.child("fullName").setValue(fName);
                        current.child("profilePic").setValue(downloadUri);

                        if (!TextUtils.isEmpty(pNumber)) {
                            current.child("phoneNumber").setValue(pNumber);
                        }

                        accountDialog.dismiss();

                        Intent intent  = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Setup Failed", Toast.LENGTH_SHORT).show();
                        accountDialog.dismiss();
                    }
                });


        }else if (imageUri == null && TextUtils.isEmpty(fName)){
            Toast.makeText(getApplicationContext(), "Please select an image" , Toast.LENGTH_SHORT).show();
            fullNameLayout.setError(getString(R.string.accountSetup_fullNameEmpty));
        }else if (imageUri != null && TextUtils.isEmpty(fName)){
            fullNameLayout.setError(getString(R.string.accountSetup_fullNameEmpty));
        }else if (imageUri == null && !TextUtils.isEmpty(fName)){
            Toast.makeText(getApplicationContext(), "Please select an image" , Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){

            Uri imageData = data.getData();

            CropImage.activity(imageData)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                imageButton.setScaleType(ImageButton.ScaleType.CENTER_CROP);
                imageButton.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


    }
}

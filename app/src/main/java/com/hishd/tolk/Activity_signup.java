package com.hishd.tolk;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hishd.tolk.util.FirebaseUserOP;
import com.hishd.tolk.util.ImgCompressor;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_signup extends AppCompatActivity implements FirebaseUserOP.OnUserSignUpListener {

    CircleImageView imgPic;
    EditText txtName;
    EditText txtEmail;
    EditText txtPassword;
    Button btnSignUp;
    FirebaseUserOP firebaseUserOP;
    ProgressDialog progressDialog;

    private final int PICK_IMAGE_REQUEST = 1;

    Uri mImageUri;
    byte[] imageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        imgPic = findViewById(R.id.imgPic);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Signing up");
        progressDialog.setMessage("Signing up. Please wait.");

        firebaseUserOP = new FirebaseUserOP(this);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(txtName.getText().length()<3){
                    Toast.makeText(Activity_signup.this,"Enter a valid name",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(txtEmail.getText().length()<3){
                    Toast.makeText(Activity_signup.this,"Enter a valid email",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(txtPassword.getText().length()<1){
                    Toast.makeText(Activity_signup.this,"Enter a valid Password",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mImageUri != null) {
                    Bitmap bitmap = ((BitmapDrawable) imgPic.getDrawable()).getBitmap();
                    imageData = ImgCompressor.comPressImageToBitmap(bitmap);
                }

                progressDialog.show();
                firebaseUserOP.registerUser(txtName.getText().toString(),
                        txtEmail.getText().toString().replace("@","_").replace(".","_"),
                        txtPassword.getText().toString(),
                        imageData,
                        Activity_signup.this);
            }
        });

        imgPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Glide.with(this).load(mImageUri).into(imgPic);
        }
    }

    @Override
    public void onUserSignUpSuccessful() {
        progressDialog.dismiss();
        Toast.makeText(this,"Signup Successful",Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onUserSignUpFailed(String message) {
        progressDialog.dismiss();
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}

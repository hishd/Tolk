package com.hishd.tolk;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hishd.tolk.model.User;
import com.hishd.tolk.util.FirebaseUserOP;

public class Activity_signin extends AppCompatActivity implements FirebaseUserOP.OnUserSignInListener {

    EditText txtEmail;
    EditText txtPassword;
    Button btnSignIn;
    TextView txtSignUp;
    FirebaseUserOP firebaseUserOP;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        txtSignUp = findViewById(R.id.txtSignUp);

        firebaseUserOP = new FirebaseUserOP(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Signing in");
        progressDialog.setMessage("Signing in. Please wait.");

        sharedPreferences = getSharedPreferences("TOLK", Context.MODE_PRIVATE);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(txtEmail.getText().length()<3){
                    Toast.makeText(Activity_signin.this,"Enter a valid email",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(txtPassword.getText().length()<1){
                    Toast.makeText(Activity_signin.this,"Enter a valid Password",Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.show();
                firebaseUserOP.userLogin(txtEmail.getText().toString().replace("@","_").replace(".","_"),
                        txtPassword.getText().toString(),
                        Activity_signin.this);
            }
        });

        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Activity_signin.this, Activity_signup.class));
            }
        });
    }

    @Override
    public void onUserSignInSuccessful(User user) {
        progressDialog.dismiss();
        editor = sharedPreferences.edit();
        editor.putString("NAME",user.getName());
        editor.putString("EMAIL",user.getEmail());
        editor.putString("IMAGE",user.getImage_url());
        editor.apply();
        startActivity(new Intent(this,Activity_chats.class));
        finish();
    }

    @Override
    public void onUserSignInFailed(String message) {
        progressDialog.dismiss();
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}

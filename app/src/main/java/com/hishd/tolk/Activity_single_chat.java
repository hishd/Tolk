package com.hishd.tolk;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.hishd.tolk.ViewAdapters.MessageAdapter;
import com.hishd.tolk.model.Message;
import com.hishd.tolk.util.FirebaseChatOP;
import com.hishd.tolk.util.ImgCompressor;
import com.hishd.tolk.util.ItemOffsetDecoration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Activity_single_chat extends AppCompatActivity implements FirebaseChatOP.OnImageUploadListener, FirebaseChatOP.OnMessageEventListener, MessageAdapter.OnImageClickedListener {

    String opponentEmail;
    String opponentName;
    String opponentImage;
    String userStatus;
    String userEmail;
    String userName;
    String userImage;

    ImageView imgBack;
    ImageView imgProfilePic;
    TextView txtName;
    TextView txtStatus;
    RecyclerView listMessages;
    RelativeLayout layout_empty_chat;
    ImageView imgAttach;
    EditText txtMessage;
    ImageView imgSend;

    FirebaseChatOP firebaseChatOP;
    SharedPreferences sharedPreferences;

    private final int PICK_IMAGE_REQUEST = 1;
    private final int CAPTURE_IMAGE_REQUEST = 2;
    final CharSequence[] options = {"Camera", "Gallery"};
    Uri mImageUri;
    byte[] imageData;

    AlertDialog.Builder imagePickDialog;
    ProgressDialog progressDialog;

    List<Message> messageList;
    MessageAdapter messageAdapter;

    InputMethodManager inputMethods;

    HashMap<String, String> userStatusHashMap;
    HashMap<String,Object> messageInfoHashMapUser;
    HashMap<String,Object> messageInfoHashMapOpponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);

        imgBack = findViewById(R.id.imgBack);
        imgProfilePic = findViewById(R.id.imgProfilePic);
        txtName = findViewById(R.id.txtName);
        txtStatus = findViewById(R.id.txtStatus);
        listMessages = findViewById(R.id.listMessages);
        layout_empty_chat = findViewById(R.id.layout_empty_chat);
        imgAttach = findViewById(R.id.imgAttach);
        txtMessage = findViewById(R.id.txtMessage);
        imgSend = findViewById(R.id.imgSend);

        listMessages.setLayoutManager(new LinearLayoutManager(this));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen._5sdp);
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);
        listMessages.setLayoutAnimation(controller);
        listMessages.addItemDecoration(itemDecoration);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Uploading image. Please wait.");

        firebaseChatOP = new FirebaseChatOP(this);
        sharedPreferences = getSharedPreferences("TOLK", Context.MODE_PRIVATE);

        userEmail = sharedPreferences.getString("EMAIL", "");
        userName = sharedPreferences.getString("NAME", "");
        userImage = sharedPreferences.getString("IMAGE", "");

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, this, userEmail, this);
        listMessages.setAdapter(messageAdapter);

        inputMethods = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        userStatusHashMap = new HashMap<>();
        messageInfoHashMapUser = new HashMap<>();
        messageInfoHashMapOpponent = new HashMap<>();

        if (getIntent().hasExtra("EMAIL"))
            opponentEmail = getIntent().getStringExtra("EMAIL");
        if (getIntent().hasExtra("NAME"))
            opponentName = getIntent().getStringExtra("NAME");
        if (getIntent().hasExtra("IMAGE"))
            opponentImage = getIntent().getStringExtra("IMAGE");

        messageInfoHashMapUser.put("name",userName);
        messageInfoHashMapUser.put("image",userImage);
        messageInfoHashMapUser.put("email",userEmail);

        messageInfoHashMapOpponent.put("name",opponentName);
        messageInfoHashMapOpponent.put("image",opponentImage);
        messageInfoHashMapOpponent.put("email",opponentEmail);

        txtName.setText(opponentName);
        if (!opponentImage.equals("DEFAULT"))
            Glide.with(this).load(opponentImage).dontAnimate().into(imgProfilePic);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
            }
        });

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtMessage.getText().toString().length() < 1) {
                    Toast.makeText(Activity_single_chat.this, "Message body is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                messageInfoHashMapUser.put("latest_timestamp",new Timestamp(new Date()));
                messageInfoHashMapUser.put("latest_message",txtMessage.getText().toString());

                messageInfoHashMapOpponent.put("latest_timestamp",new Timestamp(new Date()));
                messageInfoHashMapOpponent.put("latest_message",txtMessage.getText().toString());

                firebaseChatOP.sendMessage(userName, userEmail, opponentEmail, txtMessage.getText().toString(),messageInfoHashMapUser,messageInfoHashMapOpponent);
                txtMessage.setText("");
                txtMessage.clearFocus();
            }
        });

        txtMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    userStatus = "Typing..";
                else {
                    userStatus = "Online";
                    inputMethods.hideSoftInputFromWindow(txtMessage.getWindowToken(), 0);
                }

                userStatusHashMap.clear();
                userStatusHashMap.put("status", userStatus);
                firebaseChatOP.setChatStatus(userEmail, opponentEmail, userStatusHashMap);
            }
        });

        listMessages.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                listMessages.requestFocus();
                return false;
            }
        });

        firebaseChatOP.watchChatStatus(userEmail, opponentEmail, new FirebaseChatOP.OnOpponentStatusListener() {
            @Override
            public void onOpponentStatusChanged(String status) {
                txtStatus.setText(status);
            }
        });

        firebaseChatOP.fetchMessages(userEmail, opponentEmail, this);
    }

    void showImagePickerDialog() {
        if (imagePickDialog == null) {
            imagePickDialog = new AlertDialog.Builder(this);
            imagePickDialog.setTitle("Choose Image from");
            imagePickDialog.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (options[which].equals("Camera")) {
                        checkStoragePermissionsAndOpenCamera();
                        dialog.dismiss();
                    }
                    if (options[which].equals("Gallery")) {
                        openFileChooser();
                        dialog.dismiss();
                    }
                }
            });
        }
        imagePickDialog.show();
    }

    private void checkStoragePermissionsAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(Activity_single_chat.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Activity_single_chat.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(Activity_single_chat.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                ActivityCompat.requestPermissions(Activity_single_chat.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            openCamera();
        }
    }

    private void openFileChooser() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent m_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
        Uri uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
        m_intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(m_intent, CAPTURE_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
        }
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            //File object of camera image
            File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
            //Uri of camera image
            mImageUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
        }

        if (mImageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
                imageData = ImgCompressor.comPressImageToBitmap(bitmap);
                progressDialog.show();
                firebaseChatOP.sendImage(userName, userEmail, opponentEmail, imageData, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(Activity_single_chat.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                        checkStoragePermissionsAndOpenCamera();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        userStatus = "Offline";
        userStatusHashMap.clear();
        userStatusHashMap.put("status", userStatus);
        firebaseChatOP.setChatStatus(userEmail, opponentEmail, userStatusHashMap);
    }

    @Override
    public void onUploadSuccess() {
        progressDialog.dismiss();
    }

    @Override
    public void onUploadInProgress(int progress) {
        progressDialog.setProgress(progress);
    }

    @Override
    public void onUploadFailed(String message) {
        progressDialog.dismiss();
    }

    @Override
    public void onMessagesLoaded(List<Message> messages) {

        if(messages==null)
            return;

        if (messages.size() > 0)
            layout_empty_chat.setVisibility(View.GONE);

        this.messageList.addAll(messages);
        messageAdapter.notifyDataSetChanged();
        listMessages.scheduleLayoutAnimation();

        listMessages.scrollToPosition(messageList.size() - 1);
    }

    @Override
    public void onMessageAdded(Message message) {

        if(message==null)
            return;

        if(messageList.contains(message))
            return;

        layout_empty_chat.setVisibility(View.GONE);

        messageList.add(message);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        listMessages.smoothScrollToPosition(listMessages.getBottom());
    }

    @Override
    public void onImageClicked(int position) {
        startActivity(new Intent(Activity_single_chat.this, Activity_view_image.class).putExtra("IMAGE_URL", messageList.get(position).getImage_url()));
    }
}

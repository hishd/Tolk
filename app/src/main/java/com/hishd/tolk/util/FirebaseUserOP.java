package com.hishd.tolk.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hishd.tolk.model.User;

import java.util.List;

public class FirebaseUserOP {

    FirebaseFirestore database;
    Context context;
    boolean usersLoaded = false;

    public FirebaseUserOP(Context context) {
        this.context = context;
        database = FirebaseFirestore.getInstance();
    }

    public void registerUser(final String name, final String email, final String password, byte[] imgData, final OnUserSignUpListener onUserSignUpListener) {
        if (imgData == null)
            saveUserInfo(new User(name, email, password, "DEFAULT"), onUserSignUpListener);
        else {
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference("user_images/" + email);
            storageReference.putBytes(imgData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                saveUserInfo(new User(name, email, password, task.getResult().toString()), onUserSignUpListener);
                            } else {
                                Log.e("REGISTER_USER", "Failed to fetch download url for image.");
                                onUserSignUpListener.onUserSignUpFailed("Registration failed! Please Try again.");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("REGISTER_USER", "Failed to fetch download url for image.");
                            onUserSignUpListener.onUserSignUpFailed("Registration failed! Please Try again.");
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("REGISTER_USER", "Failed to save image to firestore.");
                    onUserSignUpListener.onUserSignUpFailed("Registration failed! Please Try again.");
                }
            });
        }
    }

    private void saveUserInfo(User user, final OnUserSignUpListener onUserSignUpListener) {

        database.collection("users").document(user.getEmail()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("REGISTER_USER", "Registration Successful");
                onUserSignUpListener.onUserSignUpSuccessful();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("REGISTER_USER", "Registration Failed");
                onUserSignUpListener.onUserSignUpFailed("Registration failed! Please Try again.");
            }
        });
    }

    public void userLogin(String email, final String password, final OnUserSignInListener onUserSignInListener) {

        database.collection("users").document(email).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                User user = documentSnapshot.toObject(User.class);
                                if (user.getPassword().equals(password))
                                    onUserSignInListener.onUserSignInSuccessful(user);
                                else
                                    onUserSignInListener.onUserSignInFailed("Invalid Email or Password");
                            } else {
                                onUserSignInListener.onUserSignInFailed("User does not exists please register.");
                            }
                        } else {
                            Log.e("SIGNIN_USER", "Signin Failed");
                            onUserSignInListener.onUserSignInFailed("Operation Failed.");
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("SIGNIN_USER", "Signin Cancelled");
                onUserSignInListener.onUserSignInFailed("Operation cancelled.");
            }
        });
    }

    public void loadAllUsers(final OnUserLoadListener onUserLoadListener) {

//        database.collection("users").get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (!queryDocumentSnapshots.isEmpty()) {
//                            List<User> userList = queryDocumentSnapshots.toObjects(User.class);
//                            Log.i("ADDED DOCUMENT","Loaded users");
//                            onUserLoadListener.onUsersLoaded(userList);
//                        }
//                    }
//                })
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                    }
//                });

        database.collection("users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(queryDocumentSnapshots==null)
                            return;

                        if(!usersLoaded){
                            List<User> userList = queryDocumentSnapshots.toObjects(User.class);
                            onUserLoadListener.onUsersLoaded(userList);
                            usersLoaded = true;
                        }else {
                            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {

                                switch (dc.getType()) {
                                    case ADDED:
                                        User user = dc.getDocument().toObject(User.class);
                                        Log.i("ADDED DOCUMENT",user.name);
                                        onUserLoadListener.onNewUserAdded(user);
                                        Log.d("ADDED", "New User: " + dc.getDocument().getData());
                                        break;
                                    case MODIFIED:
                                        Log.d("MODIFIED", "New User: " + dc.getDocument().getData());
                                        break;
                                    case REMOVED:
                                        Log.d("REMOVED", "New User: " + dc.getDocument().getData());
                                        break;
                                }

                            }
                        }
                    }
                });

    }

    public interface OnUserLoadListener {
        void onUsersLoaded(List<User> userList);

        void onNewUserAdded(User newUser);
    }

    public interface OnUserSignUpListener {
        void onUserSignUpSuccessful();

        void onUserSignUpFailed(String message);
    }

    public interface OnUserSignInListener {
        void onUserSignInSuccessful(User user);

        void onUserSignInFailed(String message);
    }

}

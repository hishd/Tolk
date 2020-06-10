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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hishd.tolk.model.Chat;
import com.hishd.tolk.model.Message;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FirebaseChatOP {
    FirebaseFirestore database;
    Context context;

    boolean chatsLoaded = false;
    boolean messagesLoaded = false;

    public FirebaseChatOP(Context context) {
        this.context = context;
        database = FirebaseFirestore.getInstance();
    }

    public void fetchChats(String userEmail, final OnChatsEventListener onChatsEventListener) {
        database.collection("users").document(userEmail).collection("chats").orderBy("latest_timestamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots == null)
                            return;

                        if (!chatsLoaded) {
                            Log.i(Constraints.TAG, "Chats loaded.");
                            List<Chat> chatList = queryDocumentSnapshots.toObjects(Chat.class);
                            Log.i("CHAT LIST SIZE", String.valueOf(chatList.size()));
                            onChatsEventListener.onChatsLoaded(chatList);
                            chatsLoaded = true;
                        } else {
                            Chat chat;
                            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {

                                switch (dc.getType()) {
                                    case ADDED:
                                        chat = dc.getDocument().toObject(Chat.class);
                                        onChatsEventListener.onChatAdded(chat);
                                        Log.d("ADDED", "New Chat: " + dc.getDocument().getData());
                                        break;
                                    case MODIFIED:
                                        chat = dc.getDocument().toObject(Chat.class);
                                        onChatsEventListener.onChatUpdated(chat);
                                        Log.d("MODIFIED", "New Chat: " + dc.getDocument().getData());
                                        break;
                                    case REMOVED:
                                        Log.d("REMOVED", "New Chat: " + dc.getDocument().getData());
                                        break;
                                }
                            }
                        }
                    }
                });
//        database.collection("users").document(userEmail).collection("chats").orderBy("latest_timestamp").get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (!queryDocumentSnapshots.isEmpty()) {
//                            List<Chat> chatList = queryDocumentSnapshots.toObjects(Chat.class);
//                            onChatsEventListener.onChatsLoaded(chatList);
//                        }
//                    }
//                });
    }

    public void fetchMessages(String userEmail, String opponentEmail, final OnMessageEventListener onMessageEventListener) {
        database.collection("users").document(userEmail).collection("chats").document(opponentEmail).collection("messages").orderBy("timestamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots == null){
                            Log.e(Constraints.TAG, "No messages.");
                            return;
                        }

                        Log.i(Constraints.TAG, "Messages Loaded.");
                        if (!messagesLoaded) {
                            List<Message> messageList = queryDocumentSnapshots.toObjects(Message.class);
                            onMessageEventListener.onMessagesLoaded(messageList);
                            messagesLoaded = true;
                        } else {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Message message = doc.getDocument().toObject(Message.class);
                                    onMessageEventListener.onMessageAdded(message);
                                }
                            }
                        }
                    }
                });

//        database.collection("users")
//                .document(userEmail)
//                .collection("chats")
//                .document(opponentEmail)
//                .collection("messages").orderBy("timestamp").get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (!queryDocumentSnapshots.isEmpty()) {
//                            List<Message> messageList = queryDocumentSnapshots.toObjects(Message.class);
//                            onMessageEventListener.onMessagesLoaded(messageList);
//                        }
//                    }
//                });
    }

    public void sendMessage(String userName, String userEmail, String opponentEmail, String message, HashMap<String, Object> messageInfoHashMapUser, HashMap<String, Object> messageInfoHashMapOpponent) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("sender", userName);
        map.put("sender_email", userEmail);
        map.put("message", message);
        map.put("timestamp", new Timestamp(new Date()));
        map.put("image_url", null);

        database.collection("users").document(userEmail).collection("chats").document(opponentEmail).collection("messages").add(map)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        Log.i(Constraints.TAG, "Messages Saved in user path.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(Constraints.TAG, "Failed to save messages in user path. E: " + e.getMessage());
                    }
                });
        database.collection("users").document(opponentEmail).collection("chats").document(userEmail).collection("messages").add(map)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        Log.i(Constraints.TAG, "Messages Saved in opponent path.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(Constraints.TAG, "Failed to save messages in opponent path. E: " + e.getMessage());
                    }
                });

        database.collection("users").document(userEmail).collection("chats").document(opponentEmail).set(messageInfoHashMapOpponent, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i(Constraints.TAG, "Latest messages updated in user path.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(Constraints.TAG, "Latest messages not updated in user path. E: " + e.getMessage());
                    }
                });
        database.collection("users").document(opponentEmail).collection("chats").document(userEmail).set(messageInfoHashMapUser, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i(Constraints.TAG, "Latest messages updated in opponent path.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(Constraints.TAG, "Latest messages not updated in opponent path. E: " + e.getMessage());
                    }
                });
    }

    public void sendImage(final String userName, final String userEmail, final String opponentEmail, byte[] imgData, final OnImageUploadListener onImageUploadListener) {
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_images/" + UUID.randomUUID().toString());
        storageReference.putBytes(imgData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            onImageUploadListener.onUploadSuccess();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("sender", userName);
                            map.put("sender_email", userEmail);
                            map.put("message", null);
                            map.put("timestamp", new Timestamp(new Date()));
                            map.put("image_url", task.getResult().toString());

                            database.collection("users").document(userEmail).collection("chats").document(opponentEmail).collection("messages").add(map)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            Log.i(Constraints.TAG, "Messages Saved in user path.");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(Constraints.TAG, "Failed to save messages in user path. E: " + e.getMessage());
                                        }
                                    });
                            database.collection("users").document(opponentEmail).collection("chats").document(userEmail).collection("messages").add(map)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            Log.i(Constraints.TAG, "Messages Saved in opponent path.");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(Constraints.TAG, "Failed to save messages in opponent path. E: " + e.getMessage());
                                        }
                                    });

                        } else {
                            Log.e(Constraints.TAG, "Failed to receive download URL. E: " + task.getException());
                            onImageUploadListener.onUploadFailed("Failed to Upload Image");
                        }
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(Constraints.TAG, "Image upload in progress");
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                onImageUploadListener.onUploadInProgress((int) progress);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(Constraints.TAG, "Image upload failed. E: " + e.getMessage());
                onImageUploadListener.onUploadFailed("Failed to Upload Image");
            }
        });
    }

    public void watchChatStatus(String userEmail, String opponentEmail, final OnOpponentStatusListener onOpponentStatusListener) {

        database.collection("users").document(userEmail).collection("chats").document(opponentEmail)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if (documentSnapshot == null)
                            return;
                        Log.i(Constraints.TAG, "Opponent status changed");
                        if (documentSnapshot.get("status") != null) {
                            onOpponentStatusListener.onOpponentStatusChanged(documentSnapshot.get("status").toString());
                        } else {
                            onOpponentStatusListener.onOpponentStatusChanged("Offline");
                        }
                    }
                });

    }

    public void setChatStatus(String userEmail, String opponentEmail, HashMap<String, String> status) {
        database.collection("users").document(opponentEmail).collection("chats").document(userEmail).set(status, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i(Constraints.TAG, "User status saved.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(Constraints.TAG, "Failed to save user status. E: " + e.getMessage());
                    }
                });
//        reference.child(opponentEmail).child(userEmail).child("status").setValue(status);
    }

    public interface OnOpponentStatusListener {
        void onOpponentStatusChanged(String status);
    }

    public interface OnImageUploadListener {

        void onUploadSuccess();

        void onUploadInProgress(int progress);

        void onUploadFailed(String message);
    }

    public interface OnMessageEventListener {
        void onMessagesLoaded(List<Message> messages);

        void onMessageAdded(Message message);
    }

    public interface OnChatsEventListener {
        void onChatsLoaded(List<Chat> chatList);

        void onChatAdded(Chat chat);

        void onChatUpdated(Chat chat);
    }

}

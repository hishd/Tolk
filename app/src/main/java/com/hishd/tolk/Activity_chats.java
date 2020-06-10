package com.hishd.tolk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hishd.tolk.ViewAdapters.ChatAdapter;
import com.hishd.tolk.model.Chat;
import com.hishd.tolk.util.FirebaseChatOP;
import com.hishd.tolk.util.ItemOffsetDecoration;

import java.util.ArrayList;
import java.util.List;

public class Activity_chats extends AppCompatActivity implements FirebaseChatOP.OnChatsEventListener, ChatAdapter.OnChatClickListener {

    ImageView imgNewChat;
    RecyclerView listChats;
    RelativeLayout layout_empty_chat;

    FirebaseChatOP firebaseChatOP;
    SharedPreferences sharedPreferences;

    List<Chat> chatList;
    ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        imgNewChat = findViewById(R.id.imgNewChat);
        layout_empty_chat = findViewById(R.id.layout_empty_chat);
        listChats = findViewById(R.id.listChats);
        listChats.setLayoutManager(new LinearLayoutManager(this));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen._5sdp);
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);
        listChats.setLayoutAnimation(controller);
        listChats.addItemDecoration(itemDecoration);

        firebaseChatOP = new FirebaseChatOP(this);
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatList, this);
        listChats.setAdapter(chatAdapter);

        imgNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Activity_chats.this, Activity_users.class));
            }
        });

        sharedPreferences = getSharedPreferences("TOLK", Context.MODE_PRIVATE);

        firebaseChatOP.fetchChats(sharedPreferences.getString("EMAIL", ""), this);
    }

    @Override
    public void onChatsLoaded(List<Chat> chatList) {

        if(chatList==null)
            return;

        if (chatList.size() > 0)
            layout_empty_chat.setVisibility(View.GONE);

        this.chatList.addAll(chatList);
        chatAdapter.notifyDataSetChanged();
        listChats.scheduleLayoutAnimation();
    }

    @Override
    public void onChatAdded(Chat chat) {

        if(chat==null)
            return;

        if(chatList.contains(chat))
            return;

        layout_empty_chat.setVisibility(View.GONE);
        chatList.add(chat);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
    }

    @Override
    public void onChatUpdated(Chat chat) {
        for(Chat c : chatList){
            if(c.getEmail().equals(chat.getEmail())){
                chatList.get(chatList.indexOf(c)).setLatest_message(chat.getLatest_message());
                chatAdapter.notifyItemChanged(chatList.indexOf(c));
            }
        }
    }

    @Override
    public void onChatClicked(int position) {
        startActivity(new Intent(Activity_chats.this, Activity_single_chat.class)
                .putExtra("EMAIL", chatList.get(position).getEmail())
                .putExtra("NAME", chatList.get(position).getName())
                .putExtra("IMAGE", chatList.get(position).getImage()));
    }
}

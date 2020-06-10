package com.hishd.tolk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hishd.tolk.ViewAdapters.UserAdapter;
import com.hishd.tolk.model.User;
import com.hishd.tolk.util.FirebaseUserOP;
import com.hishd.tolk.util.ItemOffsetDecoration;

import java.util.ArrayList;
import java.util.List;

public class Activity_users extends AppCompatActivity implements UserAdapter.OnUserClickedListener, FirebaseUserOP.OnUserLoadListener {

    RecyclerView listUsers;
    List<User> userList;

    FirebaseUserOP firebaseUserOP;
    UserAdapter userAdapter;

    SharedPreferences sharedPreferences;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        listUsers = findViewById(R.id.listUsers);
        listUsers.setLayoutManager(new GridLayoutManager(this, 3));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen._5sdp);
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);
        listUsers.setLayoutAnimation(controller);
        listUsers.addItemDecoration(itemDecoration);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this, this);
        listUsers.setAdapter(userAdapter);

        sharedPreferences = getSharedPreferences("TOLK", Context.MODE_PRIVATE);
        userEmail = sharedPreferences.getString("EMAIL", "");

        firebaseUserOP = new FirebaseUserOP(this);
        firebaseUserOP.loadAllUsers(this);
    }

    @Override
    public void onUserClicked(int position) {
        if(!userList.get(position).getEmail().equals(userEmail))
            startActivity(new Intent(Activity_users.this, Activity_single_chat.class)
                .putExtra("EMAIL", userList.get(position).getEmail())
                .putExtra("NAME", userList.get(position).getName())
                .putExtra("IMAGE", userList.get(position).getImage_url()));
    }

    @Override
    public void onUsersLoaded(List<User> userList) {

        if(userList==null)
            return;

        this.userList.addAll(userList);
        userAdapter.notifyDataSetChanged();
        listUsers.scheduleLayoutAnimation();
    }

    @Override
    public void onNewUserAdded(User newUser) {

        if (newUser == null)
            return;

        if (userList.contains(newUser))
            return;

        userList.add(newUser);
        userAdapter.notifyItemInserted(userList.size() - 1);
    }
}

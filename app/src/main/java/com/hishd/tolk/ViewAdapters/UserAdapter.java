package com.hishd.tolk.ViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hishd.tolk.R;
import com.hishd.tolk.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    List<User> userList;
    Context context;
    User user;
    OnUserClickedListener mOnUserClickedListener;

    public UserAdapter(List<User> userList, Context context, OnUserClickedListener mOnUserClickedListener) {
        this.userList = userList;
        this.context = context;
        this.mOnUserClickedListener = mOnUserClickedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_single_user,parent,false);
        return new ViewHolder(view,mOnUserClickedListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        user = userList.get(position);
        if (!user.getImage_url().equals("DEFAULT"))
            Glide.with(context).load(user.getImage_url()).dontAnimate().into(holder.imgPic);
        holder.txtName.setText(user.getName());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imgPic;
        TextView txtName;
        OnUserClickedListener onUserClickedListener;

        public ViewHolder(@NonNull View itemView, OnUserClickedListener mOnUserClickedListener) {
            super(itemView);
            imgPic = itemView.findViewById(R.id.imgPic);
            txtName = itemView.findViewById(R.id.txtName);
            onUserClickedListener = mOnUserClickedListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnUserClickedListener.onUserClicked(getAdapterPosition());
        }
    }

    public interface OnUserClickedListener{
        void onUserClicked(int position);
    }
}

package com.hishd.tolk.ViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.hishd.tolk.R;
import com.hishd.tolk.model.Chat;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    Context context;
    List<Chat> chatList;
    Chat chat;
    OnChatClickListener mOnChatClickListener;

    public ChatAdapter(Context context, List<Chat> chatList, OnChatClickListener mOnChatClickListener) {
        this.context = context;
        this.chatList = chatList;
        this.mOnChatClickListener = mOnChatClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_single_conversation_item,parent,false);
        return new ViewHolder(view,mOnChatClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        chat = chatList.get(position);

        if(!chat.getImage().equals("DEFAULT"))
            Glide.with(context).load(chat.getImage()).dontAnimate().into(holder.imgProfilePic);
        holder.txtName.setText(chat.getName());
        holder.txtLastMessage.setText(chat.getLatest_message());
        holder.txtSentTime.setReferenceTime(chat.getLatest_timestamp().toDate().getTime());
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView imgProfilePic;
        TextView txtName;
        TextView txtLastMessage;
        RelativeTimeTextView txtSentTime;
        OnChatClickListener onChatClickListener;

        public ViewHolder(@NonNull View itemView, OnChatClickListener mOnChatClickListener) {
            super(itemView);
            this.onChatClickListener = mOnChatClickListener;
            imgProfilePic = itemView.findViewById(R.id.imgProfilePic);
            txtName = itemView.findViewById(R.id.txtName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtSentTime = itemView.findViewById(R.id.txtSentTime);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onChatClickListener.onChatClicked(getAdapterPosition());
        }
    }

    public interface OnChatClickListener{
        void onChatClicked(int position);
    }
}

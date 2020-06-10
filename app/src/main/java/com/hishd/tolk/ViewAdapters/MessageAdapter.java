package com.hishd.tolk.ViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.hishd.tolk.R;
import com.hishd.tolk.model.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    List<Message> messageList;
    Context context;
    Message message;
    OnImageClickedListener mOnImageClickedListener;
    String userEmail;


    int out_margins_left, in_margins_left;
    int root_margins_top;
    int out_margins_right, in_margins_right;
    int root_margins_bottom;

    int in_text_margins_left;
    int in_text_margins_right;
    int out_text_margins_left;
    int out_text_margins_right;
    int text_margins_top, text_margins_bottom;

    LinearLayout.LayoutParams root_out_params, root_in_params, text_out_params, text_in_params;

    public MessageAdapter(List<Message> messageList, Context context, String userEmail, OnImageClickedListener onImageClickedListener) {
        this.messageList = messageList;
        this.context = context;
        this.userEmail = userEmail;
        mOnImageClickedListener = onImageClickedListener;

        in_text_margins_left = (int) context.getResources().getDimension(R.dimen._20sdp);
        in_text_margins_right = (int) context.getResources().getDimension(R.dimen._5sdp);

        out_text_margins_left = (int) context.getResources().getDimension(R.dimen._8sdp);
        out_text_margins_right = (int) context.getResources().getDimension(R.dimen._25sdp);

        text_margins_top = text_margins_bottom = (int) context.getResources().getDimension(R.dimen._2sdp);

        out_margins_left = (context.getResources().getDisplayMetrics().widthPixels) / 3;
        out_margins_right = (int) context.getResources().getDimension(R.dimen._2sdp);

        in_margins_left = (int) context.getResources().getDimension(R.dimen._2sdp);
        in_margins_right = (context.getResources().getDisplayMetrics().widthPixels) / 3;

        root_margins_top = root_margins_bottom = (int) context.getResources().getDimension(R.dimen._5sdp);

        root_out_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        root_in_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        text_out_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        text_in_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        root_out_params.setMargins(out_margins_left, root_margins_top, out_margins_right, root_margins_bottom);
        root_in_params.setMargins(in_margins_left, root_margins_top, in_margins_right, root_margins_bottom);

        text_out_params.setMargins(out_text_margins_left, text_margins_top, out_text_margins_right, text_margins_bottom);
        text_in_params.setMargins(in_text_margins_left, text_margins_top, in_text_margins_right, text_margins_bottom);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_single_chat_item, parent, false);
        return new ViewHolder(view, mOnImageClickedListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        message = messageList.get(position);
        holder.setMessage(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;
        TextView txtBody;
        ImageView imgUploaded;
        RelativeTimeTextView txtSentTime;
        OnImageClickedListener mOnImageClickedListener;

        LinearLayout chat_linear_layout;
        View rootView;

        public ViewHolder(@NonNull View itemView, final OnImageClickedListener mOnImageClickedListener) {
            super(itemView);
            this.mOnImageClickedListener = mOnImageClickedListener;
            txtName = itemView.findViewById(R.id.txtName);
            txtBody = itemView.findViewById(R.id.txtBody);
            imgUploaded = itemView.findViewById(R.id.imgUploaded);
            chat_linear_layout = itemView.findViewById(R.id.chat_linear_layout);
            txtSentTime = itemView.findViewById(R.id.txtSentTime);
            rootView = itemView;

            imgUploaded.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnImageClickedListener.onImageClicked(getAdapterPosition());
                }
            });
        }

        public void setMessage(Message message) {
            if (message.getSender_email().equals(userEmail)) {
                rootView.setLayoutParams(root_out_params);
                txtName.setLayoutParams(text_out_params);
                txtBody.setLayoutParams(text_out_params);
                txtSentTime.setLayoutParams(text_out_params);
                imgUploaded.setLayoutParams(text_out_params);
                rootView.setBackgroundResource(R.drawable.bg_outgoing_message);
                txtSentTime.setReferenceTime(message.getTimestamp().toDate().getTime());
                txtName.setText("You");
            } else {
                rootView.setLayoutParams(root_in_params);
                txtName.setLayoutParams(text_in_params);
                txtBody.setLayoutParams(text_in_params);
                txtSentTime.setLayoutParams(text_in_params);
                imgUploaded.setLayoutParams(text_in_params);
                rootView.setBackgroundResource(R.drawable.bg_incoming_message);
                txtSentTime.setReferenceTime(message.getTimestamp().toDate().getTime());
                txtName.setText(message.getSender());
            }

            if (message.getImage_url() == null) {
                imgUploaded.setVisibility(View.GONE);
                txtBody.setVisibility(View.VISIBLE);
                txtBody.setText(message.getMessage());
            } else {
                imgUploaded.setVisibility(View.VISIBLE);
                txtBody.setVisibility(View.GONE);
                Glide.with(context).load(message.getImage_url()).dontAnimate().into(imgUploaded);
            }
        }
    }

    public interface OnImageClickedListener {
        void onImageClicked(int position);
    }
}

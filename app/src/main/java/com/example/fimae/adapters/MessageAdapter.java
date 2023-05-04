package com.example.fimae.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fimae.R;
import com.example.fimae.models.Message;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {

    ArrayList<Message> msgData;
    Context context;

    final int SENDER_VIEW_HOLDER = 0;
    final int RECEIVER_VIEW_HOLDER = 1;


    public MessageAdapter(Context context, ArrayList<Message> msgData) {

        this.msgData = msgData;
        this.context = context;

    }

    @Override
    public int getItemViewType(int position) {

        if (position % 2 == 0)
            return SENDER_VIEW_HOLDER;
        else
            return RECEIVER_VIEW_HOLDER;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == SENDER_VIEW_HOLDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_message, parent, false);
            return new OutgoingViewholder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.receiver_message, parent, false);
            return new IncomingViewholder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder.getClass() == OutgoingViewholder.class) {
            ((OutgoingViewholder) holder).outgoingMsg.setText(msgData.get(position).getContent());
        } else {

            ((IncomingViewholder) holder).incomingMsg.setText(msgData.get(position).getContent());
        }


    }

    @Override
    public int getItemCount() {
        return msgData.size();
    }

    public static class OutgoingViewholder extends RecyclerView.ViewHolder {
        TextView outgoingMsg;


        public OutgoingViewholder(@NonNull View itemView) {
            super(itemView);
            outgoingMsg = itemView.findViewById(R.id.outgoing_msg);
        }
    }

    public static class IncomingViewholder extends RecyclerView.ViewHolder {

        TextView incomingMsg;

        public IncomingViewholder(@NonNull View itemView) {
            super(itemView);
            incomingMsg = itemView.findViewById(R.id.incoming_msg);
        }
    }

}

package com.example.blindchat.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.blindchat.R;
import com.example.blindchat.model.Message;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> messagesList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private String userID;

    public MessageAdapter(Context context, List<Message> data,
                          String userID) {
        this.mInflater = LayoutInflater.from(context);
        this.messagesList = data;
        this.context = context;
        this.userID = userID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.message_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Message message = messagesList.get(position);
        setMessage(holder, message);
        holder.timeTV.setText(castTime(message.getTime()));
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOnClick(holder);
            }
        });
    }
    private void setMessage(ViewHolder holder, Message message){
        holder.messageTV.setText(message.getContent());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if(message.getSender().equals(userID)){
            holder.messageTV.setBackground(context.getDrawable(R.drawable.special_background_rounded_border));
            holder.parentLayout.setGravity(Gravity.END);
            holder.timeTV.setGravity(Gravity.END);
            params.setMargins(50, 10, 10, 0);
        }else{
            holder.messageTV.setBackground(context.getDrawable(R.drawable.grey_background_rounded_border));
            holder.parentLayout.setGravity(Gravity.START);
            holder.timeTV.setGravity(Gravity.START);
            params.setMargins(10, 10, 50, 0);
        }
        holder.parentLayout.setLayoutParams(params);


    }
    private String castTime(long messageTime){
        long currentTime = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(messageTime);
        int messageDay = c.get(Calendar.DAY_OF_MONTH);
        int messageMonth = c.get(Calendar.MONTH);
        int messageYear = c.get(Calendar.YEAR);
        c.setTimeInMillis(currentTime);
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        int currentMonth = c.get(Calendar.MONTH);
        int currentYear = c.get(Calendar.YEAR);
        if(messageYear == currentYear){
            if((messageDay==currentDay)&&(messageMonth==currentMonth)){
                return new SimpleDateFormat("HH:mm").format(new Date(messageTime));
            }else{
                return new SimpleDateFormat("dd MMM. HH:mm").format(new Date(messageTime));
            }
        }else{
            return new SimpleDateFormat("dd MMM. yyyy HH:mm").format(new Date(messageTime));

        }
    }
    private void setOnClick(ViewHolder holder){
        if(holder.timeTV.getVisibility()==View.GONE){
            holder.timeTV.setVisibility(View.VISIBLE);
        }else{
            holder.timeTV.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout parentLayout;
        private TextView messageTV, timeTV;
        private View itemView;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            findViewsByIds();

            itemView.setOnClickListener(this);

        }
        void findViewsByIds(){
            parentLayout = itemView.findViewById(R.id.parentLayout);
            messageTV = itemView.findViewById(R.id.messageTV);
            timeTV = itemView.findViewById(R.id.timeTV);
        }


        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());

        }
    }


    Message getItem(int id) {
        return messagesList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
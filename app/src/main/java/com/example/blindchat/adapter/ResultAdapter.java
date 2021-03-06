package com.example.blindchat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blindchat.R;
import com.example.blindchat.StaticClass;
import com.example.blindchat.activity.core.ProfileActivity;
import com.example.blindchat.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private List<User> usersList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    public ResultAdapter(Context context, List<User> data) {
        this.mInflater = LayoutInflater.from(context);
        this.usersList = data;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.result_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final User user = usersList.get(position);
        setProfilePhoto(holder, user);
        setProfileName(holder, user);
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.getApplicationContext().startActivity(new Intent(context, ProfileActivity.class)
                        .putExtra(StaticClass.PROFILE_ID, user.getId())
                        .setFlags(FLAG_ACTIVITY_NEW_TASK));
            }
        });
    }
    private void setProfilePhoto(final ViewHolder holder, User user){
        final long ONE_MEGABYTE = 1024 * 1024;
        holder.storage.getReference(user.getId() + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.profilePhotoIV.setImageBitmap(
                        Bitmap.createScaledBitmap(bmp, holder.profilePhotoIV.getWidth(),
                                holder.profilePhotoIV.getHeight(), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Failed at getting profile photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setProfileName(final ViewHolder holder, User user){
        holder.database.collection("users")
                .document(user.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            holder.profileUsernameTV.setText("@"+document.get("username"));
                            holder.profileNameTV.setText(String.valueOf(document.get("name")));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed at getting profile name", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView profilePhotoIV;
        private TextView profileUsernameTV, profileNameTV;
        private LinearLayout parentLayout;
        private View itemView;
        private FirebaseStorage storage;
        private FirebaseFirestore database;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            getInstances();
            findViewsByIds();
            itemView.setOnClickListener(this);
        }
        void getInstances(){
            database = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();
        }
        void findViewsByIds(){
            parentLayout = itemView.findViewById(R.id.parentLayout);
            profileUsernameTV = itemView.findViewById(R.id.profileUsernameTV);
            profileNameTV = itemView.findViewById(R.id.profileNameTV);
            profilePhotoIV = itemView.findViewById(R.id.profilePhotoIV);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }


    User getItem(int id) {
        return usersList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    /*public void filter(String queryText) {
        chatsList.clear();
        if(queryText.isEmpty()) {
            chatsList.addAll(copyList);
        }else{
            for(Chat chat: copyList) {
                if(chat.getInterlocutor().getName().toLowerCase().contains(queryText.toLowerCase())) {
                    chatsList.add(chat);
                }
            }
        }
        notifyDataSetChanged();
    }*/
}
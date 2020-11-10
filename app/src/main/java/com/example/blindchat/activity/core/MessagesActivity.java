package com.example.blindchat.activity.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.blindchat.R;
import com.example.blindchat.StaticClass;
import com.example.blindchat.adapter.MessageAdapter;
import com.example.blindchat.model.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessagesActivity extends AppCompatActivity {

    private ImageView interlocutorPhotoIV;
    private TextView interlocutorUsernameTV, interlocutorNameTV, textET, seenTV;
    private RecyclerView messagesRV;
    private MessageAdapter adapter;
    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayList<String> interlocutors = new ArrayList<>();
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private DocumentReference messageReference;
    private Map<String, Object> messageMap = new HashMap<>(), chatMap = new HashMap<>();
    private Message message = new Message();
    private String email, interlocutorID, from, chatReference,
            content, emailRead, interlocutorRead, interlocutorName, reacherName;
    private boolean isNewChat=true, isSeenListenerSet, isEmailReacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getInstances();
        findViewsByIds();
        getChatReference();
        setMessagesRV();
    }
    private void getInstances(){
        interlocutorID = getIntent().getStringExtra(StaticClass.PROFILE_ID);
        interlocutorRead = interlocutorID.replace(".", "-")+"-read";
        from = getIntent().getStringExtra(StaticClass.FROM);
        email = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE).getString(StaticClass.EMAIL, "no email");
        emailRead = email.replace(".", "-")+"-read";
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        interlocutors.add(email);
        interlocutors.add(interlocutorID);
    }
    private void findViewsByIds(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageView backIV = toolbar.findViewById(R.id.backIV);
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        textET = findViewById(R.id.textET);
        ImageView sendTextIV = findViewById(R.id.sendTextIV);
        sendTextIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content = textET.getText().toString().trim();
                if (!content.isEmpty()) send();
            }
        });
        interlocutorPhotoIV = toolbar.findViewById(R.id.interlocutorPhotoIV);
        interlocutorPhotoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile();
            }
        });
        interlocutorUsernameTV = toolbar.findViewById(R.id.interlocutorUsernameTV);
        interlocutorUsernameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile();
            }
        });
        interlocutorNameTV = toolbar.findViewById(R.id.interlocutorNameTV);
        interlocutorNameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile();
            }
        });
        messagesRV = findViewById(R.id.messagesRV);
        seenTV = findViewById(R.id.seenTV);
    }
    private void setInterlocutorPhoto(){
        if(isEmailReacher) {
            final long ONE_MEGABYTE = 1024 * 1024;
            storage.getReference(interlocutorID + StaticClass.PROFILE_PHOTO)
                    .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    interlocutorPhotoIV.setImageBitmap(Bitmap.createScaledBitmap(bmp, interlocutorPhotoIV.getWidth(),
                            interlocutorPhotoIV.getHeight(), false));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "Failed at getting interlocutor profile photo", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    @SuppressLint("SetTextI18n")
    private void setInterlocutorName(){
        if(isEmailReacher) {
            database.collection("users")
                    .document(interlocutorID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(DocumentSnapshot document) {
                            if (document.exists()) {
                                interlocutorUsernameTV.setText("@" + document.get("username"));
                                interlocutorName = String.valueOf(document.get("name"));
                                interlocutorNameTV.setText(interlocutorName);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed at getting interlocutor profile name", Toast.LENGTH_LONG).show();
                        }
                    });
        }else{
            interlocutorUsernameTV.setText("@private");
            interlocutorNameTV.setText(reacherName);
        }
    }
    private void setMessagesRV(){
        adapter = new MessageAdapter(getApplicationContext(), messages, email);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, true);
        llm.setStackFromEnd(true);
        messagesRV.setLayoutManager(llm);
        messagesRV.setAdapter(adapter);
    }
    private void getChatReference(){
        database.collection("chats")
                .whereIn("interlocutors", Arrays.asList(Arrays.asList(email, interlocutorID), Arrays.asList(interlocutorID, email)))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                if (document.exists()) {
                                    chatReference = document.getId();
                                    reacherName = String.valueOf(document.get("reacher-name"));
                                    String reacher = String.valueOf(document.get("reacher"));
                                    isEmailReacher = reacher.equals(email);
                                    getMessages();
                                    if(!isSeenListenerSet) setSeenListener();
                                }
                            }
                        }
                        setInterlocutorPhoto();
                        setInterlocutorName();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed at getting chat reference", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void getMessages(){
        database.collection("messages")
                .whereEqualTo("chat", chatReference)
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                            Log.i("INDEX", error.getMessage());
                        }else{
                            messages.clear();
                            for(QueryDocumentSnapshot document: value) {
                                if (document != null && document.exists()) {
                                    messages.add(new Message(
                                            document.getId(),
                                            String.valueOf(document.get("content")),
                                            String.valueOf(document.get("sender")),
                                            (long) document.get("time")
                                    ));
                                    adapter.notifyDataSetChanged();
                                    messagesRV.smoothScrollToPosition(0);
                                    setRead();
                                }
                                if(isNewChat) isNewChat=false;
                            }
                        }
                    }
                });
    }
    private void setSeenListener(){
        isSeenListenerSet = true;
        database.collection("chats")
                .document(chatReference)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                            Log.i("INDEX", error.getMessage());
                        }else{
                            if(document!=null && document.exists()){
                                boolean seen = (boolean) document.get(interlocutorRead);
                                String sender = String.valueOf(document.get("sender"));
                                if(seen && sender.equals(email)){
                                    seenTV.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                });
    }
    private void setRead(){
        database.collection("chats")
                .document(chatReference)
                .update(emailRead, true);

    }
    private void setMessage(){
        message.setContent(content);
        message.setSender(email);
        message.setTime(System.currentTimeMillis());
    }
    private void putMessageMap(){
        messageMap.put("content", message.getContent());
        messageMap.put("sender", message.getSender());
        messageMap.put("receiver", interlocutorID);
        messageMap.put("chat", chatReference);
        messageMap.put("time", message.getTime());
    }
    private void send(){
        if(isNewChat){
            createNewChat();
            Toast.makeText(getApplicationContext(),
                    "New Chat",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        setMessage();
        putMessageMap();
        messageReference = database.collection("messages").document();
        messageReference.set(messageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        textET.setText("");
                        seenTV.setVisibility(View.GONE);
                        updateChatDB();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "Error sending message",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void putChatMap(){
        chatMap.put("last-message-content", message.getContent());
        chatMap.put("sender", message.getSender());
        chatMap.put("messages", messageReference.getId());
        chatMap.put("last-message-time", message.getTime());
        chatMap.put(emailRead, true);
        chatMap.put(interlocutorRead, false);
    }
    private void updateChatDB(){
        putChatMap();
        database.collection("chats")
                .document(chatReference)
                .update(chatMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "Error updating chatDB",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void createNewChat(){
        chatMap.put("interlocutors", interlocutors);
        chatMap.put("reacher", email);
        chatMap.put("reacher-name", setReacherName());
        final DocumentReference newDoc = database.collection("chats").document();
        newDoc.set(chatMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        chatReference = newDoc.getId();
                        getMessages();
                        database.collection("users")
                                .document(email)
                                .update("chats", FieldValue.arrayUnion(chatReference));
                        database.collection("users")
                                .document(interlocutorID)
                                .update("chats", FieldValue.arrayUnion(chatReference));
                        isNewChat = false;
                        send();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "Error updating chatDB",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @SuppressLint("SimpleDateFormat")
    private String setReacherName(){
        return "Discussion "+new SimpleDateFormat("dd MMM. yyyy HH:mm").format(new Date(System.currentTimeMillis()));
    }
    private void openProfile(){
        startActivity(new Intent(getApplicationContext(), ProfileActivity.class)
                .putExtra(StaticClass.PROFILE_ID, interlocutorID));
    }
    @Override
    public void onBackPressed() {
        if(from.equals(StaticClass.PROFILE_ACTIVITY)) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class)
                    .putExtra(StaticClass.PROFILE_ID, interlocutorID));
        }else if(from.equals(StaticClass.CHATS_FRAGMENT)) {
            startActivity(new Intent(getApplicationContext(), CoreActivity.class)
                    .putExtra(StaticClass.PROFILE_ID, interlocutorID)
                    .putExtra(StaticClass.TO, StaticClass.CHATS_FRAGMENT));
        }
    }
}

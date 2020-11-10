package com.example.blindchat.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blindchat.R;
import com.example.blindchat.StaticClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ImageView photoIV;
    private TextView usernameTV, nameTV, bioTV;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private String profileID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getInstances();
        findViewsByIds();
        getProfilePhoto();
        setProfileData();
    }

    private void getInstances(){
        profileID = getIntent().getStringExtra(StaticClass.PROFILE_ID);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }
    private void findViewsByIds(){
        photoIV = findViewById(R.id.photoIV);
        usernameTV = findViewById(R.id.usernameTV);
        nameTV = findViewById(R.id.nameTV);
        bioTV = findViewById(R.id.bioTV);
    }
    private void getProfilePhoto(){
        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        storage.getReference(profileID + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setBytesToProfilePhoto(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Failed at getting profile photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setBytesToProfilePhoto(byte[] bytes){
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        photoIV.setImageBitmap(Bitmap.createScaledBitmap(bmp, photoIV.getWidth(),
                photoIV.getHeight(), false));
    }
    private void setProfileData(){
        database.collection("users")
                .document(profileID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            usernameTV.setText(String.valueOf(document.get("username")));
                            String name = String.valueOf(document.get("name"));
                            nameTV.setText(name);
                            setActionBarTitle(name);
                            bioTV.setText(String.valueOf(document.get("bio")));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed at setting profile date", Toast.LENGTH_LONG).show();
                    }
                });
    }
    public void message(View view){
        startActivity(new Intent(getApplicationContext(), MessagesActivity.class)
                .putExtra(StaticClass.PROFILE_ID, profileID)
                .putExtra(StaticClass.FROM, StaticClass.PROFILE_ACTIVITY));
    }
    public void setActionBarTitle(String title){
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(
                Html.fromHtml("<font color=\"#ffffff\"> "+title+" </font>")
        );
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), CoreActivity.class));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }
}

package com.example.blindchat.activity.core.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blindchat.R;
import com.example.blindchat.StaticClass;
import com.example.blindchat.adapter.ResultAdapter;
import com.example.blindchat.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class SearchFragment extends Fragment {

    private View fragmentView;
    private Context context;
    private TextView noResultsTV;
    private ProgressBar progressBar;
    private RecyclerView resultsRV;
    private ResultAdapter adapter;
    private ArrayList<User> usersList = new ArrayList<>();
    private FirebaseFirestore database;
    private String email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_search, container, false);
        context = Objects.requireNonNull(getActivity()).getApplicationContext();
        database = FirebaseFirestore.getInstance();
        email = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE).getString(StaticClass.EMAIL, "no email");
        findViewsByIds();
        setResultRV();
        return fragmentView;
    }

    private void findViewsByIds(){
        progressBar = fragmentView.findViewById(R.id.progressBar);
        noResultsTV = fragmentView.findViewById(R.id.noResultsTV);
        resultsRV = fragmentView.findViewById(R.id.resultRV);
        EditText searchET = fragmentView.findViewById(R.id.searchET);
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usersList.clear();
                adapter.notifyDataSetChanged();
                if(count>0){
                    progressBar.setVisibility(View.VISIBLE);
                    search(String.valueOf(s));
                    checkIfNoResults();
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }@Override public void afterTextChanged(Editable s) {}
        });
    }
    private void setResultRV(){
        adapter = new ResultAdapter(context, usersList);
        resultsRV.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false));
        resultsRV.setAdapter(adapter);
    }
    private void search(String input){
        database.collection("users")
                .whereGreaterThanOrEqualTo("name", input.toUpperCase())
                .whereLessThanOrEqualTo("name", input.toLowerCase()+"\uF8FF")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot document: queryDocumentSnapshots.getDocuments()){
                            if(document.getId().equals(email)) continue;
                            usersList.add(new User(
                                    document.getId(),
                                    String.valueOf(document.get("username")),
                                    String.valueOf(document.get("name"))
                            ));
                        }
                        adapter.notifyDataSetChanged();
                        noResultsTV.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed at getting results", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void checkIfNoResults(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(usersList.isEmpty()){
                    noResultsTV.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, 1000);
    }
}



package com.korimart.f12;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostsViewModel extends ViewModel {
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private MutableLiveData<List<PostsFragment.Post>> posts = new MutableLiveData<>();
    private boolean firstOpen = true;

    public void onViewCreated(){
        if (!firstOpen) return;

        firstOpen = false;

        ref.child("suggestionsSummary").limitToLast(20).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<PostsFragment.Post> posts = new ArrayList<>();
                for (DataSnapshot dss : snapshot.getChildren())
                    posts.add(dss.getValue(PostsFragment.Post.class));
                PostsViewModel.this.posts.setValue(posts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public LiveData<List<PostsFragment.Post>> getPosts() {
        return posts;
    }
}

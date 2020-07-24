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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PostsViewModel extends ViewModel {
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private MutableLiveData<List<PostsFragment.PostSummary>> posts = new MutableLiveData<>();
    private boolean firstOpen = true;

    public void onViewCreated(){
        if (!firstOpen) return;

        firstOpen = false;
        fetchPosts();
    }

    public void fetchPosts(){
        ref.child("suggestionsSummary").limitToLast(20).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<PostsFragment.PostSummary> postSummaries = new ArrayList<>();
                for (DataSnapshot dss : snapshot.getChildren()){
                    PostsFragment.PostSummary sum = dss.getValue(PostsFragment.PostSummary.class);
                    sum.key = dss.getKey();
                    postSummaries.add(sum);
                }

                Collections.sort(postSummaries,
                        Collections.reverseOrder((o1, o2) -> Long.compare(o1.timeStamp, o2.timeStamp)));

                PostsViewModel.this.posts.setValue(postSummaries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public LiveData<List<PostsFragment.PostSummary>> getPosts() {
        return posts;
    }
}

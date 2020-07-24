package com.korimart.f12;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PostBodyViewModel extends ViewModel {
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private MutableLiveData<WritePostFragment.PostContent> postContent = new MutableLiveData<>();
    private MutableLiveData<PostsFragment.PostSummary> postSummary = new MutableLiveData<>();

    public void onViewCreated(String postKey){
        fetch(postKey);
    }

    public void fetch(String postKey){
        ref.child("suggestionsSummary").child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PostsFragment.PostSummary sum = snapshot.getValue(PostsFragment.PostSummary.class);
                postSummary.setValue(sum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        ref.child("suggestionsContent").child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                WritePostFragment.PostContent post = snapshot.getValue(WritePostFragment.PostContent.class);
                postContent.setValue(post);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void clear(){
        postContent.setValue(null);
    }

    public LiveData<WritePostFragment.PostContent> getPostContent() {
        return postContent;
    }

    public LiveData<PostsFragment.PostSummary> getPostSummary() {
        return postSummary;
    }
}

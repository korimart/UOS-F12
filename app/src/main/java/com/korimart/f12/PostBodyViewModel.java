package com.korimart.f12;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
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

public class PostBodyViewModel extends ViewModel {
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private MutableLiveData<PostContentDownload> postContent = new MutableLiveData<>();

    public static class PostContentDownload extends WritePostFragment.PostContentUpload {
        List<Pair<String, String>> comments = new ArrayList<>();
    }

    public void onViewCreated(String postKey){
        ref.child("suggestionsContent").child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PostContentDownload post = snapshot.getValue(PostContentDownload.class);
                for (DataSnapshot comment : snapshot.child("comments").getChildren()){
                    for (DataSnapshot content : comment.getChildren()){
                        post.comments.add(new Pair<>(content.getKey(), content.getValue(String.class)));
                    }
                }
                postContent.setValue(post);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public LiveData<PostContentDownload> getPostContent() {
        return postContent;
    }
}

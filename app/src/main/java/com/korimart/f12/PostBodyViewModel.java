package com.korimart.f12;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PostBodyViewModel extends ViewModel {
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private MutableLiveData<PostsFragment.PostSummary> postSummary = new MutableLiveData<>();
    private MutableLiveData<String> body = new MutableLiveData<>();
    private MutableLiveData<List<CommentInfo>> comments = new MutableLiveData<>();

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

        ref.child("suggestionsContent").child(postKey).child("body").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                body.setValue(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        ref.child("suggestionsContent").child(postKey).child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CommentInfo> comments = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()){
                    comments.add(new CommentInfo(
                            (long) child.child("number").getValue(),
                            (String) child.child("comment").getValue(),
                            (long) child.child("timeStamp").getValue()
                    ));
                }

                Collections.sort(comments, (o1, o2) -> Long.compare(o1.timeStamp, o2.timeStamp));
                PostBodyViewModel.this.comments.setValue(comments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    static class CommentInfo {
        long number;
        String comment;
        long timeStamp;

        CommentInfo(long number, String comment, long timeStamp){
            this.number = number;
            this.comment = comment;
            this.timeStamp = timeStamp;
        }
    }

    public void clear(){
        body.setValue(null);
        comments.setValue(null);
        postSummary.setValue(null);
    }

    public LiveData<PostsFragment.PostSummary> getPostSummary() {
        return postSummary;
    }

    public LiveData<String> getBody() {
        return body;
    }

    public LiveData<List<CommentInfo>> getComments() {
        return comments;
    }
}

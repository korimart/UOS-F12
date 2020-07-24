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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostsViewModel extends ViewModel {
    private static final int defaultFetchNum = 1;

    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private MutableLiveData<List<PostsFragment.PostSummary>> posts = new MutableLiveData<>();
    private boolean firstOpen = true;
    private long latestTimeStamp;
    private long oldestTimeStamp;
    private boolean noMorePosts = false;

    public PostsViewModel(){
        posts.setValue(new ArrayList<>());
    }

    public void onViewCreated(){
        if (!firstOpen) return;

        fetchPosts(true);
        firstOpen = false;
    }

    public void fetchPosts(boolean fetchLatest){
        Query query = ref.child("suggestionsSummary").orderByChild("timeStamp");

        if (fetchLatest)
            if (firstOpen)
                query = query.limitToLast(defaultFetchNum);
            else
                query = query.startAt(latestTimeStamp + 1);
        else {
            if (noMorePosts) return;
            query = query.endAt(oldestTimeStamp - 1).limitToLast(defaultFetchNum);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PostsFragment.PostSummary> postSummaries = posts.getValue();

                if (!fetchLatest && snapshot.getChildrenCount() < defaultFetchNum)
                    noMorePosts = true;

                for (DataSnapshot dss : snapshot.getChildren()){
                    PostsFragment.PostSummary sum = dss.getValue(PostsFragment.PostSummary.class);
                    sum.key = dss.getKey();
                    postSummaries.add(sum);
                }

                Collections.sort(postSummaries,
                        Collections.reverseOrder((o1, o2) -> Long.compare(o1.timeStamp, o2.timeStamp)));

                latestTimeStamp = postSummaries.get(0).timeStamp;
                oldestTimeStamp = postSummaries.get(postSummaries.size() - 1).timeStamp;
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

    public boolean isNoMorePosts() {
        return noMorePosts;
    }
}

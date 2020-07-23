package com.korimart.f12;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

public class PostsFragment extends Fragment {
    private MainActivity mainActivity;
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private RecyclerView posts;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        posts = view.findViewById(R.id.posts);
        posts.setLayoutManager(new LinearLayoutManager(getContext()));
        posts.setAdapter(new PostsAdapter());
        DividerItemDecoration did = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        posts.addItemDecoration(did);
    }

    @IgnoreExtraProperties
    public class Post {
        public String title;
        public String body;
    }

    class PostsAdapter extends RecyclerView.Adapter<PostsViewHolder> {
        @NonNull
        @Override
        public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_post, parent, false);

            view.setOnClickListener(v -> {
                mainActivity.goToPostBodyFrag();
            });

            return new PostsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostsViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 10;
        }
    }

    class PostsViewHolder extends RecyclerView.ViewHolder {
        PostsViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

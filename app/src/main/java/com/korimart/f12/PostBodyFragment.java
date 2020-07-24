package com.korimart.f12;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PostBodyFragment extends Fragment {
    private RecyclerView recycler;
    private PostBodyAdapter adapter;

    private int position;
    private PostsFragment.PostSummary postSummary;

    private PostsViewModel postsViewModel;
    private PostBodyViewModel postBodyViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_body, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider vmp = new ViewModelProvider(getActivity(), new ViewModelProvider.NewInstanceFactory());
        postsViewModel = vmp.get(PostsViewModel.class);
        postBodyViewModel = vmp.get(PostBodyViewModel.class);

        position = getArguments().getInt("position");
        postSummary = postsViewModel.getPosts().getValue().get(position);

        recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostBodyAdapter();
        recycler.setAdapter(adapter);
        DividerItemDecoration did = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recycler.addItemDecoration(did);

        postBodyViewModel.getPostContent().observe(this, content -> adapter.notifyDataSetChanged());

        postBodyViewModel.onViewCreated(postSummary.key);
    }

    class PostBodyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater li = LayoutInflater.from(getContext());
            View view;
            RecyclerView.ViewHolder viewHolder;

            switch (viewType){
                case 0:
                    view = li.inflate(R.layout.item_post_body, parent, false);
                    viewHolder = new PostsFragment.PostsViewHolder(view);
                    break;

                default:
                    view = li.inflate(R.layout.item_post_body_comment, parent, false);
                    viewHolder = new CommentViewHolder(view);
                    break;
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            PostBodyViewModel.PostContentDownload content = postBodyViewModel.getPostContent().getValue();
            switch (holder.getItemViewType()){
                case 0:
                    PostsFragment.PostsViewHolder post = (PostsFragment.PostsViewHolder) holder;
                    post.setMembers(
                            postSummary.title,
                            content.body,
                            postSummary.timeStamp,
                            postSummary.thumbsUp,
                            postSummary.comments);
                    break;

                default:
                    CommentViewHolder commentViewHolder = (CommentViewHolder) holder;
                    Pair<String, String> nameCommentPair = content.comments.get(position - 1);
                    commentViewHolder.setMembers(nameCommentPair.first, nameCommentPair.second);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            PostBodyViewModel.PostContentDownload post = postBodyViewModel.getPostContent().getValue();
            return post == null ? 0 : post.comments.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView comment;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            comment = itemView.findViewById(R.id.comment);
        }

        void setMembers(String nameNumber, String comment){
            name.setText("어둠의 와이저 " + nameNumber);
            this.comment.setText(comment);
        }
    }
}

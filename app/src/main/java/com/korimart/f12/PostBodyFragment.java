package com.korimart.f12;

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

public class PostBodyFragment extends Fragment {
    RecyclerView recycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_body, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(new PostBodyAdapter());
        DividerItemDecoration did = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recycler.addItemDecoration(did);
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
                    viewHolder = new BodyViewHolder(view);
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

        }

        @Override
        public int getItemCount() {
            return 10;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }

    class BodyViewHolder extends RecyclerView.ViewHolder {
        public BodyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

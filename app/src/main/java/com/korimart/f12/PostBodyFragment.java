package com.korimart.f12;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PostBodyFragment extends Fragment {
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private MainActivity mainActivity;

    private RecyclerView recycler;
    private PostBodyAdapter adapter;
    private EditText writeComment;
    private Button submit;

    private String guid;
    private String postKey;
    private Long myNumber;

    private PostBodyViewModel postBodyViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_body, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        guid = getActivity().getPreferences(Context.MODE_PRIVATE).getString("guid", null);
        if (guid == null){
            mainActivity.goToErrorFrag(new Exception("post body fragment but guid is null"));
            return;
        }

        ViewModelProvider vmp = new ViewModelProvider(getActivity(), new ViewModelProvider.NewInstanceFactory());
        postBodyViewModel = vmp.get(PostBodyViewModel.class);

        postKey = getArguments().getString("postKey");

        recycler = view.findViewById(R.id.recycler);
        writeComment = view.findViewById(R.id.writeComment);
        submit = view.findViewById(R.id.submit);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostBodyAdapter();
        recycler.setAdapter(adapter);
        DividerItemDecoration did = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recycler.addItemDecoration(did);

        submit.setOnClickListener(v -> {
            if (writeComment.getText().toString().isEmpty()){
                Toast.makeText(getContext(), "댓글을 입력하세요", Toast.LENGTH_LONG).show();
                return;
            }

            sendComment(writeComment.getText().toString());
            writeComment.setText("");
        });

        postBodyViewModel.getPostContent().observe(this, content -> {
            if (content == null) return;

            myNumber = content.mappings.get(guid);
            adapter.notifyDataSetChanged();
        });

        postBodyViewModel.onViewCreated(postKey);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        postBodyViewModel.clear();
    }


    private void sendThumbsUp() {
        dbRef.child("suggestionsContent").child(postKey).child("likers").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                HashMap<String, Object> likers = (HashMap<String, Object>) currentData.getValue();
                if (likers == null)
                    likers = new HashMap<>();

                if (likers.containsKey(guid))
                    return Transaction.abort();

                likers.put(guid, true);
                currentData.setValue(likers);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed){
                    dbRef.child("suggestionsSummary").child(postKey).child("thumbsUp")
                            .setValue(ServerValue.increment(1), (error1, ref) -> {
                        if (error1 == null)
                            postBodyViewModel.fetch(postKey);
                    });
                }
                else
                    Toast.makeText(mainActivity, "이미 따봉한 글입니다", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendComment(String comment) {
        dbRef.child("suggestionsContent").child(postKey).child("mappings").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                HashMap<String, Object> mappings = (HashMap<String, Object>) currentData.getValue();
                if (mappings == null)
                    mappings = new HashMap<>();

                if (myNumber == null){
                    int number;
                    if (mappings.isEmpty())
                        number = 1;
                    else {
                        number = 0;
                        for (Object o : mappings.values())
                            number = Math.max(number, (Integer) o);
                        number++;
                    }
                    mappings.put(guid, number);
                }
                else
                    return Transaction.success(currentData);


                currentData.setValue(mappings);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error == null){
                    myNumber = ((HashMap<String, Long>) currentData.getValue()).get(guid);
                    String commentKey = dbRef.child("suggestionsContent").child(postKey).child("comments").push().getKey();

                    Map<String, Object> update = new HashMap<>();
                    update.put("suggestionsContent/" + postKey + "/comments/" + commentKey + "/comment", comment);
                    update.put("suggestionsContent/" + postKey + "/comments/" + commentKey + "/number", myNumber);
                    update.put("suggestionsSummary/" + postKey + "/comments", ServerValue.increment(1));
                    dbRef.updateChildren(update, (error1, ref) -> {
                        if (error1 == null)
                            postBodyViewModel.fetch(postKey);
                    });
                }
            }
        });
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
                    viewHolder = new PostViewHolder(view);
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
            WritePostFragment.PostContent content = postBodyViewModel.getPostContent().getValue();
            switch (holder.getItemViewType()){
                case 0:
                    PostViewHolder post = (PostViewHolder) holder;
                    PostsFragment.PostSummary postSummary = postBodyViewModel.getPostSummary().getValue();
                    post.setMembers(
                            postSummary.title,
                            content.body,
                            postSummary.timeStamp,
                            postSummary.thumbsUp,
                            postSummary.comments);
                    break;

                default:
                    CommentViewHolder commentViewHolder = (CommentViewHolder) holder;
                    Object temp = content.comments.values().toArray()[position - 1];
                    HashMap<String, Object> comment = (HashMap<String, Object>) temp;
                    commentViewHolder.setMembers(
                            Long.toString((Long) comment.get("number")),
                            (String) comment.get("comment"));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            PostsFragment.PostSummary postSummary = postBodyViewModel.getPostSummary().getValue();
            if (postSummary == null) return 0;

            WritePostFragment.PostContent post = postBodyViewModel.getPostContent().getValue();
            return post == null ? 0 : post.comments.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }

    class PostViewHolder extends PostsFragment.PostsViewHolder {
        ImageView thumb;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.thumbsUpButton);
            thumb.setOnClickListener(v -> sendThumbsUp());
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
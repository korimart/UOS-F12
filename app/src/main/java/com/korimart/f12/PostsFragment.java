package com.korimart.f12;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PostsFragment extends Fragment {
    private MainActivity mainActivity;
    private PostsViewModel postsViewModel;

    private RecyclerView posts;
    private PostsAdapter postsAdapter;
    private Button write;

    private String guid;

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

        ViewModelProvider vmp = new ViewModelProvider(mainActivity, new ViewModelProvider.NewInstanceFactory());
        postsViewModel = vmp.get(PostsViewModel.class);

        posts = view.findViewById(R.id.postSummaries);
        write = view.findViewById(R.id.write);

        guid = getOrCreateGuid();

        posts.setLayoutManager(new LinearLayoutManager(getContext()));
        postsAdapter = new PostsAdapter();
        posts.setAdapter(postsAdapter);
        DividerItemDecoration did = new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
        posts.addItemDecoration(did);
        posts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE)
                    postsViewModel.fetchPosts(false);
                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE)
                    postsViewModel.fetchPosts(true);
            }
        });

        write.setOnClickListener(v -> mainActivity.goToWritePostFrag(() -> mainActivity.goToPostsFrag()));

        postsViewModel.getPosts().observe(this, list -> postsAdapter.notifyDataSetChanged());

        postsViewModel.onViewCreated();
    }

    private String getOrCreateGuid(){
        SharedPreferences prefs = mainActivity.getPreferences(Context.MODE_PRIVATE);
        String guid = prefs.getString("guid", null);
        if (guid == null){
            guid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("guid", guid);
            editor.apply();
        }

        return guid;
    }

    @IgnoreExtraProperties
    public static class PostSummary {
        public String key;
        public String user;
        public String title;
        public String body;
        public long timeStamp;
        public int thumbsUp;
        public int comments;

        public PostSummary(){}
    }

    class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 1){
                View view = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_post_system_message, parent, false);
                return new SystemMessageViewHolder(view);
            }

            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_post, parent, false);

            List<PostSummary> postSummaries = postsViewModel.getPosts().getValue();

            view.setOnClickListener(v -> {
                RecyclerView.ViewHolder viewHolder = posts.getChildViewHolder(v);
                int position = viewHolder.getAdapterPosition();

                String postKey = postSummaries.get(position).key;
                mainActivity.goToPostBodyFrag(() -> mainActivity.goToPostsFrag(), postKey);
            });

            return new PostsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            List<PostSummary> postSummaries = postsViewModel.getPosts().getValue();

            if (position < postSummaries.size()){
                PostSummary postSummary = postSummaries.get(position);
                ((PostsViewHolder) holder).setMembers(
                        postSummary.title,
                        postSummary.body,
                        postSummary.timeStamp,
                        postSummary.thumbsUp,
                        postSummary.comments);
            }
            else if (!postsViewModel.isNoMorePosts()) {
                ((SystemMessageViewHolder) holder).systemMessage.setText("가져오는 중이에염");
            }
            else {
                ((SystemMessageViewHolder) holder).systemMessage.setText("더 이상 가져올 글이 없습니다");
            }
        }

        @Override
        public int getItemCount() {
            List<PostSummary> postSummaries = postsViewModel.getPosts().getValue();
            return postSummaries == null ? 0 : postSummaries.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < getItemCount() - 1)
                return 0;
            return 1;
        }
    }

    static class SystemMessageViewHolder extends RecyclerView.ViewHolder {
        TextView systemMessage;

        public SystemMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            systemMessage = itemView.findViewById(R.id.systemMessage);
        }
    }

    static class PostsViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView body;
        TextView time;
        TextView thumbsUp;
        TextView comments;

        PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            body = itemView.findViewById(R.id.body);
            time = itemView.findViewById(R.id.time);
            thumbsUp = itemView.findViewById(R.id.thumbsUp);
            comments = itemView.findViewById(R.id.comments);
        }

        void setMembers(String title, String body, long timeStamp, int thumbsUp, int comments){
            this.title.setText(title);
            this.body.setText(body);
            this.thumbsUp.setText("따봉 " + thumbsUp);
            this.comments.setText("댓글 " + comments);

            long currTime = System.currentTimeMillis();
            long diff = (currTime - timeStamp) / 1000;

            String formattedTime;
            if (diff < 60)
                formattedTime = diff + "초전";
            else if (diff < 3600)
                formattedTime = diff / 60 + "분전";
            else if (diff < 86400)
                formattedTime = diff / 3600 + "시간전";
            else {
                Date dateThen = new Date(timeStamp);
                LocalDate then = dateThen.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate now = LocalDate.now();

                DateFormat df;
                if (then.getYear() == now.getYear()){
                    df = new SimpleDateFormat("MM월 dd일");
                }
                else
                    df = new SimpleDateFormat("yyyy년 MM월 dd일");

                formattedTime = df.format(dateThen);
            }

            this.time.setText(formattedTime);
        }
    }
}

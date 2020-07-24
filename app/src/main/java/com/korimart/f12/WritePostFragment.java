package com.korimart.f12;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

public class WritePostFragment extends Fragment {
    private Button submit;
    private EditText title;
    private EditText body;

    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private MainActivity mainActivity;
    private PostsViewModel postsViewModel;

    private String guid;

    @IgnoreExtraProperties
    public static class PostContent {
        public String user;
        public String body;
        public Map<String, Boolean> likers = new HashMap<>();
        public Map<String, Integer> mappings = new HashMap<>();
        public Map<String, Object> comments = new HashMap<>();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_write_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider vmp = new ViewModelProvider(mainActivity, new ViewModelProvider.NewInstanceFactory());
        postsViewModel = vmp.get(PostsViewModel.class);

        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        guid = prefs.getString("guid", null);
        if (guid == null){
            mainActivity.goToErrorFrag(new Exception("write post fragment but guid is null"));
            return;
        }

        title = view.findViewById(R.id.title);
        body = view.findViewById(R.id.body);
        submit = view.findViewById(R.id.submit);

        submit.setOnClickListener(v -> {
            String title = this.title.getText().toString();
            if (title.length() > 20){
                Toast.makeText(getContext(), "제목이 너무 깁니다", Toast.LENGTH_LONG).show();
                return;
            }

            if (title.isEmpty()){
                Toast.makeText(getContext(), "제목을 입력하세요", Toast.LENGTH_LONG).show();
                return;
            }

            if (this.body.getText().toString().isEmpty()){
                Toast.makeText(getContext(), "내용을 입력하세요", Toast.LENGTH_LONG).show();
                return;
            }

            writePost(title, this.body.getText().toString());
            mainActivity.goToPostsFrag();
        });
    }

    private void writePost(String title, String body){
        String key = dbRef.child("suggestionsSummary").push().getKey();
        PostContent postContent = new PostContent();
        postContent.user = guid;
        postContent.body = body;

        PostsFragment.PostSummary postSummary = new PostsFragment.PostSummary();
        postSummary.title = title;
        postSummary.body = body.substring(0, Math.min(20, body.length()));
        postSummary.user = guid;
        postSummary.timeStamp = System.currentTimeMillis();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/suggestionsContent/" + key, postContent);
        childUpdates.put("/suggestionsSummary/" + key, postSummary);

        dbRef.updateChildren(childUpdates, (error, ref) -> postsViewModel.fetchPosts());
    }
}

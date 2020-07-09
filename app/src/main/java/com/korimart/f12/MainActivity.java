package com.korimart.f12;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Stack;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {
    private F12Fragment gf;
    private BottomNavigationView bottomNav;
    private MainViewModel mainViewModel;
    private F12ViewModel f12ViewModel;
    private Stack<Runnable> fragStack = new Stack<>();
    private boolean firstFetch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory())
                .get(MainViewModel.class);
        f12ViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory())
                .get(F12ViewModel.class);
        bottomNav = findViewById(R.id.main_bottomNav);

        setViewListeners();

        if (savedInstanceState == null)
            goToLoginFrag();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainViewModel.fetchNoPnp(this);
        mainViewModel.fetchUpdateInfo(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainViewModel.writeNoPnp(this);
    }

    private void setViewListeners() {
        mainViewModel.getUpdateLink().observe(this, s -> {
            // TODO: add notification
        });

        bottomNav.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (fragStack.empty()){
            super.onBackPressed();
            return;
        }

        Runnable goBackFunc = fragStack.pop();
        goBackFunc.run();
    }

    public void goToF12Frag() {
        if (gf == null){
            gf = new F12Fragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, gf)
                .commit();
    }

    public void goToErrorFrag(String errorString) {
        fragStack.clear();
        ErrorFragment ef = new ErrorFragment();
        Bundle args = new Bundle();
        args.putString("errorString", errorString);
        ef.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, ef)
                .commit();
    }

    public void goToLoginFrag() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new LoginFragment())
                .commit();
    }

    public void goToOriginalXMLFrag(Runnable howToGoBack) {
        fragStack.add(howToGoBack);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new OriginalFragment())
                .commit();
    }

    public void goToHelpFrag() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new HelpFragment())
                .commit();
    }

    public void goToCoursesFrag(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new CoursesFragment())
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        fragStack.clear();

        switch (item.getItemId()){
            case R.id.bottomNav_F12:
                goToF12Frag();
                break;

            case R.id.bottomNav_help:
                goToHelpFrag();
                break;

            case R.id.bottomNav_courses:
                goToCoursesFrag();
                break;
        }
        return true;
    }

    public boolean isFirstFetch() {
        return firstFetch;
    }

    public void setFirstFetch(boolean firstFetch) {
        this.firstFetch = firstFetch;
    }
}

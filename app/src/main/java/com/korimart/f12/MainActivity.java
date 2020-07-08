package com.korimart.f12;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {
    private F12Fragment gf;
    private boolean isAddedFrag;
    private BottomNavigationView bottomNav;
    private MainViewModel mainViewModel;
    private F12ViewModel f12ViewModel;
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
        mainViewModel.fetchNoPnp(this);
        mainViewModel.fetchUpdateInfo(this);
        goToLoginFrag();
    }

    private void setViewListeners() {
        mainViewModel.getUpdateLink().observe(this, s -> {
            // TODO: add notification
        });

        bottomNav.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (isAddedFrag){
            isAddedFrag = false;
            goToGradesFrag();
            return;
        }

        super.onBackPressed();
    }

    public void goToGradesFrag() {
        if (gf == null){
            gf = new F12Fragment(mainViewModel, f12ViewModel);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, gf)
                .commit();
    }

    public void goToErrorFrag(String errorString) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new ErrorFragment(errorString))
                .commit();
    }

    public void goToLoginFrag() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new LoginFragment())
                .commit();
    }

    public void goToOriginalXMLFrag(String originalXML) {
        isAddedFrag = true;
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frag, new OriginalFragment(originalXML))
                .commit();
    }

    public void goToHelpFrag() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new HelpFragment(mainViewModel))
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.bottomNav_F12:
                goToGradesFrag();
                break;

            case R.id.bottomNav_help:
                goToHelpFrag();
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

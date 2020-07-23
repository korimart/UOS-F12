package com.korimart.f12;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Stack;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {
    private F12Fragment gf;
    private BottomNavigationView bottomNav;
    private MainViewModel mainViewModel;
    private Stack<Runnable> fragStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewModelProvider vmp = new ViewModelProvider(this,
                new ViewModelProvider.NewInstanceFactory());
        mainViewModel = vmp.get(MainViewModel.class);
        bottomNav = findViewById(R.id.main_bottomNav);

        setViewListeners();

        if (savedInstanceState == null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag, new LoginFragment())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainViewModel.fetchGithub(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof AutoCompleteTextView) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void setViewListeners() {
        mainViewModel.getUpdateLink().observe(this, s -> {
            if (s == null) return;

            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.bottomNav_help);
            badge.setVisible(true);
            badge.setNumber(1);
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

    public void goToErrorFrag(Throwable throwable) {
        fragStack.clear();
        ErrorReporter.INSTANCE.reportError(throwable);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new ErrorFragment())
                .commit();
    }

    /**
     * @param onLogin 0 f12 1 courses
     */
    public void goToLoginFrag(int onLogin) {
        getViewModelStore().clear();
        LoginFragment lf = new LoginFragment();
        Bundle args = new Bundle();
        args.putInt("onLogin", onLogin);
        lf.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, lf)
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

    public void goToCoursesFrag(boolean major){
        CourseListFragment clf = new CourseListFragment();
        Bundle args = new Bundle();
        args.putBoolean("major", major);
        clf.setArguments(args);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, clf)
                .commit();
    }

    public void goToCoursesFilterFrag(Runnable howToGoBack, boolean major){
        fragStack.add(howToGoBack);

        Fragment filterFragment = major ? new MajorsFilterFragment() : new CoresFilterFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, filterFragment)
                .commit();
    }

    public void goToCourseDescFrag(Runnable howToGoBack, int position, String timePlace, boolean major) {
        fragStack.add(howToGoBack);
        SyllabusFragment cdf = new SyllabusFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("timePlace", timePlace);
        args.putBoolean("major", major);
        cdf.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, cdf)
                .commit();
    }

    public void goToPostsFrag(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new PostsFragment())
                .commit();
    }

    public void goToPostBodyFrag(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new PostBodyFragment())
                .commit();
    }

    public void goToWritePostFrag(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new WritePostFragment())
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

            case R.id.bottomNav_majors:
                goToCoursesFrag(true);
                break;

            case R.id.bottomNav_cores:
                goToCoursesFrag(false);
                break;

            case R.id.bottomNav_posts:
                goToPostsFrag();
                break;
        }
        return true;
    }

    public BottomNavigationView getBottomNav() {
        return bottomNav;
    }
}

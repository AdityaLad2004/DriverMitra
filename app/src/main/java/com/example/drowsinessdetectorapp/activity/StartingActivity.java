package com.example.drowsinessdetectorapp.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.drowsinessdetectorapp.R;
import com.example.drowsinessdetectorapp.fragment.FactsFragment;
import com.example.drowsinessdetectorapp.fragment.HomeFragment;
import com.google.android.material.navigation.NavigationView;

public class StartingActivity extends AppCompatActivity {
    private static final String TAG = "StartingActivity";

    private NavigationView startingViewNavigation;
    private CoordinatorLayout startingCoordinatorLayout;

    private DrawerLayout startingDrawerLayout;

    private MenuItem previousMenuItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);
        Log.i(TAG,"Created");

        initializeViews();
        openHomeFragment();

        startingViewNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                if (previousMenuItem != null)
                    previousMenuItem.setChecked(false);

                menuItem.setChecked(true);
                menuItem.setCheckable(true);

                previousMenuItem = menuItem;

                switch (menuItem.getItemId()) {
                    case R.id.home_fragment :
                        Log.i(TAG,"Opening Home-Fragment");

                        addFragmentToFrame(new HomeFragment());
                        break;
                    case R.id.facts_fragment:
                        Log.i(TAG,"Opening Facts-Fragment");

                        addFragmentToFrame(new FactsFragment());
                        break;


                }

                return true;
            }
        });

    }

    private void addFragmentToFrame(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.startingFrameLayout,newFragment)
                .commit();

        startingDrawerLayout.closeDrawers();
    }

    private void openHomeFragment() {
        Log.i(TAG,"Opening Home-Fragment");

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.startingFrameLayout, new HomeFragment())
                .commit();

        startingViewNavigation.setCheckedItem(R.id.home_fragment);
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.startingFrameLayout);
        if (!(currentFragment instanceof HomeFragment)) {
            openHomeFragment();
        } else {
            super.onBackPressed();
        }
    }

    private void initializeViews() {
        Log.i(TAG,"Views are Initialized");

        startingDrawerLayout = (DrawerLayout) findViewById(R.id.startingDrawerLayout);
        startingCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.startingCoordinatorLayout);
        //startingFrameLayout = (FrameLayout) findViewById(R.id.startingFrameLayout);
        startingViewNavigation = (NavigationView) findViewById(R.id.startingViewNavigation);
    }

}
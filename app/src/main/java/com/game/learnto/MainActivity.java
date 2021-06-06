package com.game.learnto;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    ActionBar actionBar;
    ViewPager viewPager;
    LinearLayout linearLayout;
    TextView[] dostsTv;
    int[] layouts;
    Button mNextBtn, mSkipBtn;
    SliderAdapter sliderAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (!isFirstTimeAppStart()){
            setAppStartStatus(false);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();

        }
        viewPager = findViewById(R.id.ViewPager);
        linearLayout = findViewById(R.id.dotsLayout);
        mNextBtn = findViewById(R.id.btn_next);
        mSkipBtn = findViewById(R.id.btn_skip);

        statusBarTransparent();
        mSkipBtn.setOnClickListener(v -> {
            setAppStartStatus(false);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        mNextBtn.setOnClickListener(v -> {
            int currentPage = viewPager.getCurrentItem() + 1;
            if (currentPage < layouts.length) {
                viewPager.setCurrentItem(currentPage);
            } else {
                setAppStartStatus(false);
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }

        });

        layouts = new int[]{R.layout.slider_1, R.layout.slider_2, R.layout.slider_3, R.layout.slider_3};
        sliderAdapter = new SliderAdapter(layouts, getApplicationContext());
        viewPager.setAdapter(sliderAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == layouts.length - 1) {
                    mNextBtn.setText("START");
                    mSkipBtn.setVisibility(View.GONE);
                } else {
                    mNextBtn.setText("NEXT");
                    mSkipBtn.setVisibility(View.VISIBLE);
                }
                setDots(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setDots(0);
    }

    private boolean isFirstTimeAppStart(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("DONE_APP", Context.MODE_PRIVATE);
        return pref.getBoolean("APP_START",true);

    }
    private void setAppStartStatus( boolean status){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("DONE_APP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("APP_START",status);
        editor.apply();
    }

    private void statusBarTransparent() {

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN);
            //getWindow().setDecorFitsSystemWindows(false);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void setDots(int page) {
        linearLayout.removeAllViews();
        dostsTv = new TextView[layouts.length];
        for (int i = 0; i < dostsTv.length; i++) {
            dostsTv[i] = new TextView(this);
            dostsTv[i].setText(Html.fromHtml("&#8226;",Context.MODE_PRIVATE));
            dostsTv[i].setTextSize(30);
            dostsTv[i].setTextColor(Color.parseColor("#a9b5bb"));
            linearLayout.addView(dostsTv[i]);
        }

        if (dostsTv.length > 0) {

            dostsTv[page].setTextColor(Color.parseColor("#ffffff"));
        }

    }
}
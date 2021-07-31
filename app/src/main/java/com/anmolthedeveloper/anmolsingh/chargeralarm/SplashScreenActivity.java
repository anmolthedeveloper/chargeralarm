package com.anmolthedeveloper.anmolsingh.chargeralarm;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class SplashScreenActivity extends AppCompatActivity {

    TextView textView;
    Animation animation;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        textView = findViewById(R.id.textView3);


        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        // for Activity full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1);
        animation.setStartOffset(5000);
        textView.setAnimation(animation);
        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);
        handler = new Handler();


    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.startAnimation(animation);
            }
        },500);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },2500);
    }
}

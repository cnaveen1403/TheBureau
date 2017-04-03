package com.bureau.bureauapp;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.profilesetup.AccountCreation;
import com.bureau.bureauapp.profilesetup.LegalStatus;
import com.bureau.bureauapp.profilesetup.Occupation;
import com.bureau.bureauapp.profilesetup.ProfileHeritage;
import com.bureau.bureauapp.profilesetup.ProfileInfo;
import com.bureau.bureauapp.profilesetup.SocialHabit;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    Timer timer;
    MyTimerTask myTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ImageView imageView = (ImageView) findViewById(R.id.myanimation);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        final AnimationDrawable myAnimationDrawable
                = (AnimationDrawable) imageView.getDrawable();

        imageView.post(
                new Runnable() {
                    @Override
                    public void run() {
                        myAnimationDrawable.start();
                    }
                });

        int duration = 0;
        for (int i = 0; i < myAnimationDrawable.getNumberOfFrames(); i++) {
            duration += myAnimationDrawable.getDuration(i);
        }

        timer = new Timer();
        myTimerTask = new MyTimerTask();
        timer.schedule(myTimerTask, duration);
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            timer.cancel();

            Intent intent;
            if (AppData.getString(SplashScreen.this, BureauConstants.userid) != null && AppData.getString(SplashScreen.this, BureauConstants.profileStatus) != null) {

                if (AppData.getString(SplashScreen.this, BureauConstants.profileStatus).equalsIgnoreCase("completed")) {
                    intent = new Intent(getBaseContext(), HomeActivity.class);
                    intent.putExtra("pager_position", 0);
                } else {
                    String profileStatus = AppData.getString(SplashScreen.this, BureauConstants.profileStatus);
//                    Log.e("Bereau","profileStatus: "+profileStatus);

                    if (profileStatus.equals("create_account_ws")) {
                        intent = new Intent(SplashScreen.this, AccountCreation.class);
                    } else if (profileStatus.equals("update_profile_step2")) {
                        intent = new Intent(SplashScreen.this, ProfileInfo.class);
                    } else if (profileStatus.equals("update_profile_step3")) {
                        intent = new Intent(SplashScreen.this, ProfileHeritage.class);
                    } else if (profileStatus.equals("update_profile_step4")) {
                        intent = new Intent(SplashScreen.this, SocialHabit.class);
                    } else if (profileStatus.equals("update_profile_step5")) {
                        intent = new Intent(SplashScreen.this, Occupation.class);
                    } else if (profileStatus.equals("update_profile_step6")) {
                        intent = new Intent(SplashScreen.this, LegalStatus.class);
                    } else {
                        intent = new Intent(getBaseContext(), HomeActivity.class);
                        intent.putExtra("pager_position", 0);
                    }
                }
            } else {
                intent = new Intent(getBaseContext(), LoginSignup.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
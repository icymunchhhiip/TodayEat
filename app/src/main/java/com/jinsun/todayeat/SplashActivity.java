package com.jinsun.todayeat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jinsun.todayeat.databinding.ActivitySplashBinding;
import com.kakao.auth.AuthType;
import com.kakao.auth.Session;

import static com.jinsun.todayeat.Constants.SPLASH_DELAY_MILLIS;


public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding mActivitySplashBinding;
    private View mView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivitySplashBinding =
                ActivitySplashBinding.inflate(getLayoutInflater());
        mView =mActivitySplashBinding.getRoot();
        setContentView(mView);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Session session =Session.getCurrentSession();
            if(session.checkAndImplicitOpen()){
                session.open(AuthType.KAKAO_LOGIN_ALL, SplashActivity.this);
            }
            Intent intent = new Intent(mView.getContext(),MainActivity.class);
            startActivity(intent);
        },SPLASH_DELAY_MILLIS);
    }

}

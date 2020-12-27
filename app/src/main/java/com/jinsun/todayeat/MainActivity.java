package com.jinsun.todayeat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jinsun.todayeat.databinding.ActivityMainBinding;
import com.jinsun.todayeat.databinding.DialogLoginBinding;
import com.jinsun.todayeat.login.SessionCallback;
import com.jinsun.todayeat.pager.PagerAdapter;
import com.kakao.auth.AuthType;
import com.kakao.auth.Session;

import static com.jinsun.todayeat.Constants.PAGER_EXCLUSION_LIST;
import static com.jinsun.todayeat.Constants.PAGER_SEARCH;

public class MainActivity extends AppCompatActivity {

    public static long sMyId = -1;
    private ActivityMainBinding mActivityMainBinding;
    private View mView;
    private MenuItem mMenuItemPrev;
    private SessionCallback sessionCallback;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        mView = mActivityMainBinding.getRoot();
        setContentView(mView);

        sessionCallback = new SessionCallback(mView.getContext());
        session = Session.getCurrentSession();
        session.addCallback(sessionCallback);

        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mActivityMainBinding.pager.setAdapter(pagerAdapter);
        mActivityMainBinding.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mMenuItemPrev != null) {
                    mMenuItemPrev.setChecked(false);
                }

                mActivityMainBinding.bottomBar.getMenu().getItem(position).setChecked(true);
                mMenuItemPrev = mActivityMainBinding.bottomBar.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mActivityMainBinding.bottomBar.setOnNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.search) {
                mActivityMainBinding.pager.setCurrentItem(PAGER_SEARCH);
            } else if (id == R.id.exclusion_list) {
                if (MainActivity.sMyId != -1) {
                    mActivityMainBinding.pager.setCurrentItem(PAGER_EXCLUSION_LIST);
                } else {
                    getLoginDialog();
                }
            }
            return false;
        });
    }

    private void getLoginDialog() {
        // Creating the AlertDialog with a custom xml layout (you can still use the default Android version)
        AlertDialog.Builder builder = new AlertDialog.Builder(mView.getContext());

        LayoutInflater inflater = (LayoutInflater) mView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DialogLoginBinding dialogLoginBinding = DialogLoginBinding.inflate(inflater);
        View view = dialogLoginBinding.getRoot();
        builder.setView(view);

        TextView title = new TextView(mView.getContext());
        // You Can Customise your Title here
        title.setText(getString(R.string.login_need));
        title.setBackgroundColor(Color.WHITE);
        title.setPadding(30, 30, 30, 30);
        title.setGravity(Gravity.CENTER);
        title.setFontFeatureSettings("roboto_regular");
        title.setTextColor(mView.getContext().getColor(R.color.colorFontDark));
        title.setTextSize(12);

        builder.setCustomTitle(title);
        builder.setCancelable(true);
        final AlertDialog dialog = builder.create();

        dialog.show();

        dialogLoginBinding.btCancel.setOnClickListener(view12 -> dialog.dismiss());
        dialogLoginBinding.ibLoginKakao.setOnClickListener(view1 -> {
            session.open(AuthType.KAKAO_LOGIN_ALL, MainActivity.this);
            dialog.dismiss();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityMainBinding = null;
    }
}
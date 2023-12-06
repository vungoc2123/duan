package com.example.duan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.duan.fragment.OrderFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import com.example.duan.fragment.DoanhThuFragment;
import com.example.duan.fragment.FeedBackFragment;
import com.example.duan.fragment.MonAnFragment;
import com.example.duan.fragment.OrderFragment;
import com.example.duan.fragment.TableFragment;
import com.example.duan.fragment.Top10DishFragment;
import com.example.duan.fragment.Top10UserFragment;
import com.example.duan.fragment.UserFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private FrameLayout frameLayout;
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Bundle loginbBundle = getIntent().getBundleExtra("bundle");
        if (loginbBundle != null)
            userId = loginbBundle.getString("userId");

        Log.e("TAG", "onCreate: " + userId );
        getToken();
        navigationView = findViewById(R.id.navigationView);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolBar);
        frameLayout = findViewById(R.id.frameLayout);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(Color.WHITE);
        navigationView.setNavigationItemSelectedListener(this);
        replaceFragment(new OrderFragment(this));
        this.setTitle(R.string.nav_order);
        navigationView.getMenu().getItem(0).setChecked(true);

        SpannableString s = new SpannableString("Đăng xuất");
        s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
        navigationView.getMenu().getItem(6).getSubMenu().getItem(0).setTitle(s);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.dangXuat:
                String phone = "";
                String password = "";
                boolean status = false;
                List<Object> chkList;
                chkList = readPreference();
                if (chkList.size()>0) {
                    if (Boolean.parseBoolean(chkList.get(3).toString())) {
                        phone = chkList.get(1).toString();
                        password = chkList.get(2).toString();
                        status = Boolean.parseBoolean(chkList.get(3).toString());
                    }
                }
                savePreference(userId,phone,password,status);
                Intent intent = new Intent(MainActivity.this, com.example.duan.LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.quanLyFeedback:
                replaceFragment(new FeedBackFragment());
                this.setTitle(R.string.nav_feedback);
                break;
            case R.id.quanLyUser:
                replaceFragment(new UserFragment());
                this.setTitle(R.string.nav_user);
                break;
            case R.id.quanLyOrder:
                replaceFragment(new OrderFragment(MainActivity.this));
                this.setTitle(R.string.nav_order);
                break;
            case R.id.quanLyDish:
                replaceFragment(new MonAnFragment());
                this.setTitle(R.string.nav_dish);
                break;
            case R.id.top10User:
                replaceFragment(new Top10UserFragment());
                this.setTitle(R.string.nav_top10_user);
                break;
            case R.id.top10Dish:r:
            replaceFragment(new Top10DishFragment());
                this.setTitle(R.string.nav_top10_dish);
                break;
            case R.id.doanhThu:
                replaceFragment(new DoanhThuFragment());
                this.setTitle(R.string.nav_doanhThu);
                break;
            case R.id.quanLyTable:
                replaceFragment(new TableFragment());
                this.setTitle(R.string.nav_table);
                break;
        }
        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            super.onBackPressed(); // Thoát
        }
    }

    void savePreference(String userId, String phone, String pw, boolean status) {
        SharedPreferences s = getSharedPreferences("MY_FILE",MODE_PRIVATE);
        SharedPreferences.Editor editor = s.edit();
        if (!status) { // Khong luu
            editor.clear();
        } else { // luu
            editor.putString("userId",userId);
            editor.putString("phone",phone);
            editor.putString("password",pw);
            editor.putBoolean("CHK",status);
        }
        editor.commit();
    }

    List<Object> readPreference() {
        List<Object> ls = new ArrayList<>();
        SharedPreferences s = getSharedPreferences("MY_FILE", Context.MODE_PRIVATE);
        ls.add(s.getString("userId",""));
        ls.add(s.getString("phone",""));
        ls.add(s.getString("password",""));
        ls.add(s.getBoolean("CHK",false));
        return ls;
    }

    public void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();

                    // Log and toast
                    Log.e("TAG", "getToken: " + token );
                    FirebaseDatabase.getInstance().getReference("users").child(userId+"/token").setValue(token);
                });
    }
}
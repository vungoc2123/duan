package com.example.duan;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.example.duan.model.User;

public class LoginActivity extends AppCompatActivity {
    EditText txtMaTT, txtPassword;
    Button btnLogin;
    CheckBox chkRemember;
    String userId;
    List<User> users = new ArrayList<>();
    boolean check = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        txtMaTT = findViewById(R.id.loginTxtMaTT);
        txtPassword = findViewById(R.id.loginTxtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        chkRemember = findViewById(R.id.chkRemember);
        List<Object> chkList;
        chkList = readPreference();
        Log.e("TAG", "onCreate: " + users.size());
        if (chkList.size() > 0) {
            if (Boolean.parseBoolean(chkList.get(3).toString())) {
                txtMaTT.setText(chkList.get(1).toString());
                txtPassword.setText(chkList.get(2).toString());
                chkRemember.setChecked(Boolean.parseBoolean(chkList.get(3).toString()));
            }
        }

        btnLogin.setOnClickListener(v -> login());
    }


    void login() {
        String phone = txtMaTT.getText().toString();
        String pw = txtPassword.getText().toString();
        boolean status = chkRemember.isChecked();
        if (phone.length() == 0 || pw.length() == 0) {
            openFailDialog("Không để trống thông tin");
        } else {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
            myRef.child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        User user = snapshot1.getValue(User.class);
                        if (phone.equals(user.getPhone()) && pw.equals(user.getPassword()) && user.getRole() == 0) {
                            check = true;
                            userId = user.getId();
                            break;
                        }
                    }
                    if (check) {
                        savePreference(userId, phone, pw, status);
//                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("userId", userId);
                        intent.putExtra("bundle", bundle);
                        startActivity(intent);
                        finish();
                        System.exit(0);
//                        return;
                    } else
                        openFailDialog("Thông tin đăng nhập không chính xác");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        }
    }

    void savePreference(String userId, String phone, String pw, boolean status) {
        SharedPreferences s = getSharedPreferences("MY_FILE", MODE_PRIVATE);
        SharedPreferences.Editor editor = s.edit();
        if (!status) { // Khong luu
            editor.clear();
        } else { // luu
            editor.putString("userId", userId);
            editor.putString("phone", phone);
            editor.putString("password", pw);
            editor.putBoolean("CHK", status);
        }
        editor.commit();
    }

    List<Object> readPreference() {
        List<Object> ls = new ArrayList<>();
        SharedPreferences s = getSharedPreferences("MY_FILE", MODE_PRIVATE);
        ls.add(s.getString("userId", ""));
        ls.add(s.getString("phone", ""));
        ls.add(s.getString("password", ""));
        ls.add(s.getBoolean("CHK", false));
        return ls;
    }

//    public void openSuccessDialog(String text) {
//        Dialog dialog = new Dialog(this);
//
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.dialog_success_notification);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//        TextView tvNotifyContent = dialog.findViewById(R.id.tvNotifyContent);
//        tvNotifyContent.setText(text);
//        dialog.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
//
//        });
//        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        dialog.setCancelable(true);
//        dialog.show();
//    }

    public void openFailDialog(String text) {
        Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fail_notification);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvNotifyContent = dialog.findViewById(R.id.tvNotifyContent);
        tvNotifyContent.setText(text);
        dialog.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();
    }
}
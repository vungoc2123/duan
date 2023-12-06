package com.example.duan.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.example.duan.R;
import com.example.duan.adapter.UserAdapter;
import com.example.duan.model.User;

public class UserFragment extends Fragment {
    RecyclerView lvThanhVien;
    List<User> list;
    UserAdapter adapter;
    FloatingActionButton fab;
    EditText edtTenTv, edtPhone, edtPassword ,edtSearch_user;
    Button btnSave, btnCancel;
    RadioButton rdoRoleAdmin, rdoRoleClient;
    RadioGroup rdoRole;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user, container, false);
        edtSearch_user = v.findViewById(R.id.edtSearch_user);
        lvThanhVien = v.findViewById(R.id.lvUser);
        fab = v.findViewById(R.id.fabTV);
        capNhatLv();
        edtSearch_user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(edtSearch_user.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        fab.setOnClickListener(v1 -> {
            openDialog(0);//bang = thi insert
        });
        return v;
    }

    void capNhatLv() {
        list = new ArrayList<>();
        adapter = new UserAdapter(getActivity(), R.layout.item_user);
        adapter.setData(list);
        lvThanhVien.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        lvThanhVien.setAdapter(adapter);
        getListUser();
    }


    protected void openDialog(final int type) {
        Dialog dialog = new Dialog(requireActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_user);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.58);
        dialog.getWindow().setLayout(width, height);

        TextView dialogUserTitle = dialog.findViewById(R.id.dialogUserTitle);
        dialogUserTitle.setText("Thêm User");

        edtTenTv = dialog.findViewById(R.id.edtTenTV);
        edtPhone = dialog.findViewById(R.id.edtPhone);
        edtPassword = dialog.findViewById(R.id.edtPassword);
        rdoRoleAdmin = dialog.findViewById(R.id.rdoRoleAdmin);
        rdoRoleClient = dialog.findViewById(R.id.rdoRoleClient);
        rdoRole = dialog.findViewById(R.id.rdoRole);
        btnCancel = dialog.findViewById(R.id.btnCancelTV);
        btnSave = dialog.findViewById(R.id.btnSaveTV);



        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users");
            if (validate() > 0) {
                //==- thi them nguoi dung
                User item = new User();
                item.setId("user" + (Calendar.getInstance().getTimeInMillis()));
                item.setName(edtTenTv.getText().toString());
                item.setPhone(edtPhone.getText().toString());
                item.setPassword(edtPassword.getText().toString());
                if (rdoRoleAdmin.isChecked()) {
                    item.setRole(0);
                } else if (rdoRoleClient.isChecked()) {
                    item.setRole(1);
                }
                String pathObject = String.valueOf(item.getId());
                myRef.child(pathObject).setValue(item, (error, ref) -> openSuccessDialog("Thêm người dùng thành công"));
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public int validate() {
        int check = 1;
        if (edtTenTv.getText().length() == 0 || edtPhone.getText().length() == 0 || edtPassword.getText().length() == 0) {
            openFailDialog("Không để trống thông tin");
            check = -1;
        }

        if (!rdoRoleClient.isChecked() && !rdoRoleAdmin.isChecked()) {
            openFailDialog("Hãy chọn vai trò của người dùng");
            check = -1;
        }
        return check;
    }

    private void getListUser() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        myRef.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    list.add(user);
                    adapter.notifyDataSetChanged();
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User user = snapshot.getValue(User.class);
                if (user == null || list == null || list.isEmpty()) {
                    return;
                }
                for (int i = 0; i < list.size(); i++) {
                    if (user.getId().equals(list.get(i).getId())) {
                        list.set(i, user);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user == null || list == null || list.isEmpty()) {
                    return;
                }
                for (int i = 0; i < list.size(); i++) {
                    if (user.getId().equals(list.get(i).getId())) {
                        list.remove(list.get(i));
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void openSuccessDialog (String text) {
        Dialog dialog = new Dialog(getContext());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success_notification);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvNotifyContent = dialog.findViewById(R.id.tvNotifyContent);
        tvNotifyContent.setText(text);
        dialog.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();
    }

    public void openFailDialog (String text) {
        Dialog dialog = new Dialog(getContext());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fail_notification);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvNotifyContent = dialog.findViewById(R.id.tvNotifyContent);
        tvNotifyContent.setText(text);
        dialog.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();
    }
}
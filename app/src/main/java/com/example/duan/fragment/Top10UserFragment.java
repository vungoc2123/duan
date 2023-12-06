package com.example.duan.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.example.duan.R;
import com.example.duan.adapter.UserAdapter;
import com.example.duan.model.Order;
import com.example.duan.model.User;

public class Top10UserFragment extends Fragment {
    private DatabaseReference datebaseRef;
    private List<User> getListUser = new ArrayList<>();
    private List<User> listUser1 = new ArrayList<>();
    private List<User> listUser2 = new ArrayList<>();
    private int soLan = 0;
    private UserAdapter adapter;
    private RecyclerView recyclerView;
    private TextInputEditText ed_tuNgay, ed_denNgay;
    private int day, month, year;

    public Top10UserFragment() {
        // Required empty public constructor
    }

    public static Top10UserFragment newInstance() {
        Top10UserFragment fragment = new Top10UserFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_top10_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.topUserRecycleView);
        ed_tuNgay = view.findViewById(R.id.ed_TopUser_tuNgay);
        ed_denNgay = view.findViewById(R.id.ed_TopUser_denNgay);
        datebaseRef = FirebaseDatabase.getInstance().getReference();
        Calendar calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
        ed_tuNgay.setOnClickListener(v -> {
            datePickerDialog(calendar, ed_tuNgay);
        });
        ed_denNgay.setOnClickListener(v -> {
            datePickerDialog(calendar, ed_denNgay);
        });
        view.findViewById(R.id.btn_topUser).setOnClickListener(v -> {
//            getListUser();
//            getListUserBySoLan();
            getTopUsers();
        });
        adapter = new UserAdapter(getActivity(), R.layout.item_top_user);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

    }


    public void getListUserBySoLan() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = databaseReference.orderByChild("soLan").startAfter(0).limitToFirst(10);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getListUser.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    getListUser.add(0, user);
                }
                adapter.setData(getListUser);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

//    private void getListUser() {
//        listUser2.clear();
//        datebaseRef = FirebaseDatabase.getInstance().getReference().child("orders");
//        datebaseRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
//                    Order order = snapshot1.getValue(Order.class);
//                    listUser2 = getListUserByDate(order);
//                    datebaseRef = FirebaseDatabase.getInstance().getReference().child("users/" + order.getUser().getId() + "/id");
//                    datebaseRef.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if(snapshot.getValue()!= null){
//                                soLan = 0;
//                                String idUser = snapshot.getValue().toString();
//                                for (int i = 0; i < listUser2.size(); i++) {
//                                    if (idUser.equals(listUser2.get(i).getId())) {
//                                        soLan++;
//                                    }
//                                }
//                                datebaseRef = FirebaseDatabase.getInstance().getReference().child("users/" + idUser + "/soLan");
//                                datebaseRef.setValue(soLan);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


    private boolean getListUserByDate(Order order) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        //13/11 so sanh 17/11 =-1
        //17/11 so sanh 13/11 = 1
        try {
            Date fromDate = format.parse(ed_tuNgay.getText().toString());
            Date toDate = format.parse(ed_denNgay.getText().toString());
            Date orderDate = format.parse(order.getDate());
            if (orderDate.compareTo(fromDate) >= 0 && orderDate.compareTo(toDate) <= 0 && order.getStatus() == 2) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("error", e.getMessage());
            return false;
        }
        return false;
    }

    private void datePickerDialog(Calendar calendar, EditText editText) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            editText.setText(simpleDateFormat.format(calendar.getTime()));
        }, year, month, day);
        datePickerDialog.show();
    }

    List<Order> orders = new ArrayList<>();
    List<User> users = new ArrayList<>();
    List<String> usersId = new ArrayList<>();
    List<Integer> usersOccurs = new ArrayList<>();

    private void getTopUsers() {
        if (ed_tuNgay.getText().toString().trim().length() == 0
        || ed_denNgay.getText().toString().trim().length() == 0) {
            openFailDialog("Bạn chưa nhập ngày!");
            return;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            simpleDateFormat.parse(ed_tuNgay.getText().toString());
            simpleDateFormat.parse(ed_denNgay.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
            openFailDialog("Ngày sai định dạng!");
            return;
        }
        datebaseRef.child("orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orders.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
//                    orders = getListDistByDate(order);
                    if (order.getStatus() == 2 && getListUserByDate(order))
                        orders.add(order);
                }
                datebaseRef.child("users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            for (Order order : orders) {
                                if (order.getUser().getId().equals(user.getId())) {
                                    users.add(user);
                                }
                            }
                        }


                        for (Order order : orders) {
                            if (order.getStatus() == 2) {
                                for (User user : users) {
                                    if (order.getUser().getId().equals(user.getId())) {
                                        usersId.add(user.getId());
                                    }
                                }
                            }

                        }

                        usersOccurs.clear();
                        for (User user : users) {
                            int occurrence = Collections.frequency(usersId, user.getId());
                            usersOccurs.add(occurrence);
                            Log.e("size", user.getId() + "");
                            Log.e("size2", occurrence + "");
                        }

                        for (int i = 0; i < users.size() - 1; i++) {
                            for (int j = i + 1; j < users.size(); j++) {
                                if (usersOccurs.get(i) < usersOccurs.get(j)) {
                                    int dupTemp = usersOccurs.get(i);
                                    usersOccurs.set(i, usersOccurs.get(j));
                                    usersOccurs.set(j, dupTemp);
                                    User userTemp = users.get(i);
                                    users.set(i, users.get(j));
                                    users.set(j, userTemp);
                                }
                            }
                        }
                        for (int i = 0; i < 2; i++) {
                            for (int j = 0; j < users.size() - 1; j++) {
                                for (int k = j + 1; k < users.size(); k++) {
                                    if (users.get(j).getId().equals(users.get(k).getId())) {
                                        users.remove(k);
                                    }
                                }
                            }
                        }
                        adapter.setData(users);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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





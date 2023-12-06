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
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.example.duan.R;
import com.example.duan.adapter.MonAnRecycleAdapter;
import com.example.duan.model.Dish;
import com.example.duan.model.Order;
import com.example.duan.model.OrderDish;


public class Top10DishFragment extends Fragment {
    private DatabaseReference datebaseRef;
    private MonAnRecycleAdapter adapter;
    private RecyclerView recyclerView;
    List<Order> listOrders = new ArrayList<>();
    List<Order> orders = new ArrayList<>();
    List<Dish> dishes = new ArrayList<>();
    List<Integer> dishOccurs = new ArrayList<>();
    List<String> dishesId = new ArrayList<>();
    private TextInputEditText ed_tuNgay, ed_denNgay;
    private int day, month, year;

    public Top10DishFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_top10_dish, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        datebaseRef = FirebaseDatabase.getInstance().getReference();
        recyclerView = view.findViewById(R.id.recyclerViewTop10Dish);
        adapter = new MonAnRecycleAdapter(getActivity(), R.layout.item_top_dish, dish -> {

        });
        //adapter.setData(dishes);
        ed_tuNgay = view.findViewById(R.id.ed_TopDish_tuNgay);
        ed_denNgay = view.findViewById(R.id.ed_TopDish_denNgay);
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
        view.findViewById(R.id.btn_topDish).setOnClickListener(v -> {
            getTop10Dish();
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
            recyclerView.setAdapter(adapter);
        });
    }

    public void getTop10Dish() {
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
                    orders = getListDistByDate(order);
                }
                datebaseRef.child("Dish").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dishes.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Dish dish = dataSnapshot.getValue(Dish.class);
                            for (Order order : orders) {
                                for (OrderDish orderDish : order.getDishes()) {
                                    if(orderDish.getDish().getId().equals(dish.getId())){
                                        dishes.add(dish);
                                    }
                                }
                            }
                        }
                        for(int a =0; a<2;a++){
                            for(int i =0; i<dishes.size()-1;i++){
                                for(int x =i+1;x<dishes.size();x++){
                                    if(dishes.get(i).getId().equals(dishes.get(x).getId())){
                                        dishes.remove(x);
                                    }
                                }
                            }
                        }

                        for (Order order : orders) {
                            if (order.getStatus() == 2) {
                                for (OrderDish orderDish : order.getDishes()) {
                                    for (int i = 0; i < orderDish.getQuantity(); i++) {
                                        dishesId.add(orderDish.getDish().getId());
                                    }
                                }
                            }

                        }

                        dishOccurs.clear();
                        for (Dish dish : dishes) {
                            int occurrence = Collections.frequency(dishesId, dish.getId());
                            dishOccurs.add(occurrence);
                            Log.e("size",dish.getId()+"");
                            Log.e("size2",occurrence+"");
                        }

                        for (int i = 0; i < dishes.size() - 1; i++) {
                            for (int j = i + 1; j < dishes.size(); j++) {
                                if (dishOccurs.get(i) < dishOccurs.get(j)) {
                                    int dupTemp = dishOccurs.get(i);
                                    dishOccurs.set(i, dishOccurs.get(j));
                                    dishOccurs.set(j, dupTemp);
                                    Dish dishTemp = dishes.get(i);
                                    dishes.set(i, dishes.get(j));
                                    dishes.set(j, dishTemp);
                                }
                            }
                        }
                        adapter.setData(dishes);
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
    private List<Order> getListDistByDate(Order order) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        //13/11 so sanh 17/11 =-1
        //17/11 so sanh 13/11 = 1
        try {
            Date fromDate = format.parse(ed_tuNgay.getText().toString());
            Date toDate = format.parse(ed_denNgay.getText().toString());
            Date orderDate = format.parse(order.getDate());
            if (orderDate.compareTo(fromDate) >= 0 && orderDate.compareTo(toDate) <= 0 && order.getStatus() == 2) {
                listOrders.add(order);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("error", e.getMessage());
        }
        return listOrders;
    }

    private void datePickerDialog(Calendar calendar, EditText editText) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            editText.setText(simpleDateFormat.format(calendar.getTime()));
        }, year, month, day);
        datePickerDialog.show();
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
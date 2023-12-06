package com.example.duan.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.duan.R;
import com.example.duan.model.Dish;
import com.example.duan.model.Order;
import com.example.duan.model.OrderDish;
import com.example.duan.model.Table;
import com.example.duan.model.User;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> implements Filterable {
    Context context;
    List<Order> orders;
    FirebaseDatabase database;
    DatabaseReference myRef;
    OrderDishAdapter2 adapter;
    List<User> users = new ArrayList<>();
    List<Table> mSelectedTables = new ArrayList<>();
    List<Dish> dishes = new ArrayList<>();
    List<OrderDish> orderDishes = new ArrayList<>();
    List<Order> orders2 = new ArrayList<>();
    OrderDishAdapter dishAdapter = new OrderDishAdapter(context);
    RecyclerView recyclerView_orderDish;
    private SpinnerAdapter spinnerAdapter;
    int mHour, mMinute;
    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
        this.orders2 = orders;
        this.database = FirebaseDatabase.getInstance();
        this.myRef = database.getReference();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.btnTable.setText("Bàn");
        String tablesStr = "Bàn: ";
        List<Table> mTables = order.getTables();
        if (mTables != null) {
            for (Table table : mTables) {
                tablesStr += table.getNumber();
                if (mTables.indexOf(table) == mTables.size()-1)
                    break;
                tablesStr +=  " , ";
            }
            holder.btnTable.setText(tablesStr);
        }
        holder.tvOrderDate.setText("Ngày: " + order.getDate());
        holder.tvOrderTime.setText(order.getStartTime() + " - " + order.getEndTime());
        switch (order.getStatus()) {
            case 0:
                holder.tvOrderStatus.setText("Đang chờ");
                holder.tvOrderStatus.setTextColor(Color.YELLOW );
                holder.btnTable.setBackgroundResource(R.drawable.bg_gray_corner_10);
                holder.btnTable.setEnabled(true);
                break;
            case 1:
                holder.tvOrderStatus.setText("Đang dùng");
                holder.tvOrderStatus.setTextColor(Color.BLUE);
                holder.btnTable.setBackgroundResource(R.drawable.bg_gray_corner_10);
                holder.btnTable.setEnabled(true);
                break;
            case 2:
                holder.tvOrderStatus.setText("Đã xong");
                holder.tvOrderStatus.setTextColor(Color.GREEN);
                holder.btnTable.setEnabled(false);
                holder.btnTable.setBackgroundColor(Color.GRAY);
                break;
            case 3:
                holder.tvOrderStatus.setText("Hủy");
                holder.tvOrderStatus.setTextColor(Color.RED);
//                holder.imgDelete.setVisibility(View.VISIBLE);
//                holder.imgDelete.setOnClickListener(v -> {
//                    deleteOrder(order.getId());
//                });
                holder.btnTable.setEnabled(false);
                holder.btnTable.setBackgroundColor(Color.GRAY);
                break;
        }
        holder.tvOrderNoP.setText(order.getNumberOfPeople() + " người");
        holder.tvOrderUser.setText("Khách hàng: " + order.getUser().getName());
        holder.tvOrderTotal.setText("Tổng tiền: " + formatCurrency(order.getTotal()));
        holder.recyclerViewDishes.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        adapter = new OrderDishAdapter2(context);
        adapter.setData(order.getDishes());
        holder.recyclerViewDishes.setAdapter(adapter);
        holder.btnTable.setOnClickListener(v -> {
            openChooseTableDialog(position);
//            holder.btnTable.setText(mSelectedTables.size()+"");
        });

        holder.itemView.setOnClickListener(v -> {
            if (holder.contentLayout.getVisibility() == View.GONE) {
                holder.contentLayout.setVisibility(View.VISIBLE);
                holder.imgDropDown.setImageResource(R.drawable.ic_drop_up);
            } else {
                holder.contentLayout.setVisibility(View.GONE);
                holder.imgDropDown.setImageResource(R.drawable.ic_drop_down);
            }
        });
        if(order.getStatus()==2 || order.getStatus() == 3){
            holder.imgEdit.setEnabled(false);
            holder.imgEdit.setVisibility(View.INVISIBLE);
        }else{
            holder.imgEdit.setVisibility(View.VISIBLE);
            holder.imgEdit.setEnabled(true);
            holder.imgEdit.setOnClickListener(v ->{
                openOrderDialog(order);
            });
        }

    }

    public void deleteOrder(final String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete");
        builder.setMessage("Bạn có muốn xóa không ?");
        builder.setCancelable(true);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("order");
            myRef.child(id).removeValue((error, ref)
                    -> openSuccessDialog("Xóa thành công"));
            dialog.cancel();
            notifyDataSetChanged();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openChooseTableDialog(int position) {
        Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_choose_table);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

        getFilteredTablesList(dialog, orders.get(position));

        dialog.findViewById(R.id.btnChoose).setOnClickListener(v -> {

            orders.get(position).setTables(mSelectedTables);
            myRef.child("orders").child(orders.get(position).getId()).updateChildren(orders.get(position).toMap());
            mSelectedTables.clear();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.setCancelable(true);
        dialog.show();
    }

    public void getFilteredTablesList(Dialog dialog, Order order) {
        List<Table> tables = new ArrayList<>();
        if (order.getTables() != null)
            mSelectedTables = order.getTables();
        ChooseTableAdapter adapter = new ChooseTableAdapter(context, tables,mSelectedTables, selectedTables -> {
            if (selectedTables != null)
                mSelectedTables = selectedTables;
        });
        RecyclerView recyclerViewTable = dialog.findViewById(R.id.recyclerViewTable);
        recyclerViewTable.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerViewTable.setAdapter(adapter);

        myRef.child("tables").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Table table = dataSnapshot.getValue(Table.class);
                    tables.add(table);
                }
                List<Table> removeList = new ArrayList<>();
                List<Order> mOrders = orders;
                for (Order mOrder : mOrders) {
                    if (mOrder.getId().equals(order.getId()))
                        continue;
                    if (!mOrder.getDate().equals(order.getDate()))
                        continue;
                    if (mOrder.getStatus() == 2 || mOrder.getStatus() == 3)
                        continue;
                    if (mOrder.getTables() == null)
                        continue;
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                    try {
                        Date mStartTime = simpleDateFormat.parse(mOrder.getStartTime());
                        Date startTime = simpleDateFormat.parse(order.getStartTime());
                        Date endTime = simpleDateFormat.parse(order.getEndTime());

                        // Kiem tra neu thoi gian dat ban cua don nay da co ban nao duoc dat thi
                        // xoa ban do khoi danh sach chon ban
                        if (mStartTime.compareTo(startTime) >= 0 && mStartTime.compareTo(endTime) <= 0) {
                            for (Table table : mOrder.getTables()) {
                                for (Table table1 : tables) {
                                    if (table.getId().equals(table1.getId()))
                                        removeList.add(table1);
                                }
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                tables.removeAll(removeList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void openOrderDialog(Order order) {
        Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_order);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

        TextView dialogUserTitle = dialog.findViewById(R.id.dialogUserTitle);
        dialogUserTitle.setText("Sửa Order");

        Spinner spnOrderUser = dialog.findViewById(R.id.spnOrderUser);
        myRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()) {
                    User user = s.getValue(User.class);
                    if (user.getRole() == 1)
                        users.add(user);
                }
                List<String> userNames = new ArrayList<>();
                for (User u : users) {
                    userNames.add(u.getName());
                }
                spnOrderUser.setAdapter(new ArrayAdapter<>(context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, userNames));
                spnOrderUser.setSelection(userNames.indexOf(order.getUser().getName()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        spnOrderUser.setSelection(users.indexOf(order.getUser()));


        EditText edtOrderDate = dialog.findViewById(R.id.edtOrderDate);
        edtOrderDate.setText(order.getDate());
        edtOrderDate.setOnClickListener(v -> {datePickerDialog(edtOrderDate);});
        EditText edtOrderStartTime = dialog.findViewById(R.id.edtOrderStartTime);
        edtOrderStartTime.setText(order.getStartTime());
        edtOrderStartTime.setOnClickListener(v -> timePickerDialog(edtOrderStartTime));
        EditText edtOrderEndTime = dialog.findViewById(R.id.edtOrderEndTime);
        edtOrderEndTime.setText(order.getEndTime());
        edtOrderEndTime.setOnClickListener(v -> timePickerDialog(edtOrderEndTime));
        EditText edtOrderNoP = dialog.findViewById(R.id.edtOrderNoP);
        edtOrderNoP.setText(order.getNumberOfPeople() + "");


        Spinner spnOrderStatus = dialog.findViewById(R.id.spnOrderStatus);
        List<String> statuses = new ArrayList<>();
        statuses.add("Đang chờ");
        statuses.add("Đang dùng");
        statuses.add("Đã thanh toán");
        statuses.add("Hủy");
        spnOrderStatus.setAdapter(new ArrayAdapter<>(context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, statuses));
        spnOrderStatus.setSelection(order.getStatus());

        Button btnOrderDish = dialog.findViewById(R.id.btnOrderDish);
        myRef.child("orders/"+order.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!= null){
                    Order order1 = snapshot.getValue(Order.class);
                    orderDishes = order1.getDishes();
                    dishAdapter.setData(orderDishes);
                    recyclerView_orderDish = dialog.findViewById(R.id.recycleView_orderDish);
                    recyclerView_orderDish.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
                    recyclerView_orderDish.setAdapter(dishAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        orderDishes = order.getDishes();

        btnOrderDish.setOnClickListener(v -> {
            orderDishes = openDishDialog();
        });
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);


        btnSave.setOnClickListener(v -> {
            if (edtOrderDate.getText().toString().trim().length() == 0
                    || edtOrderStartTime.getText().toString().trim().length() == 0
                    || edtOrderEndTime.getText().toString().trim().length() == 0
                    || edtOrderNoP.getText().toString().trim().length() == 0) {
                openFailDialog("Không để trống dữ liệu");
                return;
            }

            SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
            String mTime = edtOrderEndTime.getText().toString().trim();
            try {
                Date startTime = parser.parse(edtOrderStartTime.getText().toString().trim());
                Date endTime = parser.parse(mTime);
                if (endTime.compareTo(startTime) <= 0) {
                    openFailDialog("Ngày và giờ sai định dạng");
                    return;
                }
            } catch (ParseException e) {
                openFailDialog("Ngày và giờ sai định dạng");
                e.printStackTrace();
                return;
            }
            order.setUser(users.get(spnOrderUser.getSelectedItemPosition()));
            order.setDate(edtOrderDate.getText().toString());
            order.setStartTime(edtOrderStartTime.getText().toString());
            order.setEndTime(edtOrderEndTime.getText().toString());
            order.setStatus(spnOrderStatus.getSelectedItemPosition());
            order.setDishes(orderDishes);
            order.setNumberOfPeople(Integer.parseInt(edtOrderNoP.getText().toString().trim()));
            order.setTotal();
            myRef.child("orders").child(order.getId()).setValue(order).addOnSuccessListener(unused -> {
                openSuccessDialog("Cập nhật thành công");
                dialog.dismiss();
            });
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(true);
        dialog.show();
    }

    private boolean validateTime (String time, String date) {
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String mTime = date + " " + time;
        try {
            Date currentTime = Calendar.getInstance().getTime();
            Date ten = parser.parse(mTime);
            if (ten.compareTo(currentTime) < 0) {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void openFailDialog (String text) {
        Dialog dialog = new Dialog(context);

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

    protected List<OrderDish> openDishDialog() {
        Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_dish_picker);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

        Spinner spnDish = dialog.findViewById(R.id.spnDish);
        myRef.child("Dish").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dishes.clear();
                List<String> dishNames = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Dish dish = s.getValue(Dish.class);
//                  dishNames.add(dish.getTen());
                    dishes.add(dish);
                }
                spinnerAdapter = new SpinnerAdapter(context, dishes);
                spnDish.setAdapter(spinnerAdapter);
//                spnDish.setAdapter(new ArrayAdapter<>(context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, dishNames));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        TextView edtQuantity = dialog.findViewById(R.id.edtQuantity);
        Button btnOk = dialog.findViewById(R.id.btnOk);
        Button btnAdd = dialog.findViewById(R.id.btnAdd);
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);


        dishAdapter.setData(orderDishes);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(dishAdapter);


        btnAdd.setOnClickListener(v -> {
            OrderDish orderDish = new OrderDish();
            try {
                if (edtQuantity.getText().toString().trim().length() == 0) {
                    Toast.makeText(context, "Hãy nhập số lượng!", Toast.LENGTH_SHORT).show();
                    return;
                }
                orderDish.setQuantity(Integer.parseInt(edtQuantity.getText().toString()));
            } catch (Exception e) {
                Toast.makeText(context, "Số lượng phải là số!", Toast.LENGTH_SHORT).show();
                return;
            }
            orderDish.setDish(dishes.get(spnDish.getSelectedItemPosition()));
            if (orderDishes == null) {
                orderDishes = new ArrayList<>();
            }
                for (OrderDish orderDish1 : orderDishes) {
                    if (orderDish1.getDish().getId().equals(orderDish.getDish().getId())) {
                        orderDish1.setQuantity(orderDish1.getQuantity() + orderDish.getQuantity());
                        dishAdapter.setData(orderDishes);
                        return;
                    }
                }
            orderDishes.add(orderDish);
            dishAdapter.setData(orderDishes);
        });
        btnOk.setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(true);
        dialog.show();
        return orderDishes;
    }

    private void datePickerDialog(EditText editText){
        Calendar calendar = Calendar.getInstance();
        int day1,month1,year1;
        day1 = calendar.get(Calendar.DAY_OF_MONTH);
        month1 = calendar.get(Calendar.MONTH);
        year1 = calendar.get(Calendar.YEAR);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DatePickerDialog datePickerDialog = new DatePickerDialog( context, (view, year, month, dayOfMonth) -> {
            calendar.set(year,month,dayOfMonth);
            editText.setText(simpleDateFormat.format(calendar.getTime()));
        },year1,month1,day1);
        datePickerDialog.show();
    }

    public void openSuccessDialog (String text) {
        Dialog dialog = new Dialog(context);

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

    private void timePickerDialog(EditText editText){
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, hourOfDay, minute) -> {
            mHour = hourOfDay;
            mMinute = minute;
            editText.setText(String.format(Locale.getDefault(), "%02d:%02d",hourOfDay,minute));
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, onTimeSetListener, mHour, mMinute, true);
        timePickerDialog.show();
    }
    
    public String formatCurrency(double money) {
        String pattern="###,###.### VNĐ";
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        return myFormatter.format(money);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String search = constraint.toString();
                List<Order> list = new ArrayList<>();
                if(search.isEmpty()){
                    orders = orders2;
                }else{
                    for(Order order : orders2){
                        if(order.getDate().toLowerCase().contains(search.toLowerCase())){
                            list.add(order);
                        }
                    }
                    orders = list;
                }
                FilterResults results = new FilterResults();
                results.values = orders;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                orders = (List<Order>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderTable, tvOrderDate, tvOrderTime, tvOrderStatus, tvOrderUser, tvOrderTotal, tvOrderNoP;
        ImageView imgEdit, imgDropDown, imgDelete;
        RecyclerView recyclerViewDishes;
        RelativeLayout contentLayout;
        Button btnTable;
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderTable = itemView.findViewById(R.id.tvOrderTable);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderUser = itemView.findViewById(R.id.tvOrderUser);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDropDown = itemView.findViewById(R.id.imgDropDown);
            contentLayout = itemView.findViewById(R.id.contentLayout);
            recyclerViewDishes = itemView.findViewById(R.id.recyclerViewDishes);
            btnTable = itemView.findViewById(R.id.btnTable);
            tvOrderNoP = itemView.findViewById(R.id.tvOrderNoP);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }

}

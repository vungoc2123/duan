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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.example.duan.R;
import com.example.duan.model.Order;

public class DoanhThuFragment extends Fragment {
    private TextInputEditText ed_tuNgay,ed_denNgay;
    private TextView tv_doanhThu;
    private int day,month,year;

    public DoanhThuFragment() {
        // Required empty public constructor
    }


    public static DoanhThuFragment newInstance() {
        DoanhThuFragment fragment = new DoanhThuFragment();
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
        return inflater.inflate(R.layout.fragment_doanh_thu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ed_tuNgay = view.findViewById(R.id.ed_DT_tuNgay);
        ed_denNgay = view.findViewById(R.id.ed_DT_denNgay);
        tv_doanhThu = view.findViewById(R.id.tv_doanhThu);
        Calendar calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
        ed_tuNgay.setOnClickListener(v -> {
            datePickerDialog(calendar,ed_tuNgay);
        });
        ed_denNgay.setOnClickListener(v -> {
            datePickerDialog(calendar,ed_denNgay);
        });
        view.findViewById(R.id.btn_doanhThu).setOnClickListener(v -> {
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
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            myRef.child("orders").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double doanhThu = 0;
                    for (DataSnapshot s : snapshot.getChildren()) {
                        Order order = s.getValue(Order.class);
                        doanhThu += getTungDoanhThu(order);
                    }

                    tv_doanhThu.setText(formatCurrency(doanhThu));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }

    private double getTungDoanhThu(Order order) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        double total = 0;
        //13/11 so sanh 17/11 =-1
        //17/11 so sanh 13/11 = 1
        try {
            Date fromDate = format.parse(ed_tuNgay.getText().toString());
            Date toDate = format.parse(ed_denNgay.getText().toString());
            Log.e("TAG2", ""+fromDate.toString() + "  " + toDate.toString());
            Date orderDate = format.parse(order.getDate());
            if (orderDate.compareTo(fromDate) >=0 && orderDate.compareTo(toDate) <= 0 && order.getStatus() == 2) {
                total += order.getTotal();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("error", e.getMessage() );
        }
        return total;
    }

    private void datePickerDialog(Calendar calendar, EditText editText){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DatePickerDialog datePickerDialog = new DatePickerDialog( getActivity(), (view, year, month, dayOfMonth) -> {
            calendar.set(year,month,dayOfMonth);
            editText.setText(simpleDateFormat.format(calendar.getTime()));
        },year,month,day);
        datePickerDialog.show();
    }

    public String formatCurrency(double money) {
        String pattern="###,###.### VNĐ";
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        return myFormatter.format(money);
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
package com.example.duan.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.duan.R;
import com.example.duan.model.Dish;

public class MonAnRecycleAdapter extends RecyclerView.Adapter<MonAnRecycleAdapter.userViewHolder> implements Filterable {
    private List<Dish> list;
    private List<Dish> dishList;
    private Context context;
    private IClickListener iClickListener;
    private int layout;

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String search = constraint.toString();
                List<Dish> dishes = new ArrayList<>();
                if(search.isEmpty()){
                    list = dishList;
                }else{
                    for(Dish dish : dishList){
                        if(dish.getTen().toLowerCase().contains(search.toLowerCase())){
                            dishes.add(dish);
                        }
                    }
                    list = dishes;
                }
                FilterResults results =new FilterResults();
                results.values = list;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list = (List<Dish>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface IClickListener{
        void OnClickUpdateItem(Dish dish);
    }

    public MonAnRecycleAdapter(Context context, int layout,IClickListener listener) {
        this.context = context;
        this.layout = layout;
        this.iClickListener = listener;
    }

    public void setData(List<Dish> list){
        this.list = list;
        this.dishList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layout,parent,false);
        return new userViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull userViewHolder holder, int position) {

        Dish dish = list.get(position);
        Glide.with(context).load(dish.getImg()).into(holder.img_monAn);
        ImageView.ScaleType scaleType = ImageView.ScaleType.FIT_XY;
        holder.img_monAn.setScaleType(scaleType);
        holder.tv_tenMonAn.setText(dish.getTen());
        holder.tv_gia.setText(formatCurrency(dish.getGia()));
        AtomicInteger soLuong = new AtomicInteger();
        soLuong.set(dish.getSoLuong());

        holder.img_xoaMonAn.setOnClickListener(v -> {
            diaLogDelete(dish);
            Log.e("id",dish.getId());

        });
        if(holder.imgEdit != null){
            holder.imgEdit.setOnClickListener(v -> {
                iClickListener.OnClickUpdateItem(dish);
            });
        }
    }

    public String formatCurrency(double money) {
        String pattern="###,###.### VNĐ";
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        return myFormatter.format(money);
    }

    @Override
    public int getItemCount() {
        if(list != null){
            return list.size();
        }
        return 0;
    }

    public class userViewHolder extends RecyclerView.ViewHolder {
        ImageView img_monAn,img_xoaMonAn, imgEdit;
        TextView tv_tenMonAn,tv_gia;
        public userViewHolder(@NonNull View itemView) {
            super(itemView);
            img_monAn = itemView.findViewById(R.id.img_monAn);
            tv_tenMonAn = itemView.findViewById(R.id.tv_ten_mon);
            tv_gia = itemView.findViewById(R.id.tv_gia);
            img_xoaMonAn = itemView.findViewById(R.id.img_xoaMonAn);
            imgEdit = itemView.findViewById(R.id.imgEdit);
        }
    }
    public void diaLogDelete(Dish dish){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Thông báo");
        builder.setMessage("Bạn có muốn xóa không");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Dish/"+dish.getId());
            databaseRef.removeValue((error, ref) -> openSuccessDialog("Xóa thành công"));
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> {
        });
        builder.show();
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

}

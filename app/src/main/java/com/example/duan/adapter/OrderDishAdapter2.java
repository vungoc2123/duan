package com.example.duan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.duan.R;
import com.example.duan.model.OrderDish;

public class OrderDishAdapter2 extends RecyclerView.Adapter<OrderDishAdapter2.OrderDishViewHolder> {
    Context context;
    List<OrderDish> list;

    public OrderDishAdapter2(Context context) {
        this.context = context;
    }
    public void setData(List<OrderDish> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderDishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderDishViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dish_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDishViewHolder holder, int position) {
        OrderDish orderDish = list.get(position);
        if (orderDish == null)
            return;
        holder.tvName.setText(orderDish.getDish().getTen());
        holder.tvPrice.setText(String.valueOf(orderDish.getDish().getGia()));
        holder.tvQuantity.setText(String.valueOf(orderDish.getQuantity()));
        holder.img_delete.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        if(list != null){
            return list.size();
        }
        return 0;
    }

    public class OrderDishViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity;
        ImageView img_delete;
        public OrderDishViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            img_delete= itemView.findViewById(R.id.img_delete_dish);
        }
    }
}

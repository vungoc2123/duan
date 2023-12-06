package com.example.duan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import com.example.duan.R;
import com.example.duan.model.Dish;

public class SpinnerAdapter extends BaseAdapter {
    private Context context;
    private List<Dish> list;
    public SpinnerAdapter(Context context,List<Dish> list){
        this.context = context;
        this.list = list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        logoViewHolder viewHolder = null;
        if(view == null){
            viewHolder = new logoViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_spinner_dish,null);
            viewHolder.img_dish = view.findViewById(R.id.img_spinner_dish);
            viewHolder.tv_ten = view.findViewById(R.id.tv_spinner_ten);
            viewHolder.tv_gia = view.findViewById(R.id.tv_spinner_gia);
            view.setTag(viewHolder);
        }else{
            viewHolder = (logoViewHolder) view.getTag();
        }
        Glide.with(context).load(list.get(i).getImg()).into(viewHolder.img_dish);
        ImageView.ScaleType scaleType = ImageView.ScaleType.FIT_XY;
        viewHolder.img_dish.setScaleType(scaleType);
        viewHolder.tv_ten.setText(list.get(i).getTen());
        viewHolder.tv_gia.setText(list.get(i).getGia()+"");

        return view;
    }

    public static class logoViewHolder{
        private ImageView img_dish;
        private TextView tv_ten,tv_gia;
    }
}

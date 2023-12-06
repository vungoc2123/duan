package com.example.duan.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import com.example.duan.R;
import com.example.duan.model.FeedBack;
import com.example.duan.model.Table;

public class FeedBackAdapter extends RecyclerView.Adapter<FeedBackAdapter.ViewHodler>{
    private List<FeedBack> feedBackList;
    private Context context;

    public FeedBackAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<FeedBack> feedBackList){
        this.feedBackList = feedBackList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback,parent,false);
        return new ViewHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHodler holder, int position) {
            FeedBack feedBack = feedBackList.get(position);
            if(feedBack == null)
                return;
            holder.tv_content.setText(String.valueOf(feedBack.getContent()));
            holder.tv_date.setText(String.valueOf(feedBack.getDate()));
            holder.tv_userId.setText(String.valueOf(feedBack.getUser().getName()));
            if (feedBack.getOrder() != null) {
                String tableStr = "Bàn: ";
                for (Table table : feedBack.getOrder().getTables()) {
                    tableStr += table.getNumber();
                    if (feedBack.getOrder().getTables().indexOf(table) == feedBack.getOrder().getTables().size()-1)
                        break;
                    tableStr +=  " , ";
                }
                holder.tvTable.setText(tableStr);
                holder.tvNoP.setText(feedBack.getOrder().getNumberOfPeople() + " người");
            }
            holder.imgDropDown.setImageResource(R.drawable.ic_drop_down);
            holder.itemView.setOnClickListener(v -> {
                if (holder.contentLayout.getVisibility() == View.GONE) {
                    holder.contentLayout.setVisibility(View.VISIBLE);
                    holder.imgDropDown.setImageResource(R.drawable.ic_drop_up);
                } else {
                    holder.contentLayout.setVisibility(View.GONE);
                    holder.imgDropDown.setImageResource(R.drawable.ic_drop_down);
                }
            });
            holder.imgDelete.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Bạn có muốn xóa không ?");
                builder.setCancelable(true);

                builder.setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("feedbacks");
                    myRef.child(feedBack.getId()).removeValue((error, ref)
                            -> openSuccessDialog("Xóa thành công"));
                    dialog.cancel();
                    notifyDataSetChanged();
                });
                builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
                builder.show();
            });
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

    @Override
    public int getItemCount() {

        return (feedBackList == null) ? 0: feedBackList.size();
    }

    public class ViewHodler extends RecyclerView.ViewHolder{
        TextView tv_content,tv_userId,tv_date, tvTable, tvNoP;
        RelativeLayout contentLayout;
        ImageView imgDropDown, imgDelete;
        public ViewHodler(@NonNull View itemView) {
            super(itemView);
            tv_content = itemView.findViewById(com.example.duan.R.id.txtContent);
            tv_userId = itemView.findViewById(com.example.duan.R.id.txtUserID);
            tv_date = itemView.findViewById(com.example.duan.R.id.txtDate);
            contentLayout = itemView.findViewById(com.example.duan.R.id.contentLayout);
            imgDropDown = itemView.findViewById(com.example.duan.R.id.imgDropDown);
            imgDelete = itemView.findViewById(com.example.duan.R.id.imgDelete);
            tvTable = itemView.findViewById(com.example.duan.R.id.tvTable);
            tvNoP = itemView.findViewById(com.example.duan.R.id.tvNoP);
        }
    }
}

package com.example.duan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.duan.R;
import com.example.duan.model.Table;

public class ChooseTableAdapter extends RecyclerView.Adapter<ChooseTableAdapter.ChooseTableViewHolder> {
    Context context;
    List<Table> tables;
    IClickListener iClickListener;
    List<Table> selectedTables = new ArrayList<>();
    public interface IClickListener {
        void OnItemClick(List<Table> tables);
    }

    public ChooseTableAdapter(Context context, List<Table> tables,List<Table> selectedTables, IClickListener iClickListener) {
        this.context = context;
        this.tables = tables;
        this.selectedTables = selectedTables;
        this.iClickListener = iClickListener;
    }

    @NonNull
    @Override
    public ChooseTableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChooseTableViewHolder(LayoutInflater.from(context).inflate(R.layout.item_choose_table, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseTableViewHolder holder, int position) {
        Table table = tables.get(position);
        holder.tvTableNumber.setText("Bàn" + table.getNumber());
        for (Table table1 : selectedTables) {
            if (table1.getId().equals(table.getId())) {
                holder.chkSelect.setChecked(true);
                break;
            }
        }

        holder.itemView.setOnClickListener(v -> {
            holder.chkSelect.setChecked(!holder.chkSelect.isChecked());
            if (holder.chkSelect.isChecked()) {
                if (!selectedTables.contains(table)) {
                    selectedTables.add(table);
                }
            } else {
                for (Table table1 : selectedTables) {
                    if (table1.getId().equals(table.getId())) {
                        selectedTables.remove(table1);
                        break;
                    }
                }

            }
            iClickListener.OnItemClick(selectedTables);
        });

        holder.chkSelect.setOnClickListener(v -> {
            holder.chkSelect.setChecked(!holder.chkSelect.isChecked());
            if (holder.chkSelect.isChecked()) {
                if (!selectedTables.contains(table)) {
                    selectedTables.add(table);
                }
            } else {
                selectedTables.remove(table);
            }
            iClickListener.OnItemClick(selectedTables);
        });
    }

    @Override
    public int getItemCount() {
        if (tables == null)
            return 0;
        return tables.size();
    }

    class ChooseTableViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableNumber;
        CheckBox chkSelect;
        public ChooseTableViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableNumber = itemView.findViewById(R.id.tvTableNumber);
            chkSelect = itemView.findViewById(R.id.chkSelect);
        }
    }
}

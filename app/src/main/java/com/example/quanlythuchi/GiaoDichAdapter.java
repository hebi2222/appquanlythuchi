package com.example.quanlythuchi;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GiaoDichAdapter
        extends RecyclerView.Adapter<GiaoDichAdapter.Holder> {

    public interface Listener {
        void sua(GiaoDich gd);

        void xoa(GiaoDich gd);
    }

    private final List<GiaoDich> items = new ArrayList<>();
    private final Listener listener;

    public GiaoDichAdapter(Listener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void setData(List<GiaoDich> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_giao_dich,
                        parent,
                        false
                );

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull Holder holder,
            int position
    ) {
        GiaoDich gd = items.get(position);

        boolean thu = gd.loai == GiaoDich.THU;

        int color = Color.parseColor(
                thu ? "#0F766E" : "#C65035"
        );

        holder.title.setText(gd.tieuDe);

        holder.amount.setText(
                (thu ? "+ " : "− ") + GiaoDich.tien(gd.soTien)
        );

        holder.amount.setTextColor(color);

        holder.category.setText(
                (thu ? "THU" : "CHI") + " · " + gd.danhMuc
        );

        holder.category.setTextColor(color);

        holder.meta.setText(
                gd.ma() + " · " + GiaoDich.ngay(gd.thoiGian)
        );

        holder.note.setText(gd.ghiChu);

        holder.note.setVisibility(
                gd.ghiChu.isEmpty() ? View.GONE : View.VISIBLE
        );

        holder.edit.setContentDescription(
                "Sửa giao dịch " + gd.ma()
        );

        holder.delete.setContentDescription(
                "Xóa giao dịch " + gd.ma()
        );

        holder.edit.setOnClickListener(v -> {
            int p = holder.getBindingAdapterPosition();

            if (p != RecyclerView.NO_POSITION) {
                listener.sua(items.get(p));
            }
        });

        holder.delete.setOnClickListener(v -> {
            int p = holder.getBindingAdapterPosition();

            if (p != RecyclerView.NO_POSITION) {
                listener.xoa(items.get(p));
            }
        });
    }

    static class Holder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView amount;
        final TextView category;
        final TextView meta;
        final TextView note;

        final ImageButton edit;
        final ImageButton delete;

        Holder(View view) {
            super(view);

            title = view.findViewById(R.id.tvTitle);
            amount = view.findViewById(R.id.tvAmount);
            category = view.findViewById(R.id.tvCategory);
            meta = view.findViewById(R.id.tvMeta);
            note = view.findViewById(R.id.tvNote);

            edit = view.findViewById(R.id.btnEdit);
            delete = view.findViewById(R.id.btnDelete);
        }
    }
}
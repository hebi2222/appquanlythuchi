package com.example.quanlythuchi;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.Holder> {

    public interface Listener {
        void onEdit(ToDo todo);

        void onDelete(ToDo todo);

        void onStatusChanged(ToDo todo, boolean checked);
    }

    private final List<ToDo> items = new ArrayList<>();
    private final Listener listener;

    private boolean busy;

    public ToDoAdapter(Listener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submit(List<ToDo> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ToDo todo = items.get(position);

        holder.title.setText(todo.title);
        holder.content.setText(todo.content);
        holder.date.setText(todo.date);

        holder.info.setText(
                todo.type + " • " +
                        (todo.done ? "Đã hoàn thành" : "Chưa hoàn thành")
        );

        holder.check.setChecked(todo.done);

        int flags = holder.title.getPaintFlags();

        holder.title.setPaintFlags(
                todo.done
                        ? flags | Paint.STRIKE_THRU_TEXT_FLAG
                        : flags & ~Paint.STRIKE_THRU_TEXT_FLAG
        );

        holder.check.setEnabled(!busy);
        holder.edit.setEnabled(!busy);
        holder.delete.setEnabled(!busy);

        holder.check.setContentDescription("Hoàn thành " + todo.title);
        holder.edit.setContentDescription("Sửa " + todo.title);
        holder.delete.setContentDescription("Xóa " + todo.title);

        holder.edit.setOnClickListener(v -> {
            int p = holder.getBindingAdapterPosition();

            if (p != RecyclerView.NO_POSITION) {
                listener.onEdit(items.get(p));
            }
        });

        holder.delete.setOnClickListener(v -> {
            int p = holder.getBindingAdapterPosition();

            if (p != RecyclerView.NO_POSITION) {
                listener.onDelete(items.get(p));
            }
        });

        holder.check.setOnClickListener(v -> {
            int p = holder.getBindingAdapterPosition();

            if (p != RecyclerView.NO_POSITION) {
                listener.onStatusChanged(
                        items.get(p),
                        holder.check.isChecked()
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        final CheckBox check;
        final TextView title;
        final TextView content;
        final TextView info;
        final TextView date;
        final ImageButton edit;
        final ImageButton delete;

        Holder(View view) {
            super(view);

            check = view.findViewById(R.id.cbDone);
            title = view.findViewById(R.id.tvTitle);
            content = view.findViewById(R.id.tvContent);
            info = view.findViewById(R.id.tvInfo);
            date = view.findViewById(R.id.tvDate);
            edit = view.findViewById(R.id.btnEdit);
            delete = view.findViewById(R.id.btnDelete);
        }
    }
}

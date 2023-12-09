package com.example.frestaurant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private List<Map<String, Object>> announcementList;
    private OnItemClickListener itemClickListener;

    public AnnouncementAdapter(List<Map<String, Object>> announcementList, OnItemClickListener itemClickListener) {
        this.announcementList = announcementList;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用布局文件创建 ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.announcement_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(v -> {
            int position = viewHolder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                itemClickListener.onItemClick(announcementList.get(position));
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 取得 RecyclerView 的 ViewHolder，这里实际上就是你的布局文件
        View itemView = holder.itemView;
        // 取得该位置的公告资料
        Map<String, Object> announcement = announcementList.get(position);

        // 将数据设置到布局文件中的 TextView 中
        TextView titleTextView = itemView.findViewById(R.id.titleTextView);
        TextView contentTextView = itemView.findViewById(R.id.contentTextView);
        titleTextView.setText(announcement.get("title").toString());
        contentTextView.setText(announcement.get("content").toString());

    }

    @Override
    public int getItemCount() {
        return announcementList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(Map<String, Object> announcement);
    }
}

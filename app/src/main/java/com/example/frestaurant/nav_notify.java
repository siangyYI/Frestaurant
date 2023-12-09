package com.example.frestaurant;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class nav_notify extends Fragment {

    public nav_notify() {
        // Required empty public constructor
    }
    private NotificationsAdapter adapter;
    private RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nav_notify, container, false);

        recyclerView = view.findViewById(R.id.notify_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 初始化适配器，但先不设置数据
        adapter = new NotificationsAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 获取通知列表
        getNotifications(); // 修改这个方法以适应异步加载

        return view;
    }
    private void getNotifications() {
        // 获取Firestore实例
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 查询通知数据
        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING) // 假设您想要按时间戳降序排序
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Notification> notifications = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Notification notification = document.toObject(Notification.class);
                                notifications.add(notification);
                            }
                            // 数据加载完成后，设置适配器的数据
                            adapter.setNotifications(notifications);
                        } else {
                            Log.d("getNotifications", "Error getting notifications: ", task.getException());
                        }
                    }
                });
    }
}
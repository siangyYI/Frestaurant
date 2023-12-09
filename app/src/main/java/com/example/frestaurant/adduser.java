package com.example.frestaurant;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class adduser extends Activity {

    private List<CheckboxItem> checkBoxList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CheckboxAdapter checkboxAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adduser);

        // 初始化 Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 查詢 "User" 集合中 isUser 為 null 的文檔並創建複選框
        queryAndCreateCheckboxes();

        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化 Adapter 並設置給 RecyclerView
        checkboxAdapter = new CheckboxAdapter(checkBoxList);
        recyclerView.setAdapter(checkboxAdapter);

        Button signOutButton = findViewById(R.id.fab);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut(); // 從 Firebase 登出
                startActivity(new Intent(adduser.this, nav_home.class));
                finish(); // 結束當前的 Activity
            }
        });
    }

    // 修改 queryAndCreateCheckboxes 方法以刷新 Adapter 數據
    private void queryAndCreateCheckboxes() {
        // 清除現有的複選框
        checkBoxList.clear();

        // 查询 "User" 集合中 isUser 為 null 的文檔
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User")
                .whereEqualTo("isUser", null)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String userName = document.getString("name");
                            String userIdCard = document.getString("UserIdCard");

                            // 根據 "isUser" 的值動態生成複選框
                            checkBoxList.add(new CheckboxItem(document.getId(), userName, userIdCard, false));
                        }

                        // 通知 Adapter 數據已更改
                        checkboxAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 處理查詢失敗的情況
                    }
                });
    }

    // CheckboxItem 類別用於表示每個複選框的數據
    public static class CheckboxItem {
        private String userId;
        private String userName;
        private String userIdCard;
        private boolean isChecked;

        public CheckboxItem(String userId, String userName, String userIdCard, boolean isChecked) {
            this.userId = userId;
            this.userName = userName;
            this.userIdCard = userIdCard;
            this.isChecked = isChecked;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public String getUserIdCard() {
            // 將 UserIdCard 隱藏中間的六碼，只顯示前四碼和後兩碼
            if (userIdCard != null && userIdCard.length() == 10) {
                return userIdCard.substring(0, 4) + "****" + userIdCard.substring(8);
            } else {
                return userIdCard;
            }
        }

        public boolean isChecked() {
            return isChecked;
        }
    }

    // CheckboxAdapter 類別用於管理 RecyclerView 中的數據和視圖
    public class CheckboxAdapter extends RecyclerView.Adapter<CheckboxAdapter.ViewHolder> {

        private List<CheckboxItem> checkboxItemList;

        public CheckboxAdapter(List<CheckboxItem> checkboxItemList) {
            this.checkboxItemList = checkboxItemList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkbox_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CheckboxItem checkboxItem = checkboxItemList.get(position);
            holder.checkbox.setText(checkboxItem.getUserName() + "   " + checkboxItem.getUserIdCard());
            holder.checkbox.setChecked(checkboxItem.isChecked());

            holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        showConfirmationDialog(checkboxItem.getUserId(), holder.getAdapterPosition());
                    } else {
                        updateIsUserValue(checkboxItem.getUserId(), isChecked);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return checkboxItemList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkbox;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                checkbox = itemView.findViewById(R.id.checkbox);
            }
        }
    }

    private void updateIsUserValue(String documentId, boolean isChecked) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 更新 "isUser" 的值
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("isUser", isChecked ? "1" : null);

        db.collection("User").document(documentId)
                .update(updateData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 更新成功
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 更新失敗
                    }
                });
    }

    private void showConfirmationDialog(String documentId, int position) {
        // 使用 AlertDialog 顯示確認訊息
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("確認移除人員");
        builder.setMessage("確定要將人員移除嗎？");

        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 更新 "isUser" 的值為 null
                updateIsUserValue(documentId, true);

                // 手動移除被取消勾選的項目
                checkBoxList.remove(position);

                // 通知 Adapter 數據已更改
                checkboxAdapter.notifyItemRemoved(position);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 恢復原來的勾選狀態
                checkBoxList.get(position).isChecked = false;

                // 通知 Adapter 數據已更改
                checkboxAdapter.notifyItemChanged(position);
            }
        });

        builder.show();
    }
}

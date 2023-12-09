package com.example.frestaurant;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class nav_now extends Fragment {

    private List<CheckboxItem> checkBoxList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CheckboxAdapter checkboxAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_now, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        queryAndCreateCheckboxes();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        checkboxAdapter = new CheckboxAdapter(checkBoxList);
        recyclerView.setAdapter(checkboxAdapter);

        return view;
    }

    private void queryAndCreateCheckboxes() {
        checkBoxList.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User")
                .whereNotEqualTo("isUser", "2")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String isUser = document.getString("isUser");
                            String userName = document.getString("name");
                            String userIdCard = document.getString("UserIdCard");
                            checkBoxList.add(new CheckboxItem(document.getId(), userName, userIdCard, "1".equals(isUser)));
                        }
                        checkboxAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
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
                    if (!isChecked) {
                        showRemoveEmployeeDialog(checkboxItem.getUserId(), holder.getAdapterPosition());
                    } else {
                        showAddEmployeeDialog(checkboxItem.getUserId(), holder.getAdapterPosition());
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
        updateData.put("isUser", isChecked ? "1" : "null");

        db.collection("User").document(documentId)
                .update(updateData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void showAddEmployeeDialog(String userId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("確定要新增該員工嗎？");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String userGender = documentSnapshot.getString("UserGender");
                            String address = documentSnapshot.getString("address");

                            // 在對話框中顯示相關資訊
                            String message =
                                    "姓名: " + name + "\n" +
                                            "性別: " + userGender + "\n" +
                                            "地址: " + address;

                            builder.setMessage(message);
                            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 執行新增員工的程式碼
                                    updateIsUserValue(userId, true);
                                }
                            });
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkBoxList.get(position).isChecked = false;
                                    checkboxAdapter.notifyItemChanged(position);
                                }
                            });

                            builder.show();
                        } else {
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }


    private void showRemoveEmployeeDialog(String userId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("確定要移除該員工嗎？");
        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateIsUserValue(userId, false);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkBoxList.get(position).isChecked = true;
                checkboxAdapter.notifyItemChanged(position);
            }
        });

        builder.show();
    }
}

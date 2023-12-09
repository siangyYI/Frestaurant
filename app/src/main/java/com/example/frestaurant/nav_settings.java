package com.example.frestaurant;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.common.subtyping.qual.Bottom;

import java.util.HashMap;
import java.util.Map;

public class nav_settings extends Fragment {
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private Button admineditButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = currentUser.getEmail();


        docRef = db.collection("User").document(userEmail); // 替换为实际的集合和文档名称

        View rootView = inflater.inflate(R.layout.fragment_nav_settings, container, false);
        TextView nameText=rootView.findViewById(R.id.titleName);
        TextView emailText=rootView.findViewById(R.id.profileEmail);
        TextView genderText=rootView.findViewById(R.id.profileUserGender);
        TextView addressText=rootView.findViewById(R.id.profileAddress);
        TextView jobText=rootView.findViewById(R.id.profileJob);
        admineditButton=rootView.findViewById(R.id.admin_editButton);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // 获取数据
                    String name = documentSnapshot.getString("name");
                    String address = documentSnapshot.getString("address");// 替换为实际的字段名称
                    String email = documentSnapshot.getString("UserEmail");
                    String gender = documentSnapshot.getString("UserGender");
                    String job = documentSnapshot.getString("UserJob");
                    // 处理数据
                    // 在这里，您可以使用获取到的数据执行您的逻辑
                    nameText.setText(name);
                    addressText.setText(address);
                    emailText.setText(email);
                    genderText.setText(gender);
                    jobText.setText(job);
                }
            }
        });
        admineditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // 设置对话框标题
                builder.setTitle("编辑个人资料");

                // 创建一个自定义的布局文件，该布局文件包含了编辑表单
                // 假设你已经有了一个layout文件叫做dialog_edit_profile.xml
                final View customLayout = getLayoutInflater().inflate(R.layout.fragment_admin_edit_profile, null);
                builder.setView(customLayout);

                // 为对话框设置正面按钮（例如“保存”）
                builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 这里获取编辑字段的值，并保存更新
                        EditText nameEditText = customLayout.findViewById(R.id.editTextName);
                        EditText emailEditText = customLayout.findViewById(R.id.editTextEmail);
                        EditText editTextAddress = customLayout.findViewById(R.id.editTextAddress);
                        RadioGroup radioGroupGender = customLayout.findViewById(R.id.radioGroupGender);

                        String name = nameEditText.getText().toString();
                        String email = emailEditText.getText().toString();
                        String address = editTextAddress.getText().toString();
                        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
                        String gender = "";
                        if (selectedGenderId == R.id.radioButtonMale) {
                            gender = "男";
                        } else if (selectedGenderId == R.id.radioButtonFemale) {
                            gender = "女";
                        }
                        updateUserData(email,name,address,gender);
                        // ... 获取其他字段的值

                        // TODO: 更新数据库或本地的用户资料
                    }
                });

                // 为对话框设置负面按钮（例如“取消”）
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击了取消，关闭对话框，不做任何事情
                        dialog.dismiss();
                    }
                });

                // 创建并显示对话框
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        return rootView;
    }
    private void updateUserData(String userId, String name, String gender, String address) {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", name);
        userUpdates.put("UserGender", gender);
        userUpdates.put("address", address);
        // 其他您想要更新的字段...

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User").document(userId)
                .update(userUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 更新成功
                        Toast.makeText(getContext(), "資料更新成功", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 更新失败
                        Toast.makeText(getContext(), "更新失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

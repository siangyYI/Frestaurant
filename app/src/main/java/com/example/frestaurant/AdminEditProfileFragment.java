package com.example.frestaurant;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminEditProfileFragment extends Fragment {
    // UI组件，例如EditTexts
    private EditText editTextName, editTextEmail, editTextAddress, editTextPosition;
    private Button button;
    private RadioGroup radioGroupGender;
    private FirebaseFirestore firestore;
    private String currentUserId;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_edit_profile, container, false);
        // 绑定UI组件
        editTextName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextAddress = view.findViewById(R.id.editTextAddress);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextAddress = view.findViewById(R.id.editTextAddress);
        button=view.findViewById(R.id.buttonCancel);
        radioGroupGender = view.findViewById(R.id.radioGroupGender);
        // 也许从数据库加载当前的用户信息并显示在UI中
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid(); // Use UID as the document id
            loadUserData(); // Load user data from Firestore
        }
        // 保存按钮的监听器
        Button buttonSave = view.findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 收集数据，验证并保存
                saveEmployeeData();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 用户点击取消按钮，执行返回操作
                goBack();
            }
        });
        return view;
        // Inflate the layout for this fragment
    }
    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("User").document(userEmail).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // 假设您的字段名称与UI组件的名称相匹配
                    editTextName.setText(documentSnapshot.getString("name"));
                    editTextEmail.setText(documentSnapshot.getString("UserEmail"));
                    editTextAddress.setText(documentSnapshot.getString("address"));
                    // 根据性别设置RadioGroup
                    String gender = documentSnapshot.getString("UserGender");
                    if ("男".equals(gender)) {
                        radioGroupGender.check(R.id.radioButtonMale);
                    } else if ("女".equals(gender)) {
                        radioGroupGender.check(R.id.radioButtonFemale);
                    }
                    // 处理其他字段...
                } else {
                    Toast.makeText(getContext(), "用戶數據不存在", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "加載用戶數劇失敗", Toast.LENGTH_SHORT).show();
            });
        }
    }
    private void saveEmployeeData() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        String gender = selectedGenderId == R.id.radioButtonMale ? "男" : "女";

        if (name.isEmpty() || email.isEmpty() || address.isEmpty()) {
            Toast.makeText(getContext(), "請填寫所有字段", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = currentUser.getEmail();
        EditProfileFragment.Employee employee = new EditProfileFragment.Employee(name, email, address, gender);
        if (currentUser != null && currentUser.getEmail() != null) {

            // 先读取现有的用户信息
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("User").document(userEmail).get().addOnSuccessListener(documentSnapshot -> {
                // 创建Map来保存需要更新的员工数据
                Map<String, Object> employeeDataToUpdate = new HashMap<>();

                // 检查每个字段是否已更改，如果更改了，就添加到要更新的数据Map中
                if (!name.equals(documentSnapshot.getString("name"))) {
                    employeeDataToUpdate.put("name", name);
                }
                if (!email.equals(documentSnapshot.getString("UserEmail"))) {
                    employeeDataToUpdate.put("UserEmail", email);
                }
                if (!address.equals(documentSnapshot.getString("address"))) {
                    employeeDataToUpdate.put("address", address);
                }
                if (!gender.equals(documentSnapshot.getString("UserGender"))) {
                    employeeDataToUpdate.put("UserGender", gender);
                }
                // 省略其他字段的检查和添加...

                // 如果有要更新的数据，就更新它们
                if (!employeeDataToUpdate.isEmpty()) {
                    db.collection("User").document(userEmail).update(employeeDataToUpdate)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "資料已更新", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "更新失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(getContext(), "沒有變更的資料需要更新", Toast.LENGTH_SHORT).show();
                }

            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "獲取現有資料失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(getContext(), "未能獲取資料", Toast.LENGTH_SHORT).show();
        }
    }

    public static class Employee {
        private String name;
        private String email;
        private String address;
        private String gender;

        public Employee() {
            // Firestore需要空的构造函数
        }

        public Employee(String name, String email, String address, String gender) {
            this.name = name;
            this.email = email;
            this.address = address;
            this.gender = gender;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
    }
    private void goBack() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack(); // 返回到上一个Fragment
        }
    }
}
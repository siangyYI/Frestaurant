package com.example.frestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    Button ButtonLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

    boolean valid = true;
    FirebaseFirestore fStore;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // 如果有當前用戶，清空資料並跳轉到 Login
            mAuth.signOut(); // 清空 Firebase 登錄資料
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        ButtonLogin = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.registerNow);
        fStore = FirebaseFirestore.getInstance();
        progressBar.setVisibility(View.GONE);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 點擊"註冊"文字時跳轉到註冊頁面
                startActivity(new Intent(getApplicationContext(), Register.class));
                finish();
            }
        });

        ButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 檢查 Email 和 Password 的輸入是否有效

                checkField(editTextEmail);
                checkField(editTextPassword);

                if (valid) {
                    progressBar.setVisibility(View.VISIBLE);
                    // 如果輸入有效，進行 Firebase 身份驗證
                    mAuth.signInWithEmailAndPassword(
                            editTextEmail.getText().toString(),
                            editTextPassword.getText().toString()
                    ).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            progressBar.setVisibility(View.GONE);
                            // 登錄成功時的處理
                            Toast.makeText(Login.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                            checkUserAccessLevel(authResult.getUser().getEmail());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            // 登錄失敗時的處理
                            Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    // 如果檢查未通過，不顯示進度條
                    progressBar.setVisibility(View.GONE);
                }

                // 顯示進度條


                String email = String.valueOf(editTextEmail.getText());
                String password = String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    // 如果 Email 或 Password 為空，顯示提示消息
                    Toast.makeText(Login.this, "Enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firebase 登錄，並處理結果
//                mAuth.signInWithEmailAndPassword(email, password)
//                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                // 隱藏進度條
//                                progressBar.setVisibility(View.GONE);
//
//                                if (task.isSuccessful()) {
//                                    // 登錄成功時的處理
//                                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
//
//                                } else {
//                                    // 登錄失敗時的處理
//                                    Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
            }
        });
    }

    private void checkUserAccessLevel(String email) {
        DocumentReference df = fStore.collection("User").document(email);
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String isUserValue = documentSnapshot.getString("isUser");
                if ("1".equals(isUserValue)) {
                    // 如果是一般用戶，跳轉到 MainActivity
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } else if ("2".equals(isUserValue)){
                    // 如果是管理員，跳轉到 MainActivity_admin
                    startActivity(new Intent(getApplicationContext(), MainActivity_admin.class));
                    finish();
                }else if("null".equals(isUserValue)){
                    Toast.makeText(Login.this, "稍等～等待確認", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(Login.this, "User not found", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "isUserValue in else: " + isUserValue);
                    Log.d("TAG", "uid: " + email);
                }
            }
        });
    }

    public boolean checkField(EditText textField) {
        if (TextUtils.isEmpty(textField.getText().toString())) {
            // 如果輸入為空，設置錯誤提示
            textField.setError("Error");
            valid = false;
        } else {
            valid = true;
        }
        return valid;
    }
}

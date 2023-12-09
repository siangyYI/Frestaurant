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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    TextInputEditText editTextEmail,editTextPassword;
    Button ButtonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    boolean valid =true;
    FirebaseFirestore fStore;
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent=new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        editTextEmail=findViewById(R.id.email);
        editTextPassword=findViewById(R.id.password);
        ButtonReg=findViewById(R.id.register_button);
        progressBar=findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        checkField(editTextEmail);
        checkField(editTextPassword);


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        ButtonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkField(editTextEmail);
                checkField(editTextPassword);
                if(valid){
                    mAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(),editTextPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            FirebaseUser user =mAuth.getCurrentUser();
                            Toast.makeText(Register.this,"註冊成功！等待確認",Toast.LENGTH_SHORT).show();
                            DocumentReference df=fStore.collection("User").document(user.getUid());
                            Map<String,Object> userInfo=new HashMap<>();
                            userInfo.put("UserEmail",editTextEmail.getText().toString());
                            userInfo.put("isUser","1");
                            df.set(userInfo);
                            startActivity(new Intent(getApplicationContext(), Login.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Register.this,"建立帳戶失敗",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
    public boolean checkField(EditText textField){
        if(textField.getText().toString().isEmpty()){
            textField.setError("Error");
            valid = false;
        }else {
            valid = true;
        }

        return valid;
    }
}
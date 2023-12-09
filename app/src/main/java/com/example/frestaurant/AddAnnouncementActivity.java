package com.example.frestaurant;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddAnnouncementActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText contentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addannouncement);

        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        Button submitButton = findViewById(R.id.submitButton);
        Button backButton = findViewById(R.id.backButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里处理发送按钮的点击事件
                String title = titleEditText.getText().toString();
                String content = contentEditText.getText().toString();

                // 将标题和内容保存到Firebase
                saveToFirestore(title, content);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里处理返回按钮的点击事件
                finish(); // 关闭当前 Activity
            }
        });
    }

    private void saveToFirestore(String title, String content) {
        // 获取Firebase实例
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 创建一个包含标题和内容的Map
        Map<String, Object> announcementData = new HashMap<>();
        announcementData.put("title", title);
        announcementData.put("content", content);

        // 添加到Firestore的announcement集合中
        db.collection("announcement")
                .add(announcementData)
                .addOnSuccessListener(documentReference -> {
                    // 添加成功
                    // 显示成功的消息
                    showToast("新增成功");

                    // 设置结果为成功
                    setResult(Activity.RESULT_OK);

                    // 关闭当前 Activity
                    finish();

                })
                .addOnFailureListener(e -> {
                    showToast("新增失败");
                });

    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

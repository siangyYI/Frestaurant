package com.example.frestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.frestaurant.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    Button button;
    FirebaseAuth auth;
    FirebaseUser user;
    ActivityMainBinding binding;

    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());
        auth=FirebaseAuth.getInstance();
        ImageView workExit=findViewById(R.id.exit_work);
        bottomNavigationView=findViewById(R.id.bottomNavigationView);
        // 訂閱主題以接收班表更新通知
        FirebaseMessaging.getInstance().subscribeToTopic("staff_schedule_updates")
                .addOnCompleteListener(task -> {
                    String msg = "訂閱成功";
                    if (!task.isSuccessful()) {
                        msg = "訂閱失敗";
                    }
                    Log.d("TAG", msg);
                });
        user= auth.getCurrentUser();


        workExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        FirebaseMessaging.getInstance().subscribeToTopic("staff_schedule_updates")
                .addOnCompleteListener(task -> {
                    String msg = "Subscription successful";
                    if (!task.isSuccessful()) {
                        msg = "Subscription failed";
                    }
                    Log.d("FCM", msg);
                });
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            replaceFragment(new HomeFragment());
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            }
            if (itemId == R.id.date) {
                replaceFragment(new DateFragment());
            }
            if (itemId == R.id.notify) {
                replaceFragment(new NotifyFragment());
            }
            if (itemId == R.id.setting) {
                replaceFragment(new SettingFragment());
            }
            return true;
        });
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }


}

package com.example.frestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity_admin extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private ViewPager viewPager;
    private MenuItem menuItem;
    private BottomNavigationView bottomNavigationView;
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if(item.getItemId()==R.id.nav_home){
                viewPager.setCurrentItem(0);
            } else if (item.getItemId()==R.id.nav_post) {
                viewPager.setCurrentItem(1);
            }else if(item.getItemId()==R.id.nav_now){
                viewPager.setCurrentItem(2);
            }else if(item.getItemId()==R.id.nav_notify){
                viewPager.setCurrentItem(3);
            }else if(item.getItemId()==R.id.nav_settings){
                viewPager.setCurrentItem(4);
            }
            return false;
        }
    };
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new nav_home(),"home");
        adapter.addFragment(new nav_past(),"past");
        adapter.addFragment(new nav_now(),"now");
        adapter.addFragment(new nav_notify(),"notify");
        adapter.addFragment(new nav_settings(),"settings");
        viewPager = findViewById(R.id.viewPagerMain);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(5);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            // ViewPaper選擇到其他頁面時:
            @Override
            public void onPageSelected(int position) {
                // Step06-將相對應的bottomNavigationView選項選取:
                menuItem = bottomNavigationView.getMenu().getItem(position).setChecked(true);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

}
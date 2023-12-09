package com.example.frestaurant;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class nav_past extends Fragment {
    private CalendarView calendarView;
    private EditText editText;
    private String stringDateSelected;
    private DatabaseReference databaseReference;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    TextView textView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_nav_past, container, false);
        calendarView = view.findViewById(R.id.calendarView);
        textView=view.findViewById(R.id.timeSet);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Calender");

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                textView.setText("");
                stringDateSelected = String.format("%04d-%02d-%02d", i, i1 + 1, i2);
                fetchScheduleForSelectedDate(stringDateSelected);
            }
        });
        return view;
    }

    private void fetchScheduleForSelectedDate(String selectedDate) {
        firestore.collection("/User/MkiEGRuFrrSZsFZP0O70LGcy1843/schedule")
                .whereEqualTo("date", selectedDate) // 设置查询条件，匹配 "date" 字段与 selectedDate 相等的文档
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // 处理匹配查询条件的文档

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String shift = document.getString("shift");
                            String date = document.getString("date");
                            String time = document.getString("time");
                            // 处理匹配的文档
                            textView.setText(date + "\n");
                            textView.append("職務："+shift+ "\n");
                            textView.append("時間："+time+ "\n");

                        }
                        if (queryDocumentSnapshots.isEmpty()) {
                            // 如果没有匹配的文档，可以显示适当的消息
                            textView.setText("無班表資料");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 处理读取失败
                        textView.setText("讀取失敗");
                    }
                });
    }
}
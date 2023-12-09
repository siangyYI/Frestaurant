package com.example.frestaurant;

import static android.content.ContentValues.TAG;

import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class HomeFragment extends Fragment {
    private Map<String, Boolean> dateHasDataMap = new HashMap<>();
    private CustomCalendarView CustomCalendarView;
    private String stringDateSelected;
    private DatabaseReference databaseReference;
    private List<String> datesWithEvents;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    TextView textView;
    private String currentEmployeeId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // 注意：Calendar.MONTH 是从 0 开始的，0 表示一月
        TextView worker=view.findViewById(R.id.worker);
        // 加载数据
        loadScheduleData(year, month);
        Button leftBtn = view.findViewById(R.id.left__btn);
        Button rightBtn = view.findViewById(R.id.right__btn);
        textView = view.findViewById(R.id.timeSet);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Calendar currentCalendar = Calendar.getInstance();

        getCurrentEmployeeId();

//        fetchScheduleForLoggedInEmployee(year, month);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // 如果用戶已登錄，獲取用戶的郵箱或UID
            String userEmail = user.getEmail();
            // 使用郵箱或UID從Firestore獲取用戶名
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("User").document(userEmail) // 假設您使用郵箱作為文檔ID
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // 獲取用戶的全名
                            String userName = documentSnapshot.getString("name");
                            // 更新UI

                            worker.setText(userName);
                        } else {
                            // 處理沒有找到用戶名的情況
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 處理錯誤
                    });
        } else {
            // 處理用戶未登錄的情況
        }
        CustomCalendarView = view.findViewById(R.id.CustomCalendarView); // 使用CustomCalendarView
        CustomCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                // 处理日期更改事件的代码
                CustomCalendarView.setCurrentMonth(year, month);
                textView.setText("");
                stringDateSelected = String.format("%04d-%02d-%02d", year, month + 1, day);
//                fetchScheduleForSelectedDate(stringDateSelected);
//                fetchScheduleForLoggedInEmployee(year,month);
                updateUIForSelectedDate(stringDateSelected);
                loadScheduleData(year,month);
            }
        });
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomCalendarView.changeMonth(-1);
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.setTimeInMillis(CustomCalendarView.getDate());

                int newYear = newCalendar.get(Calendar.YEAR);
                int newMonth = newCalendar.get(Calendar.MONTH);
                loadScheduleData(newYear,newMonth);
//                fetchScheduleForLoggedInEmployee(newYear,newMonth);

            }
        });
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomCalendarView.changeMonth(1);
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.setTimeInMillis(CustomCalendarView.getDate());
                int newYear = newCalendar.get(Calendar.YEAR);
                int newMonth = newCalendar.get(Calendar.MONTH);
                loadScheduleData(newYear,newMonth);
//                fetchScheduleForLoggedInEmployee(newYear,newMonth);
            }
        });
        return view;
    }
    // 調用這個方法來獲取當前登入員工的班次
    // 这个方法用来检索当前登录用户的员工ID
    private void getCurrentEmployeeId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("User").document(userEmail)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // 获取到员工ID后，抓取班表
                            currentEmployeeId = documentSnapshot.getString("employeeId");
                        } else {
                            // 处理用户文档中没有employeeId的情况
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 在这里处理任何错误
                    });
        }
    }

    // 该方法用于获取指定员工ID的班次信息
    public void fetchScheduleForLoggedInEmployee(int year, int month) {
        String monthString = String.format("%04d-%02d", year, month + 1);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User")
                .document("a0975349939@gmail.com")
                .collection("schedule")
                .whereGreaterThanOrEqualTo("date", monthString + "-01")
                .whereLessThanOrEqualTo("date", monthString + "-31")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    dateHasDataMap.clear();
                    HashMap<String, String> scheduleDetailsMap = new HashMap<>(); // 用于存储每个日期的班次详情
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String date = documentSnapshot.getId(); // 文档ID即日期
                        Log.d("dateHasDataMap", String.valueOf(dateHasDataMap));
                        if (date != null && !date.isEmpty() && date.startsWith(monthString)) {
                            // 只有当日期开始于指定月份时才加入
                            dateHasDataMap.put(date, true);
                        }
                        scheduleDetailsMap.put(date, ""); // 初始化每个日期的字符串
                        Map<String, Object> schedule = documentSnapshot.getData();
                        if (schedule != null && schedule.containsKey("shifts")) {
                            List<Map<String, Object>> shifts = (List<Map<String, Object>>) schedule.get("shifts");
                            for (Map<String, Object> shift : shifts) {
                                List<Map<String, Object>> detailsList = (List<Map<String, Object>>) shift.get("details");
                                for (Map<String, Object> details : detailsList) {
                                    if (currentEmployeeId.equals(String.valueOf(details.get("employeeId")))) {
                                        String startTime = (String) details.get("starttime");
                                        String endTime = (String) details.get("endtime");
                                        String zone = (String) details.get("zone");
                                        String shiftDetails = "職務：" + zone + "\n時間：" + startTime + "-" + endTime + "\n";
                                        scheduleDetailsMap.put(date, scheduleDetailsMap.get(date) + shiftDetails); // 累加当天的班次详情
                                    }

                                }
                            }
                        }
                    }
                    // 输出日志
                    CustomCalendarView.setDateHasDataMap(dateHasDataMap); // 更新视图
                    // 檢查日期數據map是否為空，來決定是否啟用“產生班表”按鈕
                    CustomCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                        @Override
                        public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                            String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                            String details = scheduleDetailsMap.getOrDefault(selectedDate, "无班次信息\n");
                            textView.setText(selectedDate + ": " + details);
                        }
                    });

                    // 设置初始选择的日期详情
                    String todayDate = String.format("%04d-%02d-%02d", year, month + 1, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                    String todayDetails = scheduleDetailsMap.getOrDefault(todayDate, "无班次信息\n");
                    textView.setText(todayDate + ": " + todayDetails);

                })
                    // ...其他UI更新或处理代码...
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching monthly schedule for employee", e));
    }

    private void loadScheduleData ( int year, int month){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userEmail = user.getEmail(); // 获取用户的电子邮件地址

            if (userEmail != null && !userEmail.isEmpty()) {
                String monthString = String.format("%04d-%02d", year, month + 1); // 构建月份字符串

                firestore.collection("/User/"+userEmail+"/schedule")
                        .whereGreaterThanOrEqualTo("date", monthString + "-01") // 月份的第一天
                        .whereLessThanOrEqualTo("date", monthString + "-30") // 月份的最后一天
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                dateHasDataMap.clear(); // 清空之前的数据
                                HashMap<String, String> scheduleDetailsMap = new HashMap<>();
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    String date = document.getString("date");
                                    Log.d("dateHasDataMap", String.valueOf(dateHasDataMap));
                                    if (date != null && !date.isEmpty() && date.startsWith(monthString)) {
                                        // 只有当日期开始于指定月份时才加入
                                        dateHasDataMap.put(date, true);
                                    }
                                }
                                // 输出日志
                                CustomCalendarView.setDateHasDataMap(dateHasDataMap); // 更新视图
                                // 檢查日期數據map是否為空，來決定是否啟用“產生班表”按鈕
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // 处理错误情况
                                Log.e("HomeFragment", "Error loading schedule data", e);
                            }
                        });
            } else {
                Toast.makeText(getContext(), "电子邮件地址为空", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 用户未登录或无法获取用户信息
            Toast.makeText(getContext(), "无法获取用户信息", Toast.LENGTH_SHORT).show();
        }
    }
    // 这个方法用于更新UI以显示所选日期的排班详情
    public void updateUIForSelectedDate(String selectedDate) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                firestore.collection("/User/" + userEmail + "/schedule")
                        .whereEqualTo("date", selectedDate)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                // 假设每个日期只有一条记录
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                        String shift = document.getString("shift");
                                        String time = document.getString("time");
                                        // 构建排班详情字符串
                                        String shiftDetails = "班次：" + shift + "\n 时间：" + time;
                                        // 更新UI
                                        textView.setText(shiftDetails);
                                    }
                                } else {
                                    // 没有找到记录，清除TextView
                                    textView.setText("没有排班信息");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("updateUIForSelectedDate", "Error loading schedule for " + selectedDate, e);
                                // 处理错误情况，例如显示错误消息
                                textView.setText("无法加载排班信息");
                            }
                        });
            } else {
                // 邮件地址为空
                Toast.makeText(getContext(), "电子邮件地址为空", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 用户未登录或无法获取用户信息
            Toast.makeText(getContext(), "无法获取用户信息", Toast.LENGTH_SHORT).show();
        }
    }
}

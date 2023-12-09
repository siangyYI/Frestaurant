package com.example.frestaurant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;


public class DateFragment extends Fragment {
    private CalendarView calendarView;
    private int year, month, day;
    TextView textView,textView2,startTime,endTime;
    private Map<String, Boolean> dateHasDataMap = new HashMap<>();
    private DatabaseReference databaseReference;
    private String stringDateSelected;
    private CustomCalendarView CustomCalendarView;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private dataService service;
    private RadioGroup radioGroupShifts;
    private Button buttonSubmit;
    private List<String> datesWithEvents;

    private Button button3,leftBtn,rightBtn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_date, container, false);
        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        radioGroupShifts = view.findViewById(R.id.radioGroupShifts);
        CustomCalendarView = view.findViewById(R.id.CustomCalendarViewDate);
        button3=view.findViewById(R.id.submit);
        leftBtn = view.findViewById(R.id.left_btn);
        rightBtn = view.findViewById(R.id.right_btn);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = user.getEmail();
        loadScheduleData(year,month);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080") // 替换为你的服务器地址
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(dataService.class);
        Calendar calendar2 = Calendar.getInstance();
        int year = calendar2.get(Calendar.YEAR);
        int month = calendar2.get(Calendar.MONTH);
        loadScheduleData(year,month);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSaveEvent();
            }
        });
        CustomCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                stringDateSelected = String.format("%04d-%02d-%02d", year, month + 1, day);
                fetchScheduleForSelectedDate(stringDateSelected);
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
                loadScheduleData(newYear, newMonth);

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
                loadScheduleData(newYear, newMonth);
            }
        });
        return view;
    }
    private void submitShift(String shift) {
        // 提交用户选择的时段
        // 这里可以将用户的选择发送到服务器或进行其他处理
        Toast.makeText(getContext(), "已選擇: " + shift, Toast.LENGTH_SHORT).show();
    }
    private void sendDataToServer(MyDataModel myData) {
        service.sendData(myData).enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                // 处理成功的响应...
                Log.d("onResponse","成功的响应");
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                // 处理网络请求失败...
                Log.d("onResponse", String.valueOf(t));
            }
        });
    }

    // 省略其他方法...

    // Retrofit接口定义
    public interface DataService {
        @POST("/api/data")
        Call<ResponseModel> sendData(@Body MyDataModel data);
    }

    // 数据模型类
        class MyDataModel {
        // 省略属性和getter、setter...
        private String date;       // 日期
        private String position;   // 职位
        private String workTime;   // 上班时间

        // Getter和Setter方法
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getWorkTime() {
            return workTime;
        }

        public void setWorkTime(String workTime) {
            this.workTime = workTime;
        }

        // 根据需要添加的其他getter和setter方法
        // ...

        // 根据需要重写的toString方法
        @Override
        public String toString() {
            return "MyDataModel{" +
                    "date='" + date + '\'' +
                    ", position='" + position + '\'' +
                    ", workTime='" + workTime + '\'' +
                    '}';
        }
    }

    // 响应模型类
    class ResponseModel {
        // 省略属性和getter、setter...
    }
    private void fetchScheduleForSelectedDate(String selectedDate) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail(); // 获取用户的电子邮件地址

            if (userEmail != null && !userEmail.isEmpty()) {
                firestore.collection("/User/" + userEmail + "/Schedule")
                        .whereEqualTo("date", selectedDate) // 设置查询条件，匹配 "date" 字段与 selectedDate 相等的文档
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            boolean dataExists = false;
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String date = document.getString("date");
                                String shift = document.getString("shift");

                                dateHasDataMap.put(date, true);
                                dataExists = true;

                                // 更新RadioGroup的选项
                                for (int i = 0; i < radioGroupShifts.getChildCount(); i++) {
                                    RadioButton radioButton = (RadioButton) radioGroupShifts.getChildAt(i);
                                    if (radioButton.getTag().equals(shift)) {
                                        radioGroupShifts.check(radioButton.getId());
                                        break;
                                    }
                                }
                            }

                            CustomCalendarView.setDateHasDataMap(dateHasDataMap);
                            if (!dataExists) {
                                dateHasDataMap.remove(selectedDate); // 如果没有数据，从map中移除这个日期
                                radioGroupShifts.clearCheck(); // 清除RadioGroup的选中状态
                            }
                            CustomCalendarView.invalidate();
                        })
                        .addOnFailureListener(e -> {
                            // 处理读取失败
                            textView.setText("读取失败");
                        });
            } else {
                Toast.makeText(getContext(), "电子邮件地址为空", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 用户未登录或无法获取用户信息
            Toast.makeText(getContext(), "无法获取用户信息", Toast.LENGTH_SHORT).show();
        }
    }
    public void buttonSaveEvent() {
        int selectedShiftId = radioGroupShifts.getCheckedRadioButtonId();
        if (selectedShiftId != -1 && stringDateSelected != null) {
            RadioButton selectedShiftButton = getView().findViewById(selectedShiftId);
            String selectedShift = selectedShiftButton.getText().toString();

            // 获取当前登录用户的电子邮件
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userEmail = user.getEmail(); // 确保用户电子邮件非空
                if (userEmail != null && !userEmail.isEmpty()) {
                    // 将用户选择的班次保存到Firestore
                    Map<String, Object> dataToSave = new HashMap<>();
                    dataToSave.put("date", stringDateSelected);
                    dataToSave.put("shift", selectedShift);

                    firestore.document("User/" + userEmail + "/Schedule/" + stringDateSelected)
                            .set(dataToSave)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "Schedule saved successfully", Toast.LENGTH_SHORT).show();
                                    // 更新日历视图
                                    dateHasDataMap.put(stringDateSelected, true);
                                    CustomCalendarView.setDateHasDataMap(dateHasDataMap);
                                    CustomCalendarView.invalidate(); // 刷新视图
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Error saving schedule", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "Email address is empty", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
            }
        } else {

        }
    }
    private void loadScheduleData ( int year, int month){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userEmail = user.getEmail(); // 获取用户的电子邮件地址

            if (userEmail != null && !userEmail.isEmpty()) {
                String monthString = String.format("%04d-%02d", year, month + 1); // 构建月份字符串

                firestore.collection("/User/"+userEmail+"/Schedule")
                        .whereGreaterThanOrEqualTo("date", monthString + "-01") // 月份的第一天
                        .whereLessThanOrEqualTo("date", monthString + "-30") // 月份的最后一天
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                dateHasDataMap.clear(); // 清空之前的数据
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    String date = document.getString("date");
                                    if (date != null && !date.isEmpty() && date.startsWith(monthString)) {
                                        // 只有当日期开始于指定月份时才加入
                                        dateHasDataMap.put(date, true);
                                    }
                                }
                                // 输出日志
                                CustomCalendarView.setDateHasDataMap(dateHasDataMap); // 更新视图
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
    // Retrofit接口定义
    public interface dataService {
        @POST("/api/data")
        Call<ResponseModel> sendData(@Body MyDataModel data);
    }

    // Retrofit实例创建



}


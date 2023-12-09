package com.example.frestaurant;


import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class nav_home extends Fragment {
    private Map<String, Boolean> dateHasDataMap = new HashMap<>();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private Retrofit retrofit;
    private ScheduleApiService scheduleApiService;
    private CustomCalendarView CustomCalendarView;
    private List<String> datesWithEvents;
    private String stringDateSelected;
    private Button addScheduleButton,deleteScheduleButton;
    ScrollView scrollView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        // 初始化Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080") // 替换为您的API地址
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        loadEmployeeNames();
        // 创建API服务实例
        scheduleApiService = retrofit.create(ScheduleApiService.class);
    }

    public interface ScheduleApiService {
        @POST("/api/optimization/solve")
        Call<Void> solveOptimization(@Query("daysInMonth") int daysInMonth);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_home, container, false);
        CustomCalendarView = view.findViewById(R.id.CustomCalendarView);
        addScheduleButton = view.findViewById(R.id.btn_add_schedule);
        deleteScheduleButton = view.findViewById(R.id.btn_delete_schedule);
        Button leftBtn = view.findViewById(R.id.left_btn);
        Button rightBtn = view.findViewById(R.id.right_btn);
        TextView proFile=view.findViewById(R.id.manager);
        ImageView ExitBtn=view.findViewById(R.id.exit);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        scrollView = view.findViewById(R.id.scrollView);
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

                            proFile.setText(userName);
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
        loadScheduleData(year, month);
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
        deleteScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("確認刪除")
                        .setMessage("您確定要刪除這個班表嗎？")
                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // 用戶確認刪除，進行刪除操作
                                // 假設 'selectedDaySchedule' 是您想要刪除的當前選中的班表對象
                                // 從 Firestore 中刪除對應文檔
                                Log.d("year, month", String.valueOf(month));
                                deleteScheduleForMonth(year, month);
                            }
                        })
                        .setNegativeButton("取消", null) // 無需額外操作，對話框將自動關閉
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        CustomCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                stringDateSelected = String.format("%04d-%02d-%02d", year, month + 1, day);
                Log.d("stringDateSelected",stringDateSelected);
                fetchScheduleForSelectedDate(stringDateSelected);
                loadScheduleData(year, month);

            }
        });

        ExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut(); // 從 Firebase 登出
                startActivity(new Intent(getActivity(), Login.class));
                getActivity().finish(); // 結束當前的 Activity
            }
        });
        addScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int daysInMonth = 30; // 根据实际情况设定
                sendDaysInMonthToServer(daysInMonth);
                sendNotification("班表生成", "班表已成功生成。");
                ProgressDialog progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage("正在生成班表，请稍候...");
                progressDialog.setCancelable(false); // 禁用返回键取消
                progressDialog.show();

// 然后开始异步任务生成班表
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        // 执行长时间运行的操作，例如从服务器获取数据或进行计算

                        // 模拟长时间运行的操作
                        try {
                            Thread.sleep(15000); // 用 Thread.sleep 来模拟长时间运行
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);

                        // 当后台任务完成后，隐藏进度对话框
                        progressDialog.dismiss();

                        // 更新UI，例如显示班表
                    }
                }.execute();

            }
        });

        return view;
    }
    // 在 Fragment 中創建通知通道

    // 发送通知的方法
    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), "myFirebaseChannel")
                .setSmallIcon(R.drawable.notify) // 设置通知图标
                .setContentTitle(title) // 设置通知标题
                .setContentText(message) // 设置通知内容
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 设置优先级
                .setAutoCancel(true); // 设置点击后自动消失

        // 创建 NotificationManagerCompat 实例来触发通知
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());

        // notificationId 是为每个通知定义的唯一的 int 值
        int notificationId = (int) System.currentTimeMillis();
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(notificationId, builder.build());
        // 獲取Firebase Firestore的實例
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 將當前日期和時間格式化為字符串
        String documentId = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

        // 創建一個新的通知記錄對象
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", new Timestamp(new Date()));

        // 使用日期字符串作為文檔ID來添加文檔到Firestore的notifications集合
        db.collection("notifications").document(documentId).set(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error writing document", e);
                });
    }
    private int getColorBasedOnTime(String starttime, String endtime) {
        // 根据时间段返回颜色，这里只是一个简单的例子，您需要根据实际逻辑调整
        int color = Color.WHITE; // 默认颜色
        if (starttime.contains("10:00")) {
            color = ContextCompat.getColor(getActivity(), R.color.morningShift);
        } else if (starttime.contains("16:00")) {
            color = ContextCompat.getColor(getActivity(), R.color.afternoonShift);
        } else if (starttime.contains("18:00")) {
            color = ContextCompat.getColor(getActivity(), R.color.nightShift);
        }
        return color;
    }
    private void createNotificationChannel() {
        // 檢查API版本是否大於等於Android 8.0 (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 創建通道名稱和描述
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            // 使用id, name, 和 importance 創建通道
            NotificationChannel channel = new NotificationChannel("myFirebaseChannel", name, importance);
            channel.setDescription(description);

            // 獲取 NotificationManager 服務
            NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            // 創建通知通道
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void deleteScheduleForMonth(int year, int month) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                // 使用正確的年份和月份格式化字符串
                String monthString = String.format("%04d-%02d", year, month);

                // 建立指向特定用戶特定月份班次的查詢
                firestore.collection("/User/" + userEmail + "/schedule")
                        .whereGreaterThanOrEqualTo("date", monthString + "-01")
                        .whereLessThanOrEqualTo("date", monthString + "-30") // 月份的最後一天可能是31，30，29或28
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            // 遍歷並刪除每個文檔
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                document.getReference().delete()
                                        .addOnSuccessListener(aVoid -> Log.d("Delete", "DocumentSnapshot successfully deleted!"))
                                        .addOnFailureListener(e -> Log.w("Delete", "Error deleting document", e));
                            }
                            Toast.makeText(getContext(), "整個月的班表已刪除", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "刪除失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getContext(), "電子郵件地址為空", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "無法獲取資料", Toast.LENGTH_SHORT).show();
        }
    }
    private void sendDaysInMonthToServer(int daysInMonth) {
        ScheduleApiService apiService = retrofit.create(ScheduleApiService.class);
        Call<Void> call = apiService.solveOptimization(daysInMonth);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // API调用成功，结果已经保存到数据库，不需要处理返回的数据
                    Toast.makeText(getActivity(), "資料已保存到数据库", Toast.LENGTH_SHORT).show();
                } else {
                    // 处理错误的响应
                    Toast.makeText(getActivity(), "请求错误: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 处理请求失败的情况，例如无法连接到服务器
            }
        });
    }
private void fetchScheduleForSelectedDate(String selectedDate) {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user != null) {
        String userEmail = user.getEmail(); // 获取用户的电子邮件地址
        if (userEmail != null && !userEmail.isEmpty()) {
            firestore.collection("/User/" + userEmail + "/schedule")
                    .whereEqualTo("date", selectedDate)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        LinearLayout scheduleContainer = getView().findViewById(R.id.scheduleContainer);
                        scheduleContainer.removeAllViews(); // 清空之前的内容以便重新加载

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            List<Map<String, Object>> shiftsList = (List<Map<String, Object>>) document.get("shifts");
                            if (shiftsList != null) {
                                for (Map<String, Object> shiftDetails : shiftsList) {
                                    List<Map<String, String>> detailsList = (List<Map<String, String>>) shiftDetails.get("details");
                                    if (detailsList != null && !detailsList.isEmpty()) {
                                        String starttime = detailsList.get(0).get("starttime");
                                        String endtime = detailsList.get(0).get("endtime");
                                        StringBuilder scheduleStringBuilder = new StringBuilder();
                                        // 为当前班次构建排班信息
                                        scheduleStringBuilder.append("班次 ").append(" (時間: ").append(starttime).append("-").append(endtime).append(")\n");
                                        for (Map<String, String> employeeDetails : detailsList) {
                                            String id = employeeDetails.get("employeeId");
                                            String name = employeeIdToNameMap.get(id);
                                            String zone = employeeDetails.get("zone");

                                            // 构建字符串，用于更新UI
                                            scheduleStringBuilder.append("員工: ").append(name).append(" - 工作區: ").append(zone).append("\n");

                                        }

                                        // 根据班次的开始时间获取颜色
                                        int color = getColorBasedOnTime(starttime, endtime);

                                        SpannableString spanString = new SpannableString(scheduleStringBuilder.toString());
                                        // 使用自定义的背景色span
                                        int padding = 5; // 您可以根据需要调整这个值
                                        spanString.setSpan(new PaddingBackgroundColorSpan(color, padding), 0, spanString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                        LinearLayout horizontalLayout = new LinearLayout(getContext());
                                        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT));
                                        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                                        horizontalLayout.setGravity(Gravity.CENTER_VERTICAL);

                                        TextView shiftTextView = new TextView(getContext());
                                        shiftTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                                0,
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                1.0f)); // 设置权重为1
                                        shiftTextView.setText(spanString); // 设置当前班次的文本

                                        // 将文本视图和图标添加到水平布局
                                        horizontalLayout.addView(shiftTextView);

                                        // 将水平布局添加到容器中
                                        scheduleContainer.addView(horizontalLayout);
                                    }
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "獲取數據失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "電子郵件為空", Toast.LENGTH_SHORT).show();
        }
    } else {
        Toast.makeText(getContext(), "無法獲取用戶資料", Toast.LENGTH_SHORT).show();
    }
}

// ...其他方法...

    private Map<String, String> employeeIdToNameMap = new HashMap<>();
    private void loadEmployeeNames() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User") // 指定正確的集合名稱
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String employeeId = document.getString("employeeId"); // 獲取 employeeId 字段
                        String name = document.getString("name"); // 獲取 name 字段
                        if (employeeId != null && name != null) {
                            employeeIdToNameMap.put(employeeId, name); // 將 employeeId 和對應的 name 添加到映射中
                        }
                    }
                    // 在這裡調用或通知數據已經加載完畢的任何相關方法
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting employee names", e);
                });
    }

    private void loadScheduleData ( int year, int month){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userEmail = user.getEmail(); // 获取用户的电子邮件地址

            if (userEmail != null && !userEmail.isEmpty()) {
                String monthString = String.format("%04d-%02d", year, month + 1); // 构建月份字符串

                firestore.collection("/User/a0975349939@gmail.com/schedule")
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
                                // 檢查日期數據map是否為空，來決定是否啟用“產生班表”按鈕
                                if (!dateHasDataMap.isEmpty()) {
                                    // 如果map不為空，表示有班表資料，禁用按鈕
                                    deleteScheduleButton.setEnabled(true);
                                    addScheduleButton.setEnabled(false);
                                } else {
                                    // 如果map為空，表示沒有班表資料，啟用按鈕
                                    deleteScheduleButton.setEnabled(false);
                                    addScheduleButton.setEnabled(true);
                                }
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
                Toast.makeText(getContext(), "電子郵件為空", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 用户未登录或无法获取用户信息
            Toast.makeText(getContext(), "無法獲取用戶資料", Toast.LENGTH_SHORT).show();
        }
    }
}

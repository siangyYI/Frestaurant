package com.example.frestaurant;

import java.util.Date;

public class Notification {
    private String title;
    private String message;
    private Date timestamp;

    // 无参构造函数，Firebase在反序列化时需要
    public Notification() {
    }

    // 带参构造函数
    public Notification(String title, String message, Date timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getter和Setter方法
    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}

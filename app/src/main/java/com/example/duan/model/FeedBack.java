package com.example.duan.model;

public class FeedBack {
    private String id;
    private String content;
    private User user;
    private String date;
    private Order order;

    public FeedBack() {
    }

    public FeedBack(String id, String content, User user, String date, Order order) {
        this.id = id;
        this.content = content;
        this.user = user;
        this.date = date;
        this.order = order;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}

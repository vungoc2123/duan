package com.example.duan.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order {
    private String id;
    private User user;
    private List<Table> tables;
    private List<OrderDish> dishes;
    private String date;
    private String startTime;
    private String endTime;
    private String note;
    private int numberOfPeople;
    private double total;
    private int status;

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("tables", tables);
        result.put("dishes", dishes);
        result.put("date", date);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("note", note);
        result.put("numberOfPeople", numberOfPeople);
        result.put("total", total);
        result.put("status", status);
        return result;
    }

    public Order() {
    }


    public Order(String id, User user, List<Table> tables, List<OrderDish> dishes, String date, String startTime, String endTime, String note, int numberOfPeople, double total, int status) {
        this.id = id;
        this.user = user;
        this.tables = tables;
        this.dishes = dishes;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.note = note;
        this.numberOfPeople = numberOfPeople;
        this.total = total;
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<OrderDish> getDishes() {
        return dishes;
    }

    public void setDishes(List<OrderDish> dishes) {
        this.dishes = dishes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal() {
        double temp = 0;
        for (OrderDish orderDish : dishes) {
            temp = temp + (orderDish.getDish().getGia()*orderDish.getQuantity());
        }
        this.total = temp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}

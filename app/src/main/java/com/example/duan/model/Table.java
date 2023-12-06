package com.example.duan.model;

import java.util.HashMap;
import java.util.Map;

public class Table {

    String id;
    int seat;
    int number;


    public Table() {
    }


    public Table(String id, int seat, int number) {
        this.id = id;
        this.seat = seat;
        this.number = number;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;

    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("seat", seat);
        result.put("number", number);
        return result;
    }
}

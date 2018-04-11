package com.model;

import java.io.Serializable;

/**
 * Created by HeXi on 2018/4/5.
 */
public class FlightUser implements Serializable {
    private long id;

    private String name;

    private String cardNo;

    private String phone;

    public FlightUser() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

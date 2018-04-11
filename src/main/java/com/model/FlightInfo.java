package com.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by HeXi on 2018/4/5.
 */
public class FlightInfo implements Serializable {
    private long id;
    private String depCity;
    private String arrCity;
    // yyyy-MM-dd
    private String depDate;
    // 用逗号分割
    private String flightNo;
    private String depTime;
    private double price;
    private int seat;
    private Date createTime;

    public FlightInfo() {
    }

    public FlightInfo(long id, String depCity, String arrCity, String depDate, String flightNo, String depTime, double price, int seat, Date createTime) {
        this.id = id;
        this.depCity = depCity;
        this.arrCity = arrCity;
        this.depDate = depDate;
        this.flightNo = flightNo;
        this.depTime = depTime;
        this.price = price;
        this.seat = seat;
        this.createTime = createTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDepCity() {
        return depCity;
    }

    public void setDepCity(String depCity) {
        this.depCity = depCity;
    }

    public String getArrCity() {
        return arrCity;
    }

    public void setArrCity(String arrCity) {
        this.arrCity = arrCity;
    }

    public String getDepDate() {
        return depDate;
    }

    public void setDepDate(String depDate) {
        this.depDate = depDate;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public String getDepTime() {
        return depTime;
    }

    public void setDepTime(String depTime) {
        this.depTime = depTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

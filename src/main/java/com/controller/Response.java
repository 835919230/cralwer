package com.controller;

import java.io.Serializable;

/**
 * Created by HeXi on 2018/4/5.
 */
public class Response<T> implements Serializable {
    private boolean success;

    private T data;

    private String msg;

    public Response() {
    }

    public Response(boolean success, T data, String msg) {
        this.success = success;
        this.data = data;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

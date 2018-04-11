package com.service;

/**
 * Created by HeXi on 2018/4/6.
 */
public class BizException extends RuntimeException {
    private final String bizErrorMsg;

    public BizException(String message, String bizErrorMsg) {
        super(message);
        this.bizErrorMsg = bizErrorMsg;
    }

    public BizException(String bizErrorMsg) {
        this(bizErrorMsg, bizErrorMsg);
    }

    public String getBizErrorMsg() {
        return bizErrorMsg;
    }
}

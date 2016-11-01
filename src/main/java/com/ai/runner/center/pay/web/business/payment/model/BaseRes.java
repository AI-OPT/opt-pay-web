package com.ai.runner.center.pay.web.business.payment.model;

import java.io.Serializable;

/**
 * 支付平台同步请求响应报文
 *
 * @param <T>
 * Date: 2015年12月11日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public class BaseRes implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 返回码<br>
     * 00: 成功<br>
     * 01: 失败<br>
     */
    private String returnCode;
    
    /**
     * 错误码
     */
    private String errCode;
    
    /**
     * 错误信息
     */
    private String errMsg;
    
    public BaseRes() {

    }
       
    public BaseRes(String returnCode, String errCode, String errMsg) {
        super();
        this.returnCode = returnCode;
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
    
}

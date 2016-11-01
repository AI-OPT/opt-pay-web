package com.ai.runner.center.pay.web.system.constants;

/**
 * 支付中心异常编码定义
 *
 * Date: 2015年11月2日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public final class ExceptCodeConstants {

    private ExceptCodeConstants() {

    }

    /**
     * 系统异常
     */
    public static final String SYSTEM_ERROR = "999999";

    /**
     * 参数为空
     */
    public static final String PARAM_IS_NULL = "888888";

    /**
     * 第三方交易失败
     */
    public static final String TRADE_FAIL = "777777";
    /**
     * 参数有误
     */
    public static final String PARAM_IS_WRONG = "000002";
    
    /**
     * 参数非法
     */
    public static final String ILLEGAL_PARAM = "000003";
    
    /**
     * 重复交易
     */
    public static final String REPEATABLE_TRADE = "000004";

    /**
     * 未识别的合作方
     */
    public static final String ILLEGAL_PARTNER = "000005";
    
    /**
     * 错误响应地址
     */
    public static final String WRONG_ACTION = "000006";
    
    /**
     * 交易不存在
     */
    public static final String TRADE_NOT_EXIST = "000007";
    

    
}

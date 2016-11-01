package com.ai.runner.center.pay.web.system.exception;

/**
 * 支付平台自定义异常基类
 *
 * Date: 2015年11月10日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public abstract class AbstractPayException extends Exception {

    private static final long serialVersionUID = -8045568104107183112L;

    public AbstractPayException() {
        super();
    }

    public AbstractPayException(String message, Throwable cause) {
        super(message, cause);
    }

    public AbstractPayException(String message) {
        super(message);
    }

    public AbstractPayException(Throwable cause) {
        super(cause);
    }

}

package com.ai.runner.center.pay.web.business.payment.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 支付平台后台类交易自定义注解
 *
 * Date: 2016年1月6日 <br>
 * Copyright (c) 2016 asiainfo.com <br>
 * @author fanpw
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BackTransService {

}

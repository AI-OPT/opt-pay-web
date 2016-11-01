package com.ai.runner.center.pay.web.system.base;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Controller基类
 * Date: 2015年10月9日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author LiangMeng
 */
public class BaseController {
    
    @Autowired
    private HttpServletRequest request;
    
    public ServletContext getServletContext() {
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();     
        return webApplicationContext.getServletContext(); 
    }
    
    public String getRealPath() {
        return getServletContext().getRealPath("/");
    }
        
    public String getPayCenterWebAppPath() {
        return request.getScheme() + "://" + request.getServerName() + ":"+ request.getServerPort() + request.getContextPath();
    }
    
}

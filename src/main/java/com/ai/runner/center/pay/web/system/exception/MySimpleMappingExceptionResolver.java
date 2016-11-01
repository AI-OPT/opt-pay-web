package com.ai.runner.center.pay.web.system.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.ai.runner.base.exception.BusinessException;
import com.ai.runner.base.exception.CallerException;
import com.ai.runner.base.exception.SystemException;
import com.ai.runner.center.pay.web.business.payment.annotation.BackTransService;
import com.ai.runner.center.pay.web.business.payment.model.BaseRes;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.alibaba.dubbo.common.json.JSONObject;
import com.alibaba.fastjson.JSON;

/**
 * 异常扩展
 * Date: 2015年12月7日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author LiangMeng
 */
public class MySimpleMappingExceptionResolver extends
		SimpleMappingExceptionResolver {
    
	private static final Logger LOG = Logger.getLogger(MySimpleMappingExceptionResolver.class);
	/**
	 * 响应客户端结果 成功、失败、错误
	 */
	private static final String RES_RESULT = "RES_RESULT";
	/**
	 * 响应客户端结果描述
	 */
	private static final String RES_MSG = "RES_MSG";
	/**
	 * 响应客户端数据
	 */
	private static final String RES_DATA = "RES_DATA";
	/**
	 * 响应客户端结果 失败
	 */
	private static final String FAILED = "FAILED";

	private static final String CODE = "CODE";
	
    @Override
    protected ModelAndView doResolveException(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex) {
        String viewName = determineViewName(ex, request);
        String _code = "";
        String _detail = "";
        if (ex instanceof BusinessException) {
            _code = ((BusinessException) ex).getErrorCode();
            _detail = ((BusinessException) ex).getErrorMessage();
        }
        LOG.error("捕捉到的异常信息[message:" + ex.getMessage() + " ;code:" + _code + " ;detail:" + _detail
                + "]", ex);
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            if(method.isAnnotationPresent(BackTransService.class)) {
                this.handlerBackTransException(response, ex);
                return new ModelAndView();
            }
        }

        if (viewName != null) {// JSP格式返回
            if (!(request.getHeader("accept").indexOf("application/json") > -1 || (request
                    .getHeader("X-Requested-With") != null && request.getHeader("X-Requested-With")
                    .indexOf("XMLHttpRequest") > -1))) {
                // 如果不是异步请求
                Integer statusCode = determineStatusCode(request, viewName);
                if (statusCode != null) {
                    applyStatusCodeIfPossible(request, response, statusCode);
                }
                return getModelAndView(viewName, ex, request);
            } else {// JSON格式返回
                try {
                    setprintWriter(response, ex.getMessage(), _code, null);
                } catch (Exception e) {
                    LOG.error(
                            "MySimpleMappingExceptionResolver.doResolveException错误:"
                                    + e.getMessage(), e);
                }
                return new ModelAndView();

            }
        } else {
            return null;
        }
    }
	
	/**
	 * 支付平台同步交易异常统一处理
	 * @param response
	 * @param ex
	 * @author fanpw
	 * @ApiDocMethod
	 * @ApiCode
	 */
    private void handlerBackTransException(HttpServletResponse response, Exception ex) {
        BaseRes errRes = this.toResponse(ex);
        PrintWriter printWriter = null;
        try {
            printWriter = response.getWriter();
            printWriter.write(JSON.toJSONString(errRes));
            printWriter.flush();
        } catch (IOException e) {
            LOG.error("支付平台同步返回出错", e);
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }

    }
	
	/**
	 * 支付平台同步接口异常转换
	 * @param ex
	 * @return
	 * @author fanpw
	 * @ApiDocMethod
	 * @ApiCode
	 */
	private BaseRes toResponse(Exception ex) {
	    String returnFaild = PayConstants.ReturnCode.FAILD;
        if (ex instanceof CallerException) {
            CallerException e = (CallerException) ex;
            return new BaseRes(returnFaild, e.getErrorCode(), e.getErrorMessage());
        }    
        
        if (ex instanceof BusinessException) {
            BusinessException e = (BusinessException) ex;
            return new BaseRes(returnFaild, e.getErrorCode(), e.getErrorMessage());
        }   
        
        if (ex instanceof SystemException) {
            SystemException e = (SystemException) ex;
            return new BaseRes(returnFaild, e.getErrorCode(), e.getErrorMessage());
        }
        
        if (ex instanceof NullPointerException) {
            return new BaseRes(returnFaild, ExceptCodeConstants.SYSTEM_ERROR, "空指针异常，请联系支付中心处理");
        }
        
        return new BaseRes(returnFaild, ExceptCodeConstants.SYSTEM_ERROR, "系统异常，请联系支付中心处理。异常摘要:" + ex.getMessage());
	}

	private void setprintWriter(HttpServletResponse response, String msg,
			String code, JSONObject data) {
		PrintWriter printWriter = null;
		try {
			printWriter = response.getWriter();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(RES_RESULT, FAILED);
			jsonObject.put(RES_MSG, msg);
			jsonObject.put(CODE, code);
			jsonObject.put(RES_DATA, data);
			printWriter.write(jsonObject.toString());
			printWriter.flush();

		} catch (Exception e) {
			LOG.error("MySimpleMappingExceptionResolver.setprintWriter异常："+e.getMessage(), e);
			throw new BusinessException("", e.getMessage());
		} finally {
		    if(printWriter != null){
		        printWriter.close();
		    }
		}
	}
	
}

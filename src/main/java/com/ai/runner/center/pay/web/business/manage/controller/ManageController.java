package com.ai.runner.center.pay.web.business.manage.controller;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.opt.sdk.dubbo.util.DubboConsumerFactory;
import com.ai.opt.sdk.util.CollectionUtil;
import com.ai.runner.center.common.api.tenant.interfaces.IGnTenantQuerySV;
import com.ai.runner.center.common.api.tenant.param.GnTenantVo;
import com.ai.runner.center.pay.api.tenantconfig.interfaces.ITenantConfigSV;
import com.ai.runner.center.pay.api.tenantconfig.param.TenantConfigParam;
import com.ai.runner.center.pay.api.tenantconfig.param.TenantConfigQryParam;
import com.ai.runner.center.pay.web.business.manage.model.AlipayConfigParam;
import com.ai.runner.center.pay.web.business.manage.model.CommonConfigParam;
import com.ai.runner.center.pay.web.business.manage.model.WeixinConfigParam;
import com.ai.runner.center.pay.web.business.manage.model.YlConfigParam;
import com.ai.runner.center.pay.web.system.base.BaseController;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.ai.runner.center.pay.web.system.util.JSONUtil;

/**
 * 支付平台管理后台主Controller Date: 2015年10月10日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author LiangMeng
 */
@Controller
@RequestMapping(value = "/manage")
public class ManageController extends BaseController {

    private static final Logger LOG = Logger.getLogger(ManageController.class);

    /**
     * 首页
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author LiangMeng
     * @ApiDocMethod
     */
    @RequestMapping(value = "/index")
    public String index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("后台管理页面首页..."); 
        /*1.查询租户信息*/
//        IGnTenantQuerySV gnTenantQuerySV = DubboConsumerFactory.getService("IGnTenantQuerySV");
//        List<GnTenantVo> tenantList = gnTenantQuerySV.getTenants();
//        if(!CollectionUtil.isEmpty(tenantList)){
//            request.setAttribute("tenantCount", tenantList.size());
//            request.setAttribute("tenantList", tenantList);
//        }
        return "/manage/index";
    }

    /**
     * 登出
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author LiangMeng
     * @ApiDocMethod
     */
    @RequestMapping(value = "/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("登出...");
        return "/manage/login";
    }

    /**
     * 登录
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author LiangMeng
     * @ApiDocMethod
     */
    @RequestMapping(value = "/login")
    public String login(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("登录...");
        return "/manage/index";
    }

    /**
     * 租户配置
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author LiangMeng
     * @ApiDocMethod
     */
    @RequestMapping(value = "/tenantConfig")
    public String tenantConfig(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        LOG.info("租户配置...");
        IGnTenantQuerySV gnTenantQuerySV = DubboConsumerFactory.getService("IGnTenantQuerySV");
        List<GnTenantVo> tenantList = gnTenantQuerySV.getTenants();
        request.setAttribute("tenantList", tenantList);
        return "/manage/tenantConfig";
    }
    /**
     * 配置页FORM格式
     * @param request
     * @param response
     * @param tenantId
     * @return
     * @throws Exception
     * @author LiangMeng
     * @ApiDocMethod
     */
    @RequestMapping(value = "/tenantConfigFORM", method = RequestMethod.POST)
    public String tenantConfigFORM(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "tenantId", required = true)
            String tenantId) throws Exception {
        LOG.info("tenantConfigFORM..."); 
        LOG.info("输入参数tenantId:["+tenantId+"]");
        ITenantConfigSV tenantConfigSV = DubboConsumerFactory.getService("ITenantConfigSV");
        TenantConfigQryParam tenantConfigQryParam = new TenantConfigQryParam();
        tenantConfigQryParam.setTenantId(tenantId);
        List<TenantConfigParam> tenantConfigList = tenantConfigSV.queryTenantConfigByTenantId(tenantConfigQryParam);
        request.setAttribute("tenantConfigList", tenantConfigList);
        if(!CollectionUtil.isEmpty(tenantConfigList)){
            for(TenantConfigParam param:tenantConfigList){
                JSONObject json = new JSONObject(param.getConfigInfo());         
                request.setAttribute(param.getConfigType(), JSONUtil.parseToMap(json));
            }
        }
        request.setAttribute("tenantId", tenantId);
        return "/manage/tenantConfigFORM";
    }
    
    
    /**
     * 配置修改页FORM格式
     * @param request
     * @param response
     * @param tenantId
     * @return
     * @throws Exception
     * @author LiangMeng
     * @ApiDocMethod
     */
    @RequestMapping(value = "/modifyAlipayTenantConfigFORM", method = RequestMethod.POST)
    public void modifyAlipayTenantConfigFORM(HttpServletRequest request, HttpServletResponse response,
            AlipayConfigParam alipayConfigParam) throws Exception {
        LOG.info("modifyAlipayTenantConfigFORM..."); 
        LOG.info("输入参数tenantId:["+alipayConfigParam.getTenantId()+"]");
        PrintWriter printWriter = null;
        try {
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            JSONObject json = new JSONObject();
            json.put("web_seller_email", alipayConfigParam.getWebSellerEmail());
            json.put("web_seller_PID", alipayConfigParam.getWebSellerPID());
            json.put("web_seller_key", alipayConfigParam.getWebSellerKey());
            json.put("wap_seller_email",alipayConfigParam.getWapSellerEmail() );
            json.put("wap_seller_PID", alipayConfigParam.getWapSellerPID());
            json.put("wap_seller_key", alipayConfigParam.getWapSellerKey());
            json.put("batch_trans_seller_email",alipayConfigParam.getBatchTransSellerEmail() );
            json.put("batch_trans_acct_name", alipayConfigParam.getBatchTransAcctName());
            json.put("batch_trans_seller_PID", alipayConfigParam.getBatchTransSellerPID());
            json.put("batch_trans_seller_key", alipayConfigParam.getBatchTransSellerKey());
            ITenantConfigSV tenantConfigSV = DubboConsumerFactory.getService("ITenantConfigSV");
            TenantConfigParam tenantConfigParam = new TenantConfigParam();
            tenantConfigParam.setConfigInfo(json.toString());
            tenantConfigParam.setConfigType(PayConstants.PayOrgCode.ZFB);
            tenantConfigParam.setTenantId(alipayConfigParam.getTenantId());
            tenantConfigParam.setState("0");
            tenantConfigSV.createOrModifyTenantConfig(tenantConfigParam);
            ConfigUtil.addProperty(tenantConfigParam.getTenantId(), PayConstants.PayOrgCode.ZFB, json.toString());
            printWriter.println("SUCCESS");
            printWriter.flush();
        } catch (Exception e) {
            LOG.error("修改支付宝配置信息出错"+e.getMessage(),e);
            printWriter.println("FAIL");
            printWriter.flush();
        }
    }
    
    @RequestMapping(value = "/modifyWeixinTenantConfigFORM", method = RequestMethod.POST)
    public void modifyWeixinTenantConfigFORM(HttpServletRequest request, HttpServletResponse response,
            WeixinConfigParam weixinConfigParam) throws Exception {
        LOG.info("modifyWeixinTenantConfigFORM..."); 
        LOG.info("输入参数tenantId:["+weixinConfigParam.getTenantId()+"]");
        PrintWriter printWriter = null;
        try {
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            JSONObject json = new JSONObject();
            json.put("WEIXIN_APPID", weixinConfigParam.getWeixinAppid());
            json.put("WEIXIN_MCH_ID", weixinConfigParam.getWeixinMchId());
            json.put("WEIXIN_APPSECRET", weixinConfigParam.getWeixinAppsecret());
            json.put("WEIXIN_API_KEY",weixinConfigParam.getWeixinApiKey());
            json.put("WEIXIN_CER_PATH", weixinConfigParam.getWeixinCerPath());
            ITenantConfigSV tenantConfigSV = DubboConsumerFactory.getService("ITenantConfigSV");
            TenantConfigParam tenantConfigParam = new TenantConfigParam();
            tenantConfigParam.setConfigInfo(json.toString());
            tenantConfigParam.setConfigType(PayConstants.PayOrgCode.WEIXIN);
            tenantConfigParam.setTenantId(weixinConfigParam.getTenantId());
            tenantConfigParam.setState("0");
            tenantConfigSV.createOrModifyTenantConfig(tenantConfigParam);
            ConfigUtil.addProperty(weixinConfigParam.getTenantId(), PayConstants.PayOrgCode.WEIXIN, json.toString());
            printWriter.println("SUCCESS");
            printWriter.flush();
        } catch (Exception e) {
            LOG.error("修改支付宝配置信息出错"+e.getMessage(),e);
            printWriter.println("FAIL");
            printWriter.flush();
        }
    }
    @RequestMapping(value = "/modifyYlTenantConfigFORM", method = RequestMethod.POST)
    public void modifyYlTenantConfigFORM(HttpServletRequest request, HttpServletResponse response,
            YlConfigParam ylConfigParam) throws Exception {
        LOG.info("modifyYlTenantConfigFORM..."); 
        LOG.info("输入参数tenantId:["+ylConfigParam.getTenantId()+"]");
        PrintWriter printWriter = null;
        try {
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            JSONObject json = new JSONObject();
            json.put("merId", ylConfigParam.getMerId());
            json.put("acpsdk.signCert.path", ylConfigParam.getSignCertPath());
            json.put("acpsdk.signCert.pwd", ylConfigParam.getSignCertPwd());
            ITenantConfigSV tenantConfigSV = DubboConsumerFactory.getService("ITenantConfigSV");
            TenantConfigParam tenantConfigParam = new TenantConfigParam();
            tenantConfigParam.setConfigInfo(json.toString());
            tenantConfigParam.setConfigType(PayConstants.PayOrgCode.YL);
            tenantConfigParam.setTenantId(ylConfigParam.getTenantId());
            tenantConfigParam.setState("0");
            tenantConfigSV.createOrModifyTenantConfig(tenantConfigParam);
            ConfigUtil.addProperty(ylConfigParam.getTenantId(), PayConstants.PayOrgCode.YL, json.toString());
            printWriter.println("SUCCESS");
            printWriter.flush();
        } catch (Exception e) {
            LOG.error("修改支付宝配置信息出错"+e.getMessage(),e);
            printWriter.println("FAIL");
            printWriter.flush();
        }
    }
    @RequestMapping(value = "/modifyCommonTenantConfigFORM", method = RequestMethod.POST)
    public void modifyCommonTenantConfigFORM(HttpServletRequest request, HttpServletResponse response,
            CommonConfigParam commonConfigParam) throws Exception {
        LOG.info("modifyYlTenantConfigFORM..."); 
        LOG.info("输入参数tenantId:["+commonConfigParam.getTenantId()+"]");
        PrintWriter printWriter = null;
        try {
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            JSONObject json = new JSONObject();
            json.put("request_key", commonConfigParam.getRequestKey());
            ITenantConfigSV tenantConfigSV = DubboConsumerFactory.getService("ITenantConfigSV");
            TenantConfigParam tenantConfigParam = new TenantConfigParam();
            tenantConfigParam.setConfigInfo(json.toString());
            tenantConfigParam.setConfigType(ConfigUtil.COMMON);
            tenantConfigParam.setTenantId(commonConfigParam.getTenantId());
            tenantConfigParam.setState("0");
            tenantConfigSV.createOrModifyTenantConfig(tenantConfigParam);
            ConfigUtil.addTenantCommonProperty(commonConfigParam.getTenantId(), json.toString());
            printWriter.println("SUCCESS");
            printWriter.flush();
        } catch (Exception e) {
            LOG.error("修改支付宝配置信息出错"+e.getMessage(),e);
            printWriter.println("FAIL");
            printWriter.flush();
        }
    }
    /**
     * 配置页JSON格式
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author LiangMeng
     * @ApiDocMethod
     */
    @RequestMapping(value = "/tenantConfigJSON", method = RequestMethod.POST)
    public String tenantConfigJSON(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "tenantId", required = true)
            String tenantId) throws Exception {
        LOG.info("tenantConfigJSON..."); 
        LOG.info("输入参数tenantId:["+tenantId+"]");
        ITenantConfigSV tenantConfigSV = DubboConsumerFactory.getService("ITenantConfigSV");
        TenantConfigQryParam tenantConfigQryParam = new TenantConfigQryParam();
        tenantConfigQryParam.setTenantId(tenantId);
        List<TenantConfigParam> tenantConfigList = tenantConfigSV.queryTenantConfigByTenantId(tenantConfigQryParam);
        request.setAttribute("tenantConfigList", tenantConfigList);
        if(!CollectionUtil.isEmpty(tenantConfigList)){
            for(TenantConfigParam param:tenantConfigList){
                request.setAttribute(param.getConfigType(), param);
            }
        }
        return "/manage/tenantConfigJSON";
    }

    /**
     * 租户配置
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author LiangMeng
     * @ApiDocMethod
     */
    @RequestMapping(value = "/tables")
    public String tables(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("信息查询...");
        return "/manage/tables";
    }

    /**
     * 租户配置
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author LiangMeng
     * @ApiDocMethod
     */
    @RequestMapping(value = "/api")
    public String api(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("API...");
        return "/manage/api";
    }
    
    

}

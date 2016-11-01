package com.ai.runner.center.pay.web.business.payment.controller.third;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.opt.sdk.util.DateUtil;
import com.ai.opt.sdk.util.StringUtil;
import com.ai.runner.base.exception.BusinessException;
import com.ai.runner.base.exception.SystemException;
import com.ai.runner.center.pay.api.tradequery.param.TradeRecord;
import com.ai.runner.center.pay.web.business.payment.annotation.BackTransService;
import com.ai.runner.center.pay.web.business.payment.controller.core.TradeBaseController;
import com.ai.runner.center.pay.web.business.payment.model.CommonPayRes;
import com.ai.runner.center.pay.web.business.payment.model.WithdrawRes;
import com.ai.runner.center.pay.web.business.payment.util.core.PaymentNotifyUtil;
import com.ai.runner.center.pay.web.business.payment.util.core.ResponseUtil;
import com.ai.runner.center.pay.web.business.payment.util.core.VerifyUtil;
import com.ai.runner.center.pay.web.business.payment.util.third.cib.AppClientServiceHolder;
import com.ai.runner.center.pay.web.system.configcenter.AbstractPayConfigManager;
import com.ai.runner.center.pay.web.system.configcenter.XyPayConfigManager;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.AmountUtil;
import com.ai.runner.center.pay.web.system.util.ConfigFromFileUtil;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.alibaba.fastjson.JSONObject;
import com.cib.cap4i.appsvr.base.base.AppResponse;
import com.cib.cap4i.appsvr.base.common.AppException;
import com.cib.cap4i.appsvr.client.AppClientService;

/**
 * 兴业银行 Date: 2015年12月28日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author LiangMeng
 */
@Controller
@RequestMapping(value = "/xypay")
public class XyPayController extends TradeBaseController {

    private static final Logger LOG = Logger.getLogger(XyPayController.class);

    /** 支付后台通知地址 **/
    private static final String XY_NOTIFY_URL = "/xypay/xyNotify";

    @BackTransService
    @RequestMapping(value = "/appPay")
    public void appPay(@RequestParam(value = "tenantId", required = true)
    String tenantId, @RequestParam(value = "orderId", required = true)
    String orderId, HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("兴业APP支付...");
        PrintWriter printWriter = null;
        CommonPayRes commonPayRes = new CommonPayRes();
        commonPayRes.setTenantId(tenantId);
        commonPayRes.setOrderId(orderId);
        commonPayRes.setPayOrgCode(XyPayConfigManager.PAY_ORG_NAME);
        commonPayRes.setReturnCode(PayConstants.ReturnCode.FAILD);
        try {
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            /* 1.接收调用端传过来的信息 */
            String infoMd5 = (String) request.getAttribute("infoMd5");
            if (StringUtil.isBlank(infoMd5)) {
                throw new SystemException("支付失败，传入的加密信息为空!");
            }
            String infoStr = orderId + VerifyUtil.SEPARATOR + tenantId;
            String key = AbstractPayConfigManager.getRequestKey();
            if (!VerifyUtil.checkParam(infoStr, infoMd5, key)) {
                LOG.error("延签失败：传入的参数已被篡改！" + infoStr);
                throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM,
                        "传入的支付请求参数非法,参数有误或已被篡改！");
            }
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if (tradeRecord == null) {
                LOG.error("发起支付时查询不到此订单支付请求数据： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("发起支付时查询订单信息异常!");
            }
            /* 2.组织返回信息给调用端 */
            String basePath = AbstractPayConfigManager.getPayUrl();
            String payCenterNotifyUrl = basePath + XY_NOTIFY_URL;
            commonPayRes.setOrderAmount(String.valueOf(tradeRecord.getPayAmount()));
            commonPayRes.setOrderId(orderId);
            commonPayRes.setPayCenterNotifyUrl(payCenterNotifyUrl);
            commonPayRes.setPayCenterOrderId(tradeRecord.getTradeOrderId());
            commonPayRes.setPayOrgCode(tradeRecord.getPayOrgId());
            commonPayRes.setReturnCode(PayConstants.ReturnCode.SUCCESS);
            commonPayRes.setRequestSource(tradeRecord.getRequestSource());
            commonPayRes.setSubject(tradeRecord.getSubject());
            commonPayRes.setTenantId(tenantId);
        } catch (BusinessException ex) {
            LOG.error(ex.getErrorMessage(), ex);
            commonPayRes.setErrCode(ex.getErrorCode());
            commonPayRes.setErrMsg(ex.getErrorMessage());
        } catch (Exception ex) {
            LOG.error("兴业APP支付失败", ex);
            commonPayRes.setErrMsg("兴业APP支付失败:" + ex.getMessage());
        } 
        String returnStr = ResponseUtil.getCommonPayResponse(commonPayRes);
        LOG.info("兴业APP支付返回报文：[" + returnStr + "]");
        printWriter.write(returnStr);
        printWriter.flush();
    }
    /**
     * 兴业银行后台通知地址
     * @param request
     * @param response
     * @author LiangMeng
     * @ApiDocMethod
     */
    @RequestMapping(value = "/xyNotify")
    public void xyNotify(HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("兴业支付后台通知...");
        PrintWriter printWriter = null;
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            /* 1.获取兴业传递过来的参数 */
                String order_no = request.getParameter("order_no");
                /* 2.如果成功，更新支付流水并回调请求端，否则什么也不做 */
                LOG.info("兴业后台通知执行回调：[" + order_no + "]");
                /* 2.1.取支付流水信息 */
                String payStates = PayConstants.ReturnCode.FAILD;
                /**兴业回调未返回成功失败标志，所以，调到这里的都是成功订单*/
                if (true ){
                    payStates = PayConstants.ReturnCode.SUCCESS;
                } 
                /* 解析第三方平台返回的orderId */
                String tenantId = ConfigFromFileUtil.getProperty("TENANT_ID");//orderInfoArray[0]; 
                String orderId = order_no;//orderInfoArray[1]; 
                TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
                if(tradeRecord == null) {
                    LOG.error("兴业码支付后台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
                    throw new SystemException("兴业扫码支付后台出错，获取订单信息失败!");
                }
                String notifyUrl = tradeRecord.getNotifyUrl();
                String orderAmount = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额 
                String subject = tradeRecord.getSubject();
                /* 3.判断是否已经回调过，如果不是同一个回调更新支付流水信息，否则什么都不做 */
                LOG.info("兴业后台通知更新订单状态befor：[" + order_no + ";" + tradeRecord.getStatus() + "]");
                if (tradeRecord.getStatus() != null
                        && PayConstants.Status.APPLY == tradeRecord.getStatus()) {
                    this.modifyTradeState(tenantId, orderId, PayConstants.Status.PAYED_SUCCESS,
                            null, null, null, null, null);
                    /* 4.异步通知业务系统订单支付状态 */
                    PaymentNotifyUtil.notifyClientAsync(notifyUrl, tenantId, orderId,
                            null, subject, orderAmount, payStates,
                            PayConstants.PayOrgCode.XY);
                }

                printWriter.write(PayConstants.WeixinReturnCode.SUCCESS);//
                printWriter.flush();
                printWriter.close();
            
        } catch (Exception e) {
            LOG.error("兴业支付后台通知失败", e);
        }
    }

    @BackTransService
    @ResponseBody
    @RequestMapping(value = "/withdraw")
    public WithdrawRes withdraw(@RequestParam(value = "tenantId", required = true)
        String tenantId, @RequestParam(value = "orderId", required = true)
    String orderId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("兴业银行代付操作开始,订单号： " + orderId);
        TradeRecord takeRecord = this.queryTradeRecord(tenantId, orderId);
        if (takeRecord == null) {
            LOG.error("单笔代付失败，查询提现沉淀信息出错： 租户标识： " + tenantId + "，订单号： " + orderId);
            throw new SystemException("提现失败，查询提现沉淀信息出错!");
        }

        String takeAmount = String.format("%.2f",
                AmountUtil.changeLiToYuan(takeRecord.getPayAmount()));
        String resp = this.cibDF(tenantId, takeAmount, takeRecord);
        LOG.info("兴业银行单笔资金代付返回报文：" + resp);
        if (StringUtil.isBlank(resp)) {
            throw new SystemException("兴业银行单笔资金代付失败，可能原因：网络异常或证书问题！");
        }

        JSONObject resJson = JSONObject.parseObject(resp);
        WithdrawRes withdrawRes = new WithdrawRes();
        withdrawRes.setTenantId(tenantId);
        withdrawRes.setOrderId(orderId);
        withdrawRes.setPayOrgCode(PayConstants.PayOrgCode.XY);
        withdrawRes.setTakeAmount(takeAmount);
        int status = PayConstants.Status.UNKNOWN;
        // 交易状态，1-成功；2-失败；3-未决
        String transStatus = resJson.getString("transStatus");
        String errcode = resJson.getString("errcode");
        String errmsg = resJson.getString("errmsg");
        if(!StringUtil.isBlank(errcode)) {
            LOG.error("兴业银行单笔代付失败，错误码： " + errcode + "，错误详情： " + errmsg);
            status = PayConstants.Status.TAKE_FAIL;
            withdrawRes.setReturnCode(PayConstants.ReturnCode.FAILD);
            withdrawRes.setErrCode(ExceptCodeConstants.TRADE_FAIL);
            withdrawRes.setErrMsg(errmsg);
        } else if ("1".equals(transStatus)) {
            LOG.info("兴业银行单笔代付成功，订单号： " + orderId);
            status = PayConstants.Status.TAKE_SUCCESS;
            withdrawRes.setReturnCode(PayConstants.ReturnCode.SUCCESS);
        } else if ("2".equals(transStatus)) {
            String remark = resJson.getString("remark");
            LOG.error("兴业银行单笔代付失败，错误详情： " + remark);
            status = PayConstants.Status.TAKE_FAIL;
            withdrawRes.setReturnCode(PayConstants.ReturnCode.FAILD);
            withdrawRes.setErrCode(ExceptCodeConstants.TRADE_FAIL);
            withdrawRes.setErrMsg(remark);
        } else {
            LOG.error("兴业银行单笔代付交易状态未知，需调用单笔代付查询接口查询具体的交易结果！");
            withdrawRes.setReturnCode(PayConstants.ReturnCode.FAILD);
            withdrawRes.setErrCode(ExceptCodeConstants.TRADE_FAIL);
        }

        String sno = resJson.getString("sno");
        this.modifyTradeState(tenantId, orderId, status, sno, null, null, null, null, null, resp);
        return withdrawRes;
    }    
    
    /**
     * 调用兴业银行单笔资金代付接口
     * @param tenantId
     * @param takeRecord
     * @return
     * @author fanpw
     * @throws AppException 
     * @ApiDocMethod
     * @ApiCode
     */
    private String cibDF(String tenantId, String takeAmount, TradeRecord takeRecord) throws AppException {
        AppClientService appClientService = AppClientServiceHolder.getInstance().getAppClientService(); 
        if(appClientService == null) {
            throw new SystemException("兴业银行单笔资金代付失败，原因： 客户端服务类实例初始化失败");
        }
        // 收款方账户信息，支付行号^银行卡号^账户户名^账户类型
        String receiverAccountInfo = takeRecord.getDetailData();
        String[] cardInfoArr = receiverAccountInfo.split("\\^");
        if (cardInfoArr.length != 4) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_WRONG, "转账收款方信息格式有误，支付行号^银行卡号^账户户名^账户类型");
        }

        String bankNo = cardInfoArr[0];
        String acctNo = cardInfoArr[1];
        String acctName = cardInfoArr[2];
        String acctType = cardInfoArr[3];
        /* 组织请求报文  */        
        Map<String, String> txnContent = new HashMap<String, String>();
        txnContent.put("appid", ConfigUtil.getProperty(tenantId, XyPayConfigManager.PAY_ORG_NAME,
                XyPayConfigManager.APP_ID));
        txnContent.put("service", "cib.epay.payment.pay");
        txnContent.put("ver", "01");
        txnContent.put("timestamp", DateUtil.getDateString(DateUtil.YYYYMMDDHHMMSS));
        txnContent.put("order_no", takeRecord.getTradeOrderId());
        txnContent.put("to_bank_no", bankNo);
        txnContent.put("to_acct_no", acctNo);
        txnContent.put("to_acct_name", acctName);
        txnContent.put("acct_type", acctType); //对方账户类型: 0-储蓄卡;1-信用卡;2-企业账户
        txnContent.put("cur", "CNY");
        txnContent.put("trans_amt", takeAmount);
        txnContent.put("trans_usage", "退款");

        /* 发送报文 */
        String resStr = null;
        AppResponse res = null;
        try {
            res = appClientService
                    .txn(txnContent, txnContent.get("service"), txnContent.get("ver"));
            resStr = res.getBody();
        } catch (Exception ex) {
            LOG.error("调用兴业银行单笔资金代付接口出错", ex);
        }
        return resStr;
    }
    
}

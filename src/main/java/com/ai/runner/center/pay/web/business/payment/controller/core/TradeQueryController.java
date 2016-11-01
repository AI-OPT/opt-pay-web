package com.ai.runner.center.pay.web.business.payment.controller.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.opt.sdk.util.StringUtil;
import com.ai.runner.base.exception.BusinessException;
import com.ai.runner.base.exception.SystemException;
import com.ai.runner.center.pay.api.tradequery.param.TradeRecord;
import com.ai.runner.center.pay.web.business.payment.annotation.BackTransService;
import com.ai.runner.center.pay.web.business.payment.model.TradeQueryReqParam;
import com.ai.runner.center.pay.web.business.payment.model.TradeQueryRes;
import com.ai.runner.center.pay.web.business.payment.service.impl.WeixinPayTradeQuerySVImpl;
import com.ai.runner.center.pay.web.business.payment.service.interfaces.ITradeQuerySV;
import com.ai.runner.center.pay.web.business.payment.util.core.VerifyUtil;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.alibaba.fastjson.JSON;

/**
 * 支付平台交易查询统一入口控制类
 *
 * Date: 2015年11月6日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
@Controller
@RequestMapping(value = "/query")
public class TradeQueryController extends TradeBaseController {

    private static final Logger LOG = Logger.getLogger(TradeQueryController.class);
    
    @BackTransService
    @ResponseBody
    @RequestMapping(value = "/tradeQuery")
    public TradeQueryRes tradeQuery(HttpServletRequest request, HttpServletResponse response,
            TradeQueryReqParam tradeQueryReqParam) {
        LOG.info("接收到交易查询请求： 商户订单号： " + tradeQueryReqParam.getOrderId() + ",租户标识："
                + tradeQueryReqParam.getTenantId());
        LOG.info("接收到交易查询请求详细报文： " + JSON.toJSONString(tradeQueryReqParam));

        /* 1.参数验证 */
        this.validateTradeQueryReq(tradeQueryReqParam);
        String tenantId = tradeQueryReqParam.getTenantId();
        String orderId = tradeQueryReqParam.getOrderId();
//        String partnerId = this.getPartnerId(tenantId);
//        if (StringUtil.isBlank(partnerId)) {
//            LOG.error("未识别的合作方身份！租户ID： " + tenantId);
//            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARTNER, "未识别的合作方身份！");
//        }

        TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
        if (tradeRecord == null) {
            LOG.error("查询不到此订单相关交易信息： 租户标识： " + tenantId + " ，订单号： " + orderId);
            throw new BusinessException(ExceptCodeConstants.TRADE_NOT_EXIST, "交易不存在！");
        }

        if (StringUtil.isBlank(tradeRecord.getTradeOrderId())) {
            LOG.error("系统异常，查询不到此订单交易结果： 租户标识： " + tenantId + " ，订单号： " + orderId);
            throw new BusinessException(ExceptCodeConstants.TRADE_NOT_EXIST, "交易不存在！");
        }

        ITradeQuerySV tradeQuerySV = this.getTradeQuerySV(tradeRecord.getPayOrgId());
        return tradeQuerySV.tradeQuery(tenantId, orderId, tradeRecord.getTradeOrderId());
    }
    
    /**
     * 获取交易结果查询服务实例
     * @param payOrgCode
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private ITradeQuerySV getTradeQuerySV(String payOrgCode) {
        ITradeQuerySV sv = null;
        if(PayConstants.PayOrgCode.WEIXIN.equals(payOrgCode)) {
            sv = new WeixinPayTradeQuerySVImpl();
        } else {
            throw new SystemException("暂不支持此支付机构[" + payOrgCode + "]交易结果查询");
        }
        
        return sv;
    }
        
    /**
     * 校验交易查询请求是否合法
     * @param tradeQueryReqParam
     * @throws BusinessException
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private void validateTradeQueryReq(TradeQueryReqParam tradeQueryReqParam)
            throws BusinessException {
        final String errMsg = "交易查询请求参数有误：";
        String tenantId = tradeQueryReqParam.getTenantId();
        if (StringUtil.isBlank(tenantId)) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "租户ID不能为空");
        }

        if (StringUtil.isBlank(tradeQueryReqParam.getOrderId())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "订单号不能为空");
        }

        if (StringUtil.isBlank(tradeQueryReqParam.getInfoMd5())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "加密信息不能为空");
        }

        // orderId;tenantId
        // (订单号，租户ID关键字段，以英文输入分号分隔;注意最后没有分号)
        String infoStr = tradeQueryReqParam.getOrderId() + VerifyUtil.SEPARATOR
                + tenantId;
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        if (!VerifyUtil.checkParam(infoStr, tradeQueryReqParam.getInfoMd5(), key)) {
            LOG.error("延签失败：传入的参数已被篡改！" + infoStr);
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "交易查询请求参数非法,参数有误或已被篡改！");
        }

    }
               
}

package com.ai.runner.center.pay.web.business.payment.util.core;

import com.ai.runner.center.pay.web.business.payment.model.BatchWithdrawReqParam;
import com.ai.runner.center.pay.web.business.payment.model.WithdrawReqParam;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.ai.runner.center.pay.web.system.util.MD5;

/**
 * 验签工具类
 *
 * Date: 2015年11月2日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public final class VerifyUtil {
    
    private VerifyUtil() {
        
    }

    /**
     * 默认编码
     */
    private static final String DEFAULT_CHARSET = "utf-8";
    
    /**
     * 加密组装分隔符
     */
    public static final String SEPARATOR = ";";
    
    /**
     * 检查参数
     * 
     * @param param
     * @param paramMd5
     * @param key
     * @return
     * @author LiangMeng
     */
    public static boolean checkParam(String param, String paramMd5, String key) {
        String paramMd5New = MD5.sign(param, key, DEFAULT_CHARSET);
        return paramMd5New.equals(paramMd5);
    }

    /**
     * 参数加密
     * 
     * @param param
     * @param key
     * @return
     * @author LiangMeng
     */
    public static String encodeParam(String param, String key) {
        return MD5.sign(param, key, DEFAULT_CHARSET);
    }
    
    /**
     * 批量付款请求参数延签
     * @param withdrawReqParam
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public static boolean verifyBatchWithDrawParam(BatchWithdrawReqParam withdrawReqParam) {
        String tenantId = withdrawReqParam.getTenantId();
        //batchNo;batchNum;batchFee;payOrgCode;detailData;notifyUrl;tenantId
        //批量付款批次号，付款总笔数，付款总金额，提现请求支付机构编码，付款详细数据，异步通知地址，租户ID字段，以英文输入分号分隔;注意最后没有分号
        String infoStr = withdrawReqParam.getBatchNo() + VerifyUtil.SEPARATOR
                + withdrawReqParam.getBatchNum() + VerifyUtil.SEPARATOR
                + withdrawReqParam.getBatchFee() + VerifyUtil.SEPARATOR
                + withdrawReqParam.getPayOrgCode() + VerifyUtil.SEPARATOR
                + withdrawReqParam.getDetailData() + VerifyUtil.SEPARATOR
                + withdrawReqParam.getNotifyUrl() + VerifyUtil.SEPARATOR
                + tenantId;
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        return checkParam(infoStr, withdrawReqParam.getInfoMd5(), key);
    }
    
    /**
     * 单笔付款参数延签
     * @param withdrawReqParam
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public static boolean verifyWithDrawParam(WithdrawReqParam withdrawReqParam) {
        String tenantId = withdrawReqParam.getTenantId();
        //batchNo;batchNum;batchFee;payOrgCode;detailData;notifyUrl;tenantId
        //批量付款批次号，付款总笔数，付款总金额，提现请求支付机构编码，付款详细数据，异步通知地址，租户ID字段，以英文输入分号分隔;注意最后没有分号
        String infoStr = withdrawReqParam.getOrderId() + VerifyUtil.SEPARATOR
                + withdrawReqParam.getWithDrawFee() + VerifyUtil.SEPARATOR
                + withdrawReqParam.getPayOrgCode() + VerifyUtil.SEPARATOR
                + withdrawReqParam.getReceiverAccountInfo() + VerifyUtil.SEPARATOR
                + withdrawReqParam.getNotifyUrl() + VerifyUtil.SEPARATOR
                + tenantId;
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        return checkParam(infoStr, withdrawReqParam.getInfoMd5(), key);
    }
}

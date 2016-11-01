package com.ai.runner.center.pay.web.business.payment.util.third.alipay;

import java.util.ArrayList;
import java.util.List;

import com.ai.runner.center.pay.web.system.exception.ParseDataException;

/**
 * 支付宝返回数据解析类
 *
 * Date: 2015年11月10日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public final class AlipayReturnParser {
    
    private AlipayReturnParser() {
        
    }

    public static class RefundDealItem {
        
        /**
         * 原付款支付宝交易号
         */
        private String tradeNo;
        
        /**
         * 退款金额
         */
        private String refundAmount;
        
        /**
         * 处理结果码
         */
        private String dealResult;

        public String getTradeNo() {
            return tradeNo;
        }

        public void setTradeNo(String tradeNo) {
            this.tradeNo = tradeNo;
        }

        public String getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(String refundAmount) {
            this.refundAmount = refundAmount;
        }

        public String getDealResult() {
            return dealResult;
        }

        public void setDealResult(String dealResult) {
            this.dealResult = dealResult;
        }
        
    }
    
    public static List<RefundDealItem> parseRefundNotifyDetails(String details)
            throws ParseDataException {
        List<RefundDealItem> list = new ArrayList<RefundDealItem>();
        try {
            String[] detailDataArr = details.split("\\#");
            for (int i = 0; i < detailDataArr.length; i++) {
                String detailData = detailDataArr[i];
                RefundDealItem item = parseRefundNotifyDetailData(detailData);
                list.add(item);
            }
        } catch (Exception ex) {
            throw new ParseDataException("解析支付宝无密退款通知数据出错！");
        }

        return list;
    }

    private static RefundDealItem parseRefundNotifyDetailData(String detailData)
            throws ParseDataException {
        RefundDealItem item = new RefundDealItem();
        try {
            String[] strArr = detailData.split("\\^");
            item.setTradeNo(strArr[0]);
            item.setRefundAmount(strArr[1]);
            item.setDealResult(strArr[2]);
        } catch (Exception ex) {
            throw new ParseDataException("解析支付宝无密退款通知数据出错！");
        }

        return item;
    }
    
}

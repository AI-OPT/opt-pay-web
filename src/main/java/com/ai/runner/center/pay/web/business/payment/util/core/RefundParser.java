package com.ai.runner.center.pay.web.business.payment.util.core;

import java.util.ArrayList;
import java.util.List;

import com.ai.runner.center.pay.web.system.exception.ParseDataException;


/**
 * 批量退款功能时解析退款数据集
 *
 * Date: 2015年11月10日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public final class RefundParser {

    private RefundParser() {
        
    }
    
    /**
     * 单笔退款数据
     *
     * Date: 2015年11月10日 <br>
     * Copyright (c) 2015 asiainfo.com <br>
     * @author fanpw
     */
    public static class RefundData {

        /**
         * 订单号
         */
        private String orderId;

        /**
         * 原订单号
         */
        private String oriOrderId;

        /**
         * 退款金额
         */
        private String refundAmount;

        /**
         * 退款理由
         */
        private String returnReason;
        
        /**
         * 第三方支付平台交易流水号
         */
        private String payOrgSerial;
        
        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getOriOrderId() {
            return oriOrderId;
        }

        public void setOriOrderId(String oriOrderId) {
            this.oriOrderId = oriOrderId;
        }

        public String getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(String refundAmount) {
            this.refundAmount = refundAmount;
        }

        public String getReturnReason() {
            return returnReason;
        }

        public void setReturnReason(String returnReason) {
            this.returnReason = returnReason;
        }

        public String getPayOrgSerial() {
            return payOrgSerial;
        }

        public void setPayOrgSerial(String payOrgSerial) {
            this.payOrgSerial = payOrgSerial;
        }

    }
    
    public static List<RefundData> parseRefundDetails(String details) throws ParseDataException {
        List<RefundData> list = new ArrayList<RefundData>();
        try {
            String[] detailDataArr = details.split("\\#");
            for (int i = 0; i < detailDataArr.length; i++) {
                String detailData = detailDataArr[i];
                RefundData item = parseRefundDetailItem(detailData);
                list.add(item);
            }
        } catch (Exception ex) {
            throw new ParseDataException("解析批量无密退款数据集出错！");
        }

        return list;
    }
    
    private static RefundData parseRefundDetailItem(String detailData) throws ParseDataException {
        RefundData data = new RefundData();
        try {
            String[] strArr = detailData.split("\\^");
            data.setOrderId(strArr[0]);
            data.setOriOrderId(strArr[1]);
            data.setRefundAmount(strArr[2]);
            data.setReturnReason(strArr[3]);
        } catch (Exception ex) {
            throw new ParseDataException("解析批量无密退款数据集出错！");
        }
        
        return data;
    }
    
}

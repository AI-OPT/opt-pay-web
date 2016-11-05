package com.ai.runner.center.pay.web.system.constants;

/**
 * 支付中心常量定义类
 * 
 * Date: 2015年10月29日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author fanpw
 */
public final class PayConstants {

    private PayConstants() {

    }
    
    /**
     * 是否商用
     */
    public static final String SERVER_TYPE = "servertype";
    
    /**
     * 请求参数加密对应key 
     */
    public static final String REQUEST_KEY = "request_key";
    
    /**
     * 支付平台外网地址 
     */
    public static final String PAY_URL = "payurl";

    /**
     * 终端来源
     * 
     * Date: 2015年11月2日 <br>
     * Copyright (c) 2015 asiainfo.com <br>
     * 
     * @author fanpw
     */
    public static final class RequestSource {

        private RequestSource() {

        }

        /**
         * 1：web;2:wap;3:APP 5:ios:6;android&支付宝；7Android&微信
         */
        public static final String WEB = "1";

        public static final String WAP = "2";

        public static final String APP = "3";

        public static final String WEIXIN = "4";

        public static final String IOS = "5";

        public static final String SDK_ALI = "6";

        public static final String SDK_TC = "7";

    }

    /**
     * 支付机构
     * 
     * Date: 2015年11月2日 <br>
     * Copyright (c) 2015 asiainfo.com <br>
     * 
     * @author fanpw
     */
    public static final class PayOrgCode {

        private PayOrgCode() {

        }

        /* 支付宝 */
        public static final String ZFB = "ZFB";
        
        /* 银联 */
        public static final String YL = "YL";

        /* 微信* */
        public static final String WEIXIN = "WEIXIN";
        
        /* 兴业 */
        public static final String XY = "XY";

		/**
		 * PayPal
		 */
		public static final String PP = "PP";

    }
    
    /**
     * 支付请求类型
     * 
     * Date: 2015年11月2日 <br>
     * Copyright (c) 2015 asiainfo.com <br>
     * 
     * @author fanpw
     */
    public static final class PayRequestType {

        private PayRequestType() {

        }

        /**
         * 支付
         */
        public static final int PAY = 1;

        /**
         * 退款
         */
        public static final int REFUND = 2;

        /**
         * 提现
         */
        public static final int TAKE = 3;
    }

    /**
     * 支付状态
     * 
     * Date: 2015年10月29日 <br>
     * Copyright (c) 2015 asiainfo.com <br>
     * 
     * @author fanpw
     */
    public final class Status {

        private Status() {

        }

        /**
         * 申请支付
         */
        public static final int CREATE = -1;

        /**
         * 待支付（向第三方支付发起支付）
         */
        public static final int APPLY = 1;

        /**
         * 支付交易成功 
         */
        public static final int PAYED_SUCCESS = 2;

        /**
         * 支付失败
         */
        public static final int PAYED_FAILED = 3;
        
        /**
         * 申请退款
         */
        public static final int REFUND_APPLY = 5;
                
        /**
         * 接受退款申请
         */
        public static final int REFUND_ACCEPT = 6;
                
        /**
         * 退款成功
         */
        public static final int REFUND_SUCCESS = 8;

        /**
         * 退款失败
         */
        public static final int REFUND_FAIL = 9;

        /**
         * 退款到账
         */
        public static final int REFUND_FINISH = 10;

        /**
         * 提现成功
         */
        public static final int TAKE_SUCCESS = 11;

        /**
         * 提现失败
         */
        public static final int TAKE_FAIL = 12;
        
        /**
         * 状态未知，需发起查询操作确定交易状态
         */
        public static final int UNKNOWN = 80;
    }
    
    /**
     * 返回状态码
     *
     * Date: 2015年11月4日 <br>
     * Copyright (c) 2015 asiainfo.com <br>
     * @author fanpw
     */
    public static final class ReturnCode {
        
        private ReturnCode() {
            
        }
        
        public static final String SUCCESS = "00";

        public static final String FAILD = "01";

    }
    
    /**
     * 支付宝支付返回码
     *
     * Date: 2015年11月4日 <br>
     * Copyright (c) 2015 asiainfo.com <br>
     * @author fanpw
     */
    public static final class AliPayReturnCode {
        
        private AliPayReturnCode() {
            
        }
        
        public static final String RETURN_URL_SUCCESS = "success";

        public static final String RETURN_URL_T = "T";

        public static final String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";

        public static final String TRADE_CLOSED = "TRADE_CLOSED";

        public static final String TRADE_SUCCESS = "TRADE_SUCCESS";

        public static final String TRADE_PENDING = "TRADE_PENDING";

        public static final String TRADE_FINISHED = "TRADE_FINISHED";

    }
    /**
     * 微信支付方式
     * Date: 2015年11月30日 <br>
     * Copyright (c) 2015 asiainfo.com <br>
     * 
     * @author LiangMeng
     */
    public static final class WeixinTradeType {
        
        private WeixinTradeType() {
            
        }
        public static final String NATIVE = "NATIVE";
        public static final String JSAPI = "JSAPI";
        public static final String APP = "APP";

    }
    
    /**
     * 微信返回码
     * Title: MVNO-CRM <br>
     * Description: <br>
     * Date: 2015年7月8日 <br>
     * Copyright (c) 2015 AILK <br>
     * 
     * @author LiangMeng
     */
    public class WeixinReturnCode{
        private WeixinReturnCode(){
            
        }
        public static final String SUCCESS = "SUCCESS";
        public static final String FAIL = "FAIL";
    }
    
}

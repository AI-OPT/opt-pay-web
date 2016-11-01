package com.ai.runner.center.pay.web.system.util;

import java.security.SecureRandom;

/**
 * 支付平台取序列工具类
 *
 * Date: 2015年11月9日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public final class PaymentSeqUtil {

    private PaymentSeqUtil() {
        
    }
    
    public static String getSixRandom() {
        int seq = new SecureRandom().nextInt(1000000);
        String seqStr = String.valueOf(seq);
        if (seqStr.length() < 6) {
            seqStr = "000000" + seqStr;
        }
        return seqStr.substring(seqStr.length() - 6);
    }
    
    public static String getFourRandom() {
        int seq = new SecureRandom().nextInt(10000);
        String seqStr = String.valueOf(seq);
        if (seqStr.length() < 4) {
            seqStr = "0000" + seqStr;
        }
        return seqStr.substring(seqStr.length() - 4);
    }
    
    /**
     * 获取随机数字
     * @param length
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    public static int getRandomNum(int length) {
        int num = 1;
        double random = Math.random();
        if (random < 0.1) {
            random = random + 0.1;
        }
        for (int i = 0; i < length; i++) {
            num = num * 10;
        }
        return (int) ((random * num));
    }
    
}

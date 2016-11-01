package com.ai.runner.center.pay.web.business.payment.service.interfaces;

import com.ai.runner.center.pay.web.business.payment.model.TradeQueryRes;

/**
 * 交易结果查询接口
 *
 * Date: 2015年12月10日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public interface ITradeQuerySV {

    TradeQueryRes tradeQuery(String tenantId, String orderId, String tradeOrderId);
    
}

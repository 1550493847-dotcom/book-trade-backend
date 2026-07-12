package com.MySpringBoot.my_first_app.service;

import com.MySpringBoot.my_first_app.config.AlipayConfig;
import com.MySpringBoot.my_first_app.entity.Order;
import com.MySpringBoot.my_first_app.mapper.OrderMapper;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AlipayService {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private OrderMapper orderMapper;

    private AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
            alipayConfig.getGatewayUrl(),
            alipayConfig.getAppId(),
            alipayConfig.getMerchantPrivateKey(),
            "json",
            "UTF-8",
            alipayConfig.getAlipayPublicKey(),
            "RSA2"
        );
    }

    /**
     * 生成支付宝支付页面（返回 HTML form 字符串）
     */
    public String createPayForm(Integer orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) return null;

        AlipayClient client = getAlipayClient();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());

        // 构造业务参数
        String bizContent = String.format(
            "{\"out_trade_no\":\"%s\",\"total_amount\":%.2f,\"subject\":\"淘籍籍-图书订单\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}",
            order.getOrderNo(), order.getTotalPrice()
        );
        request.setBizContent(bizContent);

        try {
            return client.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 验证支付宝异步通知签名
     */
    public boolean verifyNotify(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), "UTF-8", "RSA2");
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 处理支付成功（异步通知）
     */
    public boolean handlePaySuccess(String orderNo, String tradeNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null || order.getStatus() != 0) return false;

        return orderMapper.pay(order.getId(), 1) > 0;
    }
}


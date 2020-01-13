package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {

    /**
     * 保存paymentInfo信息
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 根据paymentInfo查询getPaymentInfo信息
     * @param paymentInfo
     * @return
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 修改getPaymentInfo信息
     * @param outTradeNo
     * @param paymentInfo
     */
    void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfo);

    /**
     * 退款
     * @param orderId
     * @return
     */
    boolean refund(String orderId);

    /**
     *微信支付
     * @param orderId
     * @param total_fee
     * @return
     */
    Map createNative(String orderId, String total_fee);

    /**
     * 消息队列---发送验证
     * @param paymentInfo
     * @param result
     */
    public void sendPaymentResult(PaymentInfo paymentInfo,String result);
}

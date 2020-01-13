package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.enums.ProcessStatus;

public interface OrderService {

    /**
     * 根据orderInfo 保存数据
     * @param orderInfo
     * @return
     */
    public String saveOrder(OrderInfo orderInfo);

    /**
     * 根据userId生成流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 验证流水号
     * @param userId
     * @param tradeNo
     * @return
     */
    boolean checkTradeCode(String userId, String tradeNo);

    /**
     * 删除流水号
     * @param userId
     */
    void delTradeNo(String userId);

    /**
     * 校验库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId,Integer skuNum);

    /**
     * 根据orderId查询orderInfo
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(String orderId);

    /**
     * 根据订单Id 修改订单状态
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(String orderId, ProcessStatus processStatus);

    /**
     * 发送订单状态
     * @param orderId
     */
    void sendOrderStatus(String orderId);
}

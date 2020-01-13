package com.atguigu.gmall.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.enums.ProcessStatus;
import com.atguigu.gmall.service.OrderService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

// 消费消息
@Component
public class OrderConsumer {

    @Reference
    private OrderService orderService;

    // 利用注解来获取消息的监听工厂
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        // 获取消息
        String result = mapMessage.getString("result");
        String orderId = mapMessage.getString("orderId");

        System.out.println("result = " + result);
        System.out.println("orderId = " + orderId);

        if ("success".equals(result)){
            // 支付成功！修改订单状态！
            // update orderInfo set ProcessStatus = Paid ,orderStatus = paid where id = orderId
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
            //通知减库存
            orderService.sendOrderStatus(orderId);
            orderService.updateOrderStatus(orderId,ProcessStatus.DELEVERED);
        }else {
            orderService.updateOrderStatus(orderId,ProcessStatus.UNPAID);
        }
    }


    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");
        if ("DEDUCTED".equals(status)){
            orderService.updateOrderStatus(orderId,ProcessStatus.WAITING_DELEVER);
        }else {
            orderService.updateOrderStatus(orderId,ProcessStatus.STOCK_EXCEPTION);
        }
    }









}

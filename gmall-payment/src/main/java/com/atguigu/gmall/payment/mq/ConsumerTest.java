package com.atguigu.gmall.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;

import javax.jms.*;

// 消费者
public class ConsumerTest {

    public static void main(String[] args) throws JMSException {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.182.132:61616");

        Connection connection = activeMQConnectionFactory.createConnection();

        connection.start();

        // 第一个参数表示是否开启事务，第二个参数表示对事务的解释。
        // Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue atguigu = session.createQueue("atguigu");

        // 创建消费者对象
        MessageConsumer consumer = session.createConsumer(atguigu);

        // 消费消息：
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof TextMessage){
                    String text = null;
                    try {
                        text = ((TextMessage) message).getText();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                    System.out.println(text);
                }
            }
        });

    }
}

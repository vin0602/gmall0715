package com.atguigu.gmall.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

// 消息提供者
public class ProducerTest {

    public static void main(String[] args) throws JMSException {
        /*
        1.  创建连接工厂
        2.  获取连接并打开连接
        3.  创建session
        4.  创建队列,创建消息提供者
        5.  创建消息对象
        6.  发送消息
        7.  关闭
         */
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.182.132:61616");

        Connection connection = activeMQConnectionFactory.createConnection();

        connection.start();

        // 第一个参数表示是否开启事务，第二个参数表示对事务的解释。
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue atguigu = session.createQueue("atguigu");

        MessageProducer producer = session.createProducer(atguigu);

        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText("啥时候扫出全家福！");
        producer.send(activeMQTextMessage);

        // 开启事务必须提交事务
        // session.commit();
        producer.close();
        session.close();
        connection.close();


    }
}

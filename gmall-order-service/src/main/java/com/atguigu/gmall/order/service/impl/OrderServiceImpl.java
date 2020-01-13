package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.config.ActiveMQUtil;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.enums.ProcessStatus;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;


@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ActiveMQUtil activeMQUtil;

    //保存订单（下订单）
    @Override
    public String saveOrder(OrderInfo orderInfo) {
        //设置创建时间
        orderInfo.setCreateTime(new Date());
        //设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());

        //生成第三方支付编号
        String outTradeNo="ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfoMapper.insertSelective(orderInfo);

        //插入订单详细信息
        List <OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderDetail.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        //为了跳转到支付页面使用，支付会根据订单id进行支付
        String orderId = orderInfo.getId();

        return orderId;
    }

    //生成流水号
    @Override
    public String getTradeNo(String userId) {
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:" + userId + ":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        jedis.close();

        return tradeCode;
    }

    //验证流水号
    @Override
    public boolean checkTradeCode(String userId, String tradeNo) {
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCode!=null && tradeCode.equals(tradeNo)){
            return true;
        }else {
            return false;
        }
    }

    //删除流水号
    @Override
    public void delTradeNo(String userId) {
        //获取jedis中的流水号
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String tradeNoKey ="user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        //jedis.del(tradeNoKey)
        String script ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        jedis.eval(script,Collections.singletonList(tradeNoKey),Collections.singletonList(tradeCode));
        jedis.close();
    }

    //验证库存
    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if ("1".equals(result)){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        //将orderDetail放入orderInfo中
        orderInfo.setOrderDetailList(orderDetailMapper.select(orderDetail));

        return orderInfo;
    }

    //根据订单Id 修改订单状态
    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {
        // orderId 注解
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    //发送订单状态
    @Override
    public void sendOrderStatus(String orderId) {
        Connection connection = activeMQUtil.getConnection();
        String orderJson = initWareOrder(orderId);

        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(order_result_queue);

            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText(orderJson);
            producer.send(textMessage);
            session.commit();

            session.close();
            producer.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    //根据orderId初始化发送订单
    private String initWareOrder(String orderId) {
        OrderInfo orderInfo = getOrderInfo(orderId);
        Map map = initWareOrder(orderInfo);

        return JSON.toJSONString(map);
    }

    // 设置初始化仓库信息方法
    public Map initWareOrder(OrderInfo orderInfo){
        Map <String,Object> map = new HashMap <>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());

        map.put("orderBody",orderInfo.getOrderDetailList());

        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        map.put("wareId",orderInfo.getWareId());

        // 组合json
        List detailList = new ArrayList();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap = new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailList.add(detailMap);
        }
        map.put("details",detailList);
        return map;
    }


}

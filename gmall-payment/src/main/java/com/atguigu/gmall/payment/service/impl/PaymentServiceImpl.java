package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.config.ActiveMQConfig;
import com.atguigu.gmall.config.ActiveMQUtil;
import com.atguigu.gmall.enums.PaymentStatus;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private ActiveMQUtil activeMQUtil;

    //服务号Id
    @Value("${appid}")
    private String appid;
    //商户号Id
    @Value("${partner}")
    private String mchId;
    //秘钥
    @Value("${partnerkey}")
    private String partnerkey;



    //保存paymentInfo信息
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    //根据paymentInfo查询getPaymentInfo信息
    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo) {
        return paymentInfoMapper.selectOne(paymentInfo);
    }

    //修改getPaymentInfo信息
    @Override
    public void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",outTradeNo);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    //退款
    @Override
    public boolean refund(String orderId) {

        // 通过订单Id 查询交易记录对象
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        PaymentInfo paymentInfoQuery = getPaymentInfo(paymentInfo);
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        // 封装业务参数
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfoQuery.getOutTradeNo());
        map.put("refund_amount",paymentInfoQuery.getTotalAmount());
        map.put("refund_reason","过年没钱了");

        // json
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
            // 交易记录要更改，
            PaymentInfo paymentInfoUpd = new PaymentInfo();
            paymentInfoUpd.setPaymentStatus(PaymentStatus.ClOSED);
            updatePaymentInfo(paymentInfoQuery.getOutTradeNo(),paymentInfoUpd);

            // 订单状态
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }

    }

    //微信支付
    // 微信支付
    @Override
    public Map createNative(String orderId, String totalAmout) {
        /*
            1.  封装参数
                参数都以xml 形式发送  map->xml
            2.  将参数发送给微信支付的接口
            3.  得到微信支付结果中的code_url
         */
        HashMap<String, String> param = new HashMap<>();
        param.put("appid",appid);
        param.put("mch_id",mchId);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        // sign 在生成map -- xml 处理
        param.put("body","购买敬业福");
        param.put("out_trade_no",orderId);
        param.put("total_fee",totalAmout);
        param.put("spbill_create_ip","127.0.0.1");
        param.put("notify_url","http://v2q8627575.zicp.vip:20374/wx/callback/notify");
        param.put("trade_type","NATIVE");

        // 发送map -- >xml 发送给微信接口 https://api.mch.weixin.qq.com/pay/unifiedorder
        try {
            String xmlParam  = WXPayUtil.generateSignedXml(param, partnerkey);
            // 调用httpClient 发送数据
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            // 设置发送的数据
            httpClient.setXmlParam(xmlParam);
            // 设置https
            httpClient.setHttps(true);
            // 设置发送的方式
            httpClient.post();

            // 获取执行结果
            String result  = httpClient.getContent();
            // 将result 转换为map
            Map<String, String> resultMap  = WXPayUtil.xmlToMap(result);

            // 声明一个map 集合对象
            HashMap<Object, Object> map = new HashMap<>();
            // 将业务参数放入到map中
            map.put("code_url",resultMap.get("code_url"));
            map.put("total_fee",totalAmout);
            map.put("out_trade_no",orderId);
            return map;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //发送验证
    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
        // 获取链接
        Connection connection = activeMQUtil.getConnection();
        // 打开链接
        try {
            connection.start();
            // 创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建消息对象，提供者
            Queue payment_result_queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(payment_result_queue);

            // 创建消息对象
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("orderId",paymentInfo.getOrderId());
            activeMQMapMessage.setString("result",result);
//            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
//            activeMQTextMessage.setText(paymentInfo.getOrderId()+","+result);
            // 发送消息
            producer.send(activeMQMapMessage);

            // 提交
            session.commit();

            // 关闭
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


}

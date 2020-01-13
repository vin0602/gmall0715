package com.atguigu.gmall.payment.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.enums.PaymentStatus;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.StreamUtil;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private AlipayClient alipayClient;


    @RequestMapping("index")
    public String index(HttpServletRequest request){
        //获取订单的Id
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        request.setAttribute("orderId",orderId);
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        return "index";
    }

    @RequestMapping(value = "/alipay/submit",method = RequestMethod.POST)
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response){
        //获取订单Id
        String orderId = request.getParameter("orderId");
        //获取订单信息
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        //保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("-----");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);

        //保存信息
        paymentService.savePaymentInfo(paymentInfo);

        //支付宝参数
        //创建API对应的request
        AlipayTradeAppPayRequest alipayRequest = new AlipayTradeAppPayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        //在公共参数中设置回调和通知地址
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        //声明一个Map
        Map<String, Object> bizContnetMap = new HashMap <>();
        bizContnetMap.put("out_trade_no",paymentInfo.getOutTradeNo());
        bizContnetMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        bizContnetMap.put("subject",paymentInfo.getSubject());
        bizContnetMap.put("total_amount",paymentInfo.getTotalAmount());
        //将map变成json
        String Json = JSON.toJSONString(bizContnetMap);
        alipayRequest.setBizContent(Json);
        String form = "";
        try {
            //调用SDK生成表单
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");

        return form;
    }

    @RequestMapping(value = "/alipay/callback/return",method = RequestMethod.GET)
    public String callbackReturn(){
        return "redirect:"+AlipayConfig.return_order_url;
    }

    @RequestMapping(value = "/alipay/callback/notify",method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String,String> paramMap,HttpServletRequest request) throws AlipayApiException{
        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8",AlipayConfig.sign_type);
        if (!flag){
            return "fial";
        }
        //判断结束
        String trade_status = paramMap.get("trade_status");
        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
            // 查单据是否处理
            String out_trade_no = paramMap.get("out_trade_no");
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(out_trade_no);
            PaymentInfo paymentInfoHas = paymentService.getPaymentInfo(paymentInfo);
            if (paymentInfoHas.getPaymentStatus()==PaymentStatus.PAID || paymentInfoHas.getPaymentStatus()==PaymentStatus.ClOSED){
                return "fail";
            }else {
                // 修改
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                // 设置状态
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                // 设置创建时间
                paymentInfoUpd.setCallbackTime(new Date());
                // 设置内容
                paymentInfoUpd.setCallbackContent(paramMap.toString());
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUpd);
                return "success";
            }
        }
        return  "fail";
    }

    // http://payment.gmall.com/refund?orderId=98
    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId){
        boolean flag = paymentService.refund(orderId);
        System.out.println("flag:" + flag);

        return "" + flag;
    }

//    @RequestMapping(value = "wx/submit")
//    @ResponseBody
//    public Map createNative(String orderId){
//        // 做一个判断：支付日志中的订单支付状态 如果是已支付，则不生成二维码直接重定向到消息提示页面！
//        // 调用服务层数据
//        // 第一个参数是订单Id ，第二个参数是多少钱，单位是分
//        Map map = paymentService.createNative(orderId + "","1");
//        System.out.println(map.get("code_url"));
//        //data = map
//        return map;
//    }

    // 发送验证
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,@RequestParam("result") String result){
        paymentService.sendPaymentResult(paymentInfo,result);

        return "sent payment result";
    }



}

package com.atguigu.gmall.payment.controller;

import ch.qos.logback.core.joran.conditional.ElseAction;
import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.IdWorker;
import com.atguigu.gmall.util.StreamUtil;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WxPayController {

    //密钥
    @Value("${partnerkey}")
    private String partnerkey;
    @Reference
    private PaymentService paymentService;

    // http://payment.gmall.com/wx/submit
    // 微信支付要想生成二维码必须有 code_url
    // 前台传递的参数必须有orderId , totalAmout
    @RequestMapping("wx/submit")
    @ResponseBody
    public Map wxSubmit(){
        // code_url =  weixin：//wxpay/s/An4baqw
        // 使用IdWorker 类生成一个不重复的订单号！
        IdWorker idWorker = new IdWorker();
        long id = idWorker.nextId();
        String orderId = ""+id;
        System.out.println("orderId" + orderId);
        //调用服务层方法
        Map map = paymentService.createNative(orderId, "1");
        String code_url = (String) map.get("code_url");

        return map;
    }

    @RequestMapping("wx/callback/notify")
    @ResponseBody
    public String callBack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取毁掉的数据
        System.out.println("你回来啦");
        ServletInputStream inputStream = request.getInputStream();
        //inputStream 数据流对象
        String xmlString = StreamUtil.inputStream2String(inputStream, "utf-8");
        //验证签名

        if (WXPayUtil.isSignatureValid(xmlString,partnerkey)){
            //返回true 基本验签成功
            Map <String, String> paramMap = WXPayUtil.xmlToMap(xmlString);
            String result_code = paramMap.get("result_code");
            if (result_code!=null && "SUCCESS".equals(result_code)){
                // 支付成功！
                // 修改交易记录状态

                // return_code  return_msg 返回给商户
                // 声明一个map
                HashMap <String, String> map = new HashMap <>();
                map.put("return_code","SUCCESS");
                map.put("return_msg","OK");

                //微信所有的通知形式都是以xml 为基准
                String returnXml = WXPayUtil.mapToXml(map);
                //设置xml格式
                response.setContentType("text/xml");
                System.out.println("交易编号：" + paramMap.get("out_trade_no")+ "支付成功");

                return returnXml;
            }else {
                System.out.println("交易编号："+paramMap.get("out_trade_no")+"支付失败！");
                System.out.println("验签失败！");
                Map <String, String> map = new HashMap <>();
                map.put("return_code","FAIL");

                return  WXPayUtil.mapToXml(map);
            }
        }else {
            System.out.println("验签失败！");
            Map <String, String> map = new HashMap <>();
            map.put("return_code","FAIL");

            return  WXPayUtil.mapToXml(map);
        }
    }




}

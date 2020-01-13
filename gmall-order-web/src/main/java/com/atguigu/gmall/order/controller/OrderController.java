package com.atguigu.gmall.order.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.enums.OrderStatus;
import com.atguigu.gmall.enums.ProcessStatus;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//@RestController
@Controller
public class OrderController {

    @Reference
    private UserService userService;
    @Reference
    private CartService cartService;
    @Reference
    private OrderService orderService;
    @Reference
    private ManageService manageService;

//    @RequestMapping("trade")
//    public List<UserAddress> trade(String userId){
//        return  userService.getUserAddressByUserId(userId);
//    }

    @RequestMapping(value = "trade",method = RequestMethod.GET)
    @LoginRequire
    public String tradeInit(HttpServletRequest request){
        //从域中获取userId
        String userId = (String) request.getAttribute("userId");
        //得到选中的购物车列表
        List<CartInfo> cartCheckList = cartService.getCartCheckedList(userId);
        //得到收货地址
        List <UserAddress> userAddressList = userService.getUserAddressByUserId(userId);
        request.setAttribute("userAddressList",userAddressList);
        //订单信息集合
        ArrayList <OrderDetail> orderDetailList = new ArrayList <>();
        for (CartInfo cartInfo : cartCheckList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }
        request.setAttribute("orderDetailList",orderDetailList);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        //获取TradeCode号
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeCode",tradeNo);

        return "trade";
    }


    @RequestMapping(value = "submitOrder",method = RequestMethod.POST)
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        //获取usertId
        String userId = (String) request.getAttribute("userId");
        //检查tradeCode
        String tradeNo = request.getParameter("tradeNo");
        boolean flag = orderService.checkTradeCode(userId,tradeNo);
        if (!flag){
           request.setAttribute("errMsg","页面已失效，请重新结算！");
           return  "tradeFail";
        }

        //初始化参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        //计算总金额
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);

        //校验，验价
        List <OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //从订单中取购物skuId，数量
            boolean result = orderService.checkStock(orderDetail.getSkuId(),orderDetail.getSkuNum());
            if (!result){
                request.setAttribute("errMsg","商品库存不足，请从新下单！");
                return "tradeFail";
            }
            //验证价格
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            BigDecimal price = skuInfo.getPrice();
            int res = price.compareTo(orderDetail.getOrderPrice());
            if (res!=0){
                request.setAttribute("errMsg",orderDetail.getSkuName()+"商品价格不一致，请重新下单！");
                //加载最新价格到缓存
                cartService.loadCartCache(userId);
                return "tradeFail";
            }
        }

        //保存
        String orderId = orderService.saveOrder(orderInfo);
        //删除tradeNo
        orderService.delTradeNo(userId);

        //重定向
        return "redirect://payment.gmall.com/index?orderId=" + orderId;
    }






}

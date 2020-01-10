package com.atguigu.gmall.order.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
public class OrderController {

    @Reference
    private UserService userService;
    @Reference
    private CartService cartService;

    @RequestMapping("trade")
    public List<UserAddress> trade(String userId){
        return  userService.getUserAddressByUserId(userId);
    }


    @RequestMapping(value = "trade",method = RequestMethod.GET)
    @LoginRequire
    public String tradeInit(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        //得到选中的购物车列表
        List<CartInfo> cartCheckList = cartService.getCartCheckedList(userId);
        //收货地址
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
        return "trade";
    }






}

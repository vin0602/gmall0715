package com.atguigu.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.CookieUtil;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.print.DocFlavor;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {

    @Reference
    private CartService cartService;
    @Reference
    private ManageService manageService;

    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");
        if (userId==null){
            //可能存在cookie中
            userId = CookieUtil.getCookieValue(request, "my-userId", false);
            //如果cookie中没有userId则建立一个userId，并放入cookie中
            if (userId==null){
                userId = UUID.randomUUID().toString().replace("-", "");
                CookieUtil.setCookie(request,response,"my-userId",userId,60*60*24*7,false);
            }
        }
        //添加到购物车
        cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        //保存skuInfo对象
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        //保存添加数量
        request.setAttribute("skuNum",skuNum);

        return "success";
    }


//    @RequestMapping("cartList")
//    @LoginRequire(autoRedirect = false)
//    public String cartList(HttpServletRequest request,HttpServletResponse response){
//        //从作用域中获取userId
//        String userId = (String) request.getAttribute("userId");
//        List<CartInfo> cartInfoList = new ArrayList <>();
//
//        //调用服务层
//        if (userId!=null){
//            //用户已经登录
//            cartInfoList = cartService.getCartList(userId);
//        }else {
//            //获取cookie中的my-userId
//            String userTempId = CookieUtil.getCookieValue(request, "my-userId", false);
//                 if (userTempId!=null){
//               cartInfoList =  cartService.getCartList(userTempId);
//                System.err.println(cartService.getCartList(userTempId));
//            }
//        }
//
//        request.setAttribute("cartInfoList",cartInfoList);
//        return "cartList";
//    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        //从哪里获取userId  作用域中获取userId
        String userId = (String) request.getAttribute("userId");
        //声明购物车集合列表
        List<CartInfo> cartInfoList = null;
        if (userId==null){
            //从登陆的购物车获取数据
            //redis key=user:userId:cart
            //从cookie中获取临时的userId
            String userTempId = CookieUtil.getCookieValue(request, "my-userId", false);
            //调用服务层的方法获取缓存中的数据
            if (!StringUtils.isEmpty(userTempId)){
                // cartInfoList = cartService.getCartList(userTempId)
                //从缓存中获取购物车数据列表
                cartInfoList = cartService.getCartList(userTempId);
            }
        }else {
            //从缓存中获取购物车数据列表
            //查询未登录是否有购物车数据
            //从cookie 中获取临时的userId
            String userTempId = CookieUtil.getCookieValue(request, "my-userId", false);
            //调用服服务层的方法获取缓存中的数据
            //合并购物车{合并未登录购物车数据}
            List <CartInfo> cartTempList = new ArrayList <>();
            if (!StringUtils.isEmpty(userTempId)){
                //声明一个集合来存储未登录的数据

                //从缓存中获取购物车数据列表
                cartTempList = cartService.getCartList(userTempId);
                if (cartTempList!=null && cartTempList.size()>0){
                    //合并购物车： cartTempList未登录购物车，根据userId查询登录购物车数据
                    cartInfoList = cartService.mergeToCartList(cartTempList,userId);
                    //删除未登录购物车
                    cartService.deleteCartList(userTempId);
                }
            }
            //cartTempList == null  || cartTempList.size() == 0
            if (userTempId==null || (cartTempList == null || cartTempList.size()==0)){
                //说明未登录没有数据，直接获取数据库！
                cartInfoList = cartService.getCartList(userId);

            }
        }
        //保护到作用域
        request.setAttribute("cartInfoList",cartInfoList);

        return "cartList";
    }

    //得到前台传递过来的参数
    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        //调用服务层
        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");
        //获取用户Id
        String userId = (String) request.getAttribute("userId");
        //判断用户的状态
        if (userId!=null){
            //登录状态
            userId = CookieUtil.getCookieValue(request, "my-userId", false);
        }
        cartService.checkCart(isChecked,skuId,userId);

    }

    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        //获取uerId
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartTempList = null;
        //获取cookie中的mv-userId
        String userTempId = CookieUtil.getCookieValue(request, "my-userId", false);
        if (userTempId!=null){
            cartTempList = cartService.getCartList(userTempId);
            if (cartTempList!=null && cartTempList.size()>0){
                //合并勾选状态
                List <CartInfo> cartInfoList = cartService.mergeToCartList(cartTempList, userId);
                //删除
                cartService.deleteCartList(userTempId);
            }
        }

        return  "redirect://trade.gmall.com/trade";
    }






}

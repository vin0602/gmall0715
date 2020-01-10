package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {

    /**
     * 添加购物车 用户Id，商品Id，商品数量
     * @param skuId
     * @param userId
     * @param skuNum
     */
    void addToCart(String skuId,String userId,Integer skuNum);

    /**
     * 根据userId 查询购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);


    /**
     *根据userId查询登录购物车数据
     * @param cartTempList
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartTempList, String userId);

    /**
     * 根据userTempId临时Id删除未登录购物车
     * @param userTempId
     */
    void deleteCartList(String userTempId);

    /**
     * 修改页面idchecked状态
     * @param isChecked
     * @param skuId
     * @param userId
     */
    void checkCart(String isChecked, String skuId, String userId);

    /**
     * 根据userId查询选中购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);









}

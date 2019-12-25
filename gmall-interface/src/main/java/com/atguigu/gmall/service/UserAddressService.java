package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;

import java.util.List;

public interface UserAddressService {

    /**
     * 根据用户Id查询用户地址
     * @return
     */
    List<UserAddress> getUserAddressByUserId(String userId);
}

package com.atguigu.gmall.manage;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

//业务逻辑层
public interface UserService {

    /**
     * 查询所有用户信息
     * @return
     */
    List<UserInfo> findAll();

    /**
     * 根据用户Id查询用户地址
     * @return
     */
    List<UserAddress> getUserAddressByUserId(String userId);

}

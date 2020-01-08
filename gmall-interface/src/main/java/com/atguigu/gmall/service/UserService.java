package com.atguigu.gmall.service;

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

    /**
     * 登录
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 验证token（验证功能）
     * @param userId
     * @return
     */
    UserInfo verify(String userId);
}

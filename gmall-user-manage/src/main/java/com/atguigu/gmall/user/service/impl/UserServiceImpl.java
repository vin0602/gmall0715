package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24*7;

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;
    @Autowired
    private  RedisUtil redisUtil;


    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List <UserAddress> getUserAddressByUserId(String userId) {
//        Example example = new Example(UserAddress.class);
//        example.createCriteria().andEqualTo("userId",userId);
//        return userAddressMapper.selectByExample(example);

        UserAddress userAddress = new UserAddress();
        userAddress.setId(userId);
        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(password);
        UserInfo info = userInfoMapper.selectOne(userInfo);

        if (info!=null){
            //获得到redis，将用户存储到redis中
            Jedis jedis = redisUtil.getJedis();
            // 确定数据类型 String
            // 定义key user:userId:info
            String userKey = userKey_prefix + info.getId() + userinfoKey_suffix;
            // 设置用户的过期时间
            jedis.setex(userKey,userKey_timeOut, JSON.toJSONString(info));
            jedis.close();
            return info;
        }
        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //组成userKey
        String userKey = userKey_prefix + userId + userinfoKey_suffix;
        //获取缓存中的数据
        String userJson = jedis.get(userKey);
        if (!StringUtils.isEmpty(userJson)){
            //userJson转换为对象
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }
        return null;
    }
}

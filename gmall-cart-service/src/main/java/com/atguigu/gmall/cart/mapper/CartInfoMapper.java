package com.atguigu.gmall.cart.mapper;


import com.atguigu.gmall.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo>{

    /**
     * 根据useId查询实时价格
     * @param userId
     * @return
     */
    List<CartInfo> selectCartListWithCurPrice(String userId);





}

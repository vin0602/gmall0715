package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

    /**
     * 根据spuId查询sku查询平台属性值列表
     * @param spuId
     * @return
     */
    public List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu (String spuId);

}

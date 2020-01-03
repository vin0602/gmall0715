package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    /**
     * 根据spuId查询spu销售信息
     * @param spuId
     * @return
     */
    public List<SpuSaleAttr> selectSpuSaleAttrList(long spuId);

    /**
     * 根据spuId和skuId查询spu销售信息
     * @param skuId
     * @param spuId
     * @return
     */
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(String skuId,String spuId);


}
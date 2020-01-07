package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;


public interface ListService {

    /**
     * 根据skuLsInfo保存平台商品（商品上架）
     * @param skuLsInfo
     */
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    /**
     * 根据skuLsParams查询数据（根据用户输入的检索条件查询数据）
     * @param skuLsParams
     * @return
     */
    public SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 异步自增（热度评分）
     * @param skuId
     */
    void incrHotScore(String skuId);




}
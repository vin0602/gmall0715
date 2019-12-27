package com.atguigu.gmall.manage;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface ManageService {

    /**
     * 平台属性列表-一级分类查询
     * @return
     */
    public List<BaseCatalog1> getCatalog1();

    /**
     * 平台属性列表-二级分类查询
     * @param catalog1Id
     * @return
     */
    public List<BaseCatalog2> getCatelog2(String catalog1Id);
    public List<BaseCatalog2> getCatelog2(BaseCatalog2 baseCatalog2);

    /**
     * 平台属性列表-三级分类查询
     * @param catalog2Id
     * @return
     */
    public List<BaseCatalog3> getCatalog3(String catalog2Id);
    public List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3);

    /**
     * 平台属性列表-平台属性信息
     * @param catalog3Id
     * @return
     */
    public List<BaseAttrInfo> getAttrList(String catalog3Id);
    public List<BaseAttrInfo> getAttrList(BaseAttrInfo baseAttrInfo);

    /**
     * 平台属性列表-添加属性/修改属性（保存）
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);


    /**
     * 平台属性列表
     *    选中准修改数据 ， 根据该attrId 去查找AttrInfo，该对象下 List<BaseAttrValue> ！
         所以在返回的时候，需要返回BaseAttrInfo。
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrValueList(String attrId);

    /**
     * 商品信息管理-根据商品对象查询商品信息
     * @param spuInfo
     * @return
     */
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);
}

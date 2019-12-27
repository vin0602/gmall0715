package com.atguigu.gmall.manage;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseCatalog1;
import com.atguigu.gmall.bean.BaseCatalog2;
import com.atguigu.gmall.bean.BaseCatalog3;

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

    /**
     * 平台属性列表-三级分类查询
     * @param catalog2Id
     * @return
     */
    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 平台属性列表-平台属性信息
     * @param catalog3Id
     * @return
     */
    public List<BaseAttrInfo> getAttrList(String catalog3Id);

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
}

package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    // http://list.gmall.com/list.html?catalog3Id=61

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
//    @ResponseBody
    public String list(SkuLsParams skuLsParams, HttpServletRequest request){

        // 每页显示的条数
        skuLsParams.setPageSize(3);

        SkuLsResult skuLsResult = listService.search(skuLsParams);
//        return JSON.toJSONString(skuLsResult);
        // 获取到所有的商品信息
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        // 显示平台属性，平台属性值：
        // 必须得到平台属性值Id
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        // 根据平台属性值id 查询平台属性集合
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrInfoList(attrValueIdList);
        // 制作urlParam 参数
        String urlParam = makeUrlParam(skuLsParams);

        // 声明一个保存面包屑的集合
        ArrayList<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();
        if (baseAttrInfoList!=null && baseAttrInfoList.size()>0){
            // itco
            for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();

                // 获取平台属性值集合
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
                        for (String valueId : skuLsParams.getValueId()) {
                            //  http://list.gmall.com/list.html?catalog3Id=61&valueId=83  中的valueId 与 attrValueList 中的平台属性值Id比较
                            if (baseAttrValue.getId().equals(valueId)){
                                // 删除baseAttrInfo
                                iterator.remove();

                                // 组装面包屑 平台属性名称：平台属性值名称
                                // 将面包屑的内容 赋值给了平台属性值对象的名称
                                BaseAttrValue baseAttrValueed = new BaseAttrValue();
                                baseAttrValueed.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());

                                String newUrlParam = makeUrlParam(skuLsParams, valueId);
                                // 赋值最新的参数列表
                                baseAttrValueed.setUrlParam(newUrlParam);
                                // 将每个面包屑都放入集合中！
                                baseAttrValueArrayList.add(baseAttrValueed);
                            }
                        }
                    }
                }
            }
        }

        // 分页：
        request.setAttribute("pageNo",skuLsParams.getPageNo());
        request.setAttribute("totalPages",skuLsResult.getTotalPages());

        System.out.println("查询的参数列表："+urlParam);
        // 保存数据
        request.setAttribute("keyword",skuLsParams.getKeyword());
        request.setAttribute("baseAttrValueArrayList",baseAttrValueArrayList);
        request.setAttribute("urlParam",urlParam);
        request.setAttribute("baseAttrInfoList",baseAttrInfoList);
        request.setAttribute("skuLsInfoList",skuLsInfoList);
        return "list";

    }

    // 制作查询的参数
    private String makeUrlParam(SkuLsParams skuLsParams,String ... excludeValueIds) {
        String urlParam = "";
        // 判断用户是否输入的是keyword！
        // http://list.gmall.com/list.html?keyword=手机
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        // 判断用户是否输入的三级分类Id
        // http://list.gmall.com/list.html?catalog3Id=61
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }
        // 判断用户是否输入的平台属性值Id 检索条件
        // http://list.gmall.com/list.html?catalog3Id=61&valueId=82
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            for (String valueId : skuLsParams.getValueId()) {
                if (excludeValueIds!=null && excludeValueIds.length>0){
                    // 获取对象中的第一个数据
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        // 停止当前循环
                        continue;
                    }
                }
//                if (urlParam.length()>0){
//                    urlParam+="&";
//                }
                urlParam+="&valueId="+valueId;
            }
        }

        return urlParam;
    }

}

package com.atguigu.gmall.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Retention;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
public class ItemController {

    @Reference
    private ManageService manageService;
    @Reference
    private ListService listService;

    @LoginRequire
    @RequestMapping("/{skuId}.html")
    public String getSkuInfo(@PathVariable("skuId") String skuId, Model model) {
        // 存储基本的skuInfo信息
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        model.addAttribute("skuInfo", skuInfo);

        List <SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());


        //把列表变换成 valueid1|valueid2|valueid3 ：skuId  的 哈希表 用于在页面中定位查询
        String valueIdsKey = "";
        Map<String, String> valuesSkuMap = new HashMap<>();

        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            if (valueIdsKey.length() != 0) {
                valueIdsKey = valueIdsKey + "|";
            }
            valueIdsKey = valueIdsKey + skuSaleAttrValue.getSaleAttrValueId();
            if ((i + 1) == skuSaleAttrValueListBySpu.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i + 1).getSkuId())) {

                valuesSkuMap.put(valueIdsKey, skuSaleAttrValue.getSkuId());
                valueIdsKey = "";
            }
        }

        //把map变成json串
        String valuesSkuJson = JSON.toJSONString(valuesSkuMap);
        model.addAttribute("valuesSkuJson", valuesSkuJson);

        // 存储 spu，sku数据
        List <SpuSaleAttr> saleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);
        model.addAttribute("saleAttrList", saleAttrList);

        //最终应用由异步方式调用
        listService.incrHotScore(skuId);

        return "item";
    }




}

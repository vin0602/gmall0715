package com.atguigu.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.manage.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("spuImageList")
    public List<SpuImage> getSpuImageList(String spuId){
        return manageService.getSpuImageList(spuId);
    }

    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getspuSaleAttrList(String spuId){
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
        return  spuSaleAttrList;
    }

    // http://localhost:8082/saveSkuInfo
    @PostMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return "OK";
    }


}

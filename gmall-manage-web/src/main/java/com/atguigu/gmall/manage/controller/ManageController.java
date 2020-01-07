package com.atguigu.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class ManageController {


    @Reference
    private ManageService manageService;

    @RequestMapping("getCatalog1")
    public List <BaseCatalog1> getCatalog1() {
        return manageService.getCatalog1();
    }

    //    @RequestMapping("getCatalog2")
//    public List<BaseCatalog2> getCatalog2(String catalog1Id){
//        return manageService.getCatelog2(catalog1Id);
//    }
    @RequestMapping("getCatalog2")
    public List <BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2) {
        return manageService.getCatelog2(baseCatalog2);
    }

    //    @RequestMapping("getCatalog3")
//    public List<BaseCatalog3> getCatalog3(String catalog2Id){
//        return manageService.getCatalog3(catalog2Id);
//    }
    @RequestMapping("getCatalog3")
    public List <BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3) {
        return manageService.getCatalog3(baseCatalog3);
    }

    //    @RequestMapping("attrInfoList")
//    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
//        return manageService.getAttrList(catalog3Id);
//    }
    @RequestMapping(value = "attrInfoList",method = RequestMethod.GET)
    public List <BaseAttrInfo> attrInfoList(String catalog3Id) {
        return manageService.getAttrList(catalog3Id);
    }
//    @RequestMapping("attrInfoList")
//    public List<BaseAttrInfo> attrInfoList(BaseAttrInfo baseAttrInfo){
//        return manageService.getAttrList(baseAttrInfo);
//    }


    @RequestMapping("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        manageService.saveAttrInfo(baseAttrInfo);
    }

    //    @PostMapping("getAttrValueList")
    @RequestMapping(value = "getAttrValueList", method = RequestMethod.POST)
    public List <BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrInfo attrInfo = manageService.getAttrValueList(attrId);
        return attrInfo.getAttrValueList();
    }


//    http://localhost:8082/spuList?catalog3Id=61


}

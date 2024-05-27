package com.nari.iot.vendorinfo.controller;

import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.BhtzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @program: decloud-vendorinfo
 * @description: 备货通知单
 * @author: sunheng
 * @create: 2024-01-10 11:51
 **/
@Controller
@RequestMapping("/bhtz")
public class BhtzController {
    @Autowired
    private BhtzService bhtzService;
    @PostMapping(value = "sendMessage")
    @ResponseBody
    public LayJson sendMessage(@RequestBody Map<String, Object> map) {
        try {
          return bhtzService.sendMessage(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }
    /*
    *1、共享中心提供 备货单信息接口
 含有终端供应商名称、备货终端数量、送达时间要求、终端类型（新建、存量、或其他）、招标批次、备注
    * */
    @PostMapping(value = "addStockInformation")
    @ResponseBody
    public LayJson addStockInformation(@RequestBody Map<String, Object> map) {
        try {
         return bhtzService.addStockInformation(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }

    /*
    *2、备货响应接口
    * */
    @PostMapping(value = "responseStockList")
    @ResponseBody
    public LayJson responseStockList(@RequestBody Map<String, Object> map) {
        try {
            return bhtzService.responseStockList(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }



    /**
     *@description： 2、备货/订单 标识，进行关联 接口
     *       {
     *       "stockUp"："是否备货" （1 是，0 否 ，0就是订单）,
     *       "stockData"：备货终端数量，
     *       "bhID"："备货id", （0就是订单id，1就是备货单id，多个订单，隔开）
     *       "sqd"："申请id"
     *       }
     *@author：sunheng
     *@date：2024/1/11 15:12
     *@param：
     */
    @PostMapping(value = "setStockupId")
    @ResponseBody
    public LayJson setStockupId(@RequestBody Map<String, Object> map) {
        try {
            return bhtzService.setStockupId(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }






}

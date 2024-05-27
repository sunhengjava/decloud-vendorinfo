package com.nari.iot.vendorinfo.controller;

import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.OrderProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 *@description：4、项目订单管理
 *@author：sunheng
 *@date：2022/11/23 9:07
 *@param：
 */
@RestController
@RequestMapping(value = "orderproject")
public class OrderProjectController {
    @Autowired(required = false)
    private OrderProjectService orderProjectService;
    /**
     * 1、首页查询
     * @description：
     * @author：sunheng
     * @date：2022/11/21 15:22
     * @param：
     */
    @PostMapping("/getListPO")
    public LayJson getListPO( @RequestBody Map<String, Object> map) {
        LayJson listPO = orderProjectService.getListPO(map);
        return listPO;
    }
    /**
     * @description：导出详情数据
     * @author：sunheng
     * @date：2022/11/21 15:25
     * @param：
     */
    @RequestMapping("/exportAllExcel")
    public Map<String, Object> exportAllExcel(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> queryListInfo = orderProjectService.exportAllExcel(request, response);
        return queryListInfo;
    }

    @PostMapping("/addPO")
    public Map<String, Object> addPO(HttpServletRequest request, @RequestBody Map<String, Object> map) {
        Map<String, Object> queryListInfo = orderProjectService.addPO(request, map);
        return queryListInfo;
    }

    @RequestMapping("/exportImport")
    public Map<String, Object> exportImport(MultipartFile file, HttpServletRequest request, HttpServletResponse httpServletResponse ) throws  Exception{
            Map<String, Object> queryListInfo = orderProjectService.exportImport(file,request, httpServletResponse);
        return queryListInfo;
    }

    @RequestMapping("/exportTemplate")
    public Map<String, Object> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> queryListInfo = orderProjectService.exportTemplate(request, response);
        return queryListInfo;
    }
    /**
     *@description：详情-查看订单的状态
     *@author：sunheng
     *@param：
     */
    @PostMapping("/getOrderZtPo")
    public LayJson getOrderZtPo(@RequestBody Map<String, Object> map) {
        LayJson listPO = orderProjectService.getOrderZtPo(map);
        return listPO;
    }


    /**
     *@description：查询esn明细
     *@author：sunheng
     *@param：
     */
    @PostMapping("/getOrderOneEsnPo")
    public LayJson getOrderOneEsnPo(@RequestBody Map<String, Object> map) {
        LayJson listPO = orderProjectService.getOrderOneEsnPo(map);
        return listPO;
    }


    /**
     *@description：查询配送单号
     *@author：sunheng
     *@param：
     */
    @PostMapping("/getOrderOnePsdPo")
    public LayJson getOrderOnePsdPo(@RequestBody Map<String, Object> map) {
        LayJson listPO = orderProjectService.getOrderOnePsdPo(map);
        return listPO;
    }

    /**
     *@description：查询所属产品
     *@author：sunheng
     *@param：
     */
    @RequestMapping("/getProduct")
    public List<String> getProduct() {
        List<String> listPO = orderProjectService.getProduct();
        return listPO;
    }


    /**
     *@description：删除终端
     *@author：zhangzhihao
     *@param：
     */
    @RequestMapping("/delTermFromOrder")
    public LayJson delTermFromOrder (HttpServletRequest request) {
        return orderProjectService.del_term_from_order(request);
    }
}
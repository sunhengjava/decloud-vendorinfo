package com.nari.iot.vendorinfo.controller;

import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.impl.VendorInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 *@description： 厂家信息表
 *@author：sunheng
 *@date：2023/12/19 14:38
 *@param：
 */
@Controller
@RequestMapping("/vendor")
public class VendorController {

    @Autowired
    private VendorInfoService vendorInfoService;

    @Autowired
    CommonInterface commonInterface;
        /*查询厂家列表*/
    @RequestMapping(value = "/queryVendorGrid", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> queryVendorGrid(HttpServletRequest request) {
        try {
            return vendorInfoService.queryVendorGrid(request);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/updateVendor", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> updateVendor(HttpServletRequest request) {
        try {
            return vendorInfoService.updateVendor(request);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/insertVendor", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> insertVendor(@RequestBody Map<String, Object> map ) {
        try {
            return vendorInfoService.insertVendor(map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询供应商信息
     */
    @RequestMapping(value = "/queryGysPo", method = RequestMethod.POST)
    @ResponseBody
    public LayJson queryGysPo(HttpServletRequest request) {
        try {
            return vendorInfoService.queryGysPo(request);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 下载
     * @param request
     * @param response
     */
    @RequestMapping(value = "/exportmaintable", method = RequestMethod.GET)
    @ResponseBody
    public void exportmaintable(HttpServletRequest request, HttpServletResponse response) {
        vendorInfoService.exportmaintable(request, response);
    }


}

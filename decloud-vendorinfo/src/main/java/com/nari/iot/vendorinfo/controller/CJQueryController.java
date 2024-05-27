package com.nari.iot.vendorinfo.controller;

import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.CJQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 *@description：1、厂家发货管理
 *@author：sunheng
 *@date：2022/11/23 9:06
 *@param：
 */

@RestController
@RequestMapping("/cjquery")
public class CJQueryController {
    @Autowired
    private  CJQueryService cjQueryService;
    @Autowired
    CommonInterface commonInterface;

    /**
     *@description：查询  工单id、供应商名称、统一信用码、需求发货数量、预计发货数量、送达时间、状态、送达时间、发货数量
     *@author：sunheng
     *@date：2022/11/14 17:47
     *@param：参数：供应商名称、状态、订单创建时间
     */
    @PostMapping(value = "/getListPO")
    @ResponseBody
    public LayJson getListPO(@RequestBody Map<String, Object> map) {
        try {
            return cjQueryService.getListPO(map);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return new LayJson(500, "请求失败", null, 0);
        }
    }
    /**
     * 下载
     * @param request
     * @param response
     */
    @RequestMapping("/exportAllExcelDetail")
    public Map<String, Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response){
        Map<String, Object> queryListInfo = cjQueryService.exportAllExcelDetail(request,response);
        return queryListInfo;
    }

    /**
     *@description：查询预置合同详情
     *@author：sunheng
     *@date：2022/11/16 9:17
     *@param：
     */

    @PostMapping(value = "/getHtListPO")
    @ResponseBody
    public LayJson getHtListPO(@RequestBody Map<String, Object> map) {
        try {
            return cjQueryService.getHtListPO(map);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return new LayJson(500, "请求失败", null, 0);
        }
    }

    /**
     *@description：查询发货单详情
     *@author：sunheng
     *@date：2022/11/16 9:17
     *@param：
     */
    @PostMapping(value = "/getFhdListPO")
    @ResponseBody
    public LayJson getFhdListPO(@RequestBody Map<String, Object> map) {
        try {
            return cjQueryService.getFhdListPO(map);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return new LayJson(500, "请求失败", null, 0);
        }
    }

    /**
     *@description：查询发货状态
     *@author：sunheng
     *@date：2022/11/16 9:17
     *@param：
     */
    @PostMapping(value = "/getFhztListPO")
    @ResponseBody
    public LayJson getFhztListPO(@RequestBody Map<String, Object> map) {
        try {
            return cjQueryService.getFhztListPO(map);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return new LayJson(500, "请求失败", null, 0);
        }
    }

}

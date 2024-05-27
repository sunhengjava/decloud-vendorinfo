package com.nari.iot.vendorinfo.controller;

import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.QlclProjectService;
import com.nari.iot.vendorinfo.service.StockNtificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @program:
 * @description: 备货通知管理
 * @author: sunheng
 * @create: 2024-03-06 17:04
 **/
@RestController
@RequestMapping(value = "stock")
public class StockNtificationController {
    @Autowired(required = false)
    private StockNtificationService stockNtificationService;


    @PostMapping(value = "getStockUp")
    @ResponseBody
    public LayJson stockUp(@RequestBody Map<String, Object> map) {
        try {
            return stockNtificationService.getQlcListPo(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }

    /**
     * @description：导出详情数据
     * @param：
     */
    @RequestMapping("/exportQlcExcelDetail")
    public Map<String, Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> queryListInfo = stockNtificationService.exportAllExcel(request, response);
        return queryListInfo;
    }

    @PostMapping(value = "getStockDetail")
    @ResponseBody
    public LayJson getStockDetail(@RequestBody Map<String, Object> map) {
        try {
            return stockNtificationService.getStockDetail(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }
    /**
     * @description：导出详情数据
     * @param：
     */
    @RequestMapping("/exportStockDetail")
    public Map<String, Object> exportStockDetail(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> queryListInfo = stockNtificationService.exportStockDetail(request, response);
        return queryListInfo;
    }

}

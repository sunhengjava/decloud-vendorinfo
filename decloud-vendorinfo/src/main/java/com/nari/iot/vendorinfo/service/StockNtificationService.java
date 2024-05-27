package com.nari.iot.vendorinfo.service;

import com.nari.iot.vendorinfo.entity.LayJson;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @program: decloud-vendorinfo
 * @description: 备货通知管理
 * @author: sunheng
 * @create: 2024-03-06 17:13
 **/
public interface StockNtificationService {
    LayJson getQlcListPo(@RequestBody Map<String, Object> map) ;
    LayJson getStockDetail(@RequestBody Map<String, Object> map) ;
    Map<String, Object> exportAllExcel(HttpServletRequest request, HttpServletResponse response);
    Map<String, Object> exportStockDetail(HttpServletRequest request, HttpServletResponse response);
}

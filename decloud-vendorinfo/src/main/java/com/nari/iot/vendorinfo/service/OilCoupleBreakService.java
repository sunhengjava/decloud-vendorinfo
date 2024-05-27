package com.nari.iot.vendorinfo.service;

import com.nari.iot.vendorinfo.entity.LayJson;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @program: decloud-vendorinfo
 * @description:
 * @author: sunheng
 * @create: 2023-04-20 15:37
 **/
public interface OilCoupleBreakService {

    LayJson getAnomalyCountVO(Map<String, Object> map);
    LayJson getAnomalyDetailsVO(Map<String, Object> map);
    LayJson getAnomalyXqVO(Map<String, Object> map);
    Map<String,Object> AnomalyXqVOExcel(HttpServletRequest request, HttpServletResponse response);



}

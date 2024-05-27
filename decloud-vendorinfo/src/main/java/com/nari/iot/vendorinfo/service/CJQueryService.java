package com.nari.iot.vendorinfo.service;

import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.entity.LayJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


public interface CJQueryService {

    LayJson getListPO(@RequestBody Map<String, Object> map);
    LayJson getHtListPO(@RequestBody Map<String, Object> map);
    LayJson getFhdListPO(@RequestBody Map<String, Object> map);
    LayJson getFhztListPO(@RequestBody Map<String, Object> map);
    Map<String, Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response);



}

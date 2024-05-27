package com.nari.iot.vendorinfo.service;

import com.nari.iot.vendorinfo.entity.LayJson;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface OrderProjectService {
    LayJson getListPO(@RequestBody Map<String, Object> map) ;

    Map<String,Object> exportAllExcel(HttpServletRequest request, HttpServletResponse response);

    Map<String, Object> addPO(HttpServletRequest request, @RequestBody Map<String, Object> map);
    Map<String, Object> exportImport(MultipartFile file, HttpServletRequest request, HttpServletResponse httpServletResponse );
    Map<String, Object> exportTemplate(HttpServletRequest request, HttpServletResponse response);
    LayJson getOrderZtPo(@RequestBody Map<String, Object> map);

    LayJson getOrderOneEsnPo(@RequestBody Map<String, Object> map);
    LayJson getOrderOnePsdPo(@RequestBody Map<String, Object> map) ;
    List<String> getProduct() ;
     void upOrderState(String cgddh,Integer esnState,Integer orderState);

    LayJson del_term_from_order(HttpServletRequest request);
}
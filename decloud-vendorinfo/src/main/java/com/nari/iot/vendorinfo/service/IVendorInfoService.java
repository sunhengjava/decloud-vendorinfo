package com.nari.iot.vendorinfo.service;

import com.nari.iot.vendorinfo.entity.LayJson;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Map;


public interface IVendorInfoService {
    @SuppressWarnings("unchecked")
    Map<String, Object> queryVendorGrid(HttpServletRequest request);

    Map<String, Object> insertVendor(Map<String, Object> map);

    Map<String, Object> updateVendor(HttpServletRequest request);


    void exportmaintable(HttpServletRequest request, HttpServletResponse response);
    LayJson queryGysPo(HttpServletRequest request);

}

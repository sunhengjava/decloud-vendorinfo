package com.nari.iot.vendorinfo.service;

import com.nari.iot.vendorinfo.entity.LayJson;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


public interface BsicsQueryService {
    LayJson queryIotDevcie(HttpServletRequest request);

}

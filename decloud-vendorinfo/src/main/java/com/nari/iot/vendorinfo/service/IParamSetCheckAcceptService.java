package com.nari.iot.vendorinfo.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IParamSetCheckAcceptService {
    Map<String, Object> paramSetCheck(HttpServletRequest request);
}

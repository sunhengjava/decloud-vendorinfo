package com.nari.iot.vendorinfo.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IDeviceCountCheckAcceptService {
    Map<String, Object> deviceCountCheck(HttpServletRequest request);
}

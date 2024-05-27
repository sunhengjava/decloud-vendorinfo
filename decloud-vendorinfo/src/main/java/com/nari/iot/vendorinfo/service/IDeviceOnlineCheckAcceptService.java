package com.nari.iot.vendorinfo.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IDeviceOnlineCheckAcceptService {


    Map<String, Object> deviceOnlineCheck(HttpServletRequest request);


}

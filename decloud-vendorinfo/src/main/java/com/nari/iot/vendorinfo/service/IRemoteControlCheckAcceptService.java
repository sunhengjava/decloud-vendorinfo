package com.nari.iot.vendorinfo.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IRemoteControlCheckAcceptService {
    Map<String, Object> remoteControlCheck(HttpServletRequest request);
}

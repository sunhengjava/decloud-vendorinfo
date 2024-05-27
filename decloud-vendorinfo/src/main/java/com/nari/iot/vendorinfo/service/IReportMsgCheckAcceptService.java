package com.nari.iot.vendorinfo.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IReportMsgCheckAcceptService {
    Map<String, Object> reportMsgCheck(HttpServletRequest request);
}

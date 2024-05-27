package com.nari.iot.vendorinfo.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IDataMeasureCheckAcceptService {
    Map<String, Object> dataMeasureCheck(HttpServletRequest request);
}

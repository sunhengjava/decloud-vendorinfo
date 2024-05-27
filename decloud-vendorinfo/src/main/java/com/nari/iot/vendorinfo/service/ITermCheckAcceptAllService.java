package com.nari.iot.vendorinfo.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface ITermCheckAcceptAllService {
    Map<String, Object> termAllCheck(HttpServletRequest request);

    Map<String, Object> termAllCheck2(HttpServletRequest request);

    Map<String, Object> termFeedBack(HttpServletRequest request, Map<String, Object> map);
}

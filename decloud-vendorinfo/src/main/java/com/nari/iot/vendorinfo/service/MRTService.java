package com.nari.iot.vendorinfo.service;

import com.nari.iot.vendorinfo.entity.LayJson;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface MRTService {
    LayJson upApplicationFrom(@RequestBody Map<String, Object> map);
    LayJson secondaryTransports(@RequestBody Map<String, Object> map);
    LayJson suppierRequest(@RequestBody Map<String, Object> map);
    LayJson manufacturerResponse(@RequestBody Map<String, Object> map);
    LayJson terminalhipment(@RequestBody Map<String, Object> map);
    LayJson receiptConfirmation(@RequestBody Map<String, Object> map);
    LayJson secondaryTransportation(@RequestBody Map<String, Object> map);

    Map<String, Object> exportST(HttpServletRequest request, HttpServletResponse response);
    LayJson getEsnDetail(@RequestBody Map<String, Object> map);
    LayJson getpsd(@RequestBody Map<String, Object> map);
    LayJson getStatusDetail(@RequestBody Map<String, Object> map);
    LayJson getPicture(@RequestBody Map<String, Object> map);
}

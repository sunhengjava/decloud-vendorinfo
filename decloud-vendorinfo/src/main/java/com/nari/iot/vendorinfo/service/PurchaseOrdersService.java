package com.nari.iot.vendorinfo.service;

import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.entity.LayJsonS;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface PurchaseOrdersService {

    LayJson insertSqWorkOrder( @RequestBody Map<String, Object> map);
    LayJson updateSqWorkOrder(@RequestBody Map<String, Object> map);
    LayJson insertSpWorkOrder(@RequestBody Map<String, Object> map);
    LayJson updateSpWorkOrder(@RequestBody Map<String, Object> map);
    LayJson deleteSpWorkOrder(HttpServletRequest request);

    LayJson insertFhWorkOrder(@RequestBody Map<String, Object> map);
    LayJson updateFhWorkOrder(@RequestBody Map<String, Object> map);



    LayJson insertShWorkOrder(@RequestBody Map<String, Object> map);
    LayJson updateShWorkOrder(@RequestBody Map<String, Object> map);
    LayJson addQjWorkOrder(@RequestBody Map<String, Object> map);
    LayJson addTsWorkOrder(@RequestBody Map<String, Object> map);
    LayJson addCsPzTerm(@RequestBody Map<String, Object> map);
    LayJsonS addCsPzTermSuper(@RequestBody Map<String, Object> map);
    LayJson addZdpsTerm(@RequestBody Map<String, Object> map);
    LayJson addZdshTerm(@RequestBody Map<String, Object> map);
    LayJson addZdazOssTerm(@RequestBody Map<String, Object> map);
    LayJson addZdazTerm(@RequestBody Map<String, Object> map);
    LayJson addChildDevice(@RequestBody Map<String, Object> map);
    LayJson addChildDevices(@RequestBody Map<String, Object> map);
    LayJson addChildDevicesTy(@RequestBody Map<String, Object> map);
    LayJson addIotChildSize(@RequestBody Map<String, Object> map);
    LayJson delChildDevice(@RequestBody Map<String, Object> map);
    LayJson addZdEngineeringControlInfo(@RequestBody Map<String, Object> map);
    LayJson upOderLagdays(@RequestBody Map<String, Object> map);




}

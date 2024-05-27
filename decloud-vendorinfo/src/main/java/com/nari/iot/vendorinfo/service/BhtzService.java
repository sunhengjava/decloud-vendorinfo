package com.nari.iot.vendorinfo.service;

import com.nari.iot.vendorinfo.entity.LayJson;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public interface BhtzService {


    LayJson sendMessage(@RequestBody Map<String, Object> map);
    LayJson addStockInformation(@RequestBody Map<String, Object> map);
    LayJson responseStockList(@RequestBody Map<String, Object> map);
    LayJson setStockupId(@RequestBody Map<String, Object> map);




}

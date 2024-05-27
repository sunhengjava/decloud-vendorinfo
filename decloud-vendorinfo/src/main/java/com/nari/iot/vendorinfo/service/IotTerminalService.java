package com.nari.iot.vendorinfo.service;

import com.nari.iot.vendorinfo.entity.LayJson;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface IotTerminalService {

    Map<String, Object> insertTerminalRegister(@RequestBody Map<String, Object> map) throws UnsupportedEncodingException;
    void upRegion(String city,String devLabel);
}

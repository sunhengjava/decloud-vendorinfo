package com.nari.iot.vendorinfo.controller;

import com.nari.iot.vendorinfo.entity.AbnormalDisintegrationDTO;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.entity.ReceptionDataDTO;
import com.nari.iot.vendorinfo.service.HmTpTerminalResInfoService;
import dm.jdbc.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/hmtptermianl")
public class HmTpTerminalResInfoController {

    @Autowired
    private HmTpTerminalResInfoService hmTpTerminalResInfoService;


    @PostMapping("/dataReception")
    public LayJson dataReception(@RequestBody Map<String, String> map) {
            return  hmTpTerminalResInfoService.DataReception(map);
    }

    @PostMapping("/upIotChildSize")
    public LayJson upIotChildSize(@RequestBody Map<String, String> map) {
        return  hmTpTerminalResInfoService.upIotChildSize(map);
    }



}

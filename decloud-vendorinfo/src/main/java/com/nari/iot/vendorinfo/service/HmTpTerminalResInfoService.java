package com.nari.iot.vendorinfo.service;


import com.nari.iot.vendorinfo.entity.AbnormalDisintegrationDTO;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.entity.ReceptionDataDTO;

import java.util.Map;

public interface HmTpTerminalResInfoService {

    LayJson DataReception(Map map);
    LayJson upIotChildSize(Map map);

}

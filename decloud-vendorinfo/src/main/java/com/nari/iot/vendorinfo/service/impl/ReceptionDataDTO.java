package com.nari.iot.vendorinfo.service.impl;

import com.nari.iot.vendorinfo.entity.HmTpTerminalResourceInfo;
import lombok.Data;

import java.util.List;

@Data
public class ReceptionDataDTO {

    private List<HmTpTerminalResourceInfo> resourceInfoList;

}

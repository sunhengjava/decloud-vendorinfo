package com.nari.iot.vendorinfo.entity;

import lombok.Data;

import java.util.List;

@Data
public class ReceptionDataDTO {

    private List<HmTpTerminalResourceInfo> resourceInfoList;

}

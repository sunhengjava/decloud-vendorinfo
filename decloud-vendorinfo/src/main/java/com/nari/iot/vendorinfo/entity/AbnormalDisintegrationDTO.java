package com.nari.iot.vendorinfo.entity;

import lombok.Data;

import java.util.List;

@Data
public class AbnormalDisintegrationDTO {

    //联调体id
    private String debuggingBodyId;
    //子设备编码集合
    private List<String> subDeviceCodes;
    //综配箱编码集合
    private List<String> distributionBoxCodes;
}

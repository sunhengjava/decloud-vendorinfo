package com.nari.iot.vendorinfo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @program: decloud-vendorinfo
 * @description: 终端在状态统计
 * @author: sunheng
 * @create: 2023-10-25 11:54
 **/

@Data
@AllArgsConstructor // 注在类上，提供类的全参构造
@NoArgsConstructor // 注在类上，提供类的无参构造
@EqualsAndHashCode //注在类上，提供对应的 equals 和 hashCode 方法
public class TerminalStatusAccountingDO {
    private String orgId;
    private String orgName;
    //配送 6
    private Integer delivery=0;
    //出库
    private Integer outBound =0;
    //安装
    private Integer instaill=0;

    //建档 put on record
    private Integer putOnRecord=0;
    //验收
    private Integer check=0;

}

package com.nari.iot.vendorinfo.entity;

import lombok.Data;

//综配箱表
@Data
public class HmTpComperhensiveDistributionBoxInfo {

    //主键ID
    private String id;

    //esn
    private String termEsn;

    //设备类型
    private String deviceType;

    //厂商名称
    private String factoryName;

    //设备参数
    private String deviceParam;

    //资产id
    private String assetId;

    //招标批次
    private String batchNo;

    //创建人名称
    private String creatorName;

    //创建人oa
    private String creatorOa;

    //创建人id
    private String creatorId;

    //创建人联系号码
    private String creatorTel;

    //创建时间
    private String createTime;

    //更新时间
    private String updateTime;

    //是否删除 1 是
    private String isDel;

    //删除时间
    private String delTime;

    //是否重复资产id 1-是
    private String isRepeatId;

    //生产日期
    private String factoryDate;

    //联调体id
    private String debuggingBodyId;

    //是否有效 0有效；1无效
    private String isEffective;

}

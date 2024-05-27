package com.nari.iot.vendorinfo.entity;

import lombok.Data;

//子设备表
@Data
public class HmTpTerminalSubdevInfo {

    //主键
    //ID
    private String id;

    //终端esn
    //TERM_ESN
    private String termEsn;

    //操作id
    //OPERATE_ID
    private String operateId;

    //设备类型
    //DEV_TYPE
    private String devType;

    //设备类型描述
    //REMARK
    private String remark;

    //厂商名称
    //FACTORY_NAME
    private String factoryName;

    //设备型号
    //DEV_MODEL
    private String devModel;

    //资产id
    //ASSET_ID
    private String assetId;

    //硬件版本
    //HARDWARE_VERSION
    private String hardwareVersion;

    //生产日期
    //FACTORY_DATE
    private String factoryDate;

    //是否链接
    //IS_CONNECT
    private String isConnect;

    //创建时间
    //CREATE_TIME
    private String createTime;

    //子设备类型
    //SUB_TYPE
    private String subType;

    //修改时间
    //UPDATE_TIME
    private String updateTime;

    //子设备类型名称
    //SUB_TYPE_NAME
    private String subTypeName;

    //1-已删除
    //IS_DEL
    private String isDel;

    //删除时间
    //DEL_TIME
    private String delTime;

    //是否重复资产id 1-是
    //IS_REPEAT_ID
    private String isRepeatId;

    //创建人名称
    //CREATOR_NAME
    private String creatorName;

    //创建人oa
    //CREATOR_OA
    private String creatorOa;

    //创建人id
    //CREATOR_ID
    private String creatorId;

    //创建人联系号码
    //CREATOR_TEL
    private String creatorTel;

    //联调体id
    //DEBUGGING_BODY_ID
    private String debuggingBodyId;

    //是否有效 0有效；1无效
    //IS_EFFECTIVE
    private String isEffective;
}

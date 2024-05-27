package com.nari.iot.vendorinfo.entity;

import lombok.Data;

import java.util.List;

//终端设备表
@Data
public class HmTpTerminalResourceInfo {

    //终端id
    private String termId;

    //核心板ESN
    private String termEsn;

    //发货申请批次号
    private String batchNumber;

    //设备型号
    private String devType;

    //发货日期
    private String sendTime;

    //终端厂家
    private String termFactory;

    //客户名称
    private String orgNm;

    //关联订单采购订单号
    private String linkOrderNo;

    //删除时间
    private String delTime;

    //删除人员id
    private String delUserId;

    //删除人员名称
    private String delUserName;

    //是否有效，0-无效  1-有效
    private String isValid;

    private String tmDqzt;

    //终端状态
    private String terminalState;

    //终端入库时间-新增
    private String createTime;

    //终端入库时间-修改
    private String updateTime;

    //订单与esn绑定时间
    private String orderPushDate;

    //终端检测状态--1已检测，0未检测
    private String terminalDetectionStatus;

    //终端检测状态时间
    private String terminalDetectionStatusTime;

    //终端检测结果 1合格0不合格
    private String terminalDetectionResult;

    //终端检测结果时间
    private String terminalDetectionResultTime;

    //终端注册时间
    private String terminalRegisterTime;

    //终端调试时间
    private String terminalDebugTime;

    //配送单号
    private String terminalDistribNo;

    //终端配送时间
    private String terminalDistribTime;

    //终端收货时间
    private String terminalReceiveTime;

    //终端安装时间
    private String terminalInstallTime;

    //终端建档时间
    private String terminalRecordTime;

    //终端验收时间
    private String terminalCheckTime;

    //终端试运行时间
    private String terminalTestrunTime;

    //终端试运行异常时间
    private String terminalTestrunAbnormalTime;

    //终端投运时间
    private String terminalRunTime;

    //终端安装单id
    private String terminalInstallId;

    //终端调试状态
    private String terminalDebugStatus;

    //终端扫码信息
    private String terminalScanInfo;

    //终端调试信息
    private String terminalDebugInfo;

    //sim卡IP
    private String simIp;

    //sim卡号码
    private String simNo;

    //sim卡修改时间
    private String simUpdateTime;

    //检测中心
    private String testCenter;

    //外部平台设备ID
    private String outDevId;

    //终端试运行结果
    private String terminalTestrunResult;

    //二次转运终端发货时间
    private String secondarySendTime;

    //二次转运终端收货时间
    private String secondarySignTime;

    //二次转运联调体组装时间
    private String assemTime;

    //二次转运联调体联调时间
    private String intergratedTime;

    //二次转运id
    private String secondaryId;

    //二次转运联调体发起联调时间
    private String initiateTime;

    //是否二次转运 1是
    private String isSecondarySend;

    //联调体id
    private String debuggingBodyId;

    //综配箱集合
    private List<HmTpComperhensiveDistributionBoxInfo>  boxInfoList;
    //子设备集合
    private List<HmTpTerminalSubdevInfo> subdevInfoList;

    //开关个数
    private Integer switchNum;
    //无功个数
    private Integer idleWorkNum;
    //温湿度个数
    private Integer tempAndHumidityNum;
}

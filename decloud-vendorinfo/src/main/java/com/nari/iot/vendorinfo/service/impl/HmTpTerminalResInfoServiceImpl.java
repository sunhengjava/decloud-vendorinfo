package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.entity.*;
import com.nari.iot.vendorinfo.entity.ReceptionDataDTO;
import com.nari.iot.vendorinfo.service.HmTpTerminalResInfoService;
import dm.jdbc.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class HmTpTerminalResInfoServiceImpl implements HmTpTerminalResInfoService {

    @Autowired
    private CommonInterface commonInterface;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public LayJson DataReception(Map map) {
        String esnNo = map.get("esnNo") != null ? map.get("esnNo").toString() : "";
        String devType = map.get("devType") != null ? map.get("devType").toString() : "";
        String factoryName = map.get("factoryName") != null ? map.get("factoryName").toString() : "";
        String devModel = map.get("devModel") != null ? map.get("devModel").toString() : "";
        String assetId = map.get("assetId") != null ? map.get("assetId").toString() : "";
        String hardwareVersion = map.get("hardwareVersion") != null ? map.get("hardwareVersion").toString() : "";
        String factoryDate = map.get("factoryDate") != null ? map.get("factoryDate").toString() : "";
        String type = map.get("type") != null ? map.get("type").toString() : "";
        String tgNo = map.get("tgNo") != null ? map.get("tgNo").toString() : "";
        String tgName = map.get("tgName") != null ? map.get("tgName").toString() : "";
        String tgPmsNo = map.get("tgPmsNo") != null ? map.get("tgPmsNo").toString() : "";
        String psrId = map.get("psrId") != null ? map.get("psrId").toString() : "0";
        String isValid = map.get("isValid") != null ? map.get("isValid").toString() : "1";
        String now = sdf.format(new Date());
        String czTime = map.get("czTime") != null ? map.get("czTime").toString() : now;
        String sbcs = map.get("sbcs") != null ? map.get("sbcs").toString() : "";
        String zbpc = map.get("zbpc") != null ? map.get("zbpc").toString() : "";
        String LTTNO = map.get("LTTNO") != null ? map.get("LTTNO").toString() : "0";
        String picture = map.get("picture") != null ? map.get("picture").toString() :"";
        String getType = map.get("getType") != null ? map.get("getType").toString() :"";
        if(getType.equals("1")){
            String check = "select ASSET_ID from d5000.DMS_IOT_CHILDDEVICES " + "where LTTNO='" + LTTNO + "' " +
                    "and is_valid =1 ";
            JSONArray ja = commonInterface.dbAccess_selectList(check);
            String sql = "";
            if (ja.size() > 0) {
                return new LayJson(201, "该设备已存在", null, 0);
            }else {
                try {
                    commonInterface.dbAccess_delete("DELETE   \"D5000\".\"DMS_IOT_CHILDDEVICES\" where ESN='"+esnNo+"' and psrid='"+psrId+"' and lttno='"+LTTNO+"' ");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                sql = "insert into \"D5000\".\"DMS_IOT_CHILDDEVICES\"( ESN_NO,DEV_TYPE,FACTORY_NAME,DEVMODEL,ASSET_ID,HARDWARE_VERSION,FACTORY_DATE,TYPE,TGNO,TGNAME,TGPMSNO,NOWDATE,is_valid,psrid,SBCS,ZBPC,LTTNO,picture ) \n"
                        + "VALUES('" + esnNo + "', '" + devType + "', '" + factoryName + "','" + devModel + "', " + " '" + assetId + "', '" + hardwareVersion + "','" + factoryDate + "', '" + type + "','" + tgNo + "','" + tgName + "','" + tgPmsNo + "','" + czTime + "',"+isValid+",'" + psrId + "','"+sbcs+"','"+zbpc+"','"+LTTNO+"','"+picture+"')";
                boolean b1 = commonInterface.dbAccess_insert(sql);
                commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-9','" + sql + "','','" + b1 + "')");
                if (b1) {
                    return new LayJson(200, "联调体子设备新增成功", null, 1);
                } else {
                    return new LayJson(500, "联调体子设备新增失败", null, 0);
                }
        }
        }else if(getType.equals("2")){
          String  sql = "update \"D5000\".\"DMS_IOT_CHILDDEVICES\" set  ESN_NO='"+esnNo+"',DEV_TYPE='"+devType+"',FACTORY_NAME='"+factoryName+"',DEVMODEL='"+devModel+"',ASSET_ID='"+assetId+"',HARDWARE_VERSION='"+hardwareVersion+"',FACTORY_DATE='"+factoryDate+"',TYPE='"+type+"',NOWDATE='"+now+"',psrid='"+psrId+"',SBCS='"+sbcs+"',ZBPC='"+zbpc+"',LTTNO='"+LTTNO+"',picture='"+picture+"' "+ " where LTTNO='" + LTTNO + "' " +
                  "and is_valid =1 ";
            boolean b1 = commonInterface.dbAccess_update(sql);
            commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-9','" + sql+ "','','" + b1 + "')");
            if (b1) {
                return new LayJson(200, "联调体子设备修改成功", null, 1);
            } else {
                return new LayJson(500, "联调体子设备修改失败", null, 0);
            }
        }else if(getType.equals("3")){
            String  sql = "update \"D5000\".\"DMS_IOT_CHILDDEVICES\" set  is_valid='0'  where LTTNO='" + LTTNO + "' " +
                    "and is_valid =1 ";

            boolean b1 = commonInterface.dbAccess_update(sql);
            commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-9','" + sql+ "','','" + b1 + "')");

            if (b1) {
                return new LayJson(200, "联调体子设备删除成功", null, 1);
            } else {
                return new LayJson(500, "联调体子设删除失败", null, 0);
            }
        }
        return new LayJson(500, "请填写正确的请求类型", null, 0);
    }

    @Override
    public LayJson upIotChildSize(Map map) {
        String lowBranchNum = map.get("lowBranchNum") != null ? map.get("lowBranchNum").toString() : "";
        String nonReactiveNum = map.get("nonReactiveNum") != null ? map.get("nonReactiveNum").toString() : "";
        //温湿度传感器
        String sdcgq = map.get("sdcgq") != null ? map.get("sdcgq").toString() : "";
        //油温数量
        String ywNum = map.get("ywNum") != null ? map.get("ywNum").toString() : "";
        String esnNo = map.get("esnNo") != null ? map.get("esnNo").toString() : "";
        String insert = "update iot_device set low_branch_num='" + lowBranchNum + "',non_reactive_num='" + nonReactiveNum + "',humidity_s_num='" + sdcgq + "',oil_sensor_num='" + ywNum + "'    where dev_label='" + esnNo + "' and is_valid=1  ";
        boolean b1 = commonInterface.dbAccess_update(insert);
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-9-2','" + insert + "','','" + b1 + "')");
        if (b1) {
            return new LayJson(200, "子设备数量更新成功了", null, 1);
        } else {
            return new LayJson(500, "子设备数量更新失败,请核查网络原因", null, 0);
        }


    }
}

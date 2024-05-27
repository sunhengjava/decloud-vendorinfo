package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.Constant;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.BhtzService;
import io.lettuce.core.ScriptOutputType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;


@Service("BhtzService")
@Slf4j
public class BhtzServiceImpl implements BhtzService {


    @Autowired
    CommonInterface commonInterface;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public LayJson sendMessage(Map<String, Object> map) {
        String tel = map.get("tel") != null ? map.get("tel").toString() : "";
        String msg = map.get("message") != null ? map.get("message").toString() : "";
        String[] split = tel.split(",");
        RestTemplate restTemplate = new RestTemplate();
        for (int i = 0; i < split.length; i++) {
            String mesg = Constant.SMS_URL + "?tell=" + split[i] + "&msg="+msg;
            log.info("请求短信接口url"+mesg);
            String forObject = restTemplate.getForObject(mesg, String.class);
            log.info("请求短信接口打印结果"+forObject);
            try {
                commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'20-1','" + JSONObject.toJSONString(forObject) + "','','" + true + "')");
            } catch (Exception e) {
                log.info("发送短信的日志报错了  ");
            }
        }
        return new LayJson(200,"请求成功","",1);
    }

    /**
     *@description：
     *          {
     *          “bhdID:”"备货单id"
     *          "vendorNm"："终端供应商名称",
     *          "stockData"：备货终端数量、
     *          "deliveryTime"：送达时间要求、
     *          "terminalType"：终端类型（新建、存量、或其他）、
     *          "tenderBatch"：招标批次、
     *          "notes"：备注
     *           }
     *@author：sunheng
     *@date：2024/1/10 16:22
     *@param：
     */
    @Override
    public LayJson addStockInformation(Map<String, Object> map) {
        String dataTime = simpleDateFormat.format(new Date());
        String bhdid  = map.get("bhdID") != null ? map.get("bhdID").toString() : "";
        String vendorNm  = map.get("vendorNm") != null ? map.get("vendorNm").toString() : "";
        String stockData = map.get("stockData") != null ? map.get("stockData").toString() : "";
        String deliveryTime = map.get("deliveryTime") != null ? map.get("deliveryTime").toString() : "";
        String terminalType = map.get("terminalType") != null ? map.get("terminalType").toString() : "";
        String tenderBatch = map.get("tenderBatch") != null ? map.get("tenderBatch").toString() : "";
        String promoter = map.get("promoter") != null ? map.get("promoter").toString() : "";
        String promoterOA = map.get("promoterOA") != null ? map.get("promoterOA").toString() : "";
        String fzrname = map.get("fzrname") != null ? map.get("fzrname").toString() : "";
        String fqrtel = map.get("fqrtel") != null ? map.get("fqrtel").toString() : "";
        String fzrtel = map.get("fzrtel") != null ? map.get("fzrtel").toString() : "";
        String initiateTime = map.get("initiateTime") != null ? map.get("initiateTime").toString() : "";
        String notes = map.get("notes") != null ? map.get("notes").toString() : "";
        String sql=" insert into stocklist_ask(bhdid,vendornm,stockdata,deliverytime,terminaltype,tenderbatch,promoter,promoterOA,fzrname,fzrtel,fqrtel,initiateTime,notes,createtime) " +
                " values('"+bhdid+"','"+vendorNm+"','"+stockData+"','"+deliveryTime+"','"+terminalType+"','"+tenderBatch+"','"+promoter+"','"+promoterOA+"','"+fzrname+"','"+fzrtel+"','"+fqrtel+"','"+initiateTime+"' ,'"+notes+"','"+dataTime+"')";
      log.info("sql : "+sql);
        boolean b = commonInterface.dbAccess_insert(sql);
        if (b == true) {
            commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'20-2','" + JSONObject.toJSONString(map) + "','','" + b + "')");
            return new LayJson(200, "插入成功", null, 1);
        } else {
            return new LayJson(501, "请求失败", null, 0);
        }
    }

    @Override
    public LayJson responseStockList(Map<String, Object> map) {
        String dataTime = simpleDateFormat.format(new Date());
        String bhdID  = map.get("bhdID") != null ? map.get("bhdID").toString() : "";
        String sfbh  = map.get("sfbh") != null ? map.get("sfbh").toString() : "";
        String xyStockData = map.get("xyStockData") != null ? map.get("xyStockData").toString() : "";
        String xyDeliveryTime = map.get("xyDeliveryTime") != null ? map.get("xyDeliveryTime").toString() : "";
        String xyTerminalType = map.get("xyTerminalType") != null ? map.get("xyTerminalType").toString() : "";
        String responseTime = map.get("responseTime") != null ? map.get("responseTime").toString() : "";
        String xyrName = map.get("xyrName") != null ? map.get("xyrName").toString() : "";
        String xyrOA = map.get("xyrOA") != null ? map.get("xyrOA").toString() : "";
        String xyrtel = map.get("xyrtel") != null ? map.get("xyrtel").toString() : "";
        String xyrTenderBatch = map.get("xyrTenderBatch") != null ? map.get("xyrTenderBatch").toString() : "";
        String sql=" update \"D5000\".\"STOCKLIST_ASK\" set sfbh="+sfbh+",xyStockData='"+xyStockData+"',xyDeliveryTime='"+xyDeliveryTime+"',xyTerminalType='"+xyTerminalType+"',xytime='"+responseTime+"',xyrName='"+xyrName+"'," +
                " xyrOA='"+xyrOA+"',xyrtel='"+xyrtel+"', xytenderbatch='"+xyrTenderBatch+"',updatetime='"+dataTime+"' where bhdid='"+bhdID+"'";
        log.info(" responseStockList sql : "+sql);
        boolean b = commonInterface.dbAccess_update(sql);
        if (b == true) {
            return new LayJson(200, "更新成功", null, 1);
        } else {
            return new LayJson(501, "请求失败", null, 0);
        }
    }

    /*
    * "stockUpId"："备货id",
    "sfyqbh"："是否可按要求备货" （1 是，0 否 ，否的话就会填写）,
    "terminal":"备货的终端数量"，
    "batch_year":"招标批次"，
    "planArriveDate":"送达时间"，
    "terminalType":"终端类型（新建、存量、或其他）"
    * */
        public static void main(String[] args) {
            String s1="ss";
            String[] split = s1.split(",");
            System.out.println(JSONObject.toJSONString(split));

        }
    @Override
    public LayJson setStockupId(Map<String, Object> map) {
        String stockUp  = map.get("stockUp") != null ? map.get("stockUp").toString() : "";
        String stockData  = map.get("stockData") != null ? map.get("stockData").toString() : "";
        String bhID = map.get("bhID") != null ? map.get("bhID").toString() : "";
        String sqId = map.get("sqId") != null ? map.get("sqId").toString() : "";

        //更新请求单中备货终端数量
      String   updateSql=" update dms_work_order set stockdata='"+stockData+"' , stockUp='"+stockUp+"' where work_order_id='"+sqId+"' ";
        boolean b = commonInterface.dbAccess_update(updateSql);
        //查看是不是备货标识，1是备货单id，0则是订单id
        if(stockUp.equals("1")){
               updateSql=" update stocklist_ask set sqdid='"+sqId+"' where bhdid='"+bhID+"' ";
            boolean b2 = commonInterface.dbAccess_update(updateSql);
        }else if (stockUp.equals("0")){
            String[] split = sqId.split(",");
            for(int i=0;i<=split.length;i++){
                updateSql=" update dms_tr_project_order set sqdid='"+sqId+"'  where cgddh='"+bhID+"' ";
                boolean b2 = commonInterface.dbAccess_update(updateSql);
            }
        }
        if (b == true) {
            commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'23-2','" + JSONObject.toJSONString(map) + "','','" + b + "')");
            return new LayJson(200, "插入成功", null, 1);
        } else {
            return new LayJson(501, "请求失败", null, 0);
        }
    }
}

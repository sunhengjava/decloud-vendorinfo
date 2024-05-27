package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.HttpUtil;
import com.nari.iot.vendorinfo.controller.Tbwlb;
import com.nari.iot.vendorinfo.entity.ApolloConfig;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.IotTerminalService;
import com.nari.iot.vendorinfo.service.OrderProjectService;
import com.nari.iot.vendorinfo.service.PurchaseOrdersService;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service("IotTerminalService")
public class IotTerminalServiceImpl implements IotTerminalService {


    @Autowired
    CommonInterface commonInterface;
    @Autowired
    OrderProjectService orderProjectService;
    @Autowired
    Tbwlb tbwlb;
    @Autowired
    ApolloConfig apolloConfig;
    /*
    * 融合终端esn
        融合终端名称：长沙市+天心区+配变名称（大体命名格式）
        配变信息：配变名称、配变pms编号(16M…)、台区编号、配变资源id
        * （1）共享中心在收到 “融合终端名称”时，更新iot_device表的dev_name域，并调用吕非提供的roma更新接口
        * （2）共享中心收到配变信息，要先将此条数据的iot_device表的rely_id,rely_name域置空，再将配变信息 转换 成dms_tr_device表id和name域往rely_id,rely_name写入。
        *
    * */ SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Map<String, Object> insertTerminalRegister(Map<String, Object> map) throws UnsupportedEncodingException {
        String dataTime = simpleDateFormat.format(new Date());
        String term_esn = map.get("term_esn") == null || map.get("term_esn") == "" ? "" : map.get("term_esn").toString();
        String term_name = map.get("term_name") == null || map.get("term_name") == "" ? "" : map.get("term_name").toString();
        String tg_name = map.get("tg_name") == null || map.get("tg_name") == "" ? "" : map.get("tg_name").toString();
        String tg_pms_no = map.get("tg_pms_no") == null || map.get("tg_pms_no") == "" ? "" : map.get("tg_pms_no").toString();
        String tg_no = map.get("tg_no") == null || map.get("tg_no") == "" ? "" : map.get("tg_no").toString();
        String city_region = map.get("city_region") == null || map.get("city_region") == "" ? "" : map.get("city_region").toString();
        log.info("调用insertTerminalRegister接口" + term_esn + "_" + term_name);
        //（1）共享中心在收到 “融合终端名称”时，更新iot_device表的dev_name域，并调用吕非提供的roma更新接口
        //        查询esn对应的  deviceId、pjId、deviceName的值
        String sqlEsn = "SELECT out_dev_id, pj_id from iot_device where dev_label='" + term_esn + "'";
        List<Object[]> objects = commonInterface.selectListBySql(sqlEsn);
        Map<String, Object> reuslt = new HashMap<>();
        Map<String, String> result1 = new HashMap<>();
        Map<String, String> result2 = new HashMap<>();
        //建档失败原因
        String yy = "";
        //存库-以防以后排查
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),3,'" + JSONObject.toJSONString(map) + "','" + term_esn + "',1)");
        if (objects.size() > 0 && objects.get(0)[0] != null && objects.get(0)[1] != null) {
            log.info("进入进来了1");
            String out_dev_id = objects.get(0)[0].toString();
            log.info("进入进来了2");
            String pj_id = objects.get(0)[1].toString();
            String term_names = URLEncoder.encode(term_name, "UTF-8");
            //2、调用吕飞的接口
            String addr = "http://25.212.172.39:23503/v2/iot/editDevMsg?deviceId=" + out_dev_id + "&pjId=" + pj_id + "&deviceName=" + term_names;
            log.info("开始调用吕飞接口" + addr);
            Map<String, String> headers = new HashMap<String, String>();
            String ss = HttpUtil.httpGet(addr, headers);
            log.info("调用结果为" + ss);
            Map parse1 = (Map) JSON.parse(ss);
            log.info("转化后数据" + parse1.toString());
            String code = parse1.get("code") != null ? parse1.get("code").toString() : "";
            String errMsg = parse1.get("errMsg") != null ? parse1.get("errMsg").toString() : "";
            String message = parse1.get("message") != null ? parse1.get("message").toString() : "";
            boolean b = false;
            if (code.equals("2000")) {
                //建档成功
                String upRelyIot = "  update  iot_device set  dev_name='" + term_name + "'   where  dev_label='" + term_esn + "' and   is_valid=1 and connect_mode=1 and out_iot_fac=2  ";
                log.info("开始请求sql" + upRelyIot);
                b = commonInterface.dbAccess_insert(upRelyIot);
                result1.put("code", "200");
                result1.put("msg", "请求成功");
                result1.put("result", "1");
                result1.put("value", term_name);
                result1.put("dateTime", dataTime);
            } else {
                //建档失败
                String upRelyIot = "  update  iot_device set  is_jdys=0 ,is_jdys_time='" + dataTime + "' where  dev_label='" + term_esn + "' and   is_valid=1 and connect_mode=1 and out_iot_fac=2  ";
                b = commonInterface.dbAccess_insert(upRelyIot);
                yy = "调用建档修改名称接口失败、";
                result1.put("msg", "请求失败," + errMsg + message);
                result1.put("code", code);
                if (code.equals("5001")) {
                    result1.put("msg", "请求失败,未匹配到外部设备ID");
                }
                if (code.equals("4001")) {
                    result1.put("msg", "请求失败," + errMsg);
                }
                result1.put("result", "0");
                result1.put("value", term_name);
                result1.put("dateTime", dataTime);
            }
        } else {
            yy = "调用建档修改名称接口失败，匹配的out_dev_id, pj_id 字段的值不可为空 ";
            result1.put("code", "500");
            result1.put("msg", "请求失败，匹配的out_dev_id, pj_id 字段的值不可为空  ");
            result1.put("result", "0");
            result1.put("value", term_name);
            result1.put("dateTime", dataTime);
        }

        log.info("来到了请求2");
        if (StringUtils.isBlank(tg_pms_no)) {
            yy += " 未输入tg_pms_no字段";
            result2.put("code", "500");
            result2.put("msg", "未输入tg_pms_no字段");
            result2.put("result", "0");
            result2.put("value", "");
            result2.put("dateTime", dataTime);
        } else {
            log.info("来到了请求2-1");
            //（2）共享中心收到配变信息，要先将此条数据的iot_device表的rely_id,rely_name域置空，再将配变信息 转换 成dms_tr_device表id和name域往rely_id,rely_name写入。
            //备注：我来保传过来的这两个信息我们需要存库，以防以后排查
            String upIot_Device = "update iot_device set rely_id='' , rely_name='' where dev_label='" + term_esn + "'";
            log.info("来到了请求2-2" + upIot_Device);
            commonInterface.dbAccess_update(upIot_Device);
            String sqlTrDevice = "select n1.id,n1.name from dms_tr_device as n1  " + " left join (select * from d5000.dms_tr_account_info where run_st=20 and default_i is null) n2 on n1.device_asset_id=concat('PD_',n2.tr_pms_no) " + " where n2.default_s='" + tg_pms_no + "' ";
            log.info("来接到接口2-3" + sqlTrDevice);
            List<Object[]> objectTrDevice = commonInterface.selectListBySql(sqlTrDevice);
            if (objectTrDevice.size() > 0 && objectTrDevice.get(0)[0] != null && objectTrDevice.get(0)[1] != null) {
                String id = objectTrDevice.get(0)[0].toString();
                String name = objectTrDevice.get(0)[1].toString();
                String sqlUpIot = "update iot_device   set  rely_id='" + id + "', rely_name='" + name + "'  where dev_label='" + term_esn + "'";
                boolean b = commonInterface.dbAccess_update(sqlUpIot);
                if (b) {

                    //调用更新宽表方法： tg_id
                    new Thread(() -> {

                        try {
                            String sql2 = "select   d.id as sid,d.name as sname,b.id as aid,b.name as bname,  org.id as orgid,org.name as orgname,\n"
                                    + "\t default_s,tg_no,rate_cap,zhbb,ctbb,prop_type,cn_type,start_life_date,tg_type, a.tg_id,\n"
                                    + "\t city_base_org_id,city_org_nm,county_base_org_id,county_org_nm,gds_base_org_id,gds_org_nm,iot.run_state_time||'' AS run_state_time   ,iot.dev_name as termName,"
                                    + "  c.id as fid,c.name as fname,de.id as trid ,de.name as trName "
                                    + "\t from\n" + "\t iot_device as iot\n" + "\t left join  dms_tr_device as de  on iot.rely_id=de.id\n"
                                    + "\t left  join dms_tr_account_info as a on   de.device_asset_id=concat('PD_', a.tr_pms_no) and run_st='20'\n"
                                    + "\t left join LOW_voltage_area  as b on de.low_area_ID=b.id\n" + "\t left join dms_feeder_DEVICE as c on b.feeder_id=c.id\n"
                                    + "\t left join subSTATION as d on c.st_id=d.id\n" + "\t left join osp.isc_baseorg as org on org.code=iot.dms_region_id\n"
                                    + "\t where iot.dev_label='" + term_esn + "' and  iot.is_valid=1";
                            List<Object[]> pbkb = commonInterface.selectListBySql(sql2);
                            for (Object[] ob : pbkb) {
                                String sId = ob[0] != null ? ob[0].toString() : "";
                                String sName = ob[1] != null ? ob[1].toString() : "";
                                String aId = ob[2] != null ? ob[2].toString() : "";
                                String aName = ob[3] != null ? ob[3].toString() : "";
                                String orgid = ob[4] != null ? ob[4].toString() : "";
                                String orgname = ob[5] != null ? ob[5].toString() : "";
                                String defaults = ob[6] != null ? ob[6].toString() : "";
                                String tgNo = ob[7] != null ? ob[7].toString() : "";
                                String rateCap = ob[8] != null ? ob[8].toString() : "";
                                String zhbb = ob[9] != null ? ob[9].toString() : "";
                                String ctbb = ob[10] != null ? ob[10].toString() : "";
                                String propType = ob[11] != null ? ob[11].toString() : "";
                                String cnType = ob[12] != null ? ob[13].toString() : "";
                                String startLifeDate = ob[13] != null ? ob[13].toString() : "";
                                String tgType = ob[14] != null ? ob[14].toString() : "";
                                //配变资产id
                                String tgId = ob[15] != null ? ob[15].toString() : "";
                                String cityBaseOrgId = ob[16] != null ? ob[16].toString() : "";
                                String cityOrgNm = ob[17] != null ? ob[17].toString() : "";
                                String countyBaseOrgId = ob[18] != null ? ob[18].toString() : "";
                                String countyOrgNm = ob[19] != null ? ob[19].toString() : "";
                                String gdsBaseOrgId = ob[20] != null ? ob[20].toString() : "";
                                String gdsOrgNm = ob[21] != null ? ob[21].toString() : "";
                                String runStateTime = ob[22] != null ? ob[22].toString() : "";
                                String termName = ob[23] != null ? ob[23].toString() : "";
                                String fId = ob[24] != null ? ob[24].toString() : "";
                                String fName = ob[25] != null ? ob[25].toString() : "";
                                String trId = ob[26] != null ? ob[26].toString() : "";
                                String trName = ob[27] != null ? ob[27].toString() : "";
                                if (StringUtils.isNotBlank(cityBaseOrgId) && StringUtils.isNotBlank(cityOrgNm)) {
                                    orgid = cityBaseOrgId;
                                    orgname = cityOrgNm;
                                }
                                List<Object[]> list1 = commonInterface.selectListBySql(" select count(1) from dms_tr_device_ext where trid='" + trId + "'");
                                if(Integer.parseInt(list1.get(0)[0].toString())!=0){
                                    String upSql="update dms_tr_device_ext set fid='"+fId+"',fname='"+fName+"', sid='"+sId+"' ," +
                                            " sname='"+sName+"',AID='"+aId+"',ANAME='"+aName+"'," +
                                            " default_s='"+defaults+"' ," +
                                            " TG_NO='"+tgNo+"' ," +
                                            " rate_cap='"+rateCap+"' ," +
                                            " zhbb='"+zhbb+"' ," +
                                            " ctbb='"+ctbb+"' ," +
                                            " prop_type='"+propType+"' ," +
                                            " cn_type='"+cnType+"' ," +
                                            " start_life_date='"+startLifeDate+"' ," +
                                            " tg_type='"+tgType+"' ," +
                                            " run_state_time='"+runStateTime+"' "+
                                            "  where  trid='"+trId+"'";
                                    log.info("更新陪变宽表对应的sql为：{}"+upSql);
                                    boolean b1 = commonInterface.dbAccess_update(upSql);
                                    log.info("更新配变宽表结果为"+b1);
                                }else{
                                    String insert = "insert into  dms_tr_device_ext(trid,trname,psrid,fid,fname,sid,sname,aid,aname,\n"
                                            + "\t orgid02,orgname02,orgid03,orgname03,orgid04,orgname04,orgid05,orgname05,\n"
                                            + "\t default_s,tg_no,rate_cap,zhbb,ctbb,prop_type,cn_type,start_life_date,tg_type,run_state_time ) "
                                            + "  values( '"
                                            + trId + "','" + trName + "','" + tgId + "','" + fId + "','" + fName + "','" + sId + "','" + sName + "','"
                                            + aId + "','" + aName + "'," + " '297ebd676610090d01661013d8a00008','国网湖南省电力有限公司','" + orgid + "','"
                                            + orgname + "','" + countyBaseOrgId + "'," + " '" + countyOrgNm + "','" + gdsBaseOrgId + "','" + gdsOrgNm + "','"
                                            + defaults + "','" + tgNo + "','" + rateCap + "','" + zhbb + "','" + ctbb + "','" + propType + "'," + " '" + cnType + "','"
                                            + startLifeDate + "','" + tgType + "','"+runStateTime+"')";
                                    log.info("插入陪变宽表对应的sql为：{}"+insert);
                                    boolean sf = commonInterface.dbAccess_update(insert);
                                    log.info("更新配变宽表----{}"+sf);
                                }
                            }
                            System.out.println(Thread.currentThread().getName() + " " + b);
                        } catch (NumberFormatException e) {
                           log.info("更新配宽表报错"+e.getMessage());
                        }
                    }, "线程1").start();
                    result2.put("code", "200");
                    result2.put("msg", "请求成功");
                    result2.put("result", "1");
                    result2.put("value", name);
                    result2.put("dateTime", dataTime);
                } else {
                    yy += "写入rely_id、rely_name字段时失败，未找到该记录";
                    result2.put("code", "500");
                    result2.put("msg", "请求失败");
                    result2.put("result", "0");
                    result2.put("value", name);
                    result2.put("dateTime", dataTime);
                }
            } else {
                yy += "关联配变时失败，根据 " + tg_pms_no + " 未找到匹配的台区信息";
                //去表中查询我来保传入参数是否与同源匹配（traccount中tg_pms_no）。要是不匹配则返回这个参数与同源不一致，无法关联配变

                List<Object[]> objectTrDevice1 = commonInterface.selectListBySql(" select  tr_pms_no,default_i,run_st from dms_tr_account_info where default_s='"+tg_pms_no+"' ");
                if (objectTrDevice1.size() > 0 ) {
                    List<Object[]> collect = objectTrDevice1.stream().filter(objects1 -> ((objects1[2] .equals("20") )&&(objects1[1] != null))).collect(Collectors.toList());
                            if(collect.size()>0){
                                    yy += "，图模暂未导入，导致无法关联配变。";
                            }else {
                                yy += "，该台区已退役导致无法关联配变。";
                            }
                }else {
                    yy += "，与同源不一致，无法关联配变。";
                }
            }
            result1.put("msg", "请求失败," + yy);
        }

        reuslt.put("result1", result1);
        reuslt.put("result2", result2);
        //更新该设备下的子设备
        try {
            upZsb(term_esn);
        } catch (Exception exception) {
            log.info(exception.getMessage());
        }
        //（3）在（2）将边设备的rely_id,rely_name更新后，通过direct_id将边设备下的子设备的
        //
        // +
        // rely_id,rely_name，rely_type域同步更新。目前子设备的pd_id如下（后期会有增减） ：
        String sql3 = "update iot_device set rely_id='',rely_name='',rely_type=''  where  id in (SELECT id from iot_device where out_iot_fac='2' and is_valid='1' and pd_id='4216776626102337522' and direct_id in\n" + "(SELECT direct_id from iot_device where out_iot_fac='2' and is_valid='1' and pd_id='4216776626119114757') )";
        boolean b = commonInterface.dbAccess_update(sql3);
        log.info("刷新指定设备的的rely_id、nrely_name置为空" + b);
        //如果resut1、result2都成功了则更改 is_jdys 跟is_jdys_time 字段
        String sql4 = "";
        log.info(JSONObject.toJSONString(result1) + "______" + JSONObject.toJSONString(result2));
        if (result1.size() > 0 && result2.size() > 0 && result1.get("code") != null && result1.get("code").equals("200") && result2.get("code") != null && result2.get("code").equals("200")) {
            sql4 = "   update iot_device set is_jdys='" + 1 + "' ,is_jdys_time='" + dataTime + "' where  dev_label='" + term_esn + "'  and is_valid=1\n" + "    and connect_mode=1 " + "    and out_iot_fac=2";
            reuslt.put("code", "200");
            reuslt.put("result", 1);
            reuslt.put("resultValue", "成功");
        } else {
            sql4 = "   update iot_device set is_jdys='" + 0 + "' ,is_jdys_time='" + dataTime + "' where  dev_label='" + term_esn + "'  and is_valid=1\n" + "    and connect_mode=1 " + "    and out_iot_fac=2";
            reuslt.put("code", "500");
            reuslt.put("result", 0);
            reuslt.put("resultValue", "失败");
        }
        commonInterface.dbAccess_update(sql4);
//将新增数据传递我来保
        try {
            log.info("调用我来保接口");
            tbwlb.zdzcUpdateIotDeviceInfo(term_esn);
        } catch (Exception e) {
            log.info("iot_device新增记录更新我来保失败了：" + e.getMessage());
        }

        try {
            //更新订单注册建档状态
            if (reuslt.get("code").toString().equals("200")) {
                try {
                    //更新资源库状态
                    commonInterface.dbAccess_update("update DMS_IOT_DEVICE_RESOURCE_INFO set tm_dqzt=9 ,jdresult='" + JSONObject.toJSONString(reuslt) + "',jdresult1='建档成功',jdsf=1   where term_esn='" + term_esn + "'  and is_valid=1 ");
                    //根据域号更新边子设备
                    try {
                        upRegion(city_region, term_esn);
                    } catch (Exception e) {
                        log.info("upRegion报错了" + e.getMessage());
                    }
                    //更新订单状态
                    String sql = "select link_order_no from DMS_IOT_DEVICE_RESOURCE_INFO where term_esn='" + term_esn + "' and is_valid=1";
                    List<Object[]> list = commonInterface.selectListBySql(sql);
                    if (list.size() > 0) {
                        Object[] orderIdObject = list.get(0);
                        if (orderIdObject.length > 0) {
                            orderProjectService.upOrderState(orderIdObject[0].toString(), 9, 7);
                        }
                    }
                } catch (Exception e) {
                    log.info("reuslt进行类型判断报错了" + e.getMessage());
                }
            } else {
                commonInterface.dbAccess_insert("update DMS_IOT_DEVICE_RESOURCE_INFO set jdresult='" + JSONObject.toJSONString(reuslt) + "',jdresult1='" + yy + "',jdsf=2   where term_esn='" + term_esn + "'  and is_valid=1 ");
            }
        } catch (Exception e) {
            log.info("建档完成后出现了错误" + e);
        }

        reuslt.put("dataTime", dataTime);

        return reuslt;
    }

    @Override
    public void upRegion(String city, String devLabel) {
        log.info("upRegion输出当前传参为" + city + "----" + devLabel);
        String areaId = "";
        if (city.lastIndexOf("常德") != -1) {
            areaId = "43070000";
        }
        if (city.lastIndexOf("郴州") != -1) {
            areaId = "43100000";
        }
        if (city.lastIndexOf("衡阳") != -1) {
            areaId = "43040000";
        }
        if (city.lastIndexOf("怀化") != -1) {
            areaId = "43120000";
        }
        if (city.lastIndexOf("娄底") != -1) {
            areaId = "43130000";
        }
        if (city.lastIndexOf("邵阳") != -1) {
            areaId = "43050000";
        }
        if (city.lastIndexOf("湘潭") != -1) {
            areaId = "43030000";
        }
        if (city.lastIndexOf("湘西") != -1) {
            areaId = "43310000";
        }
        if (city.lastIndexOf("益阳") != -1) {
            areaId = "43090000";
        }
        if (city.lastIndexOf("岳阳") != -1) {
            areaId = "43060000";
        }
        if (city.lastIndexOf("张家界") != -1) {
            areaId = "43080000";
        }
        if (city.lastIndexOf("长沙") != -1) {
            areaId = "43010000";
        }
        if (city.lastIndexOf("株洲") != -1) {
            areaId = "43020000";
        }
        if (city.lastIndexOf("永州") != -1) {
            areaId = "43110000";
        }
        if (StringUtils.isNotBlank(areaId)) {
            String sql = " select id from d5000.iot_project where is_valid='1' and out_iot_fac='2' and dms_region_id='" + areaId + "' ";
            List<Object[]> objects = commonInterface.selectListBySql(sql);
            if (objects.size() > 0 && objects.get(0)[0] != null && objects.get(0)[0].toString() != null) {
                String pjId = objects.get(0)[0].toString();
                String sql1 = "update iot_device set dms_region_id='" + areaId + "',pj_id='" + pjId + "'      \n" + "  where   is_valid='1' and out_iot_fac='2'  and  dev_label='" + devLabel + "' and connect_mode=1";
                log.info("更新 setdms_region_id sql1" + sql1);
                commonInterface.dbAccess_update(sql1);
                String sql2 = "  update iot_device set dms_region_id='" + areaId + "',pj_id='" + pjId + "'    \n" + "  where   is_valid='1' and out_iot_fac='2'  and  (\n" + "  direct_id in ( select id from iot_device  where  is_valid='1' and out_iot_fac='2'  and  dev_label='" + devLabel + "' and connect_mode=1 )\n" + "and connect_mode=3 )";
                commonInterface.dbAccess_update(sql2);
                log.info("更新 setdms_region_id  sql2" + sql2);
            }
        } else {
            log.info(city + "的areaId 未匹配到");

        }


    }

    public void upZsb(String devLabel) {

        String reload_pd_id = apolloConfig.getReload_pd_id();
        reload_pd_id = "'" + reload_pd_id.replace(",", "','") + "'";
        //调用吕飞接口
        //2、调用吕飞的接口
        String sql = "           select  dev.id,  dev.dev_name,  dev2.dev_name as dev_names,  sys.display_value, dev.pj_id,  dev.out_dev_id   \n" + "    from (select  id,dev_name,pd_id,direct_id,pj_id,out_dev_id   from iot_device where  is_valid='1' and out_iot_fac='2'   and connect_mode='3' ) as dev\n" + " left join    iot_product as pro on pro.id=dev.pd_id and  pro.is_valid=1 and pro.out_iot_fac='2'\n" + "left join sys_menu_info as sys on pro.pd_mode=sys.actual_value  and  sys.menu_name='IOT产品型号'  and sys.actual_value!=0\n" + "left join (select  id,dev_name,pd_id,rely_id,rely_name,rely_type,direct_id,pj_id,out_dev_id    from iot_device where  is_valid='1' and out_iot_fac='2'   and connect_mode='1' and dev_label='" + devLabel + "'  ) as dev2\n" + "on dev2.id=dev.direct_id \n" + "where  \n" + " sys.display_value is not null\n" + "and dev2.dev_name is not null\n" + "and   length(dev2.dev_name||'_'||sys.display_value)<=32\n" + " and  dev.pd_id in (" + reload_pd_id + ")";
        log.info("iot每天三点进行查询的sql为——————" + sql);
        List<Object[]> outList = commonInterface.selectListBySql(sql);
        log.info("outList大小为——————" + outList.size());
        try {
            int i = 0;
            for (Object[] ob : outList) {
                log.info("开始便利——————" + JSONObject.toJSONString(ob));
                if (ob.length > 0 && StringUtils.isNotBlank(ob[2].toString())) {
                    i++;
                    log.info("输入deviceName" + ob[2] + "_" + ob[3]);
                    String deviceName = URLEncoder.encode(ob[2] + "_" + ob[3], "UTF-8");
                    String addr = "http://25.212.172.39:23503/v2/iot/editDevMsg?deviceId=" + ob[5] + "&pjId=" + ob[4] + "&deviceName=" + deviceName;
                    Map<String, String> headers = new HashMap<String, String>();
                    String ss = HttpUtil.httpGet(addr, headers);
                    log.info("调用吕飞接口结果" + ss);
                }
            }
            log.info("调用接口次数" + i);
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        //查询菜单表 存储成map后期调用直接用
        String sysSql = "update iot_device as a set a.rely_id=b.rely_id,a.rely_name=b.rely_name,a.rely_type=b.rely_type ,\n" + " a.dev_name= \n" + " case \n" + " when b.display_value is not null and  (b.dev_names is not null) and ( length(b.dev_names||'_'||b.display_value)<=32) then b.dev_names||'_'||b.display_value\n" + " else a.dev_name end \n" + " from (\n" + "   select  dev.id,dev2.rely_id,dev2.rely_name,dev2.rely_type,dev.direct_id,dev2.dev_name as dev_names, sys.display_value   \n" + "    from (select  id,dev_name,pd_id,rely_id,rely_name,rely_type,direct_id from iot_device where  is_valid='1' and out_iot_fac='2'   and connect_mode='3'  ) as dev\n" + " left join    iot_product as pro on pro.id=dev.pd_id and  pro.is_valid=1 and pro.out_iot_fac='2'\n" + " left join sys_menu_info as sys on pro.pd_mode=sys.actual_value  and  sys.menu_name='IOT产品型号'  and sys.actual_value!=0\n" + " left join (select  id,dev_name,pd_id,rely_id,rely_name,rely_type,direct_id from iot_device where  is_valid='1' and out_iot_fac='2'   and connect_mode='1' and dev_label='" + devLabel + "' ) as dev2\n" + " on dev2.id=dev.direct_id  where dev2.id is not null \n" + " )as b\n" + "  where   a.is_valid='1' and a.out_iot_fac='2'   and a.connect_mode='3'  and a.id=b.id\n" + " and  a.pd_id in (" + reload_pd_id + ")";
        log.info("调用刷新子设备接口" + sysSql);
        boolean b = commonInterface.dbAccess_update(sysSql);

    }
}



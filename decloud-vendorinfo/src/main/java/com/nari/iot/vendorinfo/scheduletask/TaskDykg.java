package com.nari.iot.vendorinfo.scheduletask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.util.*;

/**
 * @program:
 * @description:低压开关关联
 * @author: sunheng
 * @create: 2023/4/3
 **/
@Configuration
@EnableScheduling
@Slf4j
@RequestMapping("/TaskDykg")
public class TaskDykg {
    @Autowired
    CommonInterface commonInterface;


    //表⽰每天凌晨⼀点执⾏⼀次
    @RequestMapping("/taskPbkb")
    @Scheduled(cron = " 0 05 21 * * ? ")
    public String taskPbkb() {
        log.info("开始刷新配变宽表了-----");
        String delsql=" delete dms_tr_device_ext ";
        log.info("进行删除语句"+delsql);
        boolean b1 = commonInterface.dbAccess_delete(delsql);
        log.info("删除语句结果为"+b1);
        String sql = "select de.id as trid,de.name as trname,a.psr_id as psrid,c.id as fid,c.name as fname,d.id as sid,d.name as sname, --所属场站id、name\n" +
                "\tb.id as aid,b.name as aname,'' as orgid06,'' as orgname06,a.gds_base_org_id as orgid05,gds_org_nm as orgname05,\n" +
                "\ta.county_base_org_id as orgid04,a.county_org_nm as orgname04,a.city_base_org_id as orgid03,a.city_org_nm as orgname03,\n" +
                "\t' 国网湖南省电力有限公司 'as orname02,'297ebd676610090d01661013d8a00008' as orgid02 ,a.code  as code,'' as trct,'' as is_reral,\n" +
                "\tiot.dev_label as term_id,de.pms_id as pms_id,'' as edrl,'' as dxhsx,'' as  tr_station,\n" +
                "\t de.device_asset_id as device_asset_id,a.default_s as default_s,a.tg_no as tgno,a.rate_cap as rate_cap,\n" +
                "\ta.zhbb as zhbb,a.ctbb as ctbb,a.prop_type as prop_type,a.cn_type as cn_type,a.start_life_date||'' as start_life_date,\n" +
                "\ta.tg_type as tg_type,iot.run_state_time||'' as run_state_time,org.id,org.name  \n" +
                "\t from iot_device as iot\n" +
                "\tleft join  dms_tr_device as de  on iot.rely_id=de.id\n" +
                "\tleft  join dms_tr_account_info as a on   de.device_asset_id=concat('PD_', a.tr_pms_no) and run_st='20'\n" +
                "\tleft join LOW_voltage_area  as b on de.low_area_ID=b.id\n" +
                "\tleft join dms_feeder_DEVICE as c on de.feeder_id=c.id\n" +
                "\tleft join subSTATION as d on c.st_id=d.id\n" +
                "\tleft join osp.isc_baseorg as org on org.code=iot.dms_region_id\n" +
                "\twhere iot.is_valid=1  \n" +
                "    and iot.connect_mode=1\n" +
                "    and iot.out_iot_fac =2\n" +
                "     and  rely_id is not null";
        List<Object[]> objectsZz = commonInterface.selectListBySql(sql);
        log.info("查询sql{}"+sql);
        for (Object[] objects : objectsZz) {
            try {
                if (objectsZz != null) {
                    String trid = objects[0] != null ? objects[0].toString() : "";
                    String trname = objects[1] != null ? objects[1].toString() : "";
                    String psrid = objects[2] != null ? objects[2].toString() : "";
                    String fid = objects[3] != null ? objects[3].toString() : "";
                    String fname = objects[4] != null ? objects[4].toString() : "";
                    String sid = objects[5] != null ? objects[5].toString() : "";
                    String sname = objects[6] != null ? objects[6].toString() : "";
                    String aid = objects[7] != null ? objects[7].toString() : "";
                    String aname = objects[8] != null ? objects[8].toString() : "";
                    String orgid06 = objects[9] != null ? objects[9].toString() : "";
                    String orgname06 = objects[10] != null ? objects[10].toString() : "";
                    String orgid05 = objects[11] != null ? objects[11].toString() : "";
                    String orgname05 = objects[12] != null ? objects[12].toString() : "";
                    String orgid04 = objects[13] != null ? objects[13].toString() : "";
                    String orgname04 = objects[14] != null ? objects[14].toString() : "";
                    String orgid03 = objects[15] != null ? objects[15].toString() : "";
                    String orgname03 = objects[16] != null ? objects[16].toString() : "";
                    String orname02 = objects[17] != null ? objects[17].toString() : "";
                    String orgid02 = objects[18] != null ? objects[18].toString() : "";
                    String code = objects[19] != null ? objects[19].toString() : "";
                    String trct = objects[20] != null ? objects[20].toString() : "";
                    String is_reral = objects[21] != null ? objects[21].toString() : "";
                    String term_id = objects[22] != null ? objects[22].toString() : "";
                    String pms_id = objects[23] != null ? objects[23].toString() : "";
                    String dxhsx = objects[25] != null ? objects[25].toString() : "";
                    String tr_station = objects[26] != null ? objects[26].toString() : "";
                    String edrl = objects[24] != null ? objects[24].toString() : "";
                    String device_asset_id = objects[27] != null ? objects[27].toString() : "";
                    String default_s = objects[28] != null ? objects[28].toString() : "";
                    String tgno = objects[29] != null ? objects[29].toString() : "";
                    String rate_cap = objects[30] != null ? objects[30].toString() : "";
                    String zhbb = objects[31] != null ? objects[31].toString() : "";
                    String ctbb = objects[32] != null ? objects[32].toString() : "";
                    String prop_type = objects[33] != null ? objects[33].toString() : "";
                    String cn_type = objects[34] != null ? objects[34].toString() : "";
                    String start_life_date = objects[35] != null ? objects[35].toString() : "";
                    String tg_type = objects[36] != null ? objects[36].toString() : "";
                    String run_state_time = objects[37] != null ? objects[37].toString() : "";
                    String orgid = objects[38] != null ? objects[38].toString() : "";
                    String orgName = objects[39] != null ? objects[39].toString() : "";
                    if(StringUtils.isBlank(orgid03)){
                        orgid03=orgid;
                        orgname03=orgName;
                    }

                    String upSql = "insert into dms_tr_device_ext(trid,trname,psrid,fid,fname,sid,sname,aid,aname,orgid06,orgname06,orgid05, " +
                            " orgname05,orgid04,orgname04,orgid03,orgname03,orgid02,orgname02,code,trct,is_rural,term_id,pms_id,edrl,dxhsx,tr_station," +
                            " device_asset_id,default_s,tg_no,rate_cap,zhbb,ctbb,prop_type,cn_type,start_life_date,tg_type,run_state_time ) values (" +
                            " '" + trid + "','" + trname + "','" + psrid + "','" + fid + "','" + fname + "','" + sid + "','" + sname + "','" + aid + "','" + aname + "','" + orgid06 + "','" + orgname06 + "'," +
                            " '" + orgid05 + "','" + orgname05 + "','" + orgid04 + "','" + orgname04 + "','" + orgid03 + "','" + orgname03 + "','" + orgid02 + "','" + orname02 + "','" + code + "','" + trct + "'," +
                            " '" + is_reral + "','" + term_id + "','" + pms_id + "','" + edrl + "','" + dxhsx + "','" + tr_station + "','" + device_asset_id + "','" + default_s + "','" + tgno + "'," +
                            " '" + rate_cap + "','" + zhbb + "','" + ctbb + "','" + prop_type + "','" + cn_type + "','" + start_life_date + "','" + tg_type + "','" + run_state_time + "' )";
                    log.info("插入宽表sql为：{}" + upSql);
                    boolean b = commonInterface.dbAccess_insert(upSql);
                      log.info("进行插入宽表执行结果为：{}" + b);
                }
            }catch (Exception e){
                log.info("报错了"+e);
            }
        }
        log.info("配变宽表插入完成了");
        return "统计完成";
    }

    //表⽰每天凌晨⼀点执⾏⼀次
    @RequestMapping("/taskUpDykg")
    @Scheduled(cron = " 0 0 5 * * ? ")
    public String taskUpDykg() {
        //--1将rely_id置为空
        String sql1 = "  update iot_device set rely_id ='',rely_name='' \n"
                + "        where rely_id in (select rely_id FROM iot_device  as d \n"
                + "    left join low_voltage_switch as sw \n" + "    on d.rely_id=sw.id\n"
                + "    where d.is_valid    =1\n" + "    and d.connect_mode=3\n"
                + "    and pd_id in(select id from iot_product where pd_name = '湖南低压开关APP' or pd_name = '湖南智能型低压塑壳断路器带漏电保护')\n"
                + "    and sw.id is null and rely_id is not null)\n" + "     and  is_valid    =1\n"
                + "    and connect_mode=3 ";
        log.info("执行将rely_id置空");
        commonInterface.dbAccess_update(sql1);
        // --2找到所有子设备 放到一个map中 <边设备id,id,dev_label> substr截取的是低压开关的编码
        String sql2 = " select\n" +
                "        d.direct_id                                                ,\n" +
                "        d.id                                                       ,\n" +
                "        d.dev_label                                                ,\n" +
                //   "        Substring(d.dev_label, (instr(d.dev_label,'0000000000')+10), 2),\n" +
                "       1 ,\n" +
                "        d.out_dev_id                                               ,\n" +
                "        d.dev_name                                                 ,\n" +
                "        d.pj_id\n" +
                "FROM\n" +
                "        iot_device           as d\n" +
                "left join low_voltage_switch as sw\n" +
                "on\n" +
                "        d.rely_id=sw.id\n" +
                "where\n" +
                "        d.is_valid    =1\n" +
                "    and d.connect_mode=3\n" +
                "    and pd_id        in\n" +
                "        (\n" +
                "                select id from iot_product where pd_name = '湖南低压开关APP' or pd_name = '湖南智能型低压塑壳断路器带漏电保护' \n" +
                "        )\n" +
                "    and rely_id is null " +
                "   ";
        log.info("执行了sql2" + sql2);
        List<Object[]> devList = commonInterface.selectListBySql(sql2);
        //KYE 边设备id ，VALUE :(子设备信息) 作用-根据边设备以及截取后的dev_label来确认 子设备id
        Map<String, Map<String, Object[]>> map = new HashMap();
        for (Object[] ob : devList) {
            Map<String, Object[]> map1 = new HashMap<>();
            //边：多子（子类型，子id）
            if (map.containsKey(ob[0].toString())) {
                map1 = map.get(ob[0].toString());
            }
            Object[] ob1 = ob;
            if (ob[2] != null && ob[1] != null) {
                int i = ob[2].toString().indexOf("0000000000");
                if (i != -1) {
                    try {
                        ob1[3] = ob[2].toString().substring(i + 10, i + 10 + 2);
                    } catch (Exception e) {
                        ob1[3] = "-1";
                        log.info("taskUpDykg方法2截取报错" + e);
                    }
                    //log.info("sql2截取后字符传为" + ob1[3]);
                    map1.put(ob1[3].toString(), ob1);
                }
            }
            map.put(ob1[0].toString(), map1);
        }
        //--3找到所有边设备
        String sql3 = "  with \n" +
                "    t1 as (\n" +
                "    select*from  iot_device \n" +
                "    WHERE ID IN ( select distinct direct_id FROM iot_device  as d \n" +
                "    left join low_voltage_switch as sw \n" +
                "    on d.rely_id=sw.id\n" +
                "    where d.is_valid    =1\n" +
                "    and d.connect_mode=3\n" +
                "    and pd_id in(select id from iot_product where pd_name = '湖南低压开关APP' or pd_name = '湖南智能型低压塑壳断路器带漏电保护' )\n" +
                "   and rely_id is null  )\n" +
                "  \t) \n" +
                "   " +
                "  select t1.id,t1.dev_name,sw.id as sid,sw.name, " +
                //" substr(sw.name,instr(sw.name,'低压开关')+4) as jqh \n" +
                " 1 as jqh  " +
                "   from t1 " +
                "   left join dms_tr_device as de on t1.rely_id=de.id \n" +
                "   left join low_voltage_switch as sw on de.low_area_id=sw.low_area_id\n" +
                "   where sw.name like '%低压开关%' ";
        //循环边设备 集合 ，去map 根据边设备id去匹配
        List<Object[]> devList3 = commonInterface.selectListBySql(sql3);
        log.info("查询对应的devList3数据为" + JSONObject.toJSONString(devList3));
        for (Object[] objects : devList3) {
            if (objects[0] != null) {
                Map<String, Object[]> stringStringMap = map.get(objects[0].toString());
                log.info("获取到对应的子设备信息" + JSONObject.toJSONString(stringStringMap));
                log.info(objects[3].toString());
                int i = objects[3].toString().indexOf("低压开关");
                if (i != -1) {
                    log.info(i + "找到了低压开关字符");
                    try {
                        String jqh = objects[3].toString().substring(i + 4, i + 4 + 2);
                        objects[4] = jqh;
                        if (stringStringMap.containsKey(objects[4].toString())) {
                            log.info("进入了最后判断");
                            //上面map的ob[]数据
                            Object[] sonOb = stringStringMap.get(objects[4].toString());
                            log.info("获取到对应的详情信息" + JSONObject.toJSONString(sonOb));
                            String sql4 = " update iot_device set rely_type=4 ,rely_id='" + objects[2] + "',rely_name='" + objects[3] + "',dev_name='" + objects[3] + "' where id='" + sonOb[1] + "' and is_valid=1 and connect_mode=3 ";
                            commonInterface.dbAccess_update(sql4);
                            //调用吕飞的接口 d.out_dev_id,d.dev_name,d.pj_id
                            String out_dev_id = sonOb[4].toString();
                            String dev_name = objects[3].toString();
                            String pj_id = sonOb[6].toString();

                            String deviceName = URLEncoder.encode(dev_name, "UTF-8");
                            upIotDeviceName(out_dev_id, pj_id, deviceName);
                        }
                    } catch (Exception e) {
                        objects[4] = null;
                        log.info("taskUpDykg方法3截取报错" + e);
                    }


                }

            }
        }
        log.info("taskUpDykg执行完毕了");
        return "执行完毕";
    }

    public void upIotDeviceName(String deviceId, String pjId, String deviceName) {
        //调用吕飞接口
        try {
            String addr = "http://25.212.172.39:23503/v2/iot/editDevMsg?deviceId=" + deviceId + "&pjId=" + pjId + "&deviceName=" + deviceName;
            Map<String, String> headers = new HashMap<String, String>();

            String ss = HttpUtil.httpGet(addr, headers);
            log.info("调用吕飞接口结果" + ss);
            Map parse1 = (Map) JSON.parse(ss);
            String code = parse1.get("code") != null ? parse1.get("code").toString() : "";
            if (!code.equals("2000")) {
                log.error("请求出现问题，对应的请求地址" + addr);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

}

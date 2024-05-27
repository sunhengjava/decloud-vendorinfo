package com.nari.iot.vendorinfo.scheduletask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.HttpUtil;
import com.nari.iot.vendorinfo.controller.Tbwlb;
import com.nari.iot.vendorinfo.entity.ApolloConfig;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: decloud-vendorinfo
 * @description:刷新边设备
 * @author: sunheng
 * @create: 2023-01-10 18:50
 **/
@Configuration
@EnableScheduling
@Slf4j
@RequestMapping("/rloadbsb")
public class RloadBsb {
    @Autowired
    ApolloConfig apolloConfig;
    @Autowired
    CommonInterface commonInterface;
    @Autowired
    Tbwlb tbwlb;

    //表⽰每天凌晨⼀点执⾏⼀次
    @RequestMapping("/taskUpdate")
    @Scheduled(cron = " * * 3 * * ? ")
    public String taskUpdate() {
        String reload_pd_id = apolloConfig.getReload_pd_id();
        reload_pd_id = "'" + reload_pd_id.replace(",", "','") + "'";
        //调用吕飞接口
        //2、调用吕飞的接口
        String sql = "           select  dev.id,  dev.dev_name,  dev2.dev_name as dev_names,  sys.display_value, dev.pj_id,  dev.out_dev_id   \n" +
                "    from (select  id,dev_name,pd_id,direct_id,pj_id,out_dev_id   from iot_device where  is_valid='1' and out_iot_fac='2'   and connect_mode='3' ) as dev\n" +
                " left join    iot_product as pro on pro.id=dev.pd_id and  pro.is_valid=1 and pro.out_iot_fac='2'\n" +
                "left join sys_menu_info as sys on pro.pd_mode=sys.actual_value  and  sys.menu_name='IOT产品型号'  and sys.actual_value!=0\n" +
                "left join (select  id,dev_name,pd_id,rely_id,rely_name,rely_type,direct_id,pj_id,out_dev_id    from iot_device where  is_valid='1' and out_iot_fac='2'   and connect_mode='1'  ) as dev2\n" +
                "on dev2.id=dev.direct_id \n" +
                "where dev.dev_name is  null \n" +
                "and sys.display_value is not null\n" +
                "and dev2.dev_name is not null\n" +
                "and   length(dev2.dev_name||'_'||sys.display_value)<=32\n" +
                " and  dev.pd_id in (" + reload_pd_id + ")";
        log.info("iot每天三点进行查询的sql为——————" + sql);
        List<Object[]> outList = commonInterface.selectListBySql(sql);
        log.info("outList大小为——————" + outList.size());
        try {
            int i = 0;
            for (Object[] ob : outList) {
                log.info("开始便利——————" + JSONObject.toJSONString(ob));
                if (ob.length > 0 && StringUtils.isNotBlank(ob[2].toString())) {
                    i++;
                    log.info("输入deviceName"+ob[2] + "_" + ob[3]);
                    String deviceName = URLEncoder.encode(ob[2] + "_" + ob[3], "UTF-8");
                    String addr = "http://25.212.172.39:23503/v2/iot/editDevMsg?deviceId=" + ob[5] + "&pjId=" + ob[4] + "&deviceName=" + deviceName;
                    Map<String, String> headers = new HashMap<String, String>();
                    String ss = HttpUtil.httpGet(addr, headers);
                    log.info("调用吕飞接口结果" + ss);
                    Map parse1 = (Map) JSON.parse(ss);
                    String code = parse1.get("code")!=null?parse1.get("code").toString():"";
                    boolean b = false;
                    if (code.equals("2000")) {
                    /*    try {
                            log.info("调用我来保接口");
                            tbwlb.zdzcUpdateIotDeviceInfo(term_esn);
                        } catch (Exception e) {
                            log.info("iot_device新增记录更新我来保失败了：" + e.getMessage());
                        }*/
                    }
                }
            }
            log.info("调用接口次数" + i);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        //查询菜单表 存储成map后期调用直接用
        String sysSql = "update iot_device as a set a.rely_id=b.rely_id,a.rely_name=b.rely_name,a.rely_type=b.rely_type ,\n" +
                " a.dev_name= \n" +
                " case \n" +
                " when b.display_value is not null and  (b.dev_names is not null) and ( length(b.dev_names||'_'||b.display_value)<=32) then b.dev_names||'_'||b.display_value\n" +
                " else a.dev_name end \n" +
                " from (\n" +
                "   select  dev.id,dev2.rely_id,dev2.rely_name,dev2.rely_type,dev.direct_id,dev2.dev_name as dev_names, sys.display_value   \n" +
                "    from (select  id,dev_name,pd_id,rely_id,rely_name,rely_type,direct_id from iot_device where  is_valid='1' and out_iot_fac='2'   and connect_mode='3'  ) as dev\n" +
                " left join    iot_product as pro on pro.id=dev.pd_id and  pro.is_valid=1 and pro.out_iot_fac='2'\n" +
                "left join sys_menu_info as sys on pro.pd_mode=sys.actual_value  and  sys.menu_name='IOT产品型号'  and sys.actual_value!=0\n" +
                "left join (select  id,dev_name,pd_id,rely_id,rely_name,rely_type,direct_id from iot_device where  is_valid='1' and out_iot_fac='2'   and connect_mode='1'  ) as dev2\n" +
                "on dev2.id=dev.direct_id \n" +
                ")as b\n" +
                "  where  a.is_valid='1' and a.out_iot_fac='2'   and a.connect_mode='3'  and a.id=b.id\n" +
                " and  a.pd_id in (" + reload_pd_id + ")";
        log.info("每天三点定时刷新iotdevice的sql" + sysSql);
        boolean b = commonInterface.dbAccess_update(sysSql);
        log.info("每天三点定时刷新iotdevice结果" + b);

        String sqlResult = "update iot_device set rely_id='',rely_name='',rely_type=''  where  id in (SELECT id from iot_device where out_iot_fac='2' and is_valid='1' and pd_id='4216776626102337522' and direct_id in\n" +
                "(SELECT direct_id from iot_device where out_iot_fac='2' and is_valid='1' and pd_id='4216776626119114757') )";
        boolean b2 = commonInterface.dbAccess_update(sqlResult);
        log.info("将“湖南全省_智芯交采终端_1”的关联去除" + b2);
        return "ok";
    }

    //这部分代码暂时不用
    @RequestMapping("/taskUpdateFy")
    public String taskUpdateFy() {
        //2、调用吕飞的接口
        String sql = "select out_dev_id,dev_name,pj_id from iot_device where is_valid='1' and connect_mode='1' and out_iot_fac='2' ";
        log.info("查询的sql为——————" + sql);
        List<Object[]> outList = commonInterface.selectListBySql(sql);
        log.info("outList大小为——————" + outList.size());
        try {
            int i = 0;
            for (Object[] ob : outList) {
                if (ob.length > 0 && StringUtils.isNotBlank(ob[2].toString())) {

                    String deviceName = URLEncoder.encode(ob[1].toString(), "UTF-8");
                    i++;
                    String addr = "http://25.212.172.39:23503/v2/iot/editDevMsg?deviceId=" + ob[0] + "&pjId=" + ob[2] + "&deviceName=" + deviceName;

                    log.info("开始便利——————" + addr);
                    Map<String, String> headers = new HashMap<String, String>();
                    String ss = HttpUtil.httpGet(addr, headers);
                    log.info("调用吕飞接口结果" + ss);
                }
            }
            log.info("调用接口次数" + i);
        } catch (Exception e) {
            log.info("报错了" + e.getMessage());
            e.printStackTrace();
        }

        return "ok";
    }


}

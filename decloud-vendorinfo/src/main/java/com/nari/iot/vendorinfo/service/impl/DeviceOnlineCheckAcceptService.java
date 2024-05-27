package com.nari.iot.vendorinfo.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.common.HttpUtil;
import com.nari.iot.vendorinfo.service.IDeviceOnlineCheckAcceptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service(value = "DeviceOnlineCheckAcceptService")
@Slf4j
public class DeviceOnlineCheckAcceptService  implements IDeviceOnlineCheckAcceptService {
    @Autowired
    CommonInterface commonInterface;
    public static String IPREPORT = "25.212.172.50:9099";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 1 设备在线校验
     *
     * @param request
     * @return
     */
    @Override
    public Map<String, Object> deviceOnlineCheck(HttpServletRequest request) {
        String devLabel=request.getParameter("devLabel");
        String deviceId=request.getParameter("deviceId");
        String now=sdf.format(new Date());
        String id="";
        boolean isHave =false;
        String is_pass="1";
        String is_online="1";
        String sql1="select id,dev_label from D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO where dev_label='"+devLabel+"'";
        List<Object[]> devList1 = commonInterface.selectListBySql(sql1);
        if (devList1.size()>0){
            isHave =true;
        }

        List<Object[]> devList2=null;
        if (!isHave){
            String sql2="select id,dev_label,out_dev_id,dev_name,connect_mode from d5000.iot_device where dev_label='"+devLabel+"' and is_valid =1";
            devList2 = commonInterface.selectListBySql(sql2);
        }
        if (isHave){
            id=devList1.get(0)[0].toString();
        } else {
            id=devList2.get(0)[0].toString();
        }
        String sql3="with iotdevice as(\n" +
                "select * from d5000.iot_device start with dev_label='"+devLabel+"' connect by prior id=direct_id\n" +
                ")\n" +
                "select n1.id,n1.dev_label,n1.out_dev_id,n1.dev_name,n1.connect_mode,n1.pd_id,n2.pd_name,n2.device_mode_name,n2.pd_mode \n" +
                "from iotdevice n1\n" +
                "left join d5000.iot_product n2 on n1.pd_id=n2.id\n" +
                "where n2.pd_mode in (0,2,9,10,11,12,13)" +
                " and n1.is_valid =1";
        List<Object[]> devList3 = commonInterface.selectListBySql(sql3);
        List<Map<Integer, Object>> paramList = new ArrayList<Map<Integer, Object>>();
        log.info("-------------设备在线验收开始");
        List<Map<String, Object>> value= new ArrayList<Map<String, Object>>();
        //用devlabel，获取对应哪些可以不涉及
        Map bsj = getBsj(devLabel);
        for (Object[] objs:devList3) {
            //根据objs[8] 设备型号来找哪些可以不用

            Map<String, Object> map = new HashMap<String, Object>();
            Map<Integer, Object> param = new HashMap<Integer, Object>();
            String is_online2="";
            String is_pass2="";
            String devLabel2=objs[1].toString();
            String params="{" +
                    " \"devLabel\":\""+devLabel2+"\""+
                    "}";
            String result = "";
            try {
                result = HttpUtil.httpPost("http://"+IPREPORT+"/zdts/IotController/IsOnline",params );
                System.out.println(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Object> resultMap = JSONObject.parseObject(result);
            Object isOnline = resultMap.get("isOnline");
            //去找对应的产品,如果存在直接 不涉及
            if(bsj.get(objs[8].toString())!=null&&bsj.get(objs[8].toString()).equals("1")){
                is_online2="1";
                is_pass2="2";
            }else {
                if (isOnline==null||isOnline.toString().equals("2")){//数据库定义2为离线
                    is_online2="0";
                    is_pass2="0";
                    is_pass="0";
                    //融合终端判断
                    if (objs[4].toString().equals("1")){
                        is_online="0";
                    }

                } else {
                    is_online2="1";
                    is_pass2="1";
                }
            }

            param.put(1,objs[0]);
            param.put(2,id);
            param.put(3,objs[1]);
            param.put(4,objs[2]);
            param.put(5,objs[3]);
            param.put(6,objs[4]);
            param.put(7,objs[5]);
            param.put(8,objs[6]);
            param.put(9,objs[7]);
            param.put(10,is_online2);
            param.put(11,is_pass2);
            param.put(12,now);
            paramList.add(param);

            map.put("dev_type",objs[4]);
            map.put("dev_label",objs[1]);
            map.put("dev_out_id",objs[2]);
            map.put("pd_name",objs[6]);
            map.put("device_mode_name",objs[7]);
            map.put("data_result",is_online2);
            map.put("is_pass",is_pass2);
            value.add(map);
        }
        //先删除已存在的
        String delSql="delete from D5000.DMS_ONLINE_CHECK_RESULT_ACCEPT_DETAIL where DIRECT_ID='"+id+"'";
        commonInterface.dbAccess_delete(delSql);
        String insertSql="insert into D5000.DMS_ONLINE_CHECK_RESULT_ACCEPT_DETAIL(ID,DIRECT_ID,DEV_LABEL,OUT_DEV_ID,DEV_NAME,DEV_TYPE,PD_ID,PD_NAME,DEVICE_MODE_NAME,DATA_RESULT,IS_PASS,DATA_TIME)" +
                " values (?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            long i = commonInterface.dbAccess_batchUpdate(insertSql, paramList);
            System.out.println("向D5000.DMS_ONLINE_CHECK_RESULT_ACCEPT_DETAIL表中插入数据结束！" + new Date());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("向D5000.DMS_ONLINE_CHECK_RESULT_ACCEPT_DETAIL表中插入数据失败！"+e);
            return CommonUtil.returnMap2(false,"0",null);
        }

        //二次验证是否存在
        String sql11="select id,dev_label from D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO where dev_label='"+devLabel+"'";
        List<Object[]> devList11 = commonInterface.selectListBySql(sql11);
        if (devList11.size()>0){
            isHave =true;
        }
        if (devList3.size()<=0){
            is_pass="2";
        }
        if (!isHave){
            String ID=devList2.get(0)[0].toString();
            String DEV_LABEL=devList2.get(0)[1].toString();
            String OUT_DEV_ID=devList2.get(0)[2].toString();
            String DEV_NAME=devList2.get(0)[3]==null?"":devList2.get(0)[3].toString();
            String DEV_TYPE=devList2.get(0)[4]==null?"":devList2.get(0)[4].toString();

            String insSql="insert into D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO (ID,DEV_LABEL,OUT_DEV_ID,DEV_NAME,DEV_TYPE,IS_ONLINE,IS_ONLINE_RESULT,IS_PASS,DATA_TIME)" +
                    " values ('"+ID+"','"+DEV_LABEL+"','"+OUT_DEV_ID+"','"+DEV_NAME+"','"+DEV_TYPE+"','"+is_online+"','"+is_pass+"','0','"+now+"')";
            try {
                commonInterface.dbAccess_insert(insSql);
            } catch (Exception e){
                isHave =true;
            }
            //更新iot_device
            String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='0',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
            commonInterface.dbAccess_insert(insql2);

        }
        if (isHave){
            if (is_pass.equals("0")){
                String updSql="update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                        " set IS_ONLINE='"+is_online+"',IS_ONLINE_RESULT='"+is_pass+"',IS_PASS='"+is_pass+"',DATA_TIME='"+now+"'" +
                        " where dev_label='"+devLabel+"'";
                commonInterface.dbAccess_update(updSql);
                //更新dms_iot_device_resource_info
                String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='"+is_pass+"',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
                commonInterface.dbAccess_insert(insql2);
            } else {
                /*String jySql=" select DEV_LABEL,REPORT_MSG_RESULT||PARAM_SET_RESULT||DATA_MEASURE_RESULT||DEV_COUNT_RESULT||REMOTE_CONTROL_RESULT as rest" +
                        " from  D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                        " where dev_label='"+devLabel+"'";*/
                //剔除遥控验收结果
                //  String jySql=" select DEV_LABEL,REPORT_MSG_RESULT||PARAM_SET_RESULT||DATA_MEASURE_RESULT||DEV_COUNT_RESULT as rest" +
                String jySql=" select DEV_LABEL,REPORT_MSG_RESULT||1||DATA_MEASURE_RESULT||DEV_COUNT_RESULT as rest" +
                        " from  D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                        " where dev_label='"+devLabel+"'";
                List<Object[]> jyList = commonInterface.selectListBySql(jySql);
                /*if (jyList.get(0)[1]!=null&&!jyList.get(0)[1].toString().contains("0")&&jyList.get(0)[1].toString().length()==5){*/
                if (jyList.get(0)[1]!=null&&!jyList.get(0)[1].toString().contains("0")&&jyList.get(0)[1].toString().length()==4){
                    String updSql="update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                            " set IS_ONLINE='"+is_online+"',IS_ONLINE_RESULT='"+is_pass+"',IS_PASS='1',DATA_TIME='"+now+"'" +
                            " where dev_label='"+devLabel+"'";
                    commonInterface.dbAccess_update(updSql);
                    //更新dms_iot_device_resource_info TODO  是不是需要增加字段来标识终端验收的时间
                    String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='1',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
                    commonInterface.dbAccess_insert(insql2);
                } else {
                    String updSql="update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                            " set IS_ONLINE='"+is_online+"',IS_ONLINE_RESULT='"+is_pass+"',IS_PASS='0',DATA_TIME='"+now+"'" +
                            " where dev_label='"+devLabel+"'";
                    commonInterface.dbAccess_update(updSql);
                    //更新dms_iot_device_resource_info
                    String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='0',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
                    commonInterface.dbAccess_insert(insql2);
                }
            }
        }
        log.info("-------------设备在线验收结束");
        return CommonUtil.returnMap2(true,is_pass,value);
    }
    public Map  getBsj(String devLabel){

        String params="{" +
                " \"devLabel\":\""+devLabel+"\""+
                "}";
        String result = "";
        try {
            result = HttpUtil.httpPost("http://"+IPREPORT+"/zdts/IotController/IndirectSum",params );
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> resultMap = JSONObject.parseObject(result);
        //上报告
        Map<String, Object> tbMap = (Map<String, Object>)resultMap.get("fieldSum");
        //实际数量
        Map<String, Object> sjMap = (Map<String, Object>)resultMap.get("indirectSum");
        //CAP 无功补偿app电容器
        Object nonreactivenumTb = tbMap.get("nonreactivenum");
        Object nonreactivenumSj = sjMap.get("nonreactivenum");

        //ADJ 无功补偿app分接头
        Object voltagetapnumTb = tbMap.get("voltagetapnum");
        Object voltagetapnumSj = sjMap.get("voltagetapnum");

        // OilTmpSensor  油温测量app
        Object oilsensornumTb = tbMap.get("oilsensornum");
        Object oilsensornumSj = sjMap.get("oilsensornum");

        // RCD 低压开关
        Object lowbranchnumTb = tbMap.get("lowbranchnum");
        Object lowbranchnumSj = sjMap.get("lowbranchnum");
        // 温湿度传感器
        Object wsdTb = tbMap.get("humitureNum")==null?0:tbMap.get("humitureNum");
        Object wsdSj = sjMap.get("humitureNum")==null?0:sjMap.get("humitureNum");
        HashMap map=new HashMap();
        if(!nonreactivenumSj.equals("0")&&nonreactivenumTb.equals("0")){
            //CAP 10
            map.put("10","1");
        }
        if(!voltagetapnumSj.equals("0")&&voltagetapnumTb.equals("0")){
            //ADJ 9
            map.put("9","1");
        }
        if(!oilsensornumSj.equals("0")&&oilsensornumTb.equals("0")){
            //OilTmpSensor 11
            map.put("11","1");
        }
        if(!lowbranchnumSj.equals("0")&&lowbranchnumTb.equals("0")){
            //RCD 2
            map.put("2","1");
        }
        if(!wsdSj.equals("0")&&wsdTb.equals("0")){
            //TmpHum  13
            map.put("13","1");
        }
        return map;
    }


}

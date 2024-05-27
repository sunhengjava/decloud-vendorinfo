package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.common.HttpUtil;
import com.nari.iot.vendorinfo.service.IDeviceCountCheckAcceptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service(value = "DeviceCountCheckAcceptService")
@Slf4j
public class DeviceCountCheckAcceptService implements IDeviceCountCheckAcceptService {
    @Autowired
    CommonInterface commonInterface;
    public static String IPREPORT = "25.212.172.50:9099";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 5 子设备数量校验
     *
     * @param request
     * @return
     */
    @Override
    public Map<String, Object> deviceCountCheck(HttpServletRequest request) {
        String devLabel=request.getParameter("devLabel");
        String deviceId=request.getParameter("deviceId");
        String now=sdf.format(new Date());
        String id="";
        boolean isHave =false;
        String is_pass="1";
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
        String sql3="select n1.id,n1.dev_label,n1.out_dev_id,n1.dev_name,n1.connect_mode,n1.pd_id,n2.pd_name,n2.device_mode_name,n1.is_online\n" +
                "from d5000.iot_device n1\n" +
                "left join d5000.iot_product n2 on n1.pd_id=n2.id\n" +
                "where n1.dev_label='"+devLabel+"'" +
                " and n1.is_valid =1";
        List<Object[]> devList3 = commonInterface.selectListBySql(sql3);
        List<Map<Integer, Object>> paramList = new ArrayList<Map<Integer, Object>>();
        log.info("-------------设备数量验收开始");
        List<Map<String, Object>> value= new ArrayList<Map<String, Object>>();
        for (Object[] objs:devList3) {
            Map<String, Object> map = new HashMap<String, Object>();
            Map<Integer, Object> param = new HashMap<Integer, Object>();
            //子设备项这一条数据是否通过
            String is_pass2="";
            String devLabel2=objs[1].toString();
            String params="{" +
                    " \"devLabel\":\""+devLabel2+"\""+
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
            sjMap.remove("voltagetapnum");
            sjMap.remove("oilsensornum");
            tbMap.remove("voltagetapnum");
            tbMap.remove("oilsensornum");
            Object nonreactivenumTb = tbMap.get("nonreactivenum");

            Object nonreactivenumSj = sjMap.get("nonreactivenum");
       /*     Object voltagetapnumTb = tbMap.get("voltagetapnum");
            Object voltagetapnumSj = sjMap.get("voltagetapnum");*/
            Object oilsensornumTb = tbMap.get("oilsensornum")!=null? tbMap.get("oilsensornum"):"";
            Object oilsensornumSj = sjMap.get("oilsensornum")!=null? sjMap.get("oilsensornum"):"";
            Object lowbranchnumTb = tbMap.get("lowbranchnum");
            Object lowbranchnumSj = sjMap.get("lowbranchnum");
            /*温湿度传感器*/
            Object wsdTb = tbMap.get("humitureNum")==null?0:tbMap.get("humitureNum");
            Object wsdSj = sjMap.get("humitureNum")==null?0:sjMap.get("humitureNum");
            boolean equals1 = nonreactivenumTb.equals(nonreactivenumSj);
            if(nonreactivenumTb.equals("0")){
                equals1=true;
            }
           /* boolean equals2 = voltagetapnumTb.equals(voltagetapnumSj);
            if(voltagetapnumTb.equals("0")){
                equals2=true;
            }*/
            boolean equals3 = oilsensornumTb.equals(oilsensornumSj);
            if(oilsensornumTb.equals("0")){
                equals3=true;
            }
            boolean equals4 = lowbranchnumTb.equals(lowbranchnumSj);
            if(lowbranchnumTb.equals("0")){
                equals4=true;
            }
            boolean equals5 = wsdSj.equals(wsdTb);
            if(wsdTb.equals("0")){
                equals5=true;
            }




            //equals2
            if (equals1&&equals4&&equals5&&equals3) {
                is_pass2="1";
            } else {
                is_pass2="0";
                is_pass="0";
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
            param.put(10,JSONObject.toJSONString(resultMap) );
            param.put(11,is_pass2);
            param.put(12,now);
            paramList.add(param);

            map.put("dev_type",objs[4]);
            map.put("dev_label",objs[1]);
            map.put("dev_out_id",objs[2]);
            map.put("pd_name",objs[6]);
            map.put("device_mode_name",objs[7]);
            map.put("data_result",resultMap);
            map.put("is_pass",is_pass2);
            value.add(map);
        }
        //先删除已存在的
        String delSql="delete from D5000.DMS_DEVCOUNT_CHECK_RESULT_ACCEPT_DETAIL where DIRECT_ID='"+id+"'";
        commonInterface.dbAccess_delete(delSql);
        String insertSql="insert into D5000.DMS_DEVCOUNT_CHECK_RESULT_ACCEPT_DETAIL(ID,DIRECT_ID,DEV_LABEL,OUT_DEV_ID,DEV_NAME,DEV_TYPE,PD_ID,PD_NAME,DEVICE_MODE_NAME,DATA_RESULT,IS_PASS,DATA_TIME)" +
                " values (?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            long i = commonInterface.dbAccess_batchUpdate(insertSql, paramList);
            System.out.println("向D5000.DMS_DEVCOUNT_CHECK_RESULT_ACCEPT_DETAIL表中插入数据结束！" + new Date());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("向D5000.DMS_DEVCOUNT_CHECK_RESULT_ACCEPT_DETAIL表中插入数据失败！"+e);
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

            String insSql="insert into D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO (ID,DEV_LABEL,OUT_DEV_ID,DEV_NAME,DEV_TYPE,DEV_COUNT_RESULT,IS_PASS,DATA_TIME)" +
                    " values ('"+ID+"','"+DEV_LABEL+"','"+OUT_DEV_ID+"','"+DEV_NAME+"','"+DEV_TYPE+"','"+is_pass+"','0','"+now+"')";
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
                        " set DEV_COUNT_RESULT='"+is_pass+"',IS_PASS='"+is_pass+"',DATA_TIME='"+now+"'" +
                        " where dev_label='"+devLabel+"'";
                commonInterface.dbAccess_update(updSql);
                //更新dms_iot_device_resource_info
                String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='"+is_pass+"',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
                commonInterface.dbAccess_insert(insql2);
            } else {
                /*String jySql=" select DEV_LABEL,IS_ONLINE_RESULT||PARAM_SET_RESULT||DATA_MEASURE_RESULT||REPORT_MSG_RESULT||REMOTE_CONTROL_RESULT as rest" +
                        " from  D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                        " where dev_label='"+devLabel+"'";*/
                //剔除遥控验收结果
                // String jySql=" select DEV_LABEL,IS_ONLINE_RESULT||PARAM_SET_RESULT||DATA_MEASURE_RESULT||REPORT_MSG_RESULT as rest" +
                String jySql=" select DEV_LABEL,IS_ONLINE_RESULT||1||DATA_MEASURE_RESULT||REPORT_MSG_RESULT as rest" +
                        " from  D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                        " where dev_label='"+devLabel+"'";
                List<Object[]> jyList = commonInterface.selectListBySql(jySql);
                /*if (jyList.get(0)[1]!=null&&!jyList.get(0)[1].toString().contains("0")&&jyList.get(0)[1].toString().length()==5){*/
                if (jyList.get(0)[1]!=null&&!jyList.get(0)[1].toString().contains("0")&&jyList.get(0)[1].toString().length()==4){
                    String updSql="update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                            " set DEV_COUNT_RESULT='"+is_pass+"',IS_PASS='1',DATA_TIME='"+now+"'" +
                            " where dev_label='"+devLabel+"'";
                    commonInterface.dbAccess_update(updSql);
                    //更新dms_iot_device_resource_info
                    String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='1',IS_A_CHECK_TIME='"+now+"'  where term_esn='"+devLabel+"'";
                    commonInterface.dbAccess_insert(insql2);
                } else {
                    String updSql="update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                            " set DEV_COUNT_RESULT='"+is_pass+"',IS_PASS='0',DATA_TIME='"+now+"'" +
                            " where dev_label='"+devLabel+"'";
                    commonInterface.dbAccess_update(updSql);
                    //更新dms_iot_device_resource_info
                    String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='0',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
                    commonInterface.dbAccess_insert(insql2);
                }
            }
        }
        log.info("-------------设备数量验收结束");
        return CommonUtil.returnMap2(true,is_pass,value);
    }
}

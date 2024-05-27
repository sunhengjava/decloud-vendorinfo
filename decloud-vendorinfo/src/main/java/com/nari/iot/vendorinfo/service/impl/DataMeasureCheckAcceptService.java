package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.service.IDataMeasureCheckAcceptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service(value = "DataMeasureCheckAcceptService")
public class DataMeasureCheckAcceptService implements IDataMeasureCheckAcceptService {
    @Autowired
    CommonInterface commonInterface;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private  DeviceOnlineCheckAcceptService deviceOnlineCheckAcceptService;
    /**
     * 4 数据召测校验 世康的
     *
     * @param request
     * @return
     */
    @Override
    public Map<String, Object> dataMeasureCheck(HttpServletRequest request) {
        String devLabel = request.getParameter("devLabel");
        String deviceId = request.getParameter("deviceId");
        String now = sdf.format(new Date());
        String id = "";
        boolean isHave = false;
        String is_pass = "1";  //
        String sql1 = "select id,dev_label from D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO where dev_label='" + devLabel + "'";
        List<Object[]> devList1 = commonInterface.selectListBySql(sql1);
        if (devList1.size() > 0) {
            isHave = true;
        }

        List<Object[]> devList2 = null;
        String connect_mode = "1";
        if (!isHave) {
            String sql2 = "select id,dev_label,out_dev_id,dev_name,connect_mode,direct_id from d5000.iot_device where dev_label='" + devLabel + "' and is_valid =1";
            devList2 = commonInterface.selectListBySql(sql2);
        }
        if (isHave) {
            id = devList1.get(0)[0].toString();
        } else {
            if (devList2.size() > 0 && devList2.get(0)[4].toString().equals("3")) {
                id = devList2.get(0)[5].toString();
                isHave = true;
                connect_mode = "3";
            } else {
                id = devList2.get(0)[0].toString();
            }
        }
        String sql3 = "with iotdevice as(\n" +
                "select * from d5000.iot_device start with dev_label='" + devLabel + "' connect by prior id=direct_id \n" +
                ")\n" +
                "select n1.id,n1.dev_label,n1.out_dev_id,n1.dev_name,n1.connect_mode,n1.pd_id,n2.pd_name,n2.device_mode_name,n2.pd_mode,n1.dms_region_id \n" +
                "from iotdevice n1\n" +
                "left join d5000.iot_product n2 on n1.pd_id=n2.id\n" +
                "where n2.pd_mode in (2,9,10,11,12,13)" +
                " and n1.is_valid =1";
        List<Object[]> devList3 = commonInterface.selectListBySql(sql3);
        List<Map<Integer, Object>> paramList = new ArrayList<Map<Integer, Object>>();
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();

        log.info("-------------数据召测验收开始"+sql3);
        StringBuffer ispassStr = new StringBuffer();
        CountDownLatch latch = new CountDownLatch(devList3.size());

        ExecutorService es = Executors.newFixedThreadPool(5);
        if (devList3.size() > 0) {
            es = Executors.newFixedThreadPool(devList3.size());
        }
        // 用devlabel，获取对应哪些可以不涉及
        Map bsj = deviceOnlineCheckAcceptService.getBsj(devLabel);
        for (Object[] objs : devList3) {

            String direct_id = id;
            es.submit(new Runnable() {

                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + "---->");

                    Map<String, Object> map = new HashMap<String, Object>();
                    Map<Integer, Object> param = new HashMap<Integer, Object>();
                    String is_pass2 = "";
                    String devLabel2 = objs[1].toString();
                    Map<String, Object> valueMsg = new HashMap<>();
                    log.info("输出对应的dms_region_id1："+objs[9].toString() +"---out_dev_id2--"+objs[2].toString()+"-pd_mode--"+objs[8].toString());
                    Map<String, Object> resultMapYC = CheckUtillService.dataYCCheck(objs[9].toString(), objs[2].toString(), objs[8].toString());
                    Object statusYC = resultMapYC.get("is_pass");
                    Object valueMsgYC = resultMapYC.get("values");
                    log.info(objs[7]+"对应的遥测为"+ JSONObject.toJSONString(resultMapYC));


                    Object statusYX = "0";
                    Object valueMsgYX = "无遥信";
                    if (objs[8].toString().equals("13")) {
                        statusYX="1";
                        if (valueMsgYC != null || valueMsgYX != null) {
                            valueMsg.put("yc", valueMsgYC);
                            valueMsg.put("yx", valueMsgYX);
                        }
                    } else {
                        Map<String, Object> resultMapYX = CheckUtillService.dataYXCheck(objs[9].toString(), objs[2].toString(), objs[8].toString());
                        statusYX = resultMapYX.get("is_pass");
                        valueMsgYX = resultMapYX.get("values");
                        if (valueMsgYC != null || valueMsgYX != null) {
                            valueMsg.put("yc", valueMsgYC);
                            valueMsg.put("yx", valueMsgYX);
                        }
                        log.info(objs[7]+"对应的遥信为"+JSONObject.toJSONString(resultMapYX));
                    }

                    //剔除上报为0的
                    if(bsj.get(objs[8].toString())!=null&&bsj.get(objs[8].toString()).equals("1")){
                        is_pass2 = "2";
                        ispassStr.append("1");
                    }else{
                        if (statusYC.toString().equals("0") && statusYX.toString().equals("0")) {
                            is_pass2 = "0";
                            ispassStr.append("0");
//                        is_pass="0";
                        } else {
                            is_pass2 = "1";
                            ispassStr.append("1");
                        }

                        //判断是不是油温传感器特殊类型
                        if (objs[8].toString().equals("13")) {
                            if(valueMsgYC==null){
                                is_pass2 = "0";
                                ispassStr.append("0");
                            }
                        }
                    }

                    param.put(1, objs[0]);
                    param.put(2, direct_id);
                    param.put(3, objs[1]);
                    param.put(4, objs[2]);
                    param.put(5, objs[3]);
                    param.put(6, objs[4]);
                    param.put(7, objs[5]);
                    param.put(8, objs[6]);
                    param.put(9, objs[7]);
                    param.put(10, valueMsg.isEmpty() ? null : JSONObject.toJSONString(valueMsg));
                    param.put(11, is_pass2);
                    param.put(12, now);
                    paramList.add(param);

                    map.put("dev_type", objs[4]);
                    map.put("dev_label", objs[1]);
                    map.put("dev_out_id", objs[2]);
                    map.put("pd_name", objs[6]);
                    map.put("device_mode_name", objs[7]);
                    map.put("data_result", valueMsg);
                    map.put("is_pass", is_pass2);
                    value.add(map);

                    latch.countDown();
                }
            });


        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ispassStr.toString().contains("0")) {
            is_pass = "0";
        }

        //先删除已存在的
        String delSql = "delete from D5000.DMS_MEASURE_CHECK_RESULT_ACCEPT_DETAIL where DIRECT_ID='" + id + "'";
        if (connect_mode.equals("3")) {
            delSql = "delete from D5000.DMS_MEASURE_CHECK_RESULT_ACCEPT_DETAIL where ID='" + devList2.get(0)[0].toString() + "'";
        }
        commonInterface.dbAccess_delete(delSql);
        String insertSql = "insert into D5000.DMS_MEASURE_CHECK_RESULT_ACCEPT_DETAIL (ID,DIRECT_ID,DEV_LABEL,OUT_DEV_ID,DEV_NAME,DEV_TYPE,PD_ID,PD_NAME,DEVICE_MODE_NAME,DATA_RESULT,IS_PASS,DATA_TIME)" +
                " values (?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            long i = commonInterface.dbAccess_batchUpdate(insertSql, paramList);
            System.out.println("向D5000.DMS_MEASURE_CHECK_RESULT_ACCEPT_DETAIL表中插入数据结束！" + new Date());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("向D5000.DMS_MEASURE_CHECK_RESULT_ACCEPT_DETAIL表中插入数据失败！" + e);
            return CommonUtil.returnMap2(false, "0", null);
        }

        //二次验证是否存在
        String sql11 = "select id,dev_label from D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO where dev_label='" + devLabel + "'";
        List<Object[]> devList11 = commonInterface.selectListBySql(sql11);
        if (devList11.size() > 0 || connect_mode.equals("3")) {
            isHave = true;
        }
        if (devList3.size() <= 0 && !connect_mode.equals("3")) {
            is_pass = "2";
        } else if (connect_mode.equals("3")) {
            String ispassSql = "select id,is_pass from D5000.DMS_MEASURE_CHECK_RESULT_ACCEPT_DETAIL  where direct_id='" + id + "'";
            List<Object[]> ispassList = commonInterface.selectListBySql(ispassSql);
            for (Object[] objs : ispassList) {
                if (objs[1].toString().equals("0")) {
                    is_pass = "0";
                }
            }

            String sql4 = "select id,dev_label,out_dev_id,dev_name,connect_mode,direct_id from d5000.iot_device where  connect_mode=1\n" +
                    "start with dev_label='" + devLabel + "' connect by prior direct_id=id\n" +
                    " and is_valid =1 ";
            List<Object[]> devList4 = commonInterface.selectListBySql(sql4);
            devLabel = devList4.get(0)[1].toString();
        }

        if (!isHave) {
            String ID = devList2.get(0)[0].toString();
            String DEV_LABEL = devList2.get(0)[1].toString();
            String OUT_DEV_ID = devList2.get(0)[2].toString();
            String DEV_NAME = devList2.get(0)[3] == null ? "" : devList2.get(0)[3].toString();
            String DEV_TYPE = devList2.get(0)[4] == null ? "" : devList2.get(0)[4].toString();

            String insSql = "insert into D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO (ID,DEV_LABEL,OUT_DEV_ID,DEV_NAME,DEV_TYPE,DATA_MEASURE_RESULT,IS_PASS,DATA_TIME)" +
                    " values ('" + ID + "','" + DEV_LABEL + "','" + OUT_DEV_ID + "','" + DEV_NAME + "','" + DEV_TYPE + "','" + is_pass + "','0','" + now + "')";
            try {
                commonInterface.dbAccess_insert(insSql);
            } catch (Exception e) {
                isHave = true;
            }
            //更新iot_device
            String insql2 = "update d5000.dms_iot_device_resource_info set IS_A_CHECK='0',IS_A_CHECK_TIME='" + now + "'  where term_esn='" + devLabel + "'";
            commonInterface.dbAccess_insert(insql2);

        }
        if (isHave) {
            if (is_pass.equals("0")) {
                String updSql = "update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                        " set DATA_MEASURE_RESULT='" + is_pass + "',IS_PASS='" + is_pass + "',DATA_TIME='" + now + "'" +
                        " where dev_label='" + devLabel + "'";
                commonInterface.dbAccess_update(updSql);
                //更新dms_iot_device_resource_info
                String insql2 = "update d5000.dms_iot_device_resource_info set IS_A_CHECK='" + is_pass + "',IS_A_CHECK_TIME='" + now + "'  where term_esn='" + devLabel + "'";
                commonInterface.dbAccess_insert(insql2);
            } else {
                /*String jySql=" select DEV_LABEL,IS_ONLINE_RESULT||REPORT_MSG_RESULT||PARAM_SET_RESULT||DEV_COUNT_RESULT||REMOTE_CONTROL_RESULT as rest" +
                        " from  D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                        " where dev_label='"+devLabel+"'";*/
                //剔除遥控验收结果
                //      String jySql = " select DEV_LABEL,IS_ONLINE_RESULT||REPORT_MSG_RESULT||PARAM_SET_RESULT||DEV_COUNT_RESULT as rest" +
                String jySql = " select DEV_LABEL,IS_ONLINE_RESULT||REPORT_MSG_RESULT||1||DEV_COUNT_RESULT as rest" +
                        " from  D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                        " where dev_label='" + devLabel + "'";
                List<Object[]> jyList = commonInterface.selectListBySql(jySql);
                /*if (jyList.get(0)[1]!=null&&!jyList.get(0)[1].toString().contains("0")&&jyList.get(0)[1].toString().length()==5){*/
                if (jyList.get(0)[1] != null && !jyList.get(0)[1].toString().contains("0") && jyList.get(0)[1].toString().length() == 4) {
                    String updSql = "update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                            " set DATA_MEASURE_RESULT='" + is_pass + "',IS_PASS='1',DATA_TIME='" + now + "'" +
                            " where dev_label='" + devLabel + "'";
                    commonInterface.dbAccess_update(updSql);
                    //更新dms_iot_device_resource_info
                    String insql2 = "update d5000.dms_iot_device_resource_info set IS_A_CHECK='1',IS_A_CHECK_TIME='" + now + "'  where term_esn='" + devLabel + "'";
                    commonInterface.dbAccess_insert(insql2);
                } else {
                    String updSql = "update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                            " set DATA_MEASURE_RESULT='" + is_pass + "',IS_PASS='0',DATA_TIME='" + now + "'" +
                            " where dev_label='" + devLabel + "'";
                    commonInterface.dbAccess_update(updSql);
                    //更新dms_iot_device_resource_info
                    String insql2 = "update d5000.dms_iot_device_resource_info set IS_A_CHECK='0',IS_A_CHECK_TIME='" + now + "'  where term_esn='" + devLabel + "'";
                    commonInterface.dbAccess_insert(insql2);
                }
            }
        }
        log.info("-------------数据召测验收结束");
        return CommonUtil.returnMap2(true, is_pass, value);
    }
}

package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.common.HttpUtil;
import com.nari.iot.vendorinfo.service.IReportMsgCheckAcceptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service(value = "ReportMsgCheckAcceptService")
@Slf4j
public class ReportMsgCheckAcceptService implements IReportMsgCheckAcceptService {
    @Autowired
    CommonInterface commonInterface;
    public static String IPREPORT = "25.212.172.50:9099";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private  DeviceOnlineCheckAcceptService deviceOnlineCheckAcceptService;
    /**
     * 2 报文上送校验
     *
     * @param request
     * @return
     */
    @Override
    public Map<String, Object> reportMsgCheck(HttpServletRequest request) {
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
        String sql3="with iotdevice as(\n" +
                "select * from d5000.iot_device start with dev_label='"+devLabel+"' connect by prior id=direct_id \n" +
                ")\n" +
                "select n1.id,n1.dev_label,n1.out_dev_id,n1.dev_name,n1.connect_mode,n1.pd_id,n2.pd_name,n2.device_mode_name,n2.pd_mode \n" +
                "from iotdevice n1\n" +
                "left join d5000.iot_product n2 on n1.pd_id=n2.id\n" +
                "where n2.pd_mode in (2,9,10,11,12,13)" +
                " and n1.is_valid =1";
        List<Object[]> devList3 = commonInterface.selectListBySql(sql3);
        List<Map<Integer, Object>> paramList = new ArrayList<Map<Integer, Object>>();
        log.info("-------------报文上送验收开始");
        List<Map<String, Object>> value= new ArrayList<Map<String, Object>>();
        Map bsj = deviceOnlineCheckAcceptService.getBsj(devLabel);
        for (Object[] objs:devList3) {
            Map<String, Object> map = new HashMap<String, Object>();
            Map<Integer, Object> param = new HashMap<Integer, Object>();
            String is_pass2="";
            String devLabel2=objs[1].toString();
            String params="{" +
                    " \"devLabel\":\""+devLabel2+"\""+
                    "}";
            String result = "";
            try {
                result = HttpUtil.httpPost("http://"+IPREPORT+"/zdts/IotController/MessageUpload",params );
                System.out.println(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Object> resultMap = JSONObject.parseObject(result);
            Object status = resultMap.get("status");
            Object valueMsg= resultMap.get("value");
            System.out.println(params+"获取到的value为"+valueMsg);
            System.out.println("输出status的值为："+status);
            if(bsj.get(objs[8].toString())!=null&&bsj.get(objs[8].toString()).equals("1")){
                is_pass2="2";
            }else{

                if (status==null||status.toString().equals("0")||status.toString().equals("500")){
                    is_pass2="0";
                    is_pass="0";

                } else {
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
            param.put(10,valueMsg);
            param.put(11,is_pass2);
            param.put(12,now);
            paramList.add(param);

            map.put("dev_type",objs[4]);
            map.put("dev_label",objs[1]);
            map.put("dev_out_id",objs[2]);
            map.put("pd_name",objs[6]);
            map.put("device_mode_name",objs[7]);
            map.put("data_result",valueMsg);
            map.put("is_pass",is_pass2);
            value.add(map);
        }
        //先删除已存在的
        String delSql="delete from D5000.DMS_REPORT_CHECK_RESULT_ACCEPT_DETAIL where DIRECT_ID='"+id+"'";
        commonInterface.dbAccess_delete(delSql);
        String insertSql="insert into D5000.DMS_REPORT_CHECK_RESULT_ACCEPT_DETAIL (ID,DIRECT_ID,DEV_LABEL,OUT_DEV_ID,DEV_NAME,DEV_TYPE,PD_ID,PD_NAME,DEVICE_MODE_NAME,DATA_RESULT,IS_PASS,DATA_TIME)" +
                " values (?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            long i = commonInterface.dbAccess_batchUpdate(insertSql, paramList);
            System.out.println("向D5000.DMS_REPORT_CHECK_RESULT_ACCEPT_DETAIL表中插入数据结束！" + new Date());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("向D5000.DMS_REPORT_CHECK_RESULT_ACCEPT_DETAIL表中插入数据失败！"+e);
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

            String insSql="insert into D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO (ID,DEV_LABEL,OUT_DEV_ID,DEV_NAME,DEV_TYPE,REPORT_MSG_RESULT,IS_PASS,DATA_TIME)" +
                    " values ('"+ID+"','"+DEV_LABEL+"','"+OUT_DEV_ID+"','"+DEV_NAME+"','"+DEV_TYPE+"','"+is_pass+"','0','"+now+"')";
            try {
                commonInterface.dbAccess_insert(insSql);
            } catch (Exception e){
                isHave =true;
            }
            //更新dms_iot_device_resource_info
            String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='0',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
            commonInterface.dbAccess_insert(insql2);

        }

        if (isHave){
            if (is_pass.equals("0")){
                String updSql="update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                        " set REPORT_MSG_RESULT='"+is_pass+"',IS_PASS='"+is_pass+"',DATA_TIME='"+now+"'" +
                        " where dev_label='"+devLabel+"'";
                commonInterface.dbAccess_update(updSql);
                //更新dms_iot_device_resource_info
                String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='"+is_pass+"',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
                commonInterface.dbAccess_insert(insql2);
            } else {
                /*String jySql=" select DEV_LABEL,IS_ONLINE_RESULT||PARAM_SET_RESULT||DATA_MEASURE_RESULT||DEV_COUNT_RESULT||REMOTE_CONTROL_RESULT as rest" +
                        " from  D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                        " where dev_label='"+devLabel+"'";*/
                //剔除遥控验收结果
                //   String jySql=" select DEV_LABEL,IS_ONLINE_RESULT||PARAM_SET_RESULT||DATA_MEASURE_RESULT||DEV_COUNT_RESULT as rest" +
                String jySql=" select DEV_LABEL,IS_ONLINE_RESULT||1||DATA_MEASURE_RESULT||DEV_COUNT_RESULT as rest" +
                        " from  D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                        " where dev_label='"+devLabel+"'";
                List<Object[]> jyList = commonInterface.selectListBySql(jySql);
                /*if (jyList.get(0)[1]!=null&&!jyList.get(0)[1].toString().contains("0")&&jyList.get(0)[1].toString().length()==5){*/
                if (jyList.get(0)[1]!=null&&!jyList.get(0)[1].toString().contains("0")&&jyList.get(0)[1].toString().length()==4){
                    String updSql="update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                            " set REPORT_MSG_RESULT='"+is_pass+"',IS_PASS='1',DATA_TIME='"+now+"'" +
                            " where dev_label='"+devLabel+"'";
                    commonInterface.dbAccess_update(updSql);
                    //更新dms_iot_device_resource_info
                    String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='1',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
                    commonInterface.dbAccess_insert(insql2);
                } else {
                    String updSql="update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                            " set REPORT_MSG_RESULT='"+is_pass+"',IS_PASS='0',DATA_TIME='"+now+"'" +
                            " where dev_label='"+devLabel+"'";
                    commonInterface.dbAccess_update(updSql);
                    //更新dms_iot_device_resource_info
                    String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='0',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
                    commonInterface.dbAccess_insert(insql2);
                }
            }
        }
        log.info("-------------报文上送验收结束");
        return CommonUtil.returnMap2(true,is_pass,value);
    }
}

package com.nari.iot.vendorinfo.service.impl;

import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.service.ITermCheckAcceptAllService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service(value = "TermCheckAcceptAllService")
public class TermCheckAcceptAllService implements ITermCheckAcceptAllService {
    @Autowired
    CommonInterface commonInterface;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 7 终端总校验（目前已去掉）
     *
     * @param request
     * @return
     */
    @Override
    public Map<String, Object> termAllCheck(HttpServletRequest request) {
        String devLabel=request.getParameter("devLabel");
        String deviceId=request.getParameter("deviceId");
        String is_pass="0";
        String sql="select n1.id,n1.dev_type,n1.dev_label,n1.out_dev_id,n3.pd_name,n3.device_mode_name,n1.is_pass from D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO n1 \n" +
                "inner join d5000.iot_device n2 on n1.id=n2.id\n" +
                "left join d5000.iot_product n3 on n2.pd_id=n3.id\n" +
                "where n1.dev_label='"+devLabel+"'";
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        List<Map<String, Object>> value= new ArrayList<Map<String, Object>>();
        if (devList.size()>0){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("dev_type",devList.get(0)[1]);
            map.put("dev_label",devList.get(0)[2]);
            map.put("dev_out_id",devList.get(0)[3]);
            map.put("pd_name",devList.get(0)[4]);
            map.put("device_mode_name",devList.get(0)[5]);
            map.put("data_result",null);
            is_pass=devList.get(0)[6].toString();
            map.put("is_pass",devList.get(0)[6]);
            value.add(map);
        }
        return CommonUtil.returnMap2(true,is_pass,value);
    }

    /**
     * 终端验收反馈
     *
     * @param request
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> termFeedBack(HttpServletRequest request, Map<String, Object> map) {
        Object devLabel = map.get("devLabel")==null?"":map.get("devLabel");
        Object deviceId = map.get("deviceId")==null?"":map.get("deviceId");
        Object imgUrl = map.get("imgUrl")==null?"":map.get("imgUrl");
        Object remark = map.get("remark")==null?"":map.get("remark");

        String sql="update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO set img_url='"+imgUrl+"',remark='"+remark+"'" +
                " where dev_label='"+devLabel+"'";
        Map<String, Object> result =new HashMap<>();
        result.put("result","1");
        result.put("success",true);
        try {
            commonInterface.dbAccess_update(sql);
        } catch (Exception e){
            e.printStackTrace();
            result.put("result","0");
            result.put("success",false);
        }


        return result;
    }

    /**
     * 终端总校验2
     *
     * @param request
     * @return
     */
    @Override
    public Map<String, Object> termAllCheck2(HttpServletRequest request) {
        String devLabel=request.getParameter("devLabel");
        String deviceId=request.getParameter("deviceId");
        String sqlCount="select n1.id,n1.dev_type,n1.dev_label,n1.out_dev_id,n3.pd_name,n3.device_mode_name,\n" +
                //    "n1.is_online_result,n1.report_msg_result,n1.param_set_result,n1.data_measure_result," +
                "n1.is_online_result,n1.report_msg_result,1,n1.data_measure_result," +
                "n1.dev_count_result,n1.remote_control_result,n1.is_pass\n" +
                " from D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO n1 \n" +
                "inner join d5000.iot_device n2 on n1.id=n2.id\n" +
                "left join d5000.iot_product n3 on n2.pd_id=n3.id\n" +
                "where n1.dev_label='"+devLabel+"';";
        List<Object[]> devList = commonInterface.selectListBySql(sqlCount);
        Map<String, Object> value= new HashMap<>();
        if (devList.size()>0){
            value.put("is_online_result",devList.get(0)[6]);
            value.put("report_msg_result",devList.get(0)[7]);
            value.put("param_set_result",devList.get(0)[8]);
            value.put("data_measure_result",devList.get(0)[9]);
            value.put("dev_count_result",devList.get(0)[10]);
            value.put("remote_control_result",devList.get(0)[11]);
            value.put("is_pass",devList.get(0)[12]);


            List<String> tableNameList = new ArrayList<>();
            tableNameList.add("DMS_ONLINE_CHECK_RESULT_ACCEPT_DETAIL");
            tableNameList.add("DMS_REPORT_CHECK_RESULT_ACCEPT_DETAIL");
            tableNameList.add("DMS_PARAM_CHECK_RESULT_ACCEPT_DETAIL");
            tableNameList.add("DMS_MEASURE_CHECK_RESULT_ACCEPT_DETAIL");
            tableNameList.add("DMS_DEVCOUNT_CHECK_RESULT_ACCEPT_DETAIL");
            tableNameList.add("DMS_CONTROL_CHECK_RESULT_ACCEPT_DETAIL");
            List<String> keyList = new ArrayList<>();
            keyList.add("is_online_result");
            keyList.add("report_msg_result");
            keyList.add("param_set_result");
            keyList.add("data_measure_result");
            keyList.add("dev_count_result");
            keyList.add("remote_control_result");
            Map<String, Object> resultMap= new HashMap<>();
            int k=0;
            for (String tableName:tableNameList) {
                String sql="select dev_type,dev_label,out_dev_id,pd_name,device_mode_name,dbms_lob.substr(data_result,4000),is_pass \n" +
                        "from D5000."+tableName+"\n" +
                        "where direct_id='"+devList.get(0)[0]+"'";
                List<Object[]> detailList = commonInterface.selectListBySql(sql);
                List<Map<String, Object>> value2= new ArrayList<Map<String, Object>>();
                for (Object[] detail:detailList) {
                    Map<String, Object> map= new HashMap<>();
                    map.put("dev_type",detail[0]);
                    map.put("dev_label",detail[1]);
                    map.put("out_dev_id",detail[2]);
                    map.put("pd_name",detail[3]);
                    map.put("device_mode_name",detail[4]);
                    map.put("data_result",detail[5]);
                    map.put("is_pass",detail[6]);
                    /*if(detail[6]!=null&&detail[6].toString().equals("0")){
                        String sql0 = "update d5000.DMS_IOT_CHILDDEVICES set check_err=1 , check_err_time=now() " +
                                "where esn_no='"+devLabel+"' and is_valid=1 and check_err is null";
                        commonInterface.dbAccess_update(sql0);
                    }*/
                    value2.add(map);
                }
                resultMap.put(keyList.get(k++),value2);

            }
            value.put("detail",resultMap);

        }


        return value;
    }
}

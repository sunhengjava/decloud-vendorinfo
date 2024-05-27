package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.service.IRemoteControlCheckAcceptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service(value = "RemoteControlCheckAcceptService")
@Slf4j
public class RemoteControlCheckAcceptService implements IRemoteControlCheckAcceptService {
    @Autowired
    CommonInterface commonInterface;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 6 遥控校验(目前没有)
     *
     * @param request
     * @return
     */
    @Override
    public Map<String, Object> remoteControlCheck(HttpServletRequest request) {

        String devLabel=request.getParameter("devLabel");
        String outDevId=request.getParameter("outDevId");
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
        String connect_mode="1";
        if (!isHave){
            String sql2="select id,dev_label,out_dev_id,dev_name,connect_mode,direct_id from d5000.iot_device where dev_label='"+devLabel+"' and is_valid =1";
            devList2 = commonInterface.selectListBySql(sql2);
        }
        if (isHave){
            id=devList1.get(0)[0].toString();
        } else {
            if (devList2.size()>0 && devList2.get(0)[4].toString().equals("3")){
                id=devList2.get(0)[5].toString();
                isHave=true;
                connect_mode="3";
            }else {
                id=devList2.get(0)[0].toString();
            }
        }
        String sll = " select   ID,DIRECT_ID,DEV_LABEL,OUT_DEV_ID,DEV_NAME,DEV_TYPE,PD_ID,PD_NAME,DEVICE_MODE_NAME,DATA_RESULT,IS_PASS,DATA_TIME from  DMS_CONTROL_CHECK_RESULT_ACCEPT_DETAIL where is_pass=1  and dev_label='"+devLabel+"'";

        Map<String ,Object[]> mm = new HashMap<>();

        List<Object[]> devListg = commonInterface.selectListBySql(sll);
        if(devListg!=null&&devListg.size()>0){
            for(Object[] obj : devListg){
                mm.put(obj[2].toString(),obj);
            }
        }

        String sql3="with iotdevice as(\n" +
                "select * from d5000.iot_device start with dev_label='"+devLabel+"' connect by prior id=direct_id \n" +
                ")\n" +
                "select n1.id,n1.dev_label,n1.out_dev_id,n1.dev_name,n1.connect_mode,n1.pd_id,n2.pd_name,n2.device_mode_name,n2.pd_mode,n1.dms_region_id \n" +
                "from iotdevice n1\n" +
                "left join d5000.iot_product n2 on n1.pd_id=n2.id\n" +
                "where n2.pd_mode in (2) " +
                " and n1.is_valid =1";
        List<Object[]> devList3 = commonInterface.selectListBySql(sql3);
        List<Map<Integer, Object>> paramList = new ArrayList<Map<Integer, Object>>();
        List<Map<String, Object>> value= new ArrayList<Map<String, Object>>();
        log.info("-------------开关遥控验收开始");
        CountDownLatch latch =new CountDownLatch(devList3.size());
        ExecutorService es = Executors.newFixedThreadPool(5);
        if (devList3.size()>0){
            es = Executors.newFixedThreadPool(devList3.size());
        }
        StringBuffer ispassStr = new StringBuffer();


        if (devList3.size()<=0){
            is_pass="2";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("dev_type",null);
            map.put("dev_label",null);
            map.put("dev_out_id",null);
            map.put("pd_name",null);
            map.put("device_mode_name",null);
            map.put("data_result","无开关子设备，无需校验");
            map.put("is_pass","2");
            value.add(map);
        } else {
            for (Object[] objs:devList3) {
                String direct_id=id;

                //如果已经验证通过就不需要取验证
                if (mm.containsKey(objs[2].toString())){

                    ispassStr.append("1");
                    Object[] obj= mm.get(objs[2].toString());
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("dev_type",objs[4]);
                    map.put("dev_label",objs[1]);
                    map.put("dev_out_id",objs[2]);
                    map.put("pd_name",objs[6]);
                    map.put("device_mode_name",objs[7]);
                    map.put("data_result", obj[9]); //从已经过了的取此字段即可
                    map.put("is_pass","1");
                    value.add(map);
                    Map<Integer, Object> param = new HashMap<Integer, Object>();

                    //插入数据库的数据 构建

                    param.put(1,objs[0]);
                    param.put(2,direct_id);
                    param.put(3,objs[1]);
                    param.put(4,objs[2]);
                    param.put(5,objs[3]);
                    param.put(6,objs[4]);
                    param.put(7,objs[5]);
                    param.put(8,objs[6]);
                    param.put(9,objs[7]);
                    param.put(10,obj[9]); //取开始通过的描述
                    param.put(11,"1");
                    param.put(12,obj[11]); //取开始通过的时间
                    paramList.add(param);

                }
                es.submit(new Runnable() {

                    @Override
                    public void run() {
                        System.out.println(Thread.currentThread().getName() + "---->");

                        Map<Integer, Object> param = new HashMap<Integer, Object>();
                        String is_pass2="";
                        String devLabel2=objs[1].toString();
                        Object valueMsg=null;
                        Map<String, Object> resultMapYX = CheckUtillService.dataYXCheck(objs[9].toString(), objs[2].toString(), objs[8].toString());
                        Object statusYX = resultMapYX.get("is_pass");
                        Object valueMsgYX = (Map<String,Object>)resultMapYX.get("values");
                        //验证开关初始位置
                        if (statusYX.toString().equals("0")||((Map) valueMsgYX).get("SwPos")==null){
                            ispassStr.append("0");
//                            is_pass="0";
                            is_pass2="0";
                            valueMsg="初始开关状态召测失败";
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("dev_type",objs[4]);
                            map.put("dev_label",objs[1]);
                            map.put("dev_out_id",objs[2]);
                            map.put("pd_name",objs[6]);
                            map.put("device_mode_name",objs[7]);
                            map.put("data_result",valueMsg);
                            map.put("is_pass","0");
                            value.add(map);
                        } else {

                            if(((Map) valueMsgYX).get("SwPos").equals("1")){

                                //如果是合位  控制跳闸
                                    ispassStr.append("0");
                                    Map<String, Object> resultMap = CheckUtillService.remoteControlCheckFZ(objs[9].toString(), objs[2].toString(), objs[8].toString(),objs[0].toString());
                                    Object valueMsgYX2 = (Map<String,Object>)resultMap.get("values");
                                    Object status = resultMap.get("is_pass");
                                    if (status.toString().equals("0")){
                                    //                               is_pass="0";
                                    is_pass2="0";
                                    valueMsg="第一步：遥控分闸，遥控校验返回失败";
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put("dev_type",objs[4]);
                                    map.put("dev_label",objs[1]);
                                    map.put("dev_out_id",objs[2]);
                                    map.put("pd_name",objs[6]);
                                    map.put("device_mode_name",objs[7]);
                                    map.put("data_result",valueMsg);
                                    map.put("is_pass","0");
                                    value.add(map);
                                } else {
                                        //等待10秒后召测
                                        try{
                                            Thread.currentThread().sleep( 10000);
                                        }catch(Exception ex){
                                            ex.printStackTrace();
                                        }
                                        Map<String, Object> fzresultMap = CheckUtillService.dataYXCheck(objs[9].toString(), objs[2].toString(), objs[8].toString());

                                        Object fzresultMapYX = fzresultMap.get("is_pass");
                                        Object fzresultMapYX3 = (Map<String,Object>)fzresultMap.get("values");//||((Map) valueMsgYX2).get("SwPos").equals("1")
                                        Object fzswPos = ((Map) fzresultMapYX3).get("SwPos");
                                        System.out.println(fzswPos);
                                        if (fzresultMapYX.toString().equals("0")||((Map) fzresultMapYX3).get("SwPos")==null){
                                            ispassStr.append("0");
//                                     is_pass="0";
                                            is_pass2="0";
                                            valueMsg="第一步：遥控分闸位置校验，遥控后开关位置校验失败";
                                            Map<String, Object> map = new HashMap<String, Object>();
                                            map.put("dev_type",objs[4]);
                                            map.put("dev_label",objs[1]);
                                            map.put("dev_out_id",objs[2]);
                                            map.put("pd_name",objs[6]);
                                            map.put("device_mode_name",objs[7]);
                                            map.put("data_result",valueMsg);
                                            map.put("is_pass","0");
                                            value.add(map);
                                        } else if (((Map) fzresultMapYX3).get("SwPos").equals("1")) {
                                            ispassStr.append("0");
                                            //                                   is_pass="0";
                                            is_pass2="0";
                                            valueMsg="第一步：遥控合闸后开关位置仍为合位，校验失败";
                                            Map<String, Object> map = new HashMap<String, Object>();
                                            map.put("dev_type",objs[4]);
                                            map.put("dev_label",objs[1]);
                                            map.put("dev_out_id",objs[2]);
                                            map.put("pd_name",objs[6]);
                                            map.put("device_mode_name",objs[7]);
                                            map.put("data_result",valueMsg);
                                            map.put("is_pass","0");
                                            value.add(map);
                                        } else {
                                            //分闸会直接返回不需要二次召测
//                                    ispassStr.append("1");
//                                    //                                   is_pass="1";
//                                    is_pass2="1";
//                                    valueMsg=valueMsgYX2;
//                                    Map<String, Object> map = new HashMap<String, Object>();
//                                    map.put("dev_type",objs[4]);
//                                    map.put("dev_label",objs[1]);
//                                    map.put("dev_out_id",objs[2]);
//                                    map.put("pd_name",objs[6]);
//                                    map.put("device_mode_name",objs[7]);
//                                    map.put("data_result", valueMsg);
//                                    map.put("is_pass","1");
//                                    value.add(map);
                                            Map<String, Object> resultMap1 = CheckUtillService.remoteControlCheck(objs[9].toString(), objs[2].toString(), objs[8].toString(),objs[0].toString());
//                    Map<String, Object> resultMap=new HashMap<>();
//                    resultMap.put("is_pass","0");
                                            Object status1 = resultMap1.get("is_pass");
                                            if (status1.toString().equals("0")){
                                                ispassStr.append("0");
                                                //                               is_pass="0";
                                                is_pass2="0";
                                                valueMsg="第二步：遥控合闸，遥控校验返回失败";
                                                Map<String, Object> map = new HashMap<String, Object>();
                                                map.put("dev_type",objs[4]);
                                                map.put("dev_label",objs[1]);
                                                map.put("dev_out_id",objs[2]);
                                                map.put("pd_name",objs[6]);
                                                map.put("device_mode_name",objs[7]);
                                                map.put("data_result",valueMsg);
                                                map.put("is_pass","0");
                                                value.add(map);
                                            } else {
                                                //第二次遥信开关位置验证
                                                try{
                                                    Thread.currentThread().sleep( 120000);
                                                }catch(Exception ex){
                                                    ex.printStackTrace();
                                                }
                                                Map<String, Object> resultMapYX2 = CheckUtillService.dataYXCheck(objs[9].toString(), objs[2].toString(), objs[8].toString());
                                                Object statusYX2 = resultMapYX2.get("is_pass");
                                                Object valueMsgYX3 = (Map<String,Object>)resultMapYX2.get("values");//||((Map) valueMsgYX2).get("SwPos").equals("1")
                                                Object swPos = ((Map) valueMsgYX3).get("SwPos");
                                                System.out.println(swPos);
                                                if (statusYX2.toString().equals("0")||((Map) valueMsgYX3).get("SwPos")==null){
                                                    ispassStr.append("0");
//                                    is_pass="0";
                                                    is_pass2="0";
                                                    valueMsg="第二步：遥控合闸位置校验，遥控后开关位置校验失败";
                                                    Map<String, Object> map = new HashMap<String, Object>();
                                                    map.put("dev_type",objs[4]);
                                                    map.put("dev_label",objs[1]);
                                                    map.put("dev_out_id",objs[2]);
                                                    map.put("pd_name",objs[6]);
                                                    map.put("device_mode_name",objs[7]);
                                                    map.put("data_result",valueMsg);
                                                    map.put("is_pass","0");
                                                    value.add(map);
                                                } else if (((Map) valueMsgYX3).get("SwPos").equals("0")) {
                                                    ispassStr.append("0");
                                                    //                                   is_pass="0";
                                                    is_pass2="0";
                                                    valueMsg="第二步：遥控合闸后开关位置仍为分位，校验失败";
                                                    Map<String, Object> map = new HashMap<String, Object>();
                                                    map.put("dev_type",objs[4]);
                                                    map.put("dev_label",objs[1]);
                                                    map.put("dev_out_id",objs[2]);
                                                    map.put("pd_name",objs[6]);
                                                    map.put("device_mode_name",objs[7]);
                                                    map.put("data_result",valueMsg);
                                                    map.put("is_pass","0");
                                                    value.add(map);
                                                } else {
                                                    ispassStr.append("1");
                                                    //                                   is_pass="1";
                                                    is_pass2="1";
                                                    valueMsg="遥控分合闸召测成功";
                                                    Map<String, Object> map = new HashMap<String, Object>();
                                                    map.put("dev_type",objs[4]);
                                                    map.put("dev_label",objs[1]);
                                                    map.put("dev_out_id",objs[2]);
                                                    map.put("pd_name",objs[6]);
                                                    map.put("device_mode_name",objs[7]);
                                                    map.put("data_result", valueMsg);
                                                    map.put("is_pass","1");
                                                    value.add(map);


                                                }

                                            }


                                        }



                                }

                            }
                            else {
                            //控制开关合闸
                            Map<String, Object> resultMap = CheckUtillService.remoteControlCheck(objs[9].toString(), objs[2].toString(), objs[8].toString(),objs[0].toString());
//                    Map<String, Object> resultMap=new HashMap<>();
//                    resultMap.put("is_pass","0");

                            Object status = resultMap.get("is_pass");
                            if (status.toString().equals("0")){
                                ispassStr.append("0");
                                //                               is_pass="0";
                                is_pass2="0";
                                valueMsg="第一步：合闸遥控校验返回失败";
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("dev_type",objs[4]);
                                map.put("dev_label",objs[1]);
                                map.put("dev_out_id",objs[2]);
                                map.put("pd_name",objs[6]);
                                map.put("device_mode_name",objs[7]);
                                map.put("data_result",valueMsg);
                                map.put("is_pass","0");
                                value.add(map);
                            } else {
                                //第二次遥信开关位置验证
                                try{
                                    Thread.currentThread().sleep( 120000);
                                }catch(Exception ex){
                                    ex.printStackTrace();
                                }
                                Map<String, Object> resultMapYX2 = CheckUtillService.dataYXCheck(objs[9].toString(), objs[2].toString(), objs[8].toString());
                                Object statusYX2 = resultMapYX2.get("is_pass");
                                Object valueMsgYX2 = (Map<String,Object>)resultMapYX2.get("values");//||((Map) valueMsgYX2).get("SwPos").equals("1")
                                Object swPos = ((Map) valueMsgYX2).get("SwPos");
                                System.out.println(swPos);
                                if (statusYX2.toString().equals("0")||((Map) valueMsgYX2).get("SwPos")==null){
                                    ispassStr.append("0");
//                                    is_pass="0";
                                    is_pass2="0";
                                    valueMsg="第一步：遥控合闸后开关位置校验失败";
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put("dev_type",objs[4]);
                                    map.put("dev_label",objs[1]);
                                    map.put("dev_out_id",objs[2]);
                                    map.put("pd_name",objs[6]);
                                    map.put("device_mode_name",objs[7]);
                                    map.put("data_result",valueMsg);
                                    map.put("is_pass","0");
                                    value.add(map);
                                } else if (((Map) valueMsgYX2).get("SwPos").equals("0")) {
                                    ispassStr.append("0");
                                    //                                   is_pass="0";
                                    is_pass2="0";
                                    valueMsg="第一步：遥控合闸后开关位置仍为分位，校验失败";
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put("dev_type",objs[4]);
                                    map.put("dev_label",objs[1]);
                                    map.put("dev_out_id",objs[2]);
                                    map.put("pd_name",objs[6]);
                                    map.put("device_mode_name",objs[7]);
                                    map.put("data_result",valueMsg);
                                    map.put("is_pass","0");
                                    value.add(map);
                                } else {
//                                    ispassStr.append("1");
//                                    //                                   is_pass="1";
//                                    is_pass2="1";
//                                    valueMsg=valueMsgYX2;
//                                    Map<String, Object> map = new HashMap<String, Object>();
//                                    map.put("dev_type",objs[4]);
//                                    map.put("dev_label",objs[1]);
//                                    map.put("dev_out_id",objs[2]);
//                                    map.put("pd_name",objs[6]);
//                                    map.put("device_mode_name",objs[7]);
//                                    map.put("data_result", valueMsg);
//                                    map.put("is_pass","1");
//                                    value.add(map);

                                    Map<String, Object> resultMap2 = CheckUtillService.remoteControlCheckFZ(objs[9].toString(), objs[2].toString(), objs[8].toString(),objs[0].toString());
                                    Object valueMsgYX4 = (Map<String,Object>)resultMap2.get("values");
                                    Object status4 = resultMap2.get("is_pass");
                                    if (status4.toString().equals("0")){
                                        ispassStr.append("0");
                                        //                               is_pass="0";
                                        is_pass2="0";
                                        valueMsg="第二步：分闸遥控校验返回失败";
                                        Map<String, Object> map = new HashMap<String, Object>();
                                        map.put("dev_type",objs[4]);
                                        map.put("dev_label",objs[1]);
                                        map.put("dev_out_id",objs[2]);
                                        map.put("pd_name",objs[6]);
                                        map.put("device_mode_name",objs[7]);
                                        map.put("data_result",valueMsg);
                                        map.put("is_pass","0");
                                        value.add(map);
                                    } else {
                                        //分闸会直接返回不需要二次召测
                                       /* ispassStr.append("1");
                                        //                                   is_pass="1";
                                        is_pass2="1";
                                        valueMsg="遥控分合闸召测成功";
                                        Map<String, Object> map = new HashMap<String, Object>();
                                        map.put("dev_type",objs[4]);
                                        map.put("dev_label",objs[1]);
                                        map.put("dev_out_id",objs[2]);
                                        map.put("pd_name",objs[6]);
                                        map.put("device_mode_name",objs[7]);
                                        map.put("data_result", valueMsg);
                                        map.put("is_pass","1");
                                        value.add(map);*/


                                        Map<String, Object> fzresultMap = CheckUtillService.dataYXCheck(objs[9].toString(), objs[2].toString(), objs[8].toString());

                                        Object fzresultMapYX = fzresultMap.get("is_pass");
                                        Object fzresultMapYX3 = (Map<String,Object>)fzresultMap.get("values");//||((Map) valueMsgYX2).get("SwPos").equals("1")
                                        Object fzswPos = ((Map) fzresultMapYX3).get("SwPos");
                                        System.out.println(fzswPos);
                                        if (fzresultMapYX.toString().equals("0")||((Map) fzresultMapYX3).get("SwPos")==null){
                                            ispassStr.append("0");
//                                    is_pass="0";
                                            is_pass2="0";
                                            valueMsg="第二步：遥控分闸位置校验，遥控后开关位置校验失败";
                                            Map<String, Object> map = new HashMap<String, Object>();
                                            map.put("dev_type",objs[4]);
                                            map.put("dev_label",objs[1]);
                                            map.put("dev_out_id",objs[2]);
                                            map.put("pd_name",objs[6]);
                                            map.put("device_mode_name",objs[7]);
                                            map.put("data_result",valueMsg);
                                            map.put("is_pass","0");
                                            value.add(map);
                                        } else if (((Map) fzresultMapYX3).get("SwPos").equals("1")) {
                                            ispassStr.append("0");
                                            //                                   is_pass="0";
                                            is_pass2="0";
                                            valueMsg="第二步：遥控合闸后开关位置仍为合位，校验失败";
                                            Map<String, Object> map = new HashMap<String, Object>();
                                            map.put("dev_type",objs[4]);
                                            map.put("dev_label",objs[1]);
                                            map.put("dev_out_id",objs[2]);
                                            map.put("pd_name",objs[6]);
                                            map.put("device_mode_name",objs[7]);
                                            map.put("data_result",valueMsg);
                                            map.put("is_pass","0");
                                            value.add(map);
                                        } else {
                                            ispassStr.append("1");
                                            //                                   is_pass="1";
                                            is_pass2 = "1";
                                            valueMsg = "遥控分合闸召测成功";
                                            Map<String, Object> map = new HashMap<String, Object>();
                                            map.put("dev_type", objs[4]);
                                            map.put("dev_label", objs[1]);
                                            map.put("dev_out_id", objs[2]);
                                            map.put("pd_name", objs[6]);
                                            map.put("device_mode_name", objs[7]);
                                            map.put("data_result", valueMsg);
                                            map.put("is_pass", "1");
                                            value.add(map);
                                        }
                                    }


                                }

                            }

                            }
                        }

                        param.put(1,objs[0]);
                        param.put(2,direct_id);
                        param.put(3,objs[1]);
                        param.put(4,objs[2]);
                        param.put(5,objs[3]);
                        param.put(6,objs[4]);
                        param.put(7,objs[5]);
                        param.put(8,objs[6]);
                        param.put(9,objs[7]);
                        param.put(10,valueMsg==null?null: JSONObject.toJSONString(valueMsg));
                        param.put(11,is_pass2);
                        param.put(12,now);
                        paramList.add(param);

                        latch.countDown();
                    }
                });

            }

            try {
                latch.await();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        if (ispassStr.toString().contains("0")){
            is_pass="0";
        }

        //先删除已存在的
        String delSql="delete from D5000.DMS_CONTROL_CHECK_RESULT_ACCEPT_DETAIL where DIRECT_ID='"+id+"'";
        if (connect_mode.equals("3")){
            delSql="delete from D5000.DMS_CONTROL_CHECK_RESULT_ACCEPT_DETAIL where ID='"+devList2.get(0)[0].toString()+"'";
        }
        commonInterface.dbAccess_delete(delSql);
        String insertSql="insert into D5000.DMS_CONTROL_CHECK_RESULT_ACCEPT_DETAIL (ID,DIRECT_ID,DEV_LABEL,OUT_DEV_ID,DEV_NAME,DEV_TYPE,PD_ID,PD_NAME,DEVICE_MODE_NAME,DATA_RESULT,IS_PASS,DATA_TIME)" +
                " values (?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            long i = commonInterface.dbAccess_batchUpdate(insertSql, paramList);
            System.out.println("向D5000.DMS_CONTROL_CHECK_RESULT_ACCEPT_DETAIL表中插入数据结束！" + new Date());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("向D5000.DMS_CONTROL_CHECK_RESULT_ACCEPT_DETAIL表中插入数据失败！"+e);
            return CommonUtil.returnMap2(false,"0",null);
        }

        //二次验证是否存在
        String sql11="select id,dev_label from D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO where dev_label='"+devLabel+"'";
        List<Object[]> devList11 = commonInterface.selectListBySql(sql11);
        if (devList11.size()>0||connect_mode.equals("3")){
            isHave =true;
        }
        if (devList3.size()<=0 && !connect_mode.equals("3")){
            is_pass="2";
        } else if (connect_mode.equals("3")){
            String ispassSql="select id,is_pass from D5000.DMS_CONTROL_CHECK_RESULT_ACCEPT_DETAIL  where direct_id='"+id+"'";
            List<Object[]> ispassList = commonInterface.selectListBySql(ispassSql);
            for (Object[] objs:ispassList) {
                if (objs[1].toString().equals("0")){
                    is_pass="0";
                }
            }

            String sql4="select id,dev_label,out_dev_id,dev_name,connect_mode,direct_id from d5000.iot_device where  connect_mode=1\n" +
                    "start with dev_label='"+devLabel+"' connect by prior direct_id=id\n" +
                    " and is_valid =1 ";
            List<Object[]> devList4 = commonInterface.selectListBySql(sql4);
            devLabel=devList4.get(0)[1].toString();
        }

        if (!isHave){
            String ID=devList2.get(0)[0].toString();
            String DEV_LABEL=devList2.get(0)[1].toString();
            String OUT_DEV_ID=devList2.get(0)[2].toString();
            String DEV_NAME=devList2.get(0)[3]==null?"":devList2.get(0)[3].toString();
            String DEV_TYPE=devList2.get(0)[4]==null?"":devList2.get(0)[4].toString();

            String insSql="insert into D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO (ID,DEV_LABEL,OUT_DEV_ID,DEV_NAME,DEV_TYPE,REMOTE_CONTROL_RESULT,IS_PASS,DATA_TIME)" +
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
                        " set REMOTE_CONTROL_RESULT='"+is_pass+"',IS_PASS='"+is_pass+"',DATA_TIME='"+now+"'" +
                        " where dev_label='"+devLabel+"'";
                commonInterface.dbAccess_update(updSql);
                //更新dms_iot_device_resource_info
                String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='"+is_pass+"',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
                commonInterface.dbAccess_insert(insql2);
            } else {
                //String jySql=" select DEV_LABEL,IS_ONLINE_RESULT||REPORT_MSG_RESULT||DATA_MEASURE_RESULT||DEV_COUNT_RESULT||PARAM_SET_RESULT as rest" +
                String jySql=" select DEV_LABEL,IS_ONLINE_RESULT||REPORT_MSG_RESULT||DATA_MEASURE_RESULT||DEV_COUNT_RESULT||1 as rest" +
                        " from  D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                        " where dev_label='"+devLabel+"'";
                List<Object[]> jyList = commonInterface.selectListBySql(jySql);
                if (jyList.get(0)[1]!=null&&!jyList.get(0)[1].toString().contains("0")&&jyList.get(0)[1].toString().length()==5){
                    String updSql="update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                            " set REMOTE_CONTROL_RESULT='"+is_pass+"',IS_PASS='1',DATA_TIME='"+now+"'" +
                            " where dev_label='"+devLabel+"'";
                    commonInterface.dbAccess_update(updSql);
                    //更新dms_iot_device_resource_info
                    String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='1',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
                    commonInterface.dbAccess_insert(insql2);
                } else {
                    String updSql="update D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO" +
                            " set REMOTE_CONTROL_RESULT='"+is_pass+"',IS_PASS='0',DATA_TIME='"+now+"'" +
                            " where dev_label='"+devLabel+"'";
                    commonInterface.dbAccess_update(updSql);
                    //更新dms_iot_device_resource_info
                    String insql2="update d5000.dms_iot_device_resource_info set IS_A_CHECK='0',IS_A_CHECK_TIME='"+now+"' where term_esn='"+devLabel+"'";
                    commonInterface.dbAccess_insert(insql2);
                }
            }
        }
        log.info("-------------开关遥控验收结束");
        return CommonUtil.returnMap2(true,is_pass,value);
    }
}

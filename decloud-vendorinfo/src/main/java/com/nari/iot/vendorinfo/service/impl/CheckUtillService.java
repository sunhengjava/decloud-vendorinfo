package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CheckUtillService {

    /**
     * 3 参数下发校验公共方法
     * @param areaId
     * @param deviceId
     * @return
     */
    public static Map<String, Object> paramCheck(String areaId, String deviceId, String pdModel){
        String url ="http://25.212.172.50:10179/decloud-interaction/commandApi/commandParameterApi";
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("is_pass","0");
        retMap.put("values",null);


        String paras= "";
        if (pdModel.equals("2")){
            paras= "{"+
                    "\"PTUV_strVal\":\"150\"" +
                    "}";
        } else if (pdModel.equals("9")){
            paras= "{"+
                    "\"Dec_Lim\":\"250\"," +
                    "\"Ris_Lim\":\"230\"," +
                    "\"Adj_Tick\":\"30\"," +
                    "\"Adj_Dly\":\"600\"" +
                    "}";
        }else if (pdModel.equals("10")){
            paras= "{"+
                    "\"PFGoal\":\"0.95\"" +
                    "}";
        }else if (pdModel.equals("11")){
            paras= "{"+
                    "\"Tmp_OilSetVal\":\"90\"," +
                    "\"TmpRate_OilSetVal\":\"5\"" +
                    "}";
        }else if (pdModel.equals("12")){
            paras= "{"+
                    "\"set_ctrl_U_max\":\"250\"," +
                    "\"set_ctrl_U_min\":\"230\"," +
                    "\"set_ctrl_Q_max\":\"10\"," +
                    "\"set_ctrl_Q_min\":\"0\"," +
                    "\"en_VQC\":\"0\"," +
                    "\"set_max_day_op_num_cap\":\"50\"," +
                    "\"set_max_day_op_num_trans\":\"100\"" +
                    "}";
        }else if (pdModel.equals("13")){
            paras= "{"+
                    "\"set_ctrl_U_max\":\"250\"," +
                    "\"set_ctrl_U_min\":\"230\"," +
                    "\"set_ctrl_Q_max\":\"10\"," +
                    "\"set_ctrl_Q_min\":\"0\"," +
                    "\"en_VQC\":\"0\"," +
                    "\"set_max_day_op_num_cap\":\"50\"," +
                    "\"set_max_day_op_num_trans\":\"100\"" +
                    "}";
        }
        String params1="{" +
                "\"method\":\"ParameterSet\","+
                "\"areaId\":\""+areaId+"\","+
                "\"deviceId\":\""+deviceId+"\","+
                " \"paras\":"+paras+
                "}";
        String result1 = "";
        try {
            result1 = HttpUtil.httpPost(url,params1 );
            System.out.println(result1);
        } catch (Exception e) {
            e.printStackTrace();
            return retMap;
        }
        Map<String, Object> resultMap1 = JSONObject.parseObject(result1);

        Object paras1 = resultMap1.get("paras");
        if (paras1==null||paras1.toString().equals("")||paras1.toString().equals("{}")) {
            return retMap;
        }

        String params2="{" +
                "\"method\":\"ParameterActive\","+
                "\"areaId\":\""+areaId+"\","+
                "\"deviceId\":\""+deviceId+"\","+
                "\"paras\":"+ paras+
                "}";
        String result2 = "";
        try {
            result2 = HttpUtil.httpPost(url,params2 );
            System.out.println(result2);
        } catch (Exception e) {
            e.printStackTrace();
            return retMap;
        }
        Map<String, Object> resultMap2 = JSONObject.parseObject(result2);
        Object paras2 = resultMap2.get("paras");
        if (paras2==null||paras2.toString().equals("")||paras2.toString().equals("{}")) {
            return retMap;
        }
        String paras3= "";
        if (pdModel.equals("2")){
            paras3= "{"+
                    "\"PTUV_strVal\":\"\"" +
                    "}";
        } else if (pdModel.equals("9")){
            paras3= "{"+
                    "\"Dec_Lim\":\"\"," +
                    "\"Ris_Lim\":\"\"," +
                    "\"Adj_Tick\":\"\"," +
                    "\"Adj_Dly\":\"\"" +
                    "}";
        }else if (pdModel.equals("10")){
            paras3= "{"+
                    "\"PFGoal\":\"\"" +
                    "}";
        }else if (pdModel.equals("11")){
            paras3= "{"+
                    "\"Tmp_OilSetVal\":\"\"," +
                    "\"TmpRate_OilSetVal\":\"\"" +
                    "}";
        }else if (pdModel.equals("12")){
            paras3= "{"+
                    "\"set_ctrl_U_max\":\"\"," +
                    "\"set_ctrl_U_min\":\"\"," +
                    "\"set_ctrl_Q_max\":\"\"," +
                    "\"set_ctrl_Q_min\":\"\"," +
                    "\"en_VQC\":\"\"," +
                    "\"set_max_day_op_num_cap\":\"\"," +
                    "\"set_max_day_op_num_trans\":\"\"" +
                    "}";
        }else if (pdModel.equals("13")){
            paras3= "{"+
                    "\"Hum\":\"\"," +
                    "\"Tmp\":\"\""+
                    "}";
        }
        String params3="{" +
                "\"method\":\"ParameterGet\","+
                "\"areaId\":\""+areaId+"\","+
                "\"deviceId\":\""+deviceId+"\","+
                "\"paras\":"+paras3+
                "}";
        String result3 = "";
        try {
            result3 = HttpUtil.httpPost(url,params3 );
            System.out.println(result3);
        } catch (Exception e) {
            e.printStackTrace();
            return retMap;
        }
        Map<String, Object> resultMap3 = JSONObject.parseObject(result3);
        Object parasValue = resultMap3.get("paras");
        if (parasValue==null||parasValue.toString().equals("")||parasValue.toString().equals("{}")) {
            return retMap;
        } else {
            Map<String,Object> map1=(Map<String, Object>) JSONObject.parseObject(paras);
            Map<String,Object> map2=(Map<String, Object>)parasValue;
            boolean equals = isEquals(map1, map2);
            if (equals){
                retMap.put("is_pass","1");
                retMap.put("values",parasValue);
                return retMap;
            }else {
                return retMap;
            }

        }

    }


    /**
     * 数据召测-遥测校验公共方法
     * @param areaId
     * @param deviceId
     * @return
     */
    public static Map<String, Object> dataYCCheck(String areaId, String deviceId, String pdModel){
        String url ="http://25.212.172.50:10179/decloud-interaction/commandApi/commandAnalogOrDiscreteApi";
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("is_pass","0");
        retMap.put("values",null);

        String paras= "";
        if (pdModel.equals("2")){
            paras= "{"+
                    "\"PhV_phsA\":\"\"," +
                    "\"PhV_phsB\":\"\"," +
                    "\"PhV_phsC\":\"\"," +
                    "\"A_phsA\":\"\"," +
                    "\"A_phsB\":\"\"," +
                    "\"A_phsC\":\"\"" +
                    "}";
        } else if (pdModel.equals("9")){
            paras= "{"+
                    "\"PhV_phsA\":\"\"," +
                    "\"A_phsA\":\"\"," +
                    "\"PhW_phsA\":\"\"," +
                    "\"PhVar_phsA\":\"\"," +
                    "\"TotPF\":\"\"," +
                    "\"Hz\":\"\"," +
                    "\"Tmp\":\"\"," +
                    "\"OpCnt\":\"\"," +
                    "\"Gea\":\"\"" +
                    "}";
        }else if (pdModel.equals("10")){
            paras= "{"+
                    "\"Lod_Gr1\":\"\"," +
                    "\"Lod_Gr2\":\"\"," +
                    "\"Tmp_Gr1\":\"\"," +
                    "\"Tmp_Gr2\":\"\"," +
                    "\"PhV_phsA\":\"\"," +
                    "\"A_phsA\":\"\"," +
                    "\"PhW_phsA\":\"\"," +
                    "\"PhVar_phsA\":\"\"," +
                    "\"PhPF_phsA\":\"\"," +
                    "\"ThdPhV_phsA\":\"\"," +
                    "\"ThdA_phsA\":\"\"" +
                    "}";
        }else if (pdModel.equals("11")){
            paras= "{"+
                    "\"Tmp_Oil\":\"\"," +
                    "\"TmpRate_Oil\":\"\"" +
                    "}";
        }else if (pdModel.equals("12")){
            paras= "{"+
                    "\"out_action_type\":\"\"," +
                    "\"out_trans_cn\":\"\"," +
                    "\"out_cap_val_1\":\"\"," +
                    "\"out_cap_addr_1\":\"\"," +
                    "\"out_cap_obj_1\":\"\"," +
                    "\"out_cap_type_1\":\"\"," +
                    "\"out_cap_cnt_1\":\"\"" +
                    "}";
        }else if (pdModel.equals("13")){
            paras= "{"+
                    "\"Hum\":\"\"," +
                    "\"Tmp\":\"\"" +
                    "}";
        }
        String params1="{" +
                "\"method\":\"analogGet\","+
                "\"areaId\":\""+areaId+"\","+
                "\"deviceId\":\""+deviceId+"\","+
                " \"paras\":"+paras+
                "}";
        String result1 = "";
        try {
            result1 = HttpUtil.httpPost(url,params1 );
        } catch (Exception e) {
            e.printStackTrace();
            return retMap;
        }
        Map<String, Object> resultMap1 = JSONObject.parseObject(result1);
        System.out.println("地址为"+url);
        System.out.println("遥信参数为："+params1);
        System.out.println("--返回结果为--"+result1);
        Map<String,Object> paras1 = (Map<String,Object>)resultMap1.get("paras");
        if (paras1.size()<=0) {
            return retMap;
        } else {
            retMap.put("is_pass","1");
            retMap.put("values",paras1);
            return retMap;
        }

    }

    /**
     * 数据召测-遥信校验公共方法
     * @param areaId
     * @param deviceId
     * @return
     */
    public static Map<String, Object> dataYXCheck(String areaId, String deviceId, String pdModel){
        String url ="http://25.212.172.50:10179/decloud-interaction/commandApi/commandAnalogOrDiscreteApi";
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("is_pass","0");
        retMap.put("values",null);

        String paras= "";
        if (pdModel.equals("2")){
            paras= "{"+
                    "\"SwPos\":\"\"," +
                    "\"SwChange\":\"\"" +
                    "}";
        } else if (pdModel.equals("9")){
            paras= "{"+
                    "\"Local_mode\":\"\"," +
                    "\"Remote_mode\":\"\"," +
                    "\"Adj_Alm\":\"\"," +
                    "\"Init_Alm\":\"\"" +
                    "}";
        }else if (pdModel.equals("10")){
            paras= "{"+
                    "\"Local_mode\":\"\"," +
                    "\"Remote_mode\":\"\"," +
                    "\"Adj_Alm\":\"\"," +
                    "\"Init_Alm\":\"\"," +
                    "\"SwSt_Gr1\":\"\"," +
                    "\"LOSt_Gr1\":\"\"," +
                    "\"SwSt_Gr2\":\"\"," +
                    "\"LOSt_Gr2\":\"\"," +
                    "\"SwSt_phsA_Gr1\":\"\"," +
                    "\"LOSt_phsA_Gr1\":\"\"," +
                    "\"SwSt_phsB_Gr1\":\"\"," +
                    "\"LOSt_phsB_Gr1\":\"\"," +
                    "\"SwSt_phsC_Gr1\":\"\"," +
                    "\"LOSt_phsC_Gr1\":\"\"," +
                    "\"Ctrl_mode\":\"\"" +
                    "}";
        }else if (pdModel.equals("11")){
            paras= "{"+
                    "\"Comm_fail\":\"\"," +
                    "\"Tmp_Alm\":\"\"," +
                    "\"TmpSet_fail\":\"\"" +
                    "}";
        }else if (pdModel.equals("12")){
            paras= "{"+
                    "\"out_alm_OV\":\"\"," +
                    "\"out_alm_LV\":\"\"," +
                    "\"connect_cap_ctrl\":\"\"," +
                    "\"connect_trans_ctrl\":\"\"" +
                    "}";
        }
        String params1="{" +
                "\"method\":\"discreteGet\","+
                "\"areaId\":\""+areaId+"\","+
                "\"deviceId\":\""+deviceId+"\","+
                " \"paras\":"+paras+
                "}";
        String result1 = "";
        try {
            result1 = HttpUtil.httpPost(url,params1 );
        } catch (Exception e) {
            e.printStackTrace();
            return retMap;
        }
        Map<String, Object> resultMap1 = JSONObject.parseObject(result1);
      log.info("地址为"+url);
        log.info("遥信参数为："+params1);
        log.info("--返回结果为--"+result1);
        Map<String,Object> paras1 = (Map<String,Object>)resultMap1.get("paras");
        if (paras1.size()<=0) {
            return retMap;
        } else {
            retMap.put("is_pass","1");
            retMap.put("values",paras1);
            return retMap;
        }

    }

    /**
     * 遥控校验公共方法 合闸
     * @param areaId
     * @param deviceId
     * @return
     */
    public static Map<String, Object> remoteControlCheck(String areaId, String deviceId, String pdModel,String devId){


        String token = getToken(devId);
        Map<String, String> headers = new HashMap<>();
        headers.put("token",token);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String url ="http://25.212.172.50:10179/decloud-interaction/commandApi/commandControlApi";
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("is_pass","0");
        retMap.put("values",null);

        String params1="{" +
                "\"method\":\"commad_HZ\","+
                "\"areaId\":\""+areaId+"\","+
                "\"deviceId\":\""+deviceId+"\","+
                "\"paras\":{" +
                        "\"RemCtl_TrCtl\":\"HZ\"" +
                    "}"+
                "}";
        String result1 = "";
        try {
            log.info(" 合闸"+sdf.format(new Date())+"遥控请求："+params1);
            result1 = HttpUtil.httpPost(url,params1,headers );
            System.out.println(sdf.format(new Date())+"遥控返回："+result1);
            log.info("遥控 合闸返回："+result1);
        } catch (Exception e) {
            e.printStackTrace();
            return retMap;
        }
        Map<String, Object> resultMap1 = JSONObject.parseObject(result1);
        System.out.println(deviceId+"----"+result1);
        Map<String,Object> paras1 = (Map<String,Object>)resultMap1.get("paras");
        if (paras1.size()<=0) {
            return retMap;
        } else {
            retMap.put("is_pass","1");
            retMap.put("values",paras1);
            return retMap;
        }

    }




    /**
     * 遥控校验公共方法 分闸
     * @param areaId
     * @param deviceId
     * @return
     */
    public static Map<String, Object> remoteControlCheckFZ(String areaId, String deviceId, String pdModel,String devId){
        String token = getToken(devId);
        Map<String, String> headers = new HashMap<>();
        headers.put("token",token);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String url ="http://25.212.172.50:10179/decloud-interaction/commandApi/commandControlApi";
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("is_pass","0");
        retMap.put("values",null);

        String params1="{" +
                "\"method\":\"commad_TZ\","+
                "\"areaId\":\""+areaId+"\","+
                "\"deviceId\":\""+deviceId+"\","+
                "\"paras\":{" +
                "\"RemCtl_TrCtl\":\"TZ\"" +
                "}"+
                "}";
        String result1 = "";
        try {
            log.info("分闸"+sdf.format(new Date())+"遥控请求："+params1);
            result1 = HttpUtil.httpPost(url,params1,headers );
            log.info("remoteControlCheckFZ" +sdf.format(new Date())+"遥控返回："+result1);
            log.info("遥控分闸返回："+result1);
        } catch (Exception e) {
            e.printStackTrace();
            return retMap;
        }
        Map<String, Object> resultMap1 = JSONObject.parseObject(result1);
        System.out.println(deviceId+"----"+result1);
        Map<String,Object> paras1 = (Map<String,Object>)resultMap1.get("paras");
        if (paras1.size()<=0) {
            return retMap;
        } else {
            retMap.put("is_pass","1");
            retMap.put("values",paras1);
            return retMap;
        }

    }



    /**
     * 获取token
     * @param devId
     * @return
     */
    public static String getToken(String devId){


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String url ="http://25.212.172.50:9099/decloud-interaction/login/getToken";
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("is_pass","0");
        retMap.put("values",null);

        String params1="{" +
                "\"loginName\":\"TSYS\","+
                "\"devId\":\""+devId+"\""+
                "}";
        String result1 = "";
        try {
            System.out.println(sdf.format(new Date())+"遥控请求："+params1);
            result1 = HttpUtil.httpPost(url,params1 );
            System.out.println(sdf.format(new Date())+"遥控返回："+result1);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        Map<String, Object> resultMap1 = JSONObject.parseObject(result1);
        Map<String,Object> paras1 = (Map<String,Object>)resultMap1.get("data");
        if (paras1.size()<=0) {
            return "";
        } else {
            return paras1.get("token").toString();
        }

    }



    /**
     * 判断两个map共同值是否相同
     * @param map1
     * @param map2
     * @return
     */
    public static boolean isEquals(Map<String, Object> map1,Map<String, Object> map2){
        boolean is_equals=true;
        for (String key :map1.keySet()) {
            if (map2.get(key)==null||map2.get(key)==""){
                return false;
            } else if (!map1.get(key).equals(map2.get(key))){
                return false;
            }
        }
        return is_equals;
    }


    /**
     * 参数下发公共方法-临时
     * @param areaId
     * @param deviceId
     * @return
     */
    public static Map<String, Object> paramSet(String areaId, String deviceId, String pdModel){
        String url ="http://25.212.172.50:10179/decloud-interaction/commandApi/commandParameterApi";
        Map<String, Object> retMap = new HashMap<>();
//        retMap.put("state","0");
        retMap.put("values","参数下发成功！");


        String paras= "";
        if (pdModel.equals("2")){
            paras= "{"+
                    "\"PTUV_strVal\":\"150\"" +
                    "}";
        } else if (pdModel.equals("9")){
            paras= "{"+
                    "\"Dec_Lim\":\"250\"," +
                    "\"Ris_Lim\":\"230\"" +
                    "}";
        }else if (pdModel.equals("10")){
            paras= "{"+
                    "\"PFGoal\":\"0.95\"" +
                    "}";
        }else if (pdModel.equals("11")){
            paras= "{"+
                    "\"Tmp_OilSetVal\":\"90\"," +
                    "\"TmpRate_OilSetVal\":\"5\"" +
                    "}";
        }else if (pdModel.equals("12")){
            paras= "{"+
                    "\"set_ctrl_U_max\":\"250\"," +
                    "\"set_ctrl_U_min\":\"230\"," +
                    "\"en_VQC\":\"0\"," +
                    "\"en_Trans\":\"1\"" +
                    "}";
        }
        String params1="{" +
                "\"method\":\"ParameterSet\","+
                "\"areaId\":\""+areaId+"\","+
                "\"deviceId\":\""+deviceId+"\","+
                " \"paras\":"+paras+
                "}";
        String result1 = "";
        try {
            result1 = HttpUtil.httpPost(url,params1 );
        } catch (Exception e) {
            e.printStackTrace();
            retMap.put("values","参数下发失败！");
            return retMap;
        }
        Map<String, Object> resultMap1 = JSONObject.parseObject(result1);

        Object paras1 = resultMap1.get("paras");
        if (paras1==null||paras1.toString().equals("")||paras1.toString().equals("{}")) {
            retMap.put("values","参数下发失败！");
            return retMap;
        }

        String params2="{" +
                "\"method\":\"ParameterActive\","+
                "\"areaId\":\""+areaId+"\","+
                "\"deviceId\":\""+deviceId+"\","+
                "\"paras\":"+ paras+
                "}";
        String result2 = "";
        try {
            result2 = HttpUtil.httpPost(url,params2 );
        } catch (Exception e) {
            e.printStackTrace();
            retMap.put("values","参数激活失败！");
            return retMap;
        }
        Map<String, Object> resultMap2 = JSONObject.parseObject(result2);
        Object paras2 = resultMap2.get("paras");
        if (paras2==null||paras2.toString().equals("")||paras2.toString().equals("{}")) {
            retMap.put("values","参数激活失败！");
            return retMap;
        }
        return retMap;

    }

    /**
     * 参数下发公共方法-临时  没改
     * @param areaId
     * @param deviceId
     * @return
     */
    public static Map<String, Object> paramGet(String areaId, String deviceId, String pdModel){
        String url ="http://25.212.172.50:10179/decloud-interaction/commandApi/commandParameterApi";
        Map<String, Object> retMap = new HashMap<>();

        String paras3= "";

        if (pdModel.equals("2")){
            paras3= "{"+
                    "\"PTUV_strVal\":\"\"" +
                    "}";
        } else if (pdModel.equals("9")){
            paras3= "{"+
                    "\"Dec_Lim\":\"\"," +
                    "\"Ris_Lim\":\"\"," +
                    "\"Adj_Tick\":\"\"," +
                    "\"Adj_Dly\":\"\"" +
                    "}";
        }else if (pdModel.equals("10")){
            paras3= "{"+
                    "\"PFGoal\":\"\"" +
                    "}";
        }else if (pdModel.equals("11")){
            paras3= "{"+
                    "\"Tmp_OilSetVal\":\"\"," +
                    "\"TmpRate_OilSetVal\":\"\"" +
                    "}";
        }else if (pdModel.equals("12")){
            paras3= "{"+
                    "\"set_ctrl_U_max\":\"\"," +
                    "\"set_ctrl_U_min\":\"\"," +
                    "\"set_ctrl_Q_max\":\"\"," +
                    "\"set_ctrl_Q_min\":\"\"," +
                    "\"en_VQC\":\"\"," +
                    "\"set_max_day_op_num_cap\":\"\"," +
                    "\"set_max_day_op_num_trans\":\"\"" +
                    "}";
        }else if (pdModel.equals("1")){
            paras3= "{"+
                    "\"Load\":\"\"," +
                    "\"ARtg\":\"\"," +
                    "\"ARtgSnd\":\"\"" +
                    "}";
        }
        String params3="{" +
                "\"method\":\"ParameterGet\","+
                "\"areaId\":\""+areaId+"\","+
                "\"deviceId\":\""+deviceId+"\","+
                "\"paras\":"+paras3+
                "}";
        String result3 = "";
        try {
            result3 = HttpUtil.httpPost(url,params3 );
            System.out.println(result3);
        } catch (Exception e) {
            e.printStackTrace();
            return retMap;
        }
        Map<String, Object> resultMap3 = JSONObject.parseObject(result3);
        Object parasValue = resultMap3.get("paras");
        if (parasValue==null||parasValue.toString().equals("")||parasValue.toString().equals("{}")) {
            return retMap;
        } else {
            retMap=(Map<String, Object>)parasValue;
        }

        return retMap;

    }



}

package com.nari.iot.vendorinfo.config;

import com.alibaba.fastjson.JSONArray;
import com.nari.iot.vendorinfo.common.CommonInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

 @RestController
@RequestMapping("/IndirectController")
public class IndirectController {
//    Logger log = LoggerFactory.getLogger(this.getClass());
        @Autowired
        CommonInterface commonInterface;

    @RequestMapping("/IndirectSum") @ResponseBody
    public Map<String,Object> indirectSum(@RequestBody Map<String,Object> request) throws Exception {
        System.out.println("------------------>in indirectSum");
        String devLabel =String.valueOf(request.get("devLabel")) ;
        Map<String, Object> resultMap = new HashMap<>();
        if(devLabel==null||"".equals(devLabel)){
            resultMap.put("code","500");
            resultMap.put("message","请求参数devLabel为空");
            return  resultMap;
        }
//        LOW_BRANCH_NUM as LOWBRANCHNUM,NON_REACTIVE_NUM as NONREACTIVENUM,VOLTAGE_TAP_NUM as VOLTAGETAPNUM,OIL_SENSOR_NUM as OILSENSORNUM,HUMIDITY_S_NUM as TEMPNUM
        String sql="select id,is_online,pd_id from d5000.iot_device  where dev_label='"+devLabel+"'";
        JSONArray jsonArray;
        try {
            System.out.println("-----indirectSum-->sql:"+sql);
            jsonArray = commonInterface.dbAccess_selectList(sql);

        } catch (RuntimeException e) {
            System.out.println("调用商用库失败" + e);
            System.out.println("执行sql:" + sql);
            return null;
        }
        Map<String,Object> mapSum=new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            String lowbranchnum = jsonArray.getJSONObject(i).getString("LOWBRANCHNUM") == null ? "" :
                jsonArray.getJSONObject(i).getString("LOWBRANCHNUM");//低压出线分支开关数量
            String nonreactivenum = jsonArray.getJSONObject(i).getString("NONREACTIVENUM") == null ? "" :
                jsonArray.getJSONObject(i).getString("NONREACTIVENUM");//无功补偿电容台数
            String voltagetapnum = jsonArray.getJSONObject(i).getString("VOLTAGETAPNUM") == null ? "" :
                jsonArray.getJSONObject(i).getString("VOLTAGETAPNUM");//有载调压分接头数量
            String oilsensornum = jsonArray.getJSONObject(i).getString("OILSENSORNUM") == null ? "" :
                jsonArray.getJSONObject(i).getString("OILSENSORNUM");//油温传感器数量

            String humitureNum = jsonArray.getJSONObject(i).getString("TEMPNUM") == null ? "" :
                    jsonArray.getJSONObject(i).getString("TEMPNUM");//台区温湿度
            mapSum.put("lowbranchnum",lowbranchnum);
            mapSum.put("nonreactivenum",nonreactivenum);
            mapSum.put("voltagetapnum",voltagetapnum);
            mapSum.put("oilsensornum",oilsensornum);
            mapSum.put("humitureNum",humitureNum);
        }

        String sqlSum="select COUNT(CASE WHEN p.PD_MODE = '9' then '9' end) as VOLTAGETAPNUM ,  "
                + "COUNT(CASE WHEN p.PD_MODE = '2' then '2' end) as LOWBRANCHNUM ,  "
                + "COUNT(CASE WHEN p.PD_MODE = '11' then '11' end) as OILSENSORNUM, "
                + "COUNT(CASE WHEN p.PD_MODE = '10' then '10' end) as NONREACTIVENUM , "
                + "COUNT(CASE WHEN p.PD_MODE = '13' then '13' end) as TEMPNUM "
                + "from d5000.iot_device i1 INNER join (select i.id from d5000.iot_device i where i.DEV_LABEL ='"+devLabel+"') t "
                + "on i1.DIRECT_ID = t.id inner join d5000.IOT_PRODUCT p on i1.PD_ID = p.ID where i1.is_valid='1'";

        JSONArray jsonArrays;
        try {
            jsonArrays = commonInterface.dbAccess_selectList(sqlSum);
        } catch (RuntimeException e) {
            System.out.println("调用商用库失败" + e.getMessage());
            System.out.println("执行sql:" + sql);
            return null;
        }
        Map<String,Object> mapSums=new HashMap<>();
        for (int i = 0; i < jsonArrays.size(); i++) {
            String lowbranchnum = jsonArrays.getJSONObject(i).getString("LOWBRANCHNUM") == null ? "" :
                jsonArrays.getJSONObject(i).getString("LOWBRANCHNUM");//低压出线分支开关数量
            String nonreactivenum = jsonArrays.getJSONObject(i).getString("NONREACTIVENUM") == null ? "" :
                jsonArrays.getJSONObject(i).getString("NONREACTIVENUM");//无功补偿电容台数
            String voltagetapnum = jsonArrays.getJSONObject(i).getString("VOLTAGETAPNUM") == null ? "" :
                jsonArrays.getJSONObject(i).getString("VOLTAGETAPNUM");//有载调压分接头数量
            String oilsensornum = jsonArrays.getJSONObject(i).getString("OILSENSORNUM") == null ? "" :
                jsonArrays.getJSONObject(i).getString("OILSENSORNUM");//油温传感器数量

            String humitureNum = jsonArrays.getJSONObject(i).getString("TEMPNUM") == null ? "" :
                    jsonArrays.getJSONObject(i).getString("TEMPNUM");//台区温湿度
            mapSums.put("lowbranchnum",lowbranchnum);
            mapSums.put("nonreactivenum",nonreactivenum);
            mapSums.put("voltagetapnum",voltagetapnum);
            mapSums.put("oilsensornum",oilsensornum);
            mapSums.put("humitureNum",humitureNum);
        }
        resultMap.put("code","200");
        resultMap.put("fieldSum",mapSum);
        resultMap.put("indirectSum",mapSums);
        return resultMap;
    }

}

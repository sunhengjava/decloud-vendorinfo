package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.OilCoupleBreakService;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * @program: decloud-vendorinfo
 * @description:
 * @author: sunheng
 * @create: 2023-04-20 15:37
 **/
@Slf4j
@Service("OilCoupleBreakService")
public class OilCoupleBreakServiceImpl implements OilCoupleBreakService {

    @Autowired
    CommonInterface commonInterface;

    @Override
    public LayJson getAnomalyCountVO(Map<String, Object> map) {
        String funType = map.get("funcType") == null || map.get("funcType") == "" ? "" : map.get("funcType").toString();
        String orgId = map.get("orgId") == null || map.get("orgId") == "" ? "" : map.get("orgId").toString();
        String startTime = map.get("startTime") == null || map.get("startTime") == "" ? "" : map.get("startTime").toString();
        String endTime = map.get("endTime") == null || map.get("endTime") == "" ? "" : map.get("endTime").toString();

        String parmCity = "";
        String parmCitys = "";
        String resultCity = "";
        if (funType.equals("02")) {
            parmCity = "city_org_nm";
            parmCitys = "city_base_org_id";
        } else if (funType.equals("03")) {
            parmCity = " county_org_nm";
            parmCitys = "county_base_org_id";
            resultCity = "and n3.city_base_org_id='" + orgId + "'";
        } else {
            parmCity = "gds_org_nm";
            parmCitys = "gds_base_org_id";
            resultCity = "and n3.county_base_org_id='" + orgId + "'";
        }
        //总共返回
        LinkedHashMap<String, Map<String, String>> resultMap = new LinkedHashMap();

        String orgSql="select id,name from osp.isc_baseorg where parent_id='"+orgId+"' order by unicode  ";
        List<Object[]> orgList = commonInterface.selectListBySql(orgSql);
        for(Object[] or:orgList){
            Map<String, String> entityMap = new HashMap<>();
            entityMap.put("dwVlue", "0");
            entityMap.put("upVlue", "0");
            entityMap.put("orgName", or[1].toString());
            entityMap.put("yctq", "0");
            resultMap.put(or[0].toString(), entityMap);
        }

        String yctqSql="  SELECT count(distinct tgid), "+parmCitys+" as orgId FROM \"D5000\".\"DMS_TEMPERATURE_UPANDDOWN\" n1\n" +
                "  left join d5000.dms_tr_device n2  on n1.tgid=n2.id\n" +
                " left join (select * from d5000.dms_tr_account_info where run_st=20 and default_i is null)  n3 on n2.device_asset_id=concat('PD_',n3.tr_pms_no)\n" +
                " where 1=1  and  timestamp_date >= '"+startTime+" 00:00:00' \n" +
                " and timestamp_date <= '"+endTime+" 23:59:59' and ( measured_value < restricts2 or  measured_value>120 )   group by city_base_org_id ";
        List<Object[]> yctqList = commonInterface.selectListBySql(yctqSql);
        Map yctqMap=new HashMap();
        for(Object[]li :yctqList ){
            yctqMap.put(li[1],li[0]);
        }
        String sql = " SELECT count(1),outof_value, " + parmCity + " as orgName,"+parmCitys+" as orgId ,count(distinct(tgid)) FROM \"D5000\".\"DMS_TEMPERATURE_UPANDDOWN\" n1\n" +
                "  left join d5000.dms_tr_device n2  on n1.tgid=n2.id\n" +
                " left join (select * from d5000.dms_tr_account_info where run_st=20 and default_i is null)  n3 on n2.device_asset_id=concat('PD_',n3.tr_pms_no)\n" +
                " where 1=1  and ( measured_value < restricts2 or  measured_value>120 )  " ;
                if(StringUtils.isNotBlank(startTime)&&StringUtils.isNotBlank(endTime)) {
                sql+=" and  timestamp_date >= '" + startTime + " 00:00:00' and timestamp_date <= '" + endTime  + " 23:59:59' ";
                }


                sql+= resultCity ;
        sql+="group by outof_value," + parmCity + " ,"+parmCitys+" ";
        log.info("统计sql为"+sql);

        List<Object[]> list = commonInterface.selectListBySql(sql);
        if (list.size() > 0) {
            for (Object[] li : list) {
                if(li[3]!=null){
                    if(li[0]==null){
                        li[0]=0;
                    }
                    if (resultMap.containsKey(li[3].toString())) {
                        Map<String, String> map1 = resultMap.get(li[3].toString());
                        if (li[1].toString().equals("1")) {
                            int upVlue = Integer.parseInt(map1.get("upVlue")) + Integer.parseInt(li[0].toString());
                            map1.put("upVlue", upVlue + "");
                        }
                        if (li[1].toString().equals("0")) {
                            int upVlue = Integer.parseInt(map1.get("dwVlue")) + Integer.parseInt(li[0].toString());
                            map1.put("dwVlue", upVlue + "");
                        }
                        String o = yctqMap.get(li[3].toString())!=null?yctqMap.get(li[3].toString()).toString():"0";
                        map1.put("yctq",o);
                        resultMap.put(li[3].toString(), map1);
                    }
                }

                /*
                * else {
                    Map<String, String> entityMap = new HashMap<>();
                    entityMap.put("dwVlue", "0");
                    entityMap.put("upVlue", "0");
                    if (li[1].toString().equals("1")) {
                        entityMap.put("upVlue", li[0].toString());
                    }
                    if (li[1].toString().equals("0")) {
                        entityMap.put("dwVlue", li[0].toString());
                    }
                    entityMap.put("orgName", li[2].toString());
                    resultMap.put(li[3].toString(), entityMap);
                }
                * */
            }
        }

        List<Map> list1 = new ArrayList<>();
        int sizes = 0;
        int upVlue = 0;
        int dwVlue = 0;
        int yctq = 0;
        for (String s : resultMap.keySet()) {
            int upVlue1 = Integer.parseInt(StringUtils.isNotBlank(resultMap.get(s).get("upVlue")) ? resultMap.get(s).get("upVlue") : "0");
            upVlue+= upVlue1;
            int dwVlue1 = Integer.parseInt(StringUtils.isNotBlank(resultMap.get(s).get("dwVlue")) ? resultMap.get(s).get("dwVlue") : "0");
            dwVlue+= dwVlue1;
            int yctq1 = Integer.parseInt(StringUtils.isNotBlank(resultMap.get(s).get("yctq")) ? resultMap.get(s).get("yctq") : "0");
            yctq+= yctq1;
             resultMap.get(s).put("size",String.valueOf(upVlue1+dwVlue1));
            sizes+=Integer.parseInt(StringUtils.isNotBlank(resultMap.get(s).get("size"))?resultMap.get(s).get("size"):"0");
            list1.add( resultMap.get(s));
        }
        HashMap<String,String> zjMap=new HashMap<>();
        zjMap.put("size",sizes+"");
        zjMap.put("upVlue",upVlue+"");
        zjMap.put("dwVlue",dwVlue+"");
        zjMap.put("orgName","总计");
        zjMap.put("yctq",yctq+"");
        list1.add(zjMap);
        return new LayJson(200, "请求成功", list1, 1);
    }

    //详情的
    @Override
    public LayJson getAnomalyDetailsVO(Map<String, Object> map) {
        String funType = map.get("funcType") == null || map.get("funcType") == "" ? "" : map.get("funcType").toString();
        String orgId = map.get("orgId") == null || map.get("orgId") == "" ? "" : map.get("orgId").toString();
        String startTime = map.get("startTime") == null || map.get("startTime") == "" ? "" : map.get("startTime").toString();
        String endTime = map.get("endTime") == null || map.get("endTime") == "" ? "" : map.get("endTime").toString();
        String tgId = map.get("tgId") == null || map.get("tgId") == "" ? "" : map.get("tgId").toString();
        String tgName = map.get("tgName") == null || map.get("tgName") == "" ? "" : map.get("tgName").toString();
        int pageNo = map.get("pageNo") == null || map.get("pageNo") == "" ? 1 : Integer.parseInt(map.get("pageNo").toString());
        int pageSize = map.get("pageSize") == null || map.get("pageSize") == "" ? 50 : Integer.parseInt(map.get("pageSize").toString());
        String sql2="";
        if (funType.equals("03")) {
            sql2 = " and n3.city_base_org_id='"+orgId+"' ";
        } else if(funType.equals("04")) {
            sql2 = "and n3.county_base_org_id='"+orgId+"' ";
        }else if(funType.equals("05")){
            sql2 = "and n3.gds_base_org_id='"+orgId+"' ";
        }
        sql2 +=" AND n3.city_base_org_id is not null and n3.county_base_org_id is not null  and  n3.gds_base_org_id is not null ";
        String sql="   with n1 as( " +
                " select  measured_id,count(1) as gs,outof_value, 120 as restricts,min(restricts2)as restricts2,tgid from DMS_TEMPERATURE_UPANDDOWN \n" +
                "  where 1=1   and ( measured_value < restricts2 or  measured_value>120 )  " ;
        String sqlCount="  with n1 as(\n" +
                " select  measured_id,count(1) as gs,outof_value, 120 as restricts,min(restricts2)as restricts2,tgid from DMS_TEMPERATURE_UPANDDOWN \n" +
                "  where 1=1   and ( measured_value < restricts2 or  measured_value>120 )  ";
        if(StringUtils.isNotBlank(startTime)&&StringUtils.isNotBlank(endTime) ){
            sql+=" and  timestamp_date >= '" + startTime + " 00:00:00' and timestamp_date <= '" + endTime  + " 23:59:59' ";
            sqlCount+=" and  timestamp_date >= '" + startTime + " 00:00:00' and timestamp_date <= '" + endTime  + " 23:59:59' ";
        }
        sql+= "  group by outof_value,measured_id ,tgid )  " ;
        sqlCount+= "  group by outof_value,measured_id ,tgid )  " ;


         sql+=   "SELECT  city_org_nm as cityName,county_org_nm as countyName,gds_org_nm as gdsName,measured_id as measured_id,n3.name as tgName,\n" +
                "outof_value as outofValue,restricts,restricts2,gs   FROM  n1\n" +
                "  left join d5000.dms_tr_device n2  on n1.tgid=n2.id\n" +
                " left join (select * from d5000.dms_tr_account_info where run_st=20 and default_i is null)  n3 on n2.device_asset_id=concat('PD_',n3.tr_pms_no)\n" +
                " where 1=1       " ;
        sqlCount+=   "SELECT  count(distinct measured_id)  FROM  n1\n" +
                "  left join d5000.dms_tr_device n2  on n1.tgid=n2.id\n" +
                " left join (select * from d5000.dms_tr_account_info where run_st=20 and default_i is null)  n3 on n2.device_asset_id=concat('PD_',n3.tr_pms_no)\n" +
                " where 1=1        " ;
        sql+=sql2 ;
        sqlCount+=sql2 ;


        if(StringUtils.isNotBlank(tgId)){
            sql+="  and  n3.tg_id='"+tgId+"' ";
            sqlCount+="  and n3.tg_id='"+tgId+"' ";
        }
        if(StringUtils.isNotBlank(tgName)){
            sql+=" and  n3.name='"+tgName+"' ";
            sqlCount+=" and  n3.name='"+tgName+"' ";
        }

        sql+= sql2 + " order by n1.tgid    limit " + (pageNo - 1) * pageSize + "," + pageSize;
        log.info("查询sql为"+sql);
        List<Object[]> objects = commonInterface.selectListBySql(sql);
        Map<String,Map<String,Object>> result=new HashMap<>();
        for(Object[] objects1:objects){

            if(result.containsKey(objects1[3])){
                Map<String, Object> map1 = result.get(objects1[3]);
                if(objects1[5]!=null&&objects1[5].toString().equals("0")){
                    int XXCS = Integer.parseInt(map1.get("XXCS")!=null?map1.get("XXCS").toString():"0");
                    int i = Integer.parseInt(objects1[8].toString());
                    int i1 = XXCS + i;
                    map1.put("XXCS",i1+"");
                }else{
                    int SXCS = Integer.parseInt(map1.get("SXCS")!=null?map1.get("SXCS").toString():"0");
                    int i = Integer.parseInt(objects1[8].toString());
                    int i1 = SXCS + i;
                    map1.put("SXCS",i1+"");
                }
                result.put(objects1[3].toString(),map1);
            }else {
                HashMap map1=new HashMap();
                map1.put("CITYNAME",objects1[0]);
                map1.put("COUNTYNAME",objects1[1]);
                map1.put("GDSNAME",objects1[2]);
                map1.put("TGNAME",objects1[4]);
                if(objects1[5]!=null&&objects1[5].toString().equals("0")){
                    map1.put("XXCS",objects1[8]);
                    map1.put("SXCS",0);
                }else{
                    map1.put("SXCS",objects1[8]);
                    map1.put("XXCS",0);
                }
                map1.put("RESTRICTS",objects1[6]);
                map1.put("RESTRICTS2",objects1[7]);
                map1.put("MEASUREDID",objects1[3]);
                result.put(objects1[3].toString(),map1);
            }
        }
        List<Map> listResult=new ArrayList<>();
        for(Map.Entry<String,Map<String,Object>> ma:result.entrySet()){
            listResult.add(ma.getValue());
        }
        List<Object[]> list = commonInterface.selectListBySql(sqlCount);
        String count = list.get(0)[0] != null ? list.get(0)[0].toString() : "0";
        return new LayJson(200,"请求成功",listResult,Integer.parseInt(count));
    }
    //弹窗的
    @Override
    public LayJson getAnomalyXqVO(Map<String, Object> map) {
        String funType = map.get("funcType") == null || map.get("funcType") == "" ? "" : map.get("funcType").toString();
        String orgId = map.get("orgId") == null || map.get("orgId") == "" ? "" : map.get("orgId").toString();
        String startTime = map.get("startTime") == null || map.get("startTime") == "" ? "" : map.get("startTime").toString();
        String endTime = map.get("endTime") == null || map.get("endTime") == "" ? "" : map.get("endTime").toString();
        String tgId = map.get("tgId") == null || map.get("tgId") == "" ? "" : map.get("tgId").toString();
        String tgName = map.get("tgName") == null || map.get("tgName") == "" ? "" : map.get("tgName").toString();
        String measuredid = map.get("measuredid") == null || map.get("measuredid") == "" ? "" : map.get("measuredid").toString();
        String restrictsType = map.get("restrictsType") == null || map.get("restrictsType") == "" ? "" : map.get("restrictsType").toString();
        int pageNo = map.get("pageNo") == null || map.get("pageNo") == "" ? 1 : Integer.parseInt(map.get("pageNo").toString());
        int pageSize = map.get("pageSize") == null || map.get("pageSize") == "" ? 50 : Integer.parseInt(map.get("pageSize").toString());
        String sql2="";
        if (funType.equals("03")) {
            sql2 = " and n3.city_base_org_id='"+orgId+"' ";
        } else if(funType.equals("03")) {
            sql2 = "and n3.county_base_org_id='"+orgId+"' ";
        }else if(funType.equals("04")){
            sql2 = "and n3.gds_base_org_id='"+orgId+"' ";
        }
        String sql="  SELECT  city_org_nm as cityName,county_org_nm as countyName,gds_org_nm as gdsName,tg_id as tgId,n3.name as tgName,measured_value as measuredValue,measured_id as measuredId,outof_value as outofValue,120 as restricts,timestamp_date||'' as timestampDate,restricts2  FROM \"D5000\".\"DMS_TEMPERATURE_UPANDDOWN\" n1\n" +
                "  left join d5000.dms_tr_device n2  on n1.tgid=n2.id\n" +
                " left join (select * from d5000.dms_tr_account_info where run_st=20 and default_i is null)  n3 on n2.device_asset_id=concat('PD_',n3.tr_pms_no)\n" +
                " where 1=1   and ( measured_value < restricts2 or  measured_value>120 )   " ;

        String sqlCount="  SELECT  count(1)  FROM \"D5000\".\"DMS_TEMPERATURE_UPANDDOWN\" n1\n" +
                "  left join d5000.dms_tr_device n2  on n1.tgid=n2.id\n" +
                " left join (select * from d5000.dms_tr_account_info where run_st=20 and default_i is null)  n3 on n2.device_asset_id=concat('PD_',n3.tr_pms_no)\n" +
                " where 1=1  and ( measured_value < restricts2 or  measured_value>120 )   " ;
        sqlCount+=sql2 ;
        if(StringUtils.isNotBlank(startTime)&&StringUtils.isNotBlank(endTime) ){
            sql+=" and  timestamp_date >= '" + startTime + " 00:00:00' and timestamp_date <= '" + endTime  + " 23:59:59' ";
            sqlCount+=" and  timestamp_date >= '" + startTime + " 00:00:00' and timestamp_date <= '" + endTime  + " 23:59:59' ";
        }
        if(StringUtils.isNotBlank(tgId)){
            sql+="  and  n3.tg_id='"+tgId+"' ";
            sqlCount+="  and n3.tg_id='"+tgId+"' ";
        }
        if(StringUtils.isNotBlank(tgName)){
            sql+=" and  n3.name='"+tgName+"' ";
            sqlCount+=" and  n3.name='"+tgName+"' ";
        }
        if(StringUtils.isNotBlank(measuredid)){
            sql+=" and  measured_id='"+measuredid+"' ";
            sqlCount+=" and  measured_id='"+measuredid+"' ";
        }
        if(StringUtils.isNotBlank(restrictsType)){
            sql+=" and  outof_value='"+restrictsType+"' ";
            sqlCount+=" and  outof_value='"+restrictsType+"' ";
        }
        sql+= sql2 + " order by n1.tgid    limit " + (pageNo - 1) * pageSize + "," + pageSize;
        log.info("查询sql为"+sql);
        JSONArray objects = commonInterface.dbAccess_selectList(sql);
        List<Object[]> list = commonInterface.selectListBySql(sqlCount);
        String count = list.get(0)[0] != null ? list.get(0)[0].toString() : "0";
        return new LayJson(200,"请求成功",objects,Integer.parseInt(count));
    }
//详情的导出
    @Override
    public Map<String,Object> AnomalyXqVOExcel(HttpServletRequest request, HttpServletResponse response) {
        String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss") + ".xls";
        String targetFilePath = request.getSession().getServletContext().getRealPath("download") + File.separator + fileName;
        String sheet = "异常监测油温详情";
        String funType = request.getParameter("funcType") == null || request.getParameter("funcType") == "" ? "" : request.getParameter("funcType").toString();
        String orgId = request.getParameter("orgId") == null || request.getParameter("orgId") == "" ? "" : request.getParameter("orgId").toString();
        String startTime = request.getParameter("startTime") == null || request.getParameter("startTime") == "" ? "" : request.getParameter("startTime").toString();
        String endTime = request.getParameter("endTime") == null || request.getParameter("endTime") == "" ? "" : request.getParameter("endTime").toString();
        String tgName = request.getParameter("tgName") == null || request.getParameter("tgName") == "" ? "" : request.getParameter("tgName").toString();
        String sql2="";
        if (funType.equals("03")) {
            sql2 = " and n3.city_base_org_id='"+orgId+"' ";
        } else if(funType.equals("03")) {
            sql2 = "and n3.county_base_org_id='"+orgId+"' ";
        }else if(funType.equals("04")){
            sql2 = "and n3.gds_base_org_id='"+orgId+"' ";
        }
        sql2 +=" AND n3.city_base_org_id is not null and n3.county_base_org_id is not null  and  n3.gds_base_org_id is not null ";
        String sql="   with n1 as( " +
                " select  measured_id,count(1) as gs,outof_value, 120 as restricts,min(restricts2)as restricts2,tgid from DMS_TEMPERATURE_UPANDDOWN \n" +
                "  where 1=1   and ( measured_value < restricts2 or  measured_value>120 )  " ;




        if(StringUtils.isNotBlank(startTime)&&StringUtils.isNotBlank(endTime) ){
            sql+=" and  timestamp_date >= '" + startTime + " 00:00:00' and timestamp_date <= '" + endTime  + " 23:59:59' ";
        }
        sql+= "  group by outof_value,measured_id ,tgid )  " ;


        sql+=   "SELECT  city_org_nm as cityName,county_org_nm as countyName,gds_org_nm as gdsName,measured_id as measured_id,n3.name as tgName,\n" +
                "outof_value as outofValue,restricts,restricts2,gs   FROM  n1\n" +
                "  left join d5000.dms_tr_device n2  on n1.tgid=n2.id\n" +
                " left join (select * from d5000.dms_tr_account_info where run_st=20 and default_i is null)  n3 on n2.device_asset_id=concat('PD_',n3.tr_pms_no)\n" +
                " where 1=1       " ;

        sql+=sql2 ;
        if(StringUtils.isNotBlank(tgName)){
            sql+=" and  n3.name='"+tgName+"' ";
        }
        sql+= sql2 + " order by n1.tgid  ";
        log.info("查询sql为"+sql);
        List<Object[]> objects = commonInterface.selectListBySql(sql);
        Map<String,Map<String,Object>> result=new HashMap<>();
        for(Object[] objects1:objects){

            if(result.containsKey(objects1[3])){
                Map<String, Object> map1 = result.get(objects1[3]);
                if(objects1[5]!=null&&objects1[5].toString().equals("0")){
                    int XXCS = Integer.parseInt(map1.get("XXCS")!=null?map1.get("XXCS").toString():"0");
                    int i = Integer.parseInt(objects1[8].toString());
                    int i1 = XXCS + i;
                    map1.put("XXCS",i1+"");
                }else{
                    int SXCS = Integer.parseInt(map1.get("SXCS")!=null?map1.get("SXCS").toString():"0");
                    int i = Integer.parseInt(objects1[8].toString());
                    int i1 = SXCS + i;
                    map1.put("SXCS",i1+"");
                }
                result.put(objects1[3].toString(),map1);
            }else {
                HashMap map1=new HashMap();
                map1.put("CITYNAME",objects1[0]);
                map1.put("COUNTYNAME",objects1[1]);
                map1.put("GDSNAME",objects1[2]);
                map1.put("TGNAME",objects1[4]);
                if(objects1[5]!=null&&objects1[5].toString().equals("0")){
                    map1.put("XXCS",objects1[8]);
                    map1.put("SXCS",0);
                }else{
                    map1.put("SXCS",objects1[8]);
                    map1.put("XXCS",0);
                }
                map1.put("RESTRICTS",objects1[6]);
                map1.put("RESTRICTS2",objects1[7]);
                map1.put("MEASUREDID",objects1[3]);
                result.put(objects1[3].toString(),map1);
            }
        }

        String titles[] = new String[]{"序号", "市公司", "区县公司", "供电所", "台区名称", "越上限次数", "越下限次数", "上限限值（°C）", "下限限值（°C）" };
        List<Object[]> newlist = new ArrayList<Object[]>();
        int i = 0;
        for(Map.Entry<String,Map<String,Object>> ma:result.entrySet()){
            i++;
            Object[] newObj = new Object[9];
            newObj[0] = i;
            newObj[1] = ma.getValue().get("CITYNAME") == null || ma.getValue().get("CITYNAME").equals("") ? "-" : ma.getValue().get("CITYNAME");
            newObj[2] = ma.getValue().get("COUNTYNAME") == null || ma.getValue().get("COUNTYNAME").equals("") ? "-" : ma.getValue().get("COUNTYNAME");
            newObj[3] = ma.getValue().get("GDSNAME") == null || ma.getValue().get("GDSNAME").equals("") ? "-" : ma.getValue().get("GDSNAME");
            newObj[4] = ma.getValue().get("TGNAME") == null || ma.getValue().get("TGNAME").equals("") ? "-" : ma.getValue().get("TGNAME");
            newObj[5] = ma.getValue().get("SXCS") == null || ma.getValue().get("SXCS").equals("") ? "-" : ma.getValue().get("SXCS");
            newObj[6] = ma.getValue().get("XXCS") == null || ma.getValue().get("XXCS").equals("") ? "-" : ma.getValue().get("XXCS");
            newObj[7] = ma.getValue().get("RESTRICTS") == null || ma.getValue().get("RESTRICTS").equals("") ? "-" : ma.getValue().get("RESTRICTS");
            newObj[8] = ma.getValue().get("RESTRICTS2") == null || ma.getValue().get("RESTRICTS2").equals("") ? "-" : ma.getValue().get("RESTRICTS2");
            newlist.add(newObj);
        }

        try {
            createmsgExcel(targetFilePath, sheet, titles, newlist);
        } catch (Exception e) {
            return CommonUtil.returnMap(false, 0, "", false);
        }
        try {
            downloadOnlineExcel(request, response, fileName);
        } catch (Exception e) {
            return CommonUtil.returnMap(false, 0, "", null);
        }
        return CommonUtil.returnMap(true, 0, "", fileName);
    }
    private void createmsgExcel(String targetFile, String sheet, String[] titles, List<Object[]> rows) {
        WritableWorkbook workbook = null;
        WritableSheet worksheet = null;
        Label label = null;
        CommonUtil.checkFile(targetFile);
        OutputStream os = null;
        try {
            os = new FileOutputStream(targetFile);
            workbook = Workbook.createWorkbook(os);
            worksheet = workbook.createSheet(sheet, 0);

            for (int i = 0; i < titles.length; i++) {
                label = new Label(i, 0, titles[i]);// 列 行 内容
                worksheet.addCell(label);
            }
            int k = 1;
            int m = 0;
            for (int i = 0; i < rows.size(); i++) {

                if (m >= 60000) {
                    worksheet = workbook.createSheet(sheet + (k + 1), k);
                    for (int j = 0; j < titles.length; j++) {
                        label = new Label(j, 0, titles[j]);// 列 行 内容
                        worksheet.addCell(label);
                    }
                    k++;
                    m = 0;
                }
                for (int j = 0; j < rows.get(i).length; j++) {

                    worksheet.addCell(new Label(j, m + 1, String.valueOf(rows.get(i)[j])));
                }
                m++;

            }
            workbook.write();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void downloadOnlineExcel(HttpServletRequest request, HttpServletResponse response, String fileName) {
        String filePath = request.getSession().getServletContext().getRealPath("download") + File.separator + fileName;
        downloadFile(request, response, "异常监测油温-" + fileName, filePath);
    }
    /**
     * 下载公用方法
     *
     * @param request
     * @param response
     * @param fileName
     * @param filePath
     */
    private void downloadFile(HttpServletRequest request, HttpServletResponse response, String fileName, String filePath) {

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        OutputStream out = null;
        InputStream in = null;

        File downFiles = new File(filePath);
        if (!downFiles.exists()) {
            return;
        }

        try {
            in = new FileInputStream(downFiles);
            bis = new BufferedInputStream(in);
            out = response.getOutputStream();
            bos = new BufferedOutputStream(out);

            CommonUtil.setFileDownloadHeader(request, response, fileName);
            int byteRead = 0;
            byte[] buffer = new byte[8192];
            while ((byteRead = bis.read(buffer, 0, 8192)) != -1) {
                bos.write(buffer, 0, byteRead);
            }

            bos.flush();
            in.close();
            bis.close();
            out.close();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String ss="{\"msgId\":\"324c1ad16e3946c7b773f083a576bbb5\",\"regionCode\":\"421000\",\"timeStamp\":\"2023-07-06 00:33:38\",\"cbstatusSection\":[{\"mRId\":\"JZ_00144679246231502890\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231502891\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231502892\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231502893\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231502894\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231502895\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231502896\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231502897\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251029786638\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251029786639\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251029786640\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251029786642\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251029786643\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251029786644\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251029786645\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251029786646\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251029786647\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251029786648\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394038\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394039\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394040\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394041\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394042\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394043\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394044\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394045\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394046\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394047\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394048\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394049\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394050\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394054\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394055\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394056\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394057\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394058\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394059\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394060\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394061\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394062\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394063\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394064\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394065\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394066\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394087\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394088\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394089\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394090\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394091\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394093\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394094\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394095\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394096\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394097\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394098\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361299\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361300\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361301\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361282\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361283\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361284\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361285\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361286\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361287\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361288\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361289\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361290\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361291\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361292\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361302\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361294\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361298\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394306\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394307\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394308\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394309\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394310\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394311\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394312\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394316\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394317\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394318\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394319\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394320\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394321\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394322\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394323\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394324\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394325\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394326\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250492915716\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250492915717\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250492915718\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250492915719\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250492915720\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250492915721\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250492915722\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250492915723\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250492915725\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250492915732\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383902\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383903\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246466383\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383905\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246466383\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383907\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948546\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948547\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948549\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948552\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242404\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242405\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242406\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242408\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242409\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242410\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242411\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341202\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341155\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341156\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341157\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341158\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341159\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341160\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341161\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341162\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341163\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341165\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341166\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341167\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341168\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341169\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341172\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341173\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341174\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341175\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341176\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341177\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341178\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341181\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341182\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341183\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341184\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341185\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341186\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341187\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341188\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246265057\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246265057\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246265057\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246265057\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246265057\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246265057\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246265057\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246265057\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246265057\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948499\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948500\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948502\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948504\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948505\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948508\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948509\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948510\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948511\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948512\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948513\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948516\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948517\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948518\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246197948519\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250509692961\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250509692962\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250509692964\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250509692965\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250509692966\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250509692968\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250509692969\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250509692970\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250509692971\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782160\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782161\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782162\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782163\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782164\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782165\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782166\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782167\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782169\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782170\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782171\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782172\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782173\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782174\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782175\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782176\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782178\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782179\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782180\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782181\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782182\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782183\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782184\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782186\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782187\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814565\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814566\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814567\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814568\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814569\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814570\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814571\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814572\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814573\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814574\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814577\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814578\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814579\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814580\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885814581\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341122\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341123\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341124\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341125\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341126\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341127\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341128\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341129\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341130\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341131\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341132\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341133\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341136\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341137\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341138\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341139\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341140\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341141\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341143\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341144\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341145\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341146\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341147\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341148\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251063341149\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383874\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383875\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383876\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383877\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383878\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383879\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383880\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383881\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383882\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383883\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383884\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383885\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383886\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383887\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383888\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383889\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383890\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383891\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351170\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351171\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351172\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351173\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351174\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351175\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351176\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351177\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351178\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351179\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351180\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351181\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351182\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250761351183\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383952\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383953\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383954\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383955\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383956\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383957\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383958\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383959\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383960\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383961\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383962\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383963\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383965\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383966\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383967\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383968\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383969\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383970\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383971\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383972\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246466383973\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373976\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246768373\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373978\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246768373\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373980\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373981\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373982\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373983\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373984\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373985\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373986\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373987\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373990\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373991\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373992\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373993\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373994\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373995\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373996\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373997\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373998\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768373999\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768374000\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246768374001\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579033\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579036\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579037\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579038\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579039\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579035\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579040\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579041\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579042\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579043\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579044\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246265057461\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246265057462\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246265057463\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246265057464\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246265057467\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246265057468\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246265057469\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246265057470\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024607\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024608\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024609\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024619\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024615\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024614\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024613\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024612\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024611\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024610\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250526470162\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250526470163\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250526470164\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250526470165\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250526470166\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250526470168\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250526470169\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250526470170\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250526470171\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250526470172\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885815399\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885815400\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885815401\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885815402\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885815403\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885815404\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246885815408\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782191\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782195\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782196\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782197\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782198\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782199\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782200\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782201\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782202\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782203\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679251180782204\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231502994\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231502995\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231502996\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231502998\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231502999\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231503007\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231503003\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246231503004\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024629\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024630\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024631\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024632\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024633\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024634\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024635\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024636\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024639\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024640\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024641\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024642\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024643\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024644\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250560024645\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725790\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725791\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725792\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725795\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725796\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250794905760\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250794905761\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250794905762\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250794905763\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250794905764\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250794905765\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250794905766\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250794905767\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361378\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361379\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361380\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361381\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361382\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361383\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361384\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361386\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361387\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361388\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361389\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361390\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361391\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250459361392\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579020\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579021\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579022\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579023\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579024\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579025\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579026\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579027\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579028\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242416\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242417\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242433\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242420\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242421\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242422\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242423\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242424\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242425\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242427\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242428\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394330\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394331\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394334\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394335\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394336\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246164394337\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246298611\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246298611\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246298611\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246298611\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_AM_00144679246298611\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579046\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579047\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579048\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579049\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579050\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579051\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579052\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250593579053\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242436\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242437\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242438\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242440\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242441\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242442\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250694242443\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246499938756\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246499938758\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246499938759\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246499938760\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246499938763\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246499938766\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246499938767\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246499938768\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246499938770\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246499938772\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250794905770\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250794905771\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250794905772\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679250794905773\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725804\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725805\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725806\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725807\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725808\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725809\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725811\",\"equipType\":\"PD_1001\",\"currentState\":\"0\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725812\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725813\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"},{\"mRId\":\"JZ_00144679246214725814\",\"equipType\":\"PD_1001\",\"currentState\":\"1\",\"qualValue\":\"31\"}]}";
    }
}

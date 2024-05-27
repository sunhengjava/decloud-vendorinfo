package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.service.ITermCheckAcceptResultService;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service(value = "TermCheckAcceptResultService")
@Slf4j
public class TermCheckAcceptResultService implements ITermCheckAcceptResultService {

    @Autowired
    CommonInterface commonInterface;
    @Autowired
    ExportWordReportService exportWordReportService;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 8 获取终端验收结果表格数据
     *
     * @param request
     * @return
     */
   /* @Override
    public List<Map<String, Object>> getList(HttpServletRequest request, Map<String, Object> mapP) {
        String treeId=request.getParameter("treeid");
        String treeType=mapP.get("treetype")==null||mapP.get("treetype")==""?null:mapP.get("treetype").toString();
        String devName=mapP.get("name")==null||mapP.get("name")==""?null:mapP.get("name").toString();
        String devLabel=mapP.get("esn")==null||mapP.get("esn")==""?null:mapP.get("esn").toString();
        String feederName=mapP.get("xlmc")==null||mapP.get("xlmc")==""?null:mapP.get("xlmc").toString();
        String trName=mapP.get("pbmc")==null||mapP.get("pbmc")==""?null:mapP.get("pbmc").toString();
        String isOnline=mapP.get("isonline")==null||mapP.get("isonline")==""?null:mapP.get("isonline").toString();
        String isPass=mapP.get("status")==null||mapP.get("status")==""?null:mapP.get("status").toString();
        StringBuffer sql =new StringBuffer();
        sql.append("select n2.dev_name,n2.dev_label,n2.is_check,n2.is_check_time||'',n2.is_online,n3.name as trName,\n" +
                "n4.name as feederName,n8.name as sgsName,n7.name as xgsName,n6.name as gdsName,n1.img_url,n1.remark\n" +
                " from d5000.iot_device n2\n" +
                "left join D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO n1  on n1.id=n2.id\n" +
                "left join d5000.dms_tr_device n3 on n2.rely_id=n3.id\n" +
                "left join d5000.dms_feeder_device n4 on n3.feeder_id=n4.id\n" +
                "left join osp.device_auth_manage n5 on n3.id=n5.deviceid\n" +
                "left join osp.isc_baseorg n6 on n5.orgid=n6.id\n" +
                "left join osp.isc_baseorg n7 on n6.parent_id=n7.id\n" +
                "left join osp.isc_baseorg n8 on n7.parent_id=n8.id  \n"
        );
        if (treeType.equals("feeder")) {
            sql.append("where n2.is_valid='1'\n" +
                    " and n2.connect_mode='1'" +
                    " and n4.id='"+treeId+"'");
        } else if (treeType.equals("tr")) {
            sql.append("where n2.is_valid='1'\n" +
                    " and n2.connect_mode='1'" +
                    " and n3.id='"+treeId+"'");
        } else {
            sql.append("inner join (select id,name from osp.isc_baseorg start with id='"+treeId+"' connect by prior id=parent_id) n9 on n5.orgid=n9.id\n" +
                    "where n2.is_valid='1'\n" +
                    " and n2.connect_mode='1'");
        }
        if (devName!=null&&!devName.equals("")) {
            sql.append(" and n2.dev_name like '%"+devName+"%'");
        }
        if (devLabel!=null&&!devLabel.equals("")) {
            sql.append(" and n2.dev_label='"+devLabel+"'");
        }
        if (feederName!=null&&!feederName.equals("")) {
            sql.append(" and n4.name like '%"+feederName+"%'");
        }
        if (trName!=null&&!trName.equals("")) {
            sql.append(" and n3.name like '%"+trName+"%'");
        }
        if (isOnline!=null&&!isOnline.equals("")) {
            if (isOnline.equals("0")){
                sql.append(" and n2.is_online='2'");
            } else {
                sql.append(" and n2.is_online='"+isOnline+"'");
            }
        }
        if (isPass!=null&&!isPass.equals("")) {
            if (isPass.equals("2")) {
                sql.append(" and n2.is_check is null");
            } else {
                sql.append(" and n2.is_check='"+isPass+"'");
            }
        }

        StringBuffer sql2 =new StringBuffer();
        sql2.append(" union ( ");
        sql2.append("select n2.dev_name,n2.dev_label,n2.is_check,n2.is_check_time||'',n2.is_online,n3.name as trName,\n" +
                "n4.name as feederName,n8.name as sgsName,n7.name as xgsName,n6.name as gdsName,n1.img_url,n1.remark\n" +
                "from d5000.iot_device n2\n" +
                "left join D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO n1  on n1.id=n2.id\n" +
                "left join d5000.dms_tr_device n3 on n2.rely_id=n3.id\n" +
                "left join d5000.dms_feeder_device n4 on n3.feeder_id=n4.id\n" +
                "left join osp.device_auth_manage n5 on n3.id=n5.deviceid\n" +
                "left join osp.isc_baseorg n6 on n5.orgid=n6.id\n" +
                "left join osp.isc_baseorg n7 on n6.parent_id=n7.id\n" +
                "left join osp.isc_baseorg n8 on n7.parent_id=n8.id  \n");

        if (treeType.equals("feeder")) {
            sql2.append("where n2.is_valid='1'\n" +
                    " and n2.connect_mode='1'" +
                    " and n4.id='"+treeId+"'");
        } else if (treeType.equals("tr")) {
            sql2.append("where n2.is_valid='1'\n" +
                    " and n2.connect_mode='1'" +
                    " and n3.id='"+treeId+"'");
        } else {
            sql2.append("inner join (select code,name from osp.isc_baseorg start with id='" + treeId + "' connect by prior id=parent_id) n9 on n2.dms_region_id=n9.code \n" +
                    "where n2.is_valid='1'\n" +
                    " and n2.connect_mode='1' ");

        }
        if (devName!=null&&!devName.equals("")) {
            sql2.append(" and n2.dev_name like '%"+devName+"%'");
        }
        if (devLabel!=null&&!devLabel.equals("")) {
            sql2.append(" and n2.dev_label='"+devLabel+"'");
        }
        if (feederName!=null&&!feederName.equals("")) {
            sql2.append(" and n4.name like '%"+feederName+"%'");
        }
        if (trName!=null&&!trName.equals("")) {
            sql2.append(" and n3.name like '%"+trName+"%'");
        }
        if (isOnline!=null&&!isOnline.equals("")) {
            if (isOnline.equals("0")){
                sql2.append(" and n2.is_online='2'");
            } else {
                sql2.append(" and n2.is_online='"+isOnline+"'");
            }
        }
        if (isPass!=null&&!isPass.equals("")) {
            if (isPass.equals("2")) {
                sql2.append(" and n2.is_check is null");
            } else {
                sql2.append(" and n2.is_check='"+isPass+"'");
            }
        }
        sql2.append(" )");
        sql.append(sql2);
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        log.info("输出验收查询逻辑"+sql);
        List<Map<String, Object>> value= new ArrayList<Map<String, Object>>();
        int i=1;
        for (Object[] objs:devList) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("key",i++);
            map.put("name",objs[0]==null?"-":objs[0]);
            map.put("esn",objs[1]==null?"-":objs[1].toString());
            String is_pass="";
            if (objs[2]==null) {
                is_pass="2";
            } else {
                is_pass=objs[2].toString();
            }
            map.put("status",is_pass);
            map.put("time",objs[3]==null||objs[3]==""?"-":objs[3].toString());
            String is_online="-";
            if (objs[4]==null) {
                is_online="-";
            } else if (objs[4].toString().equals("2")) {
                is_online="0";
            } else if (objs[4].toString().equals("1")){
                is_online="1";
            }else if (objs[4].toString().equals("3")){
                is_online="3";
            }
            map.put("isonline",is_online);
            map.put("glpb",objs[5]==null?"-":objs[5]);
            map.put("ssxl",objs[6]==null?"-":objs[6]);
            map.put("ds",objs[7]==null?"-":objs[7]);
            map.put("xgs",objs[8]==null?"-":objs[8]);
            map.put("gds",objs[9]==null?"-":objs[9]);
            List<String> zpList =new ArrayList<>();
            if (objs[10]==null) {
                zpList=null;
            } else {
                zpList= Arrays.asList(objs[10].toString().split(";"));
            }
            map.put("zp",zpList);
            map.put("bz",objs[11]==null?"-":objs[11]);
            value.add(map);
        }
        return value;
    }*/

    /**
     * 获取单个设备每项验收结果
     *
     * @param request
     * @return
     */
    @Override
    public List<List<Object>> getDetail(HttpServletRequest request) {
        String devLabel=request.getParameter("esn");
        //  String sql="select dev_label,is_online_result,report_msg_result,param_set_result," +
        String sql="select dev_label,is_online_result,report_msg_result,1," +
                " data_measure_result,dev_count_result,remote_control_result,is_pass\n" +
                " from D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO\n" +
                " where dev_label='"+devLabel+"'";
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        List<List<Object>> value= new ArrayList<>();
        List<Object> list1= new ArrayList<>();
        List<Object> list2= new ArrayList<>();
        if (devList.size()>0) {
            list1.add(devList.get(0)[1]==null?"0":devList.get(0)[1]);
            list1.add(devList.get(0)[2]==null?"0":devList.get(0)[2]);
            // list1.add(devList.get(0)[3]==null?"0":devList.get(0)[3]);
            list1.add(devList.get(0)[4]==null?"0":devList.get(0)[4]);
            list1.add(devList.get(0)[5]==null?"0":devList.get(0)[5]);
            list1.add(devList.get(0)[6]==null?"0":devList.get(0)[6]);
            list2.add(devList.get(0)[7]==null?"0":devList.get(0)[7]);
            value.add(list1);
            value.add(list2);
        }

        return value;
    }


    /**
     * 获取终端验收结果子设备表格数据
     *   云主站页面请求
     * @param request
     * @return
     */
    @Override
    public List<Map<String, Object>> getDevList(HttpServletRequest request) {
        String devLabel=request.getParameter("esn");
        String type=request.getParameter("type");
        String tableName="";
        if (type.equals("1")) {
            tableName="DMS_ONLINE_CHECK_RESULT_ACCEPT_DETAIL";
        } else if (type.equals("2")) {
            tableName="DMS_REPORT_CHECK_RESULT_ACCEPT_DETAIL";
        } else if (type.equals("3")) {
            tableName="DMS_PARAM_CHECK_RESULT_ACCEPT_DETAIL";
        } else if (type.equals("4")) {
            tableName="DMS_MEASURE_CHECK_RESULT_ACCEPT_DETAIL";
        } else if (type.equals("5")) {
            tableName="DMS_DEVCOUNT_CHECK_RESULT_ACCEPT_DETAIL";
        } else if (type.equals("6")) {
            tableName="DMS_CONTROL_CHECK_RESULT_ACCEPT_DETAIL";
        }
        String sql="select n1.dev_type,n1.dev_label,n1.pd_name,n1.device_mode_name,n1.is_pass,dbms_lob.substr(data_result,4000)" +
                " from D5000."+tableName+" n1\n" +
                " inner join D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO n2 on n1.direct_id=n2.id\n" +
                " where n2.dev_label='"+devLabel+"'";
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        log.info("输出对应的sql"+sql);
        List<Map<String, Object>> value= new ArrayList<Map<String, Object>>();

        for (Object[] objs:devList) {
            Map<String, Object> map = new HashMap<String, Object>();
            if (type.equals("5")) {

                Map<String, Object> dataResult = JSONObject.parseObject(objs[5].toString());
                Map<String, Object> fieldSum = (Map<String, Object>) dataResult.get("fieldSum");
                Map<String, Object> indirectSum = (Map<String, Object>) dataResult.get("indirectSum");

                map.put("devType","3");
                map.put("esn","-");
                String nonreactivenum1 = fieldSum.get("nonreactivenum")==null? "0": fieldSum.get("nonreactivenum").toString();
                String nonreactivenum2 = indirectSum.get("nonreactivenum")==null? "0" : indirectSum.get("nonreactivenum").toString();

                if (nonreactivenum1.equals(nonreactivenum2)) {
                    map.put("isPass","1");
                } else {
                    map.put("isPass","0");
                }
                //不涉及
                if(!nonreactivenum2.equals("0")&&nonreactivenum1.equals("0")){
                    map.put("isPass","2");
                }
                map.put("pdName","`无功补偿电容器");
                map.put("pdModel","CAP");
                map.put("sbsl",nonreactivenum1);
                map.put("sjsl",nonreactivenum2);
                value.add(map);

                map = new HashMap<String, Object>();
                map.put("devType","3");
                map.put("esn","-");
                String voltagetapnum1 = fieldSum.get("voltagetapnum")==null? "0": fieldSum.get("voltagetapnum").toString();
                String voltagetapnum2 = indirectSum.get("voltagetapnum")==null? "0" : indirectSum.get("voltagetapnum").toString();
                if (voltagetapnum1.equals(voltagetapnum2)) {
                    map.put("isPass","1");
                } else {
                    map.put("isPass","0");
                }
                //不涉及
                if(!voltagetapnum2.equals("0")&&voltagetapnum1.equals("0")){
                    map.put("isPass","2");
                }

              /*  map.put("pdName","有载调压控制器");
                map.put("pdModel","ADJ");
                map.put("sbsl",voltagetapnum1);
                map.put("sjsl",voltagetapnum2);
                value.add(map);*/

                map = new HashMap<String, Object>();
                map.put("devType","3");
                map.put("esn","-");
                String oilsensornum1 = fieldSum.get("oilsensornum")==null? "0": fieldSum.get("oilsensornum").toString();
                String oilsensornum2 = indirectSum.get("oilsensornum")==null? "0" : indirectSum.get("oilsensornum").toString();
                if (oilsensornum1.equals(oilsensornum2)) {
                    map.put("isPass","1");
                } else {
                    map.put("isPass","0");
                }
                //不涉及
                if(!oilsensornum2.equals("0")&&oilsensornum1.equals("0")){
                    map.put("isPass","2");
                }
                map.put("pdName","配变油温");
                map.put("pdModel","OilTmpSensor");
                map.put("sbsl",oilsensornum1);
                map.put("sjsl",oilsensornum2);
                value.add(map);

                map = new HashMap<String, Object>();
                map.put("devType","3");
                map.put("esn","-");
                String lowbranchnum1 = fieldSum.get("lowbranchnum")==null? "0": fieldSum.get("lowbranchnum").toString();
                String lowbranchnum2 = indirectSum.get("lowbranchnum")==null? "0" : indirectSum.get("lowbranchnum").toString();
                if (lowbranchnum1.equals(lowbranchnum2)) {
                    map.put("isPass","1");
                } else {
                    map.put("isPass","0");
                }
                //不涉及
                if(!lowbranchnum2.equals("0")&&lowbranchnum1.equals("0")){
                    map.put("isPass","2");
                }
                map.put("pdName","低压开关");
                map.put("pdModel","RCD");
                map.put("sbsl",lowbranchnum1);//fieldSum -边设备
                map.put("sjsl",lowbranchnum2);     //子设备
                value.add(map);


                if( fieldSum.containsKey("humitureNum")&&indirectSum.containsKey("humitureNum")){
                    map = new HashMap<String, Object>();
                    map.put("devType","3");
                    map.put("esn","-");
                    String humitureNum1 = fieldSum.get("humitureNum")==null||fieldSum.get("humitureNum").equals("")? "0": fieldSum.get("humitureNum").toString();
                    String humitureNum2 = indirectSum.get("humitureNum")==null||indirectSum.get("humitureNum").equals("")? "0" : indirectSum.get("humitureNum").toString();
                    if (humitureNum1.equals(humitureNum2)) {
                        map.put("isPass","1");
                    } else {
                        map.put("isPass","0");
                    }
                    //不涉及
                    if(!humitureNum2.equals("0")&&humitureNum1.equals("0")){
                        map.put("isPass","2");
                    }
                    map.put("pdName","台区温湿度");
                    map.put("pdModel","TmpHum");
                    map.put("sbsl",humitureNum1);//fieldSum -边设备
                    map.put("sjsl",humitureNum2);     //子设备
                    value.add(map);
                }

            } else {
                map.put("devType",objs[0]);
                map.put("esn",objs[1]);
                map.put("pdName",objs[2]);
                map.put("pdModel",objs[3]);
                map.put("isPass",objs[4]);
                value.add(map);
            }


        }
        return value;
    }

    public static void main(String[] args) {
        Map<String,String> map=new HashMap<>();
        map.put("1","2");
        System.out.println(map.get("2"));
        System.out.println(map.containsKey("1"));
    }
    /**
     * 导出表格接口
     * @param request
     * @param response
     * @return
     */
    /*@Override
    public Map<String, Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response) {
        String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss") + ".xls";
        String targetFilePath = request.getSession().getServletContext()
                .getRealPath("download")
                + File.separator + fileName;
        String sheet = "终端验收详情";

        String treeId=request.getParameter("treeid");
        String treeType=request.getParameter("treetype");
        String devName=request.getParameter("name");
        String devLabel=request.getParameter("esn");
        String feederName=request.getParameter("xlmc");
        String trName=request.getParameter("pbmc");
        String isOnline=request.getParameter("isonline");
        String isPass=request.getParameter("status");
        StringBuffer sql =new StringBuffer();
        sql.append("select n2.dev_name,n2.dev_label,n2.is_check,n2.is_check_time||'',n2.is_online,n3.name as trName,\n" +
                "n4.name as feederName,n8.name as sgsName,n7.name as xgsName,n6.name as gdsName,n1.img_url,n1.remark\n" +
                " from d5000.iot_device n2\n" +
                "left join D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO n1  on n1.id=n2.id\n" +
                "left join d5000.dms_tr_device n3 on n2.rely_id=n3.id\n" +
                "left join d5000.dms_feeder_device n4 on n3.feeder_id=n4.id\n" +
                "left join osp.device_auth_manage n5 on n3.id=n5.deviceid\n" +
                "left join osp.isc_baseorg n6 on n5.orgid=n6.id\n" +
                "left join osp.isc_baseorg n7 on n6.parent_id=n7.id\n" +
                "left join osp.isc_baseorg n8 on n7.parent_id=n8.id\n"
        );
        if (treeType.equals("feeder")) {
            sql.append("where n2.is_valid='1'\n" +
                    " and n2.connect_mode='1'" +
                    " and n4.id='"+treeId+"'");
        } else if (treeType.equals("tr")) {
            sql.append("where n2.is_valid='1'\n" +
                    " and n2.connect_mode='1'" +
                    " and n3.id='"+treeId+"'");
        } else {
            sql.append("inner join (select id,name from osp.isc_baseorg start with id='"+treeId+"' connect by prior id=parent_id) n9 on n5.orgid=n9.id\n" +
                    "where n2.is_valid='1'\n" +
                    " and n2.connect_mode='1'");
        }

        String typStr="inner join (select id,name from osp.isc_baseorg start with id='"+treeId+"' connect by prior id=parent_id) n5 on n4.orgid=n5.id\n" +
                "where is_valid='1'\n" +
                "and connect_mode='1'\n" ;
        String name="";
        if (treeType.equals("02")) {
            name="n8.name";
        } else if (treeType.equals("03")) {
            name="n7.name";
        } else if (treeType.equals("04")) {
            name="n6.name";
        } else if (treeType.equals("05")) {
            name="n3.name";
        } else if (treeType.equals("feeder")) {
            name="n2.name";
            typStr="where is_valid='1'\n" +
                    " and connect_mode='1'" +
                    " and n3.id='"+treeId+"'";
        } else if (treeType.equals("tr")) {
            name="n2.name";
            typStr="where is_valid='1'\n" +
                    " and connect_mode='1'" +
                    " and n2.id='"+treeId+"'";
        }
        String comSql="inner join d5000.dms_tr_device n2 on n1.rely_id=n2.id\n" +
                "inner join d5000.dms_feeder_device n3 on n2.feeder_id=n3.id\n" +
                "inner join osp.device_auth_manage n4 on n2.id=n4.deviceid\n" +
                "inner join osp.isc_baseorg n6 on n4.orgid=n6.id\n" +
                "inner join osp.isc_baseorg n7 on n6.parent_id=n7.id\n" +
                "inner join osp.isc_baseorg n8 on n7.parent_id=n8.id\n" +
                typStr;
        String countSql="select sum(allCount),sum(passCount),sum(noPassCount),sum(notCheckCount),name from (\n" +
                "select count(n1.id) as allCount,0 as passCount,0 as noPassCount,0 as notCheckCount,"+name+" as name from d5000.iot_device n1\n" +
                comSql+
                " group by "+name+
                " union all\n" +
                "select 0 as allCount,count(n1.id) as passCount,0 as noPassCount,0 as notCheckCount,"+name+" as name from d5000.iot_device n1\n" +
                comSql+
                "and is_check='1'\n" +
                " group by "+name+
                " union all\n" +
                "select 0 as allCount,0 as passCount,count(n1.id) as noPassCount,0 as notCheckCount,"+name+" as name from d5000.iot_device n1\n" +
                comSql+
                "and is_check='0'\n" +
                " group by "+name+
                " union all\n" +
                "select 0 as allCount,0 as passCount,0 as noPassCount,count(n1.id) as notCheckCount,"+name+" as name from d5000.iot_device n1\n" +
                comSql+
                "and is_check is null\n" +
                " group by "+name+
                ")" +
                " group by name";


        if (devName!=null&&!devName.equals("")) {
            sql.append(" and n2.dev_name like '%"+devName+"%'");
        }
        if (devLabel!=null&&!devLabel.equals("")) {
            sql.append(" and n2.dev_label='"+devLabel+"'");
        }
        if (feederName!=null&&!feederName.equals("")) {
            sql.append(" and n4.name like '%"+feederName+"%'");
        }
        if (trName!=null&&!trName.equals("")) {
            sql.append(" and n3.name like '%"+trName+"%'");
        }
        if (isOnline!=null&&!isOnline.equals("")) {
            if (isOnline.equals("0")){
                sql.append(" and n2.is_online='2'");
            } else {
                sql.append(" and n2.is_online='"+isOnline+"'");
            }
        }
        if (isPass!=null&&!isPass.equals("")) {
            if (isPass.equals("2")) {
                sql.append(" and n2.is_check is null");
            } else {
                sql.append(" and n2.is_check='"+isPass+"'");
            }
        }
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        String titles[]=new String[]{"终端名称","终端标识ESN","验收状态","验收时间","在线状态","关联配变","所属线路","地市公司","县公司","供电所","现场备注"};
        List<Object[]> newlist = new ArrayList<Object[]>();
        for (Object[] temObj : devList) {
            Object[] newObj =new Object[11];
            newObj[0] = temObj[0] == null || temObj[0].equals("") ? "-" : temObj[0];
            newObj[1] = temObj[1] == null || temObj[1].equals("") ? "-" : temObj[1];
            String is_pass="-";
            if (temObj[2]==null) {
                is_pass="未验收";
            } else if (temObj[2].toString().equals("1")) {
                is_pass="验收通过";
            } else {
                is_pass="验收未通过";
            }
            newObj[2] = is_pass;
            newObj[3] = temObj[3] == null || temObj[3].equals("") ? "-" : temObj[3].toString();
            String is_online="-";
            if (temObj[4]==null||temObj[4].toString().equals("3")) {
                is_online="未连接";
            } else if (temObj[4].toString().equals("2")) {
                is_online="离线";
            } else {
                is_online="在线";
            }
            newObj[4] = is_online;
            newObj[5] = temObj[5] == null || temObj[5].equals("") ? "-" : temObj[5];
            newObj[6] = temObj[6] == null || temObj[6].equals("") ? "-" : temObj[6];
            newObj[7] = temObj[7] == null || temObj[7].equals("") ? "-" : temObj[7];
            newObj[8] = temObj[8] == null || temObj[8].equals("") ? "-" : temObj[8];
            newObj[9] = temObj[9] == null || temObj[9].equals("") ? "-" : temObj[9];
            newObj[10] = temObj[11] == null || temObj[11].equals("") ? "-" : temObj[11];
            newlist.add(newObj);
        }

        String sheet2 = "终端验收统计";
        List<Object[]> devList2 = commonInterface.selectListBySql(countSql);
        String titles2[]=new String[]{"组织单位","终端总数","通过数","通过率","未通过数","未通过率","未验收数","未验收率"};
        List<Object[]> newlist2 = new ArrayList<Object[]>();
        DecimalFormat df = new DecimalFormat("#.##");
        double allCountAll=0;
        double passCountAll=0;
        double noPassCountAll=0;
        double noCheckCountAll=0;
        for (Object[] temObj : devList2) {
            Object[] newObj = new Object[8];
            double allCount=temObj[0]==null?0:Double.parseDouble(temObj[0].toString());
            allCountAll+=allCount;
            double passCount=temObj[1]==null?0:Double.parseDouble(temObj[1].toString());
            passCountAll+=passCount;
            double noPassCount=temObj[2]==null?0:Double.parseDouble(temObj[2].toString());
            noPassCountAll+=noPassCount;
            double noCheckCount=temObj[3]==null?0:Double.parseDouble(temObj[3].toString());
            noCheckCountAll+=noCheckCount;
            String passRate = allCount==0?"0%":df.format((passCount / allCount)*100)+"%";
            String noPassRate = allCount==0?"0%":df.format((noPassCount / allCount)*100)+"%";
            String noCheckRate = allCount==0?"0%":df.format((noCheckCount / allCount)*100)+"%";
            newObj[0] =temObj[4];
            newObj[1] =(int)allCount;
            newObj[2] =(int)passCount;
            newObj[3] =passRate;
            newObj[4] =(int)noPassCount;
            newObj[5] =noPassRate;
            newObj[6] =(int)noCheckCount;
            newObj[7] =noCheckRate;
            newlist2.add(newObj);
        }
        //合计
        String passRateAll = allCountAll==0?"0%":df.format((passCountAll / allCountAll)*100)+"%";
        String noPassRateAll = allCountAll==0?"0%":df.format((noPassCountAll / allCountAll)*100)+"%";
        String noCheckRateAll = allCountAll==0?"0%":df.format((noCheckCountAll / allCountAll)*100)+"%";
        Object[] newObj2 = new Object[8];
        newObj2[0] ="合计";
        newObj2[1] =(int)allCountAll;
        newObj2[2] =(int)passCountAll;
        newObj2[3] =passRateAll;
        newObj2[4] =(int)noPassCountAll;
        newObj2[5] =noPassRateAll;
        newObj2[6] =(int)noCheckCountAll;
        newObj2[7] =noCheckRateAll;
        newlist2.add(newObj2);

        try {
            createmsgExcel(targetFilePath,sheet,titles,newlist,sheet2,titles2,newlist2);
        } catch (Exception e) {
            return CommonUtil.returnMap(false, 0, "", false);
        }
        try {
            downloadOnlineExcel(request,response,fileName);
        } catch (Exception e) {
            return CommonUtil.returnMap(false, 0, "", null);
        }
        return CommonUtil.returnMap(true, 0, "", fileName);
    }*/

    /**
     * 下载
     * @param request
     * @param response
     */
    public void downloadOnlineExcel(HttpServletRequest request,HttpServletResponse response,String fileName) {
        String filePath = request.getSession().getServletContext()
                .getRealPath("download")
                + File.separator + fileName;
        downloadFile(request, response, "终端验收详情-" + fileName, filePath);
    }

    /**
     * 生成
     * @param targetFile
     * @param sheet
     * @param titles
     * @param rows
     */
    private void createmsgExcel(String targetFile,
                                String sheet,String[] titles, List<Object[]> rows,
                                String sheet2,String[] titles2, List<Object[]> rows2) {
        WritableWorkbook workbook = null;
        WritableSheet worksheet = null;
        WritableSheet worksheet2 = null;
        Label label = null;
        CommonUtil.checkFile(targetFile);
        OutputStream os = null;
        try {
            os = new FileOutputStream(targetFile);
            workbook = Workbook.createWorkbook(os);
            worksheet = workbook.createSheet(sheet, 0);
            worksheet2 = workbook.createSheet(sheet2, 1);

            for (int i = 0; i < titles.length; i++) {
                label = new Label(i, 0, titles[i]);// 列 行 内容
                worksheet.addCell(label);
            }
            for (int i = 0; i < titles2.length; i++) {
                label = new Label(i, 0, titles2[i]);// 列 行 内容
                worksheet2.addCell(label);
            }
            for (int i = 0; i < rows.size(); i++) {
                for (int j = 0; j < rows.get(i).length; j++) {
                    worksheet.addCell(new Label(j, i + 1, String.valueOf(rows
                            .get(i)[j])));
                }
            }

            for (int i = 0; i < rows2.size(); i++) {
                for (int j = 0; j < rows2.get(i).length; j++) {
                    worksheet2.addCell(new Label(j, i + 1, String.valueOf(rows2
                            .get(i)[j])));
                }
            }
            workbook.write();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载公用方法
     * @param request
     * @param response
     * @param fileName
     * @param filePath
     */
    private void downloadFile(HttpServletRequest request,HttpServletResponse response, String fileName, String filePath) {

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

    @Override
    public Map<String, Object> exportWordReport(HttpServletRequest request, HttpServletResponse response) throws Exception{

        /*String imgUrl="http://ggzj-center.oss-hn-1-a.ops.sgmc.sgcc.com.cn/%E9%85%8D%E7%BD%91%E6%88%91%E6%9D%A5%E4%BF%9D/igwApp_sstda22cf921-0b9c-469a-a62c-8a8f037fe1c9.jpg";
        URL u = new URL(imgUrl);
        System.out.println(u);
        String b1 = CommonUtil.encodeImageToBase64(new URL(imgUrl));*/
        String fileName = exportWordReportService.exportWord(request);
        String filePath = System.getenv("D5000_HOME")+File.separator+"yzzjar"+File.separator+"zdys"+File.separator+"tempFile"
                + File.separator + fileName;
        downloadFile(request, response, "终端验收报告-" + fileName, filePath);
        File file = new File(filePath);
        file.delete();

        return CommonUtil.returnMap(true, 0, "", fileName);
    }


}

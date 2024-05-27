package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.common.HttpUtil;
import com.nari.iot.vendorinfo.common.JDBCUtils;
import com.nari.iot.vendorinfo.controller.OrderProjectController;
import com.nari.iot.vendorinfo.controller.Tbwlb;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.ITerminalProjectService;
import com.nari.iot.vendorinfo.service.OrderProjectService;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFeatures;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service(value = "TerminalProjectService")
@Slf4j
public class TerminalProjectService implements ITerminalProjectService {
    @Autowired
    CommonInterface commonInterface;
    @Autowired
    Tbwlb tbwlb;

    @Autowired
    JdbcTemplate jdbcTemplate = new JdbcTemplate(JDBCUtils.getDataSource());


    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 1、终端厂家信息查询
     *
     * @param request
     * @param mapP
     * @return
     */
    @Override
    public LayJson getListPO(HttpServletRequest request, Map<String, Object> mapP) {
        String workId = mapP.get("workId") == null || mapP.get("workId") == "" ? null : mapP.get("workId").toString();
        String termId = mapP.get("term_id") == null || mapP.get("term_id") == "" ? null : mapP.get("term_id").toString();
        String termEsn = mapP.get("term_esn") == null || mapP.get("term_esn") == "" ? null : mapP.get("term_esn").toString();
        String devType = mapP.get("dev_type") == null || mapP.get("dev_type") == "" ? null : mapP.get("dev_type").toString();
        String termFactory = mapP.get("term_factory") == null || mapP.get("term_factory") == "" ? null : mapP.get("term_factory").toString();
        String endTime = mapP.get("endTime") == null || mapP.get("endTime") == "" ? null : mapP.get("endTime").toString();
        String startTime = mapP.get("startTime") == null || mapP.get("startTime") == "" ? null : mapP.get("startTime").toString();
        String terminalDetectionResult = mapP.get("terminal_detection_result") == null || mapP.get("terminal_detection_result") == "" ? null : mapP.get("terminal_detection_result").toString();
        String terminalDetectionStatus = mapP.get("terminal_detection_status") == null || mapP.get("terminal_detection_status") == "" ? null : mapP.get("terminal_detection_status").toString();
        int pageNo = mapP.get("pageNo") == null || mapP.get("pageNo") == "" ? 1 : Integer.parseInt(mapP.get("pageNo").toString());
        int pageSize = mapP.get("pageSize") == null || mapP.get("pageSize") == "" ? 50 : Integer.parseInt(mapP.get("pageSize").toString());
        String sqlWorkOrder = " select  distinct Apply_for_batch_number as pc  from dms_dispatch_list where work_order_id='" + workId + "'and apply_for_batch_number is not null";
        List<Object[]> devList = commonInterface.selectListBySql(sqlWorkOrder);
        String pc = "";
        if (devList.size() > 0) {
            for (Object[] objs : devList) {
                pc += "'" + (objs[0] == null ? "" : objs[0].toString()) + "',";
            }
            pc = pc.substring(0, pc.length() - 1);
        }
        StringBuffer sql = new StringBuffer();
        StringBuffer sqlCount = new StringBuffer();
        sql.append("select org_nm,term_id,term_esn,dev_type,send_time||'',term_factory, batch_number,terminal_detection_result,terminal_detection_result_time||'', " +
                " terminal_detection_status,terminal_detection_status_time||'',sscp,bhtzid,zzjm  \n" +
                " from DMS_IOT_DEVICE_RESOURCE_INFO  where 1=1  and is_valid=1 ");
        sqlCount.append(" select count(1) from DMS_IOT_DEVICE_RESOURCE_INFO  where 1=1  and is_valid=1 ");
        /*根据条件查询*/
        if (StringUtils.isBlank(pc)) {
            pc = "''";
        }
        if (StringUtils.isNotBlank(workId)) {
            sql.append(" and batch_number in (" + pc + ")");
            sqlCount.append(" and batch_number in (" + pc + ")");
        }
        if (termId != null && !termId.equals("")) {
            sql.append(" and term_id='" + termId + "'\n");
            sqlCount.append("  and term_id='" + termId + "'\n");
        }
        if (termEsn != null && !termEsn.equals("")) {
            sql.append(" and  term_esn= '" + termEsn + "'\n");
            sqlCount.append(" and  term_esn='" + termEsn + "'\n");
        }
        if (devType != null && !devType.equals("")) {
            sql.append(" and dev_type='" + devType + "'\n");
            sqlCount.append(" and dev_type='" + devType + "'\n");
        }
        if (termFactory != null && !termFactory.equals("")) {
            sql.append(" and term_factory like '%" + termFactory + "%' ");
            sqlCount.append(" and term_factory like '%" + termFactory + "%' ");
        }
        if (startTime != null && !startTime.equals("")) {
            sql.append(" and send_time>='" + startTime + "' ");
            sqlCount.append(" and send_time>='" + startTime + "' ");
        }
        if (endTime != null && !endTime.equals("")) {
            sql.append(" and send_time<='" + endTime + "' ");
            sqlCount.append(" and send_time<='" + endTime + "' ");
        }
        if (terminalDetectionResult != null && !terminalDetectionResult.equals("")) {
            sql.append(" and terminal_detection_result='" + terminalDetectionResult + "' ");
            sqlCount.append(" and terminal_detection_result='" + terminalDetectionResult + "' ");
        }

        if (terminalDetectionStatus != null && !terminalDetectionStatus.equals("")) {
            sql.append(" and terminal_detection_status='" + terminalDetectionStatus + "' ");
            sqlCount.append(" and terminal_detection_status='" + terminalDetectionStatus + "' ");
        }
        sql.append("  order by send_time desc  limit " + (pageNo - 1) * pageSize + "," + pageSize);
        System.out.println("终端查询" + sql);
        List<Object[]> devListResult = commonInterface.selectListBySql(sql.toString());
        List<Object[]> devCount = commonInterface.selectListBySql(sqlCount.toString());
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        for (Object[] objs : devListResult) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("org_nm", objs[0] == null ? "-" : objs[0].toString());
            map.put("term_id", objs[1] == null ? "-" : objs[1].toString());
            map.put("term_esn", objs[2] == null ? "-" : objs[2].toString());
            map.put("dev_type", objs[3] == null ? "-" : objs[3].toString());
            map.put("send_time", objs[4] == null ? "-" : objs[4].toString());
            map.put("term_factory", objs[5] == null ? "-" : objs[5].toString());
            map.put("batch_number", objs[6] == null ? "-" : objs[6].toString());
            if (objs[7] != null) {
                if (objs[7].toString().equals("1")) {
                    map.put("terminal_detection_result", "合格");
                }
                if (objs[7].toString().equals("0")) {
                    map.put("terminal_detection_result", "不合格");
                }
            } else {
                map.put("terminal_detection_result", "-");
            }

            map.put("terminal_detection_result_time", objs[8] == null ? "-" : objs[8].toString());


            if (objs[9] != null) {
                if (objs[9].toString().equals("1")) {
                    map.put("terminal_detection_status", "已检测");
                }
                if (objs[9].toString().equals("0")) {
                    map.put("terminal_detection_status", "未检测");
                }
            } else {
                map.put("terminal_detection_status", "-");
            }
            map.put("terminal_detection_status_time", objs[10] == null ? "-" : objs[10].toString());


            map.put("sscp", objs[11] == null ? "-" : objs[11].toString());
            map.put("bhtzid", objs[12] == null ? "-" : objs[12].toString());
            map.put("zzjm", objs[13] == null ? "-" : objs[13].toString());

            value.add(map);
        }
        return new LayJson(200, "请求成功", value, Integer.parseInt(devCount.get(0)[0].toString()));
    }

    //检测线 导入esn检测结果
    @Override
    public LayJson upTerminalCheck(Map<String, Object> map) {
        String terminal_detection_status = map.get("terminalDetectionStatus") == null || map.get("terminalDetectionStatus") == "" ? null : map.get("terminalDetectionStatus").toString();
        String terminal_detection_status_time = map.get("terminalDetectionStatusTime") == null || map.get("terminalDetectionStatusTime") == "" ? null : map.get("terminalDetectionStatusTime").toString();
        String terminal_detection_result = map.get("terminalDetectionResult") == null || map.get("terminalDetectionResult") == "" ? null : map.get("terminalDetectionResult").toString();
        String terminal_detection_result_time = map.get("terminalDetectionResultTime") == null || map.get("terminalDetectionResultTime") == "" ? null : map.get("terminalDetectionResultTime").toString();
        String tm_dqzt = map.get("tmDqzt") == null || map.get("tmDqzt") == "" ? null : map.get("tmDqzt").toString();
        String zzjm = map.get("zzjm") == null || map.get("zzjm") == "" ? null : map.get("zzjm").toString();
        String termId = map.get("termId") == null || map.get("termId") == "" ? null : map.get("termId").toString();


        String sql = " select zzjm   where term_id='"
                + termId + "' and is_valid='1' ";
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        if (StringUtils.isBlank(devList.get(0)[0].toString())) {
            return new LayJson(202, "该终端没有硬加密端口已设置", null, 1);
        }
        String sql2 = " update DMS_IOT_DEVICE_RESOURCE_INFO set  terminal_detection_status='" + terminal_detection_status
                + "', terminal_detection_status_time='" + terminal_detection_status_time + "', terminal_detection_result='" + terminal_detection_result
                + "',terminal_detection_result_time='" + terminal_detection_result_time + "', tm_dqzt='" + tm_dqzt + "',zzjm='" + zzjm + "'  where term_id='"
                + termId + "' and is_valid='1' ";
        Boolean b2 = commonInterface.dbAccess_update(sql2);
        return new LayJson(200, "请求成功", null, 1);

    }

    /**
     * 导入excle
     */

    @Override
    public Map exportImport(MultipartFile file, HttpServletResponse httpServletResponse) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, String>> map = new ArrayList<>();
        boolean kfkproducestart = false;
        ObjectMapper objectMapper = new ObjectMapper();
        List<List<String>> list = new ArrayList<>();
        InputStream inputStream = null;
        int resultCount = 0;
        String xh = "";
        if (file == null) {
            resultMap.put("total", "0");
            resultMap.put("isTrue", false);
            resultMap.put("value", "文件为空");
            return resultMap;
        }
        try {
            inputStream = file.getInputStream();//获取前端传递过来的文件对象，存储在“inputStream”中
            String fileName = file.getOriginalFilename();//获取文件名
            //Workbook workbook =null; //用于存储解析后的Excel文件
            /// 为了解决针对excel 2003 和 excel 2007 的多种格式，使用如下代码，提供了良好的兼容性：
            org.apache.poi.ss.usermodel.Workbook workbook = WorkbookFactory.create(file.getInputStream());

            Sheet sheet; //工作表
            Row row;      //行
            Cell cell;    //单元格

            //循环遍历，获取数据
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheet = workbook.getSheetAt(i);//获取sheet

                if (sheet.getLastRowNum() > 0) {
                    for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {//从有数据的第行开始遍历
                        row = sheet.getRow(j);
                        /*if(row!=null&&row.getFirstCellNum()!=j){ //row.getFirstCellNum()!=j的作用是去除首行，即标题行，如果无标题行可将该条件去掉*/
                        ArrayList tempList = new ArrayList();
                        for (int k = row.getFirstCellNum(); k < row.getLastCellNum(); k++) {//这里需要注意的是getLastCellNum()的返回值为“下标+1”
                            cell = row.getCell(k);
                            if (cell != null) {

                                switch (cell.getCellType()) {
                                    //判断读取的数据中是否有String类型的
                                    case STRING:
                                        //    System.out.println(cell.getStringCellValue());
                                        cell.setCellType(CellType.STRING);
                                        tempList.add(cell.toString());
                                        break;
                                    case NUMERIC:
                        /*
                        判断是否读取到了日期数据：
                        如果是那就进行格式转换，否则会读取的科学计数值
                        不是就输出number 数字
                         */
                                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                                            Date date = cell.getDateCellValue();
                                            //格式转换
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
                                            String format = sdf.format(date);
                                            tempList.add(format);
                                        } else {
                                            cell.setCellType(CellType.STRING);
                                            tempList.add(cell.toString());
                                        }
                                        break;
                                    default:
                                        tempList.add("");
                                        break;
                                }
                            } else {
                                tempList.add("-");
                            }
                            /*}*/
                        }
                        list.add(tempList);
                    }
                }
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            resultMap.put("total", "0");
            resultMap.put("isTrue", false);
            resultMap.put("value", "文件为空");
            return resultMap;

        }
        System.out.println("我是解析的Excel：" + list.toString());

        ArrayList<String> arrayList = new ArrayList<>();
        String cityId = "96b8401074dc974d01752a4d37d6013e";
        String cityName = "湖南电科院";
        List<Map> listWlb = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            Object[] params = new Object[18];
            for (int j = 0; j < list.get(i).size(); j++) {
                params[j] = list.get(i).get(j) != null ? list.get(i).get(j).trim() : "";
            }

            String sql = "";
            sql = " insert into DMS_IOT_DEVICE_RESOURCE_INFO(term_id,term_esn,dev_type,send_time,term_factory,org_nm,is_valid,batch_number,bhtzid )\n" +
                    " values('" + (params[2] == null ? "" : params[2]) + "','" + (params[3] == null ? "" : params[3]) + "', " +
                    " '" + (params[4] == null ? "" : params[4]) + "','" + (params[5] == null ? "" : params[5]) + "','" + (params[6] == null ? "" : params[6]) + "','" + (params[1] == null ? "" : params[1]) + "' ,1,'" + params[7] + "','" + (params[8] == null ? "" : params[8]) + "')";

            try {
                boolean b = false;
                if ((params[7] != null && StringUtils.isNotBlank(params[7].toString()))) {
                    b = commonInterface.dbAccess_insert(sql);
                }
                if (b == true) {
                    //调用我来保接口
                    String sql2 = "select max(id)+1 as maxid from d5000.dms_tr_device ";
                    List<Map<String, Object>> list2 = jdbcTemplate.queryForList(sql2);
                    Long id = Long.parseLong(list2.get(0).get("maxid").toString());
                    String name = params[2] == null ? "" : params[2] + "";
                    String now = sdf.format(new Date());
                    String dev_label = params[3] == null ? "" : params[3] + "";
                    String insertSql = "insert into D5000.DMS_TR_PMS_LINK_INFO" +
                            " (ID,NAME,FEEDER_ID,FEEDER_NAME,CITY_BASE_ORG_ID,CITY_ORG_NM," +
                            " SAVE_TIME,IS_VIRTUAL,IS_VALID )" +
                            " values ('" + id + "','" + name + "','" + "3799912185610633653" + "','" + "融合终端临时馈线" + "','" + cityId + "','" + cityName + "'," +
                            "'" + now + "',1,1 )";
                    String insertSql2 = "insert into d5000.dms_tr_device (id,name,feeder_id) values ('" + id + "','" + name + "','" + "3799912185610633653" + "')";
                    commonInterface.dbAccess_insert(insertSql);
                    jdbcTemplate.update(insertSql2);
                    // 往云主站的  13505 插入   id、名称、馈线
                    commonInterface.dbAccess_insert(insertSql2);
                    try {
                        HashMap maps = new HashMap();
                        maps.put("termId", (params[2] == null ? "" : params[2]));
                        maps.put("termEsn", (params[3] == null ? "" : params[3]));
                        maps.put("devType", (params[4] == null ? "" : params[4]));
                        maps.put("sendTime", (params[5] == null ? "" : params[5]));
                        maps.put("termFactory", (params[6] == null ? "" : params[6]));
                        maps.put("orgNm", (params[1] == null ? "" : params[1]));
                        maps.put("isValid", 1);
                        maps.put("batchNumber", params[7]);
                        maps.put("bhtzId", params[8]);
                        listWlb.add(maps);
                    } catch (Exception e) {
                        log.info(e.toString());
                    }
                    resultCount++;
                } else {
                    xh += params[0] + "、";
                }
            } catch (Exception e) {
                xh += params[0] + "、";
                System.out.println(e);
            }
        }
        //调用我来保接口
        try {
            tbwlb.qjInsertTerminalResourceInfo(listWlb);
        } catch (Exception e) {
            log.info(e.toString());
        }
        if (resultCount == list.size() - 1) {
            resultMap.put("value", "新增成功!");
            resultMap.put("total", resultCount + "");
            resultMap.put("isTrue", true);
            return resultMap;
        } else {
            if (xh.length() > 0) {
                xh = xh.substring(0, xh.length() - 1);
            }
            resultMap.put("value", "新增成功了" + resultCount + "条,其中序号" + xh + "未增加成功");
            resultMap.put("total", resultCount + "");
            resultMap.put("isTrue", true);
            return resultMap;
        }
    }


    @Override
    public Map<String, Object> exportDetectionResultImport(MultipartFile file, HttpServletResponse httpServletResponse) {

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, String>> map = new ArrayList<>();
        boolean kfkproducestart = false;
        ObjectMapper objectMapper = new ObjectMapper();
        List<List<String>> list = new ArrayList<>();
        InputStream inputStream = null;
        int resultCount = 0;


        String xh = "";


        if (file == null) {
            resultMap.put("total", "0");
            resultMap.put("isTrue", false);
            resultMap.put("value", "文件为空");
            return resultMap;
        }
        try {
            inputStream = file.getInputStream();//获取前端传递过来的文件对象，存储在“inputStream”中
            String fileName = file.getOriginalFilename();//获取文件名
            //Workbook workbook =null; //用于存储解析后的Excel文件
            /// 为了解决针对excel 2003 和 excel 2007 的多种格式，使用如下代码，提供了良好的兼容性：
            org.apache.poi.ss.usermodel.Workbook workbook = WorkbookFactory.create(file.getInputStream());

            Sheet sheet; //工作表
            Row row;      //行
            Cell cell;    //单元格

            //循环遍历，获取数据
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheet = workbook.getSheetAt(i);//获取sheet

                if (sheet.getLastRowNum() > 0) {
                    for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {//从有数据的第行开始遍历
                        row = sheet.getRow(j);
                        /*if(row!=null&&row.getFirstCellNum()!=j){ //row.getFirstCellNum()!=j的作用是去除首行，即标题行，如果无标题行可将该条件去掉*/
                        ArrayList tempList = new ArrayList();
                        for (int k = row.getFirstCellNum(); k < row.getLastCellNum(); k++) {//这里需要注意的是getLastCellNum()的返回值为“下标+1”
                            cell = row.getCell(k);
                            if (cell != null) {

                                switch (cell.getCellType()) {
                                    //判断读取的数据中是否有String类型的
                                    case STRING:
                                        //    System.out.println(cell.getStringCellValue());
                                        cell.setCellType(CellType.STRING);
                                        tempList.add(cell.toString());
                                        break;
                                    case NUMERIC:
                        /*
                        判断是否读取到了日期数据：
                        如果是那就进行格式转换，否则会读取的科学计数值
                        不是就输出number 数字
                         */
                                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                                            Date date = cell.getDateCellValue();
                                            //格式转换
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
                                            String format = sdf.format(date);
                                            tempList.add(format);
                                        } else {
                                            cell.setCellType(CellType.STRING);
                                            tempList.add(cell.toString());
                                        }
                                        break;
                                    default:
                                        tempList.add("");
                                        break;
                                }
                            } else {
                                tempList.add("-");
                            }
                            /*}*/
                        }
                        list.add(tempList);
                    }
                }
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            resultMap.put("total", "0");
            resultMap.put("isTrue", false);
            resultMap.put("value", "文件为空");
            return resultMap;

        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String order_time = format.format(new Date());
        String areaId = "43990000";
        String pjId = "4216495151142404112";

        //进行修改
        for (int i = 1; i < list.size(); i++) {
            Object[] params = new Object[18];
            for (int j = 0; j < list.get(i).size(); j++) {
                params[j] = list.get(i).get(j) != null ? list.get(i).get(j).toString() : "";

            }

            //失败的esn
            String tips = "";
            //查询这个终端在不在
            String sql = "select term_esn,term_factory from DMS_IOT_DEVICE_RESOURCE_INFO where term_id='" + params[2] + "'  and is_valid=1";
            List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
            if (devList.get(0).length<=0) {
                //该终端先导入再注册
                continue;
            }
            String sscp = params[13] != null ? params[13].toString() : "";
            String zzjm = params[14] != null ? params[14].toString() : "";
            String tmDqzt = "";
            if (params[9].toString().equals("已检测")) {
                params[9] = 1;
                tmDqzt = "2";
            }
            if (params[9].toString().equals("未检测")) {
                params[9] = 0;
            }

            if (params[11].toString().equals("合格")) {
                params[11] = 1;
            }
            if (params[11].toString().equals("不合格")) {
                params[11] = 0;
            }
            sql = " update DMS_IOT_DEVICE_RESOURCE_INFO set  terminal_detection_status='" + params[9] + "', terminal_detection_status_time='" + params[10] + "', terminal_detection_result='" + params[11] + "',terminal_detection_result_time='" + params[12] + "', tm_dqzt='" + tmDqzt + "',zzjm='" + params[14] + "'  where term_id='" + params[2] + "' and is_valid='1' ";
            log.info("注册1");
            try {
                boolean b = false;
                if (params[7] != null || StringUtils.isNotBlank(params[7].toString())) {

                    //查询后的所属产品
                    String sscpQuery = "";
                    b = commonInterface.dbAccess_update(sql);
                    if (StringUtils.isNotBlank(sscp)) {
                        String sscpSql = " select  wm_concat(pd_name) from d5000.iot_product  where is_valid=1 and out_iot_fac='2' and device_type_name='edgeGateway' and factory_name ='" + devList.get(0)[1].toString() + "'";
                        List<Object[]> sscpList = commonInterface.selectListBySql(sscpSql);
                        sscpQuery = sscpList.get(0)[0] != null ? sscpList.get(0)[0].toString() : "";
                    }

                    log.info("注册2"+sscpQuery+"、对应的所属产品集合为"+sscpQuery);
                    // 检测合格 可以注册  并且 厂家名称对应的上才行
                    if (params[11].toString().equals("1")) {
                        if (StringUtils.isNotBlank(sscp) && StringUtils.isNotBlank(zzjm)) {
                            log.info("注册3"+sscp);
                            if (sscpQuery.contains(sscp)) {
                                try {
                                    commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'9-1','" + JSONObject.toJSONString(params) + "','','" + b + "')");
                                } catch (Exception e) {
                                    log.info("写入日志表的时候 报错了 inLinkOrderNo()  ");
                                }
                                String sszcSql = " select count(1) as gs from iot_device where  is_valid='1' and out_iot_fac='2' and dev_label='"+devList.get(0)[0]+"'  and connect_mode='3' ";
                                log.info("注册4"+sszcSql);
                                List<Object[]> sscpList = commonInterface.selectListBySql(sszcSql);
                                if ((Integer.parseInt(sscpList.get(0)[0].toString()) > 0)) {

                                    //该终端已经注册过
                                    String sql1 = " update DMS_IOT_DEVICE_RESOURCE_INFO  set  tm_dqzt=4 ,zc_time='" + order_time + "' ,zcresult='{\"msg\":\"原来注册过\"}',zcresult1='注册成功',zcsf='1'  where term_id='" + params[2] + "'";
                                    log.info("注册4-2"+sql1);
                                    commonInterface.dbAccess_update(sql1);
                                    continue;
                                } else {

                                    //查找其虚拟配变是否存在，以及是否绑定过了
                                    String sqlUnBding = "select id,name,dev_label  from DMS_TR_PMS_LINK_INFO where name = '" + params[2] + "' and is_valid=1 and IS_VIRTUAL=1";
                                    log.info("注册5"+sqlUnBding);
                                    List<Object[]> devLists = commonInterface.selectListBySql(sqlUnBding);
                                    String sqlNature = "SELECT id,pd_name FROM iot_product  where out_iot_fac='2' and is_valid='1' and pd_mode='0' and pd_name='" + sscp + "' ";
                                    List<Object[]> objectsNature = commonInterface.selectListBySql(sqlNature);
                                    log.info("查询的对应的虚拟配变" + JSONObject.toJSONString(devLists));
                                    if (devLists.get(0)[2] != null) {
                                        //生成的虚拟配变已绑定终端
                                        continue;
                                    }
                                    ArrayList<Map> resultList = new ArrayList();

                                        Map parm = new HashMap();
                                        parm.put("devName", params[2]);
                                        parm.put("devLabel", params[3] + "");//esn编码
                                        parm.put("relyId", devLists.get(0)[0] + ""); //配变id
                                        parm.put("relyName", params[2]);
                                        parm.put("feederId", "3799912185610633653");
                                        parm.put("areaId", areaId);
                                        parm.put("pjId", pjId);
                                        parm.put("pjName", "湖南_全省_配电_融合终端");

                                        if (objectsNature.size() > 0) {
                                            //根据公司名称 、项目性质  来取决所属产品id、name
                                            parm.put("pdId", objectsNature.get(0)[0]);
                                            parm.put("pdName", objectsNature.get(0)[1]);
                                        }
                                        resultList.add(parm);

                                        //注册失败原因
                                        String zcresult = "";
                                        //注册是否成功 1成功，2失败
                                        int zcsf = 0;
                                        //注册返回描述
                                        String tail = "";
                                        if (parm.get("pdName") == null) {
                                            zcsf = 2;
                                            tail = "未找到所属项目";
                                        }

                                        if (parm.get("areaId") == null && !parm.get("areaId").equals("")) {
                                            zcsf = 2;
                                            tail = "未匹配到所属组织";
                                        }

                                        if (parm.get("pdName") != null && parm.get("areaId") != null && !(parm.get("areaId").equals(""))) {
                                            Map resultMaps = getHttpDeviceBatchRegister(resultList);
                                            log.info(resultList + "融合终端注册建档接口结果" + resultMaps);
                                            if (Integer.parseInt(resultMaps.get("code").toString()) != 2000) {
                                                Thread.sleep(2000);
                                                resultMaps = getHttpDeviceBatchRegister(resultList);
                                                log.info(resultList + "融合终端再次注册建档接口结果" + resultMaps);
                                            }
                                            zcresult = JSONObject.toJSONString(resultMaps);
                                            //进行判断
                                            if (zcresult.isEmpty() || Integer.parseInt(resultMaps.get("code").toString()) != 2000) {
                                                zcsf = 2;
                                                tips += params[2] + "、";
                                                log.info("增加了————————————————————");
                                                if (resultMaps.get("code").toString().equals("4000") ||
                                                        resultMaps.get("code").toString().equals("4001") ||
                                                        resultMaps.get("code").toString().equals("4002")
                                                ) {
                                                    tail = "无效产品信息";
                                                } else if (resultMaps.get("code").toString().equals("5001")) {
                                                    //进行多个判断
                                                    List value = (List) (resultMaps.get("value"));
                                                    if (value.get(0) != null) {
                                                        tail = value.get(0).toString();
                                                        if (value.get(0).toString().contains("exit")) {
                                                            tail = "在终端台账表中该设备已存";
                                                        }
                                                    }
                                                }
                                            } else {
                                                zcsf = 1;
                                                tail = "注册成功";

                                            }

                                        }

                                log.info("____________________注册完成开始进行加工了"+zcsf);
                                        //注册成功的 进行加密


                                        if (zcsf == 1) {
                                            String sql1 = " update DMS_IOT_DEVICE_RESOURCE_INFO  set  tm_dqzt=4 ,zc_time='" + order_time + "' ,zcresult='" + zcresult + "',zcresult1='" + tail + "',zcsf='" + zcsf + "',sscp='"+sscp+"'  where term_id='" + params[2] + "'";
                                            commonInterface.dbAccess_update(sql1);
                                            String upDevLable = "update DMS_TR_PMS_LINK_INFO  set dev_label='" + params[3] + "'   where name='" + params[2] + "'";
                                            boolean b1 = commonInterface.dbAccess_update(upDevLable);
                                            log.info("执行结果为-_______"+upDevLable + "____" + b1);
                                            //将新增数据传递我来保
                                            try {

                                                if (!zzjm.equals("无硬加密")) {
                                                    log.info("进行硬加密 调用方俊接口");
                                                    //进行硬加密 调用方俊接口
                                                    HashMap<String, Object> map1 = new HashMap();
                                                    map1.put("devLabel", devList.get(0)[0]);
                                                    map1.put("businessPortNo", zzjm);
                                                    List listjm = new ArrayList();
                                                    listjm.add(map1);
                                                    getBatchSaveBusiness(listjm);
                                                }

                                                log.info("调用我来保接口");
                                                tbwlb.zdzcUpdateIotDeviceInfo(devList.get(0)[0].toString());
                                            } catch (Exception e) {
                                                log.info("终端台账表新增记录更新我来保失败了：" + e.getMessage());
                                            }


                                        } else {
                                            String sql1 = " update DMS_IOT_DEVICE_RESOURCE_INFO  set  zc_time='" + order_time + "' ,zcresult='" + zcresult + "',zcresult1='" + tail + "',zcsf='" + zcsf + "'  where term_esn='" + params[3] + "'";
                                            commonInterface.dbAccess_update(sql1);
                                        }

                                }
                            } else {
                                //厂家下无该所属产品、请联系工程人员

                                continue;
                            }
                        } else {
                            //所属产品不为空
                            continue;
                        }
                    } else {
                        //检测合格的

                        continue;
                    }
                }


                if (b == true) {
                    //调用我来保接口资源库修改接口
                    try {
                        List<Map> listWlb = new ArrayList<>();
                        HashMap maps = new HashMap();
                        maps.put("termEsn", (params[3] == null ? "" : params[3]));
                        maps.put("terminalDetectionResult", (params[10] == null ? "" : params[10]));
                        maps.put("terminalDetectionResultTime", (params[11] == null ? "" : params[11]));
                        maps.put("terminalDetectionStatus", (params[8] == null ? "" : params[8]));
                        maps.put("terminalDetectionStatusTime", (params[9] == null ? "" : params[9]));
                        maps.put("zzjm", (params[13] == null ? "" : params[13]));
                        listWlb.add(maps);
                        tbwlb.qjUpdateDetectionState(listWlb);
                    } catch (Exception e) {
                        log.info(e.toString());
                    }
                    resultCount++;
                } else {
                    xh += params[0] + "、";
                }
            } catch (Exception e) {
                xh += params[0] + "、";
                System.out.println(e);
            }
        }

        String resultValue = "";
        if (resultCount == list.size() - 1) {
            resultMap.put("value", "修改成功!");
            resultMap.put("total", resultCount + "");
            resultMap.put("isTrue", true);
            return resultMap;
        } else {
            if (xh.length() > 0) {
                xh = xh.substring(0, xh.length() - 1);
            }

            resultMap.put("value", "修改成功" + resultCount + "条,其中序号" + xh + "未修改成功  ");
            resultMap.put("total", resultCount + "");
            resultMap.put("isTrue", true);
            return resultMap;
        }
    }


    public Map getBatchSaveBusiness(List<Map> list) {
        String addr = "http://25.212.172.39:23503/v2/iot/lvTerminal/batchSaveBusiness";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        log.info("调用方俊的通道加密接口地址为：" + addr + "   --请求参数为：" + JSONObject.toJSONString(list));
        String ss = HttpUtil.httpPost(addr, JSONObject.toJSONString(list), headers);
        log.info("加密结果为" + ss);
        Map parse = (Map) JSON.parse(ss);
        return parse;
    }

    @Override
    public Map<String, Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response) {
        String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss") + ".xls";
        String targetFilePath = request.getSession().getServletContext()
                .getRealPath("download")
                + File.separator + fileName;
        String sheet = "终端资源库";
        String workId = request.getParameter("workId") == null || request.getParameter("workId") == "" ? null : request.getParameter("workId").toString();
        String termId = request.getParameter("term_id") == null || request.getParameter("term_id") == "" ? null : request.getParameter("term_id").toString();
        String termEsn = request.getParameter("term_esn") == null || request.getParameter("term_esn") == "" ? null : request.getParameter("term_esn").toString();
        String devType = request.getParameter("dev_type") == null || request.getParameter("dev_type") == "" ? null : request.getParameter("dev_type").toString();
        String termFactory = request.getParameter("term_factory") == null || request.getParameter("term_factory") == "" ? null : request.getParameter("term_factory").toString();
        String endTime = request.getParameter("endTime") == null || request.getParameter("endTime") == "" ? null : request.getParameter("endTime").toString();
        String startTime = request.getParameter("startTime") == null || request.getParameter("startTime") == "" ? null : request.getParameter("startTime").toString();
        String terminalDetectionResult = request.getParameter("term_factory") == null || request.getParameter("terminal_detection_result") == "" ? null : request.getParameter("terminal_detection_result").toString();
        String terminalDetectionStatus = request.getParameter("terminal_detection_status") == null || request.getParameter("terminal_detection_status") == "" ? null : request.getParameter("terminal_detection_status").toString();

        String sqlWorkOrder = " select  distinct Apply_for_batch_number as pc  from dms_dispatch_list where work_order_id='" + workId + "'and apply_for_batch_number is not null";
        List<Object[]> devList = commonInterface.selectListBySql(sqlWorkOrder);
        String pc = "";
        if (devList.size() > 0) {
            for (Object[] objs : devList) {
                pc += "'" + (objs[0] == null ? "" : objs[0].toString()) + "',";
            }
            pc = pc.substring(0, pc.length() - 1);
        }

        StringBuffer sql = new StringBuffer();
        sql.append("select org_nm,term_id,term_esn,dev_type,send_time||'',term_factory, batch_number,terminal_detection_result,terminal_detection_result_time||'', " +
                " terminal_detection_status,terminal_detection_status_time||'' ,sscp,bhtzid,zzjm \n" +
                " from DMS_IOT_DEVICE_RESOURCE_INFO  where 1=1  and is_valid=1 ");
        /*根据条件查询*/
        if (StringUtils.isBlank(pc)) {
            pc = "''";
        }
        if (StringUtils.isNotBlank(workId)) {
            sql.append(" and batch_number in (" + pc + ")");
        }
        if (termId != null && !termId.equals("")) {
            sql.append(" and term_id='" + termId + "'\n");
        }
        if (termEsn != null && !termEsn.equals("")) {
            sql.append(" and  term_esn= '" + termEsn + "'\n");
        }
        if (devType != null && !devType.equals("")) {
            sql.append(" and dev_type='" + devType + "'\n");
        }
        if (termFactory != null && !termFactory.equals("")) {
            sql.append(" and term_factory like '%" + termFactory + "%' ");
        }
        if (startTime != null && !startTime.equals("")) {
            sql.append(" and send_time>='" + startTime + "' ");
        }
        if (endTime != null && !endTime.equals("")) {
            sql.append(" and send_time<='" + endTime + "' ");
        }
        if (terminalDetectionResult != null && !terminalDetectionResult.equals("")) {
            sql.append(" and terminal_detection_result='" + terminalDetectionResult + "' ");
        }

        if (terminalDetectionStatus != null && !terminalDetectionStatus.equals("")) {
            sql.append(" and terminal_detection_status='" + terminalDetectionStatus + "' ");
        }

        List<Object[]> devListResult = commonInterface.selectListBySql(sql.toString());

        String titles[] = new String[]{"序号", "客户名称", "终端ID", "核心板ESN", "设备型号", "发货日期", "终端厂家", "发货申请批次号",
                "终端检测结果", "终端检测结果时间", "终端检测状态", "终端检测状态时间", "所属产品", "备货通知ID", "主站硬加密端口"};
        List<Object[]> newlist = new ArrayList<Object[]>();
        int i = 0;
        for (Object[] temObj : devListResult) {
            i++;
            Object[] newObj = new Object[15];
            newObj[0] = i;
            newObj[1] = temObj[0] == null || temObj[0].equals("") ? "-" : temObj[0];
            newObj[2] = temObj[1] == null || temObj[1].equals("") ? "-" : temObj[1];
            newObj[3] = temObj[2] == null || temObj[2].equals("") ? "-" : temObj[2];
            newObj[4] = temObj[3] == null || temObj[3].equals("") ? "-" : temObj[3];
            newObj[5] = temObj[4] == null || temObj[4].equals("") ? "-" : temObj[4];
            newObj[6] = temObj[5] == null || temObj[5].equals("") ? "-" : temObj[5];
            newObj[7] = temObj[6] == null || temObj[6].equals("") ? "-" : temObj[6];
            newObj[8] = temObj[7] == null || temObj[7].equals("") ? "-" : temObj[7];
            if (temObj[7] != null) {
                if (temObj[7].toString().equals("1")) {
                    newObj[8] = "合格";
                }
                if (temObj[7].toString().equals("0")) {
                    newObj[8] = "不合格";
                }
            } else {
                newObj[8] = "不合格";
            }
            newObj[9] = temObj[8] == null || temObj[8].equals("") ? "-" : temObj[8];
            newObj[10] = temObj[9] == null || temObj[9].equals("") ? "-" : temObj[9];

            if (temObj[9] != null) {
                if (temObj[9].toString().equals("1")) {
                    newObj[10] = "已检测";
                }
                if (temObj[9].toString().equals("0")) {
                    newObj[10] = "未检测";
                }
            } else {
                newObj[10] = "-";
            }
            newObj[11] = temObj[10] == null || temObj[10].equals("") ? "-" : temObj[10];
            newObj[12] = temObj[11] == null || temObj[11].equals("") ? "-" : temObj[11];
            newObj[13] = temObj[12] == null || temObj[12].equals("") ? "-" : temObj[12];
            newObj[14] = temObj[13] == null || temObj[13].equals("") ? "-" : temObj[13];
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


    @Override
    public Map<String, Object> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss") + ".xls";
        String targetFilePath = request.getSession().getServletContext()
                .getRealPath("download")
                + File.separator + fileName;
        String sheet = "融合终端资源模板";
        String titles[] = new String[]{"序号", "客户名称", "终端ID", "核心板ESN", "设备型号", "发货日期", "终端厂家", "发货申请批次号", "备货通知ID"};
        List<Object[]> newlist = new ArrayList<Object[]>();
        Object[] newObj = new Object[9];
        newObj[0] = "1"; //序号
        newObj[1] = "湖南省电力公司"; //客户名称
        newObj[2] = "T231202SC201202110270001";//终端ID
        newObj[3] = "1301021260593635"; //核心板ESN
        newObj[4] = "SCT230A"; //设备型号
        newObj[5] = "2021/11/11";//发货日期
        newObj[6] = "北京智芯微电子科技有限公司";//终端厂家
        newObj[7] = "xxxx1";//终端厂家
        newObj[8] = "bhxxxxx1"; //备货通知ID
        newlist.add(newObj);
        try {
            createmsgExcel2(targetFilePath, sheet, titles, newlist);

        } catch (Exception e) {
            return CommonUtil.returnMap(false, 0, "", false);
        }
        try {
            downloadOnlineExcels(request, response, fileName);
        } catch (Exception e) {
            return CommonUtil.returnMap(false, 0, "", null);
        }

        return CommonUtil.returnMap(true, 0, "", fileName);

    }

    @Override
    public Map<String, Object> exportTemplate2(HttpServletRequest request, HttpServletResponse response) {
        String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss") + ".xls";
        String targetFilePath = request.getSession().getServletContext()
                .getRealPath("download")
                + File.separator + fileName;
        String sheet = "终端检测结果模板";
        String titles[] = new String[]{"序号", "客户名称", "终端ID", "核心板ESN", "设备型号", "发货日期", "终端厂家", "发货申请批次号", "备货通知ID",
                "终端检测状态", "终端检测状态时间", "终端检测结果", "终端检测结果时间", "所属产品", "主站硬加密端口"};
        List<Object[]> newlist = new ArrayList<Object[]>();
        Object[] newObj = new Object[15];
        newObj[0] = "1"; //序号
        newObj[1] = "湖南省电力公司"; //客户名称
        newObj[2] = "T231202SC201202110270001";//终端ID
        newObj[3] = "1301021260593635"; //核心板ESN
        newObj[4] = "SCT230A"; //设备型号
        newObj[5] = "2021/11/11";//发货日期
        newObj[6] = "北京智芯微电子科技有限公司";//终端厂家
        newObj[7] = "xxxx1";//终端厂家
        newObj[8] = "bhxxxxx1";//备货通知ID

        newObj[9] = "已检测";//终端检测状态
        newObj[10] = "2022/11/10 11:00:01";//终端检测状态时间
        newObj[11] = "合格";//终端检测结果
        newObj[12] = "2022/11/10 11:00:01";//终端检测结果时间
        newObj[13] = "智芯配电融合终端";//所属产品
        newObj[14] = "2404";//主站硬加密端口
        log.info("来到了资源库模板2导出1");
        newlist.add(newObj);
        try {
            createmsgExcelXLK(targetFilePath, sheet, titles, newlist);
        } catch (Exception e) {
            return CommonUtil.returnMap(false, 0, "", false);
        }
        try {
            downloadOnlineExcels(request, response, fileName);
        } catch (Exception e) {
            return CommonUtil.returnMap(false, 0, "", null);
        }
        return CommonUtil.returnMap(true, 0, "", fileName);
    }

    @Override
    public Map getHttpDeviceBatchRegister(List<Map> list) {
        String addr = "http://25.212.172.39:23503/v2/iot/batch/deviceBatchRegister";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        String ss = HttpUtil.httpPost(addr, JSONObject.toJSONString(list), headers);
        Map parse = (Map) JSON.parse(ss);
        System.out.println("注册接口请求地址：" + addr + "   --请求参数：" + JSONObject.toJSONString(list));
        return parse;
    }


    /**
     * 生成 excle 带下拉框的
     *
     * @param targetFile
     * @param sheet
     * @param titles
     * @param rows
     */
    private void createmsgExcelXLK(String targetFile, String sheet, String[] titles, List<Object[]> rows) {
        WritableWorkbook workbook = null;
        WritableSheet worksheet = null;
        Label label = null;
        Label label2 = null;
        Label label3 = null;
        CommonUtil.checkFile(targetFile);
        OutputStream os = null;
        try {
            os = new FileOutputStream(targetFile);
            workbook = Workbook.createWorkbook(os);
            worksheet = workbook.createSheet(sheet, 0);
            // worksheet.setRowView(0, 400, false); //设置行高
            for (int i = 0; i < titles.length; i++) {
                // worksheet.setRowView(0, 400, false); //设置行高
                label = new Label(i, 0, titles[i]);// 列 行 内容
                worksheet.addCell(label);
            }


            log.info("来到了资源库模板2导出3");

            String[] str = {"合格", "不合格"};
            List<String> list2 = new ArrayList<>(Arrays.asList(str));
            WritableCellFeatures wcf = new WritableCellFeatures();
            wcf.setDataValidationList(list2);
            label = new Label(11, 1, null);
            label.setCellFeatures(wcf);
            worksheet.addCell(label);

            log.info("来到了资源库模板2导出4");
            String[] str3 = {"无硬加密", "2404", "2405", "2406", "2407"};
            List<String> list4 = new ArrayList<>(Arrays.asList(str3));
            WritableCellFeatures wcf3 = new WritableCellFeatures();
            wcf3.setDataValidationList(list4);
            label3 = new Label(14, 1, null);
            label3.setCellFeatures(wcf3);
            worksheet.addCell(label3);
            log.info("来到了资源库模板2导出5");

            log.info("来到了资源库模板2导出6");
            //所属产品
            log.info("创建od对象");
            WritableCellFeatures wcf2 = new WritableCellFeatures();
            wcf2.setDataValidationList(getProduct());
            label2 = new Label(13, 1, null);
            label2.setCellFeatures(wcf2);
            worksheet.addCell(label2);

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
    public List<String> getProduct() {
        String sql = "select id,pd_name from iot_product where is_valid='1' and out_iot_fac='2' and pd_mode='0'";
        List result = new ArrayList();
        List<Object[]> list = commonInterface.selectListBySql(sql);
        if (list.size() > 0) {
            for (Object[] objects : list) {
                result.add(objects[1]);

            }
        }
        return result;
    }

    /**
     * 下载
     *
     * @param request
     * @param response
     */
    public void downloadOnlineExcel(HttpServletRequest request, HttpServletResponse response, String fileName) {
        String filePath = request.getSession().getServletContext()
                .getRealPath("download")
                + File.separator + fileName;
        downloadFile(request, response, "融合终端产品资-" + fileName, filePath);
    }

    public void downloadOnlineExcels(HttpServletRequest request, HttpServletResponse response, String fileName) {
        String filePath = request.getSession().getServletContext()
                .getRealPath("download")
                + File.separator + fileName;
        downloadFile(request, response, "融合终端资源模板-" + fileName, filePath);
    }


    private void createmsgExcel(String targetFile, String sheet,
                                String[] titles, List<Object[]> rows) {
        WritableWorkbook workbook = null;
        WritableSheet worksheet = null;
        Label label = null;
        Label label2 = null;
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

                    worksheet.addCell(new Label(j, m + 1, String.valueOf(rows
                            .get(i)[j])));
                }
                m++;

            }

            workbook.write();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成
     *
     * @param targetFile
     * @param sheet
     * @param titles
     * @param rows
     */
    private void createmsgExcel2(String targetFile, String sheet,
                                 String[] titles, List<Object[]> rows) {
        WritableWorkbook workbook = null;
        WritableSheet worksheet = null;
        Label label = null;
        Label label2 = null;
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

                    worksheet.addCell(new Label(j, m + 1, String.valueOf(rows
                            .get(i)[j])));
                }
                m++;

            }

           /* for (int i = 0; i < titles.length; i++) {
                label = new Label(i, 0, titles[i]);// 列 行 内容
                worksheet.addCell(label);
            }

            for (int i = 0; i < rows.size(); i++) {
                for (int j = 0; j < rows.get(i).length; j++) {
                    worksheet.addCell(new Label(j, i + 1, String.valueOf(rows
                            .get(i)[j])));
                }
            }*/
            workbook.write();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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


}

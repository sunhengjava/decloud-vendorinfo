package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.common.HttpUtil;
import com.nari.iot.vendorinfo.common.JDBCUtils;
import com.nari.iot.vendorinfo.controller.Tbwlb;
import com.nari.iot.vendorinfo.entity.Address;
import com.nari.iot.vendorinfo.entity.LayJson;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service(value = "OrderProjectService")
@Slf4j
public class OrderProjectServiceImpl implements OrderProjectService {

    @Autowired
    CommonInterface commonInterface;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    JdbcTemplate jdbcTemplate = new JdbcTemplate(JDBCUtils.getDataSource());
    @Autowired
    Tbwlb tbwlb;

    @Override
    public LayJson getListPO(Map<String, Object> map) {
        String orgType = map.get("orgType") == null || map.get("orgType") == "" ? null : map.get("orgType").toString();
        String orgId = map.get("orgId") == null || map.get("orgId") == "" ? null : map.get("orgId").toString();
        String project_no = map.get("project_no") == null || map.get("project_no") == "" ? null : map.get("project_no").toString();
        String project_name = map.get("project_name") == null || map.get("project_name") == "" ? null : map.get("project_name").toString();
        String cgddh = map.get("cgddh") == null || map.get("cgddh") == "" ? null : map.get("cgddh").toString();
        String gys_name = map.get("gys_name") == null || map.get("gys_name") == "" ? null : map.get("gys_name").toString();
        String ht_number = map.get("ht_number") == null || map.get("ht_number") == "" ? null : map.get("ht_number").toString();
        String sfglesn = map.get("sfglesn") == null || map.get("sfglesn") == "" ? null : map.get("sfglesn").toString();
        String order_state = map.get("order_state") == null || map.get("order_state") == "" ? "" : map.get("order_state").toString();
        String xmxz = map.get("xmxz") == null || map.get("xmxz") == "" ? null : map.get("xmxz").toString();
        int pageNo = map.get("pageNo") == null || map.get("pageNo") == "" ? 1 : Integer.parseInt(map.get("pageNo").toString());
        int pageSize = map.get("pageSize") == null || map.get("pageSize") == "" ? 50 : Integer.parseInt(map.get("pageSize").toString());
        String deviceType = map.get("deviceType") == null || map.get("deviceType") == "" ? "1" : map.get("deviceType").toString();

        String sql = "";
        String sqlCount = "";
        if (order_state.equals("8") || order_state.equals("7")) {
            sql += "  select  unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    "            cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date||'',handover_date||'',\n" +
                    "                    terminal_consignee,telephone,address,editor,editor_date||'',bind_tg_quntity,  esnSize,order_state,kgrq||'',\n" +
                    "                    jgrq||'',sfglesn,glesntime||'',sum(dis.term_number)as psesnsize ,unit_id ,\n" +
                    "                    az_size ,\n" +
                    "                    jd_size ,\n" +
                    "                    ys_size,\n" +
                    "                    zsyx_size,sscp,zzjm,device_type,sfcq  from (  select unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    " cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date,handover_date,\n" +
                    " terminal_consignee,telephone,address,editor,editor_date,bind_tg_quntity, count(info.term_esn) as esnSize,order_state,kgrq,\n" +
                    " jgrq,sfglesn,glesntime ,unit_id , \n" +
                    " SUM(if(info.tm_dqzt>=8,1,0))as az_size , \n" +
                    " SUM(if(info.tm_dqzt>=8,1,0))as jd_size , \n" +
                    " SUM(if(d.is_check =1,1,0)) AS ys_size, \n" +
                    " SUM(if(info.tm_dqzt=13,1,0))as zsyx_size ,od.sscp,od.zzjm ,od.device_type,od.sfcq \n" +
                    " from dms_tr_project_order    as od \n" +
                    " left join  DMS_IOT_DEVICE_RESOURCE_INFO  as info on info.link_order_no=od.cgddh and info.is_valid=1 \n" +
                    " left join iot_device                 as d\n" +
                    "on\n" +
                    "    d.dev_label   =info.term_esn\n" +
                    "    and d.is_valid    =1     and d.connect_mode=1     and d.out_iot_fac =2  where 1=1  ";

            sqlCount = " select  count(1)  as gs   from  ( " +
                    " select project_no,project_name,terminal_quantity,\n" +
                    " SUM(if(d.is_check =1,1,0)) AS ys_size\n" +
                    " from dms_tr_project_order    as od\n" +
                    " left join  DMS_IOT_DEVICE_RESOURCE_INFO  as info on info.link_order_no=od.cgddh and  info.is_valid=1 \n" +
                    " left join iot_device                 as d\n" +
                    "on\n" +
                    "    d.dev_label   =info.term_esn\n" +
                    "    and d.is_valid    =1\n" +
                    "    and d.connect_mode=1\n" +
                    "    and d.out_iot_fac =2\n" +
                    "  left join dms_termesn_dispatch as dis on dis.order_id=od.cgddh   " +
                    " where 1=1 ";
        } else {
            sql += "select \n" +
                    " unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    " cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date||'',handover_date||'',\n" +
                    " terminal_consignee,telephone,address,editor,editor_date||'',bind_tg_quntity,  esnSize,order_state,kgrq||'',\n" +
                    " jgrq||'',sfglesn,glesntime||'',sum(dis.term_number) as psesnsize,unit_id ,\n" +
                    "az_size ,\n" +
                    "  jd_size ,\n" +
                    " ys_size,\n" +
                    " zsyx_size ,sscp,zzjm ,device_type,sfcq \n" +
                    "  from ( select\n" +
                    " unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    " cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date,handover_date,\n" +
                    " terminal_consignee,telephone,address,editor,editor_date,bind_tg_quntity, count(term_esn)as esnSize,order_state,kgrq,\n" +
                    " jgrq,sfglesn,glesntime,unit_id ,\n" +
                    " SUM(if(info.tm_dqzt>=8,1,0))as az_size ,\n" +
                    " SUM(if(info.tm_dqzt>=9,1,0))as jd_size ,\n" +
                    " SUM(if(d.is_check =1,1,0)) AS ys_size,\n" +
                    " SUM(if(info.tm_dqzt=13,1,0))as zsyx_size,od.sscp,od.zzjm,od.device_type,od.sfcq  \n" +
                    " from dms_tr_project_order    as od\n" +
                    " left join  DMS_IOT_DEVICE_RESOURCE_INFO  as info on info.link_order_no=od.cgddh  and info.is_valid=1 \n" +
                    " left join iot_device                 as d\n" +
                    "on\n" +
                    "    d.dev_label   =info.term_esn\n" +
                    "    and d.is_valid    =1\n" +
                    "    and d.connect_mode=1\n" +
                    "    and d.out_iot_fac =2\n" +
                    " where 1=1 ";
            sqlCount = "select  count(1) " + "         from dms_tr_project_order " + "        where 1=1 ";

        }

        if (!StringUtils.isBlank(deviceType)) {
            sql += " and  od.device_type in (" + deviceType + ") ";
            sqlCount += " and device_type in (" + deviceType + ") ";
        }
        if (!StringUtils.isBlank(project_no)) {
            sql += " and project_no='" + project_no + "' ";
            sqlCount += " and project_no='" + project_no + "' ";
        }
        if (!StringUtils.isBlank(project_name)) {
            sql += " and project_name like '%" + project_name + "%' ";
            sqlCount += " and project_name like'%" + project_name + "%' ";
        }

        if (!StringUtils.isBlank(cgddh)) {
            sql += " and cgddh='" + cgddh + "' ";
            sqlCount += " and cgddh='" + cgddh + "' ";
        }

        if (!StringUtils.isBlank(gys_name)) {
            sql += " and gys_name like '%" + gys_name + "%' ";
            sqlCount += " and gys_name like'%" + gys_name + "%' ";
        }

        if (!StringUtils.isBlank(ht_number)) {
            sql += " and ht_number like '%" + ht_number + "%' ";
            sqlCount += " and ht_number like '%" + ht_number + "%' ";
        }
        if (!StringUtils.isBlank(sfglesn)) {
            sql += " and sfglesn='" + sfglesn + "' ";
            sqlCount += " and sfglesn='" + sfglesn + "' ";
        }
        if (!StringUtils.isBlank(order_state)) {
            if (order_state.equals("8")) {
                sql += " and order_state='7' ";
                sqlCount += " and order_state='7' ";
            } else {
                sql += " and order_state='" + order_state + "' ";
                sqlCount += " and order_state='" + order_state + "' ";
            }
        }
        if (!StringUtils.isBlank(xmxz)) {
            sql += " and xmxz='" + xmxz + "' ";
            sqlCount += " and xmxz='" + xmxz + "' ";
        }
        if (StringUtils.isNotBlank(orgType)) {
            if (orgType.equals("03")) {
                sql += " and ( unit_id='" + orgId + "' or unit_id is null)  ";
                sqlCount += " and ( unit_id='" + orgId + "' or unit_id is null)  ";
            }
            if (orgType.equals("04")) {
                sql += " and  (county_id='" + orgId + "')  ";
                sqlCount += " and  (county_id='" + orgId + "')  ";
            }
            if (orgType.equals("05")) {
                String sqlCounty = "select parent_id from osp.isc_baseorg where id='" + orgId + "'";
                List<Object[]> objectsZz = commonInterface.selectListBySql(sqlCounty);
                if (objectsZz.size() > 0 && objectsZz.get(0) != null) {
                    orgId = objectsZz.get(0)[0].toString();
                }

                //查询对应县公司下的数据
                sql += " and  (county_id='" + orgId + "')  ";
                sqlCount += " and  (county_id='" + orgId + "')  ";
            }
        }
        if (order_state.equals("8") || order_state.equals("7")) {
            sql += "  GROUP BY  unit,county_nm,project_no,project_name,terminal_quantity,   \n" +
                    "      cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date,handover_date,    \n" +
                    "       terminal_consignee,telephone,address,editor,editor_date,bind_tg_quntity,order_state,kgrq,   \n" +
                    "         jgrq,sfglesn,glesntime ,unit_id,od.sscp,od.zzjm , od.device_type,od.sfcq \n" +
                    "         \n" +
                    "         ) as one \n" +
                    "       left join dms_termesn_dispatch as dis on dis.order_id=one.cgddh \n";
            if (order_state.equals("8")) {
                sql += "     where terminal_quantity=ys_size \n";
            } else {
                sql += "     where terminal_quantity!=ys_size \n";
            }
            sql += "     group by \n" +
                    "      unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    "            cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date||'',handover_date||'',\n" +
                    "                    terminal_consignee,telephone,address,editor,editor_date||'',bind_tg_quntity,esnSize,order_state,kgrq||'',\n" +
                    "                    jgrq||'',sfglesn,glesntime||'' ,unit_id ,\n" +
                    "                    az_size ,\n" +
                    "                    jd_size ,\n" +
                    "                    ys_size,\n" +
                    "                    zsyx_size,sscp,zzjm ,device_type,sfcq " +
                    "     limit " + (pageNo - 1) * pageSize + "," + pageSize;
            sqlCount += "  GROUP BY project_no,project_name,terminal_quantity  ";
            if (order_state.equals("8")) {
                sqlCount += "  ) where terminal_quantity=ys_size  ";
            } else {
                sqlCount += "  ) where terminal_quantity!=ys_size  ";
            }
        } else {
            sql += "  GROUP BY unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    "\t cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date,handover_date,\n" +
                    "\t terminal_consignee,telephone,address,editor,editor_date,bind_tg_quntity,order_state,kgrq,\n" +
                    "\t jgrq,sfglesn,glesntime,unit_id ,od.sscp,od.zzjm ) as one \n" +
                    "left join dms_termesn_dispatch as dis on dis.order_id=one.cgddh \n" +
                    "group by unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    " cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date,handover_date,\n" +
                    " terminal_consignee,telephone,address,editor,editor_date,bind_tg_quntity,   esnSize,order_state,kgrq,\n" +
                    " jgrq,sfglesn,glesntime,unit_id ,\n" +
                    " az_size ,\n" +
                    " jd_size ,\n" +
                    "  ys_size,\n" +
                    "zsyx_size,sscp,zzjm,od.device_type,od.sfcq \n" +
                    "order by cgghd_create_date  desc  limit " + (pageNo - 1) * pageSize + "," + pageSize;
        }

        log.info("订单查询————" + sql);
        log.info("订单查询总条数————" + sqlCount);
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        List<Object[]> devCount = commonInterface.selectListBySql(sqlCount);
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        for (Object[] objs : devList) {
            String terminal_quantity = objs[4] == null ? "-" : objs[4].toString();
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("unit", objs[0] == null ? "-" : objs[0].toString());
            hashMap.put("county_nm", objs[1] == null ? "-" : objs[1].toString());
            hashMap.put("project_no", objs[2] == null ? "-" : objs[2].toString());
            hashMap.put("project_name", objs[3] == null ? "-" : objs[3].toString());
            hashMap.put("terminal_quantity", terminal_quantity);
            hashMap.put("cgghd_number", objs[5] == null ? "-" : objs[5].toString());
            hashMap.put("cgddh", objs[6] == null ? "-" : objs[6].toString());
            hashMap.put("gys_number", objs[7] == null ? "-" : objs[7].toString());
            hashMap.put("ht_number", objs[8] == null ? "-" : objs[8].toString());
            hashMap.put("gys_name", objs[9] == null ? "-" : objs[9].toString());
            hashMap.put("xmxz", objs[10] == null ? "-" : objs[10].toString());
            hashMap.put("cgghd_create_date", objs[11] == null ? "-" : objs[11].toString());
            hashMap.put("handover_date", objs[12] == null ? "-" : objs[12].toString());
            hashMap.put("terminal_consignee", objs[13] == null ? "-" : objs[13].toString());
            hashMap.put("telephone", objs[14] == null ? "-" : objs[14].toString());
            hashMap.put("address", objs[15] == null ? "-" : objs[15].toString());
            hashMap.put("editor", objs[16] == null ? "-" : objs[16].toString());
            hashMap.put("editor_date", objs[17] == null ? "-" : objs[17].toString());
            //hashMap.put("bind_tg_quntity", objs[18] == null ? "-" : objs[18].toString());
            hashMap.put("esn_num", objs[19] == null ? "-" : objs[19].toString());
            hashMap.put("order_state", "-");
            if (objs[20] != null) {
                switch (objs[20].toString()) {
                    case "1":
                        hashMap.put("order_state", "已创建");
                        break;
                    case "2":
                        hashMap.put("order_state", "已绑定终端");
                        break;
                    case "3":
                        hashMap.put("order_state", "注册调试");
                        break;
                    case "4":
                        hashMap.put("order_state", "配送完成");
                        break;
                    case "5":
                        hashMap.put("order_state", "收货完成");
                        break;
                    case "6":
                        hashMap.put("order_state", "安装完成");
                        break;
                    case "7":
                        hashMap.put("order_state", "建档完成");
                        break;
                    case "8":
                        hashMap.put("order_state", "验收完成");
                        break;
                    default:
                        hashMap.put("order_state", "-");
                        break;
                }
            }
            hashMap.put("kgrq", objs[21] == null ? "-" : objs[21].toString());
            hashMap.put("jgrq", objs[22] == null ? "-" : objs[22].toString());
            hashMap.put("sfglesn", "-");
            if (objs[23] != null) {
                if (objs[23].toString().equals("0")) {
                    hashMap.put("sfglesn", "未关联");
                }
                if (objs[23].toString().equals("1")) {
                    hashMap.put("sfglesn", "已关联");
                }
            }
            hashMap.put("glesntime", objs[24] == null ? "-" : objs[24].toString());
            hashMap.put("psesncount", objs[25] == null ? "-" : objs[25].toString());
            hashMap.put("unit_id", objs[26] == null ? "-" : objs[26].toString());

            hashMap.put("azzb", objs[27] == null ? "-" : objs[27] + "/" + terminal_quantity);
            hashMap.put("jdzb", objs[28] == null ? "-" : objs[28] + "/" + terminal_quantity);
            hashMap.put("yszb", objs[29] == null ? "-" : objs[29] + "/" + terminal_quantity);
            hashMap.put("yxzb", objs[30] == null ? "-" : objs[30] + "/" + terminal_quantity);
            hashMap.put("sscp", objs[31] == null ? "-" : objs[31].toString());
            hashMap.put("zzjm", objs[32] == null ? "-" : objs[32].toString());
            String deviceTypes=objs[33] == null ? "-" : objs[33].toString();
                    if(deviceTypes.equals("0")){
                        deviceTypes="融合终端";
                    }else if(deviceTypes.equals("1")){
                        deviceTypes="成套设备";
                    } else if (deviceTypes.equals("2")){
                        deviceTypes="综配箱";
                    }else if(deviceTypes.equals("3")){
                        deviceTypes="箱变";
                    }
                    hashMap.put("deviceType", deviceTypes);
            String sfcqs = objs[34] == null ? "-" : objs[34].toString();
            if(sfcqs.equals("0")){
                sfcqs="否";
            }else if(sfcqs.equals("1")){
                sfcqs="是";
            }
            hashMap.put("sfcq",sfcqs);
            value.add(hashMap);
        }
        String s = devCount.get(0)[0] != null ? devCount.get(0)[0].toString() : "0";
        return new LayJson(200, "请求成功", value, Integer.parseInt(s));
    }

    @Override
    public Map<String, Object> exportAllExcel(HttpServletRequest request, HttpServletResponse response) {
        String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss") + ".xls";
        String targetFilePath = request.getSession().getServletContext().getRealPath("download") + File.separator + fileName;
        String sheet = "项目订单查询";
        String orgType = request.getParameter("orgType") == null || request.getParameter("orgType") == "" ? null : request.getParameter("orgType").toString();
        String orgId = request.getParameter("orgId") == null || request.getParameter("orgId") == "" ? null : request.getParameter("orgId").toString();
        String project_no = request.getParameter("project_no") == null || request.getParameter("project_no") == "" ? null : request.getParameter("project_no").toString();
        String project_name = request.getParameter("project_name") == null || request.getParameter("project_name") == "" ? null : request.getParameter("project_name").toString();
        String cgddh = request.getParameter("cgddh") == null || request.getParameter("cgddh") == "" ? null : request.getParameter("cgddh").toString();
        String gys_name = request.getParameter("gys_name") == null || request.getParameter("gys_name") == "" ? null : request.getParameter("gys_name").toString();
        String ht_number = request.getParameter("ht_number") == null || request.getParameter("ht_number") == "" ? null : request.getParameter("ht_number").toString();
        String sfglesn = request.getParameter("sfglesn") == null || request.getParameter("sfglesn") == "" ? null : request.getParameter("sfglesn").toString();
        String order_state = request.getParameter("order_state") == null || request.getParameter("order_state") == "" ? "" : request.getParameter("order_state").toString();
        String xmxz = request.getParameter("xmxz") == null || request.getParameter("xmxz") == "" ? null : request.getParameter("xmxz").toString();
        String deviceType = request.getParameter("deviceType") == null || request.getParameter("deviceType") == "" ? null : request.getParameter("deviceType").toString();


        String sql = "";
        String sqlCount = "";
        if (order_state.equals("8") || order_state.equals("7")) {
            sql += "  select  unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    "            cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date||'',handover_date||'',\n" +
                    "                    terminal_consignee,telephone,address,editor,editor_date||'',bind_tg_quntity,  esnSize,order_state,kgrq||'',\n" +
                    "                    jgrq||'',sfglesn,glesntime||'',sum(dis.term_number)as psesnsize ,unit_id ,\n" +
                    "                    az_size ,\n" +
                    "                    jd_size ,\n" +
                    "                    ys_size,\n" +
                    "                    zsyx_size,sscp,zzjm device_type,sfcq from (  select unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    " cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date,handover_date,\n" +
                    " terminal_consignee,telephone,address,editor,editor_date,bind_tg_quntity, count(info.term_esn) as esnSize,order_state,kgrq,\n" +
                    " jgrq,sfglesn,glesntime ,unit_id , \n" +
                    " SUM(if(info.tm_dqzt>=9,1,0))as az_size , \n" +
                    " SUM(if(info.tm_dqzt>=9,1,0))as jd_size , \n" +
                    " SUM(if(d.is_check =1,1,0)) AS ys_size, \n" +
                    " SUM(if(info.tm_dqzt=13,1,0))as zsyx_size,od.sscp,od.zzjm,od.device_type,od.sfcq \n" +
                    " from dms_tr_project_order    as od \n" +
                    " left join  DMS_IOT_DEVICE_RESOURCE_INFO  as info on info.link_order_no=od.cgddh  and info.is_valid=1 \n" +
                    " left join iot_device                 as d\n" +
                    "on\n" +
                    "    d.dev_label   =info.term_esn\n" +
                    "    and d.is_valid    =1     and d.connect_mode=1     and d.out_iot_fac =2  where 1=1  ";

            sqlCount = " select  count(1)  as gs   from  ( " +
                    " select project_no,project_name,terminal_quantity,\n" +
                    " SUM(if(d.is_check =1,1,0)) AS ys_size\n" +
                    " from dms_tr_project_order    as od\n" +
                    " left join  DMS_IOT_DEVICE_RESOURCE_INFO  as info on info.link_order_no=od.cgddh   and info.is_valid=1 \n" +
                    " left join iot_device                 as d\n" +
                    "on\n" +
                    "    d.dev_label   =info.term_esn\n" +
                    "    and d.is_valid    =1\n" +
                    "    and d.connect_mode=1\n" +
                    "    and d.out_iot_fac =2\n" +
                    "  left join dms_termesn_dispatch as dis on dis.order_id=od.cgddh   " +
                    " where 1=1 ";
        } else {
            sql += "select \n" +
                    " unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    " cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date||'',handover_date||'',\n" +
                    " terminal_consignee,telephone,address,editor,editor_date||'',bind_tg_quntity,  esnSize,order_state,kgrq||'',\n" +
                    " jgrq||'',sfglesn,glesntime||'',sum(dis.term_number) as psesnsize,unit_id ,\n" +
                    "az_size ,\n" +
                    "  jd_size ,\n" +
                    " ys_size,\n" +
                    " zsyx_size,sscp,zzjm,device_type,sfcq \n" +
                    "  from ( select\n" +
                    " unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    " cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date,handover_date,\n" +
                    " terminal_consignee,telephone,address,editor,editor_date,bind_tg_quntity, count(term_esn)as esnSize,order_state,kgrq,\n" +
                    " jgrq,sfglesn,glesntime,unit_id ,\n" +
                    " SUM(if(info.tm_dqzt>=8,1,0))as az_size ,\n" +
                    " SUM(if(info.tm_dqzt>=9,1,0))as jd_size ,\n" +
                    " SUM(if(d.is_check =1,1,0)) AS ys_size,\n" +
                    " SUM(if(info.tm_dqzt=13,1,0))as zsyx_size,od.sscp,od.zzjm \n" +
                    " from dms_tr_project_order    as od\n" +
                    " left join  DMS_IOT_DEVICE_RESOURCE_INFO  as info on info.link_order_no=od.cgddh and info.is_valid=1 \n" +
                    " left join iot_device                 as d\n" +
                    "on\n" +
                    "    d.dev_label   =info.term_esn\n" +
                    "    and d.is_valid    =1\n" +
                    "    and d.connect_mode=1\n" +
                    "    and d.out_iot_fac =2\n" +
                    " where 1=1 ";
            sqlCount = "select  count(1) " + "         from dms_tr_project_order " + "        where 1=1 ";
        }

        if (!StringUtils.isBlank(deviceType)) {
            sql += " and  od.device_type in (" + deviceType + ") ";
            sqlCount += " and device_type in (" + deviceType + ") ";
        }
        if (!StringUtils.isBlank(project_no)) {
            sql += " and project_no='" + project_no + "' ";
            sqlCount += " and project_no='" + project_no + "' ";
        }
        if (!StringUtils.isBlank(project_name)) {
            sql += " and project_name like '%" + project_name + "%' ";
            sqlCount += " and project_name like'%" + project_name + "%' ";
        }

        if (!StringUtils.isBlank(cgddh)) {
            sql += " and cgddh='" + cgddh + "' ";
            sqlCount += " and cgddh='" + cgddh + "' ";
        }

        if (!StringUtils.isBlank(gys_name)) {
            sql += " and gys_name like '%" + gys_name + "%' ";
            sqlCount += " and gys_name like'%" + gys_name + "%' ";
        }

        if (!StringUtils.isBlank(ht_number)) {
            sql += " and ht_number like  '%" + ht_number + "%' ";
            sqlCount += " and ht_number like '%" + ht_number + "%' ";
        }
        if (!StringUtils.isBlank(sfglesn)) {
            sql += " and sfglesn='" + sfglesn + "' ";
            sqlCount += " and sfglesn='" + sfglesn + "' ";
        }

        if (!StringUtils.isBlank(order_state)) {
            if (order_state.equals("8")) {
                sql += " and order_state='7' ";
                sqlCount += " and order_state='7' ";
            } else {
                sql += " and order_state='" + order_state + "' ";
                sqlCount += " and order_state='" + order_state + "' ";
            }
        }

        if (!StringUtils.isBlank(xmxz)) {
            sql += " and xmxz='" + xmxz + "' ";
            sqlCount += " and xmxz='" + xmxz + "' ";
        }
        if (StringUtils.isNotBlank(orgType)) {
            if (orgType.equals("03")) {
                sql += " and ( unit_id='" + orgId + "' or unit_id is null)  ";
                sqlCount += " and ( unit_id='" + orgId + "' or unit_id is null)  ";
            }
            if (orgType.equals("04")) {
                sql += " and  (county_id='" + orgId + "' or county_id is null)  ";
                sqlCount += " and  (county_id='" + orgId + "' or county_id is null)  ";
            }
            if (orgType.equals("05")) {
                String sqlCounty = "select parent_id from osp.isc_baseorg where id='" + orgId + "'";
                List<Object[]> objectsZz = commonInterface.selectListBySql(sqlCounty);
                if (objectsZz.size() > 0 && objectsZz.get(0) != null) {
                    orgId = objectsZz.get(0)[0].toString();
                }

                //查询对应县公司下的数据
                sql += " and  (county_id='" + orgId + "')  ";
                sqlCount += " and  (county_id='" + orgId + "')  ";
            }
        }
        if (order_state.equals("8") || order_state.equals("7")) {
            sql += "  GROUP BY  unit,county_nm,project_no,project_name,terminal_quantity,   \n" +
                    "      cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date,handover_date,    \n" +
                    "       terminal_consignee,telephone,address,editor,editor_date,bind_tg_quntity,order_state,kgrq,   \n" +
                    "         jgrq,sfglesn,glesntime ,unit_id,od.sscp,od.zzjm ,od.device_type,od.sfcq\n" +
                    "         \n" +
                    "         ) as one \n" +
                    "       left join dms_termesn_dispatch as dis on dis.order_id=one.cgddh \n";
            if (order_state.equals("8")) {
                sql += "     where terminal_quantity=ys_size \n";
            } else {
                sql += "     where terminal_quantity!=ys_size \n";
            }
            sql += "     group by \n" +
                    "      unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    "            cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date||'',handover_date||'',\n" +
                    "                    terminal_consignee,telephone,address,editor,editor_date||'',bind_tg_quntity,esnSize,order_state,kgrq||'',\n" +
                    "                    jgrq||'',sfglesn,glesntime||'' ,unit_id ,\n" +
                    "                    az_size ,\n" +
                    "                    jd_size ,\n" +
                    "                    ys_size,\n" +
                    "                    zsyx_size ,sscp,zzjm,device_type,sfcq";
            sqlCount += "  GROUP BY project_no,project_name,terminal_quantity  ";
            if (order_state.equals("8")) {
                sqlCount += "  ) where terminal_quantity=ys_size  ";
            } else {
                sqlCount += "  ) where terminal_quantity!=ys_size  ";
            }

        } else {
            sql += "  GROUP BY unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    "\t cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date,handover_date,\n" +
                    "\t terminal_consignee,telephone,address,editor,editor_date,bind_tg_quntity,order_state,kgrq,\n" +
                    "\t jgrq,sfglesn,glesntime,unit_id ,od.sscp,od.zzjm ) as one \n" +
                    "left join dms_termesn_dispatch as dis on dis.order_id=one.cgddh \n" +
                    "group by unit,county_nm,project_no,project_name,terminal_quantity,\n" +
                    " cgghd_number,cgddh,gys_number,ht_number,gys_name,xmxz ,cgghd_create_date,handover_date,\n" +
                    " terminal_consignee,telephone,address,editor,editor_date,bind_tg_quntity,   esnSize,order_state,kgrq,\n" +
                    " jgrq,sfglesn,glesntime,unit_id ,\n" +
                    " az_size ,\n" +
                    " jd_size ,\n" +
                    "  ys_size,\n" +
                    "zsyx_size ,sscp,zzjm,device_type,sfcq \n" +
                    "order by cgghd_create_date  desc ";
        }

        System.out.println("订单查询————" + sql);
        System.out.println("订单查询总条数————" + sqlCount);
        List<Object[]> devList = commonInterface.selectListBySql(sql);

        String titles[] = new String[]{"序号", "需求单位", "区县公司", "项目编号", "项目名称", "订单内融合终端数量", "采购供货单编号", //7
                "采购订单号", "供应商编号", "协议库存标识符", "供应商名称", "项目性质", "采购供货单生成日期", "交接日期", "终端收货人"    //8
                , "联系电话", "收货地址", "编辑人", "编辑时间", "终端ESN数量", "订单状态", "开工日期", "竣工日期", "订单关联ESN状态"  //9
                , "订单关联ESN状态时间", "发货ESN数量", "安装进度", "建档进度", "上线验收进度", "正式运行进度", "所属产品", "主站硬加密"}; //6
        List<Object[]> newlist = new ArrayList<Object[]>();
        int i = 0;
        for (Object[] temObj : devList) {
            i++;
            Object[] newObj = new Object[32];
            newObj[0] = i;
            newObj[1] = temObj[0] == null || temObj[0].equals("") ? "-" : temObj[0];
            newObj[2] = temObj[1] == null || temObj[1].equals("") ? "-" : temObj[1];
            newObj[3] = temObj[2] == null || temObj[2].equals("") ? "-" : temObj[2];
            newObj[4] = temObj[3] == null || temObj[3].equals("") ? "-" : temObj[3];

            String deviceTypes=temObj[33] == null ? "-" : temObj[33].toString();
            if(deviceTypes.equals("0")){
                deviceTypes="融合终端";
            }else if(deviceTypes.equals("1")){
                deviceTypes="成套设备";
            } else if (deviceTypes.equals("2")){
                deviceTypes="综配箱";
            }else if(deviceTypes.equals("3")){
                deviceTypes="箱变";
            }
            newObj[5] = deviceTypes;
            newObj[6] = temObj[4] == null || temObj[4].equals("") ? "-" : temObj[4];
            newObj[7] = temObj[5] == null || temObj[5].equals("") ? "-" : temObj[5];

            newObj[8] = temObj[6] == null || temObj[6].equals("") ? "-" : temObj[6];
            newObj[9] = temObj[7] == null || temObj[7].equals("") ? "-" : temObj[7];
            newObj[10] = temObj[8] == null || temObj[8].equals("") ? "-" : temObj[8];
            newObj[11] = temObj[9] == null || temObj[9].equals("") ? "-" : temObj[9];
            newObj[12] = temObj[10] == null || temObj[10].equals("") ? "-" : temObj[10];
            newObj[13] = temObj[11] == null || temObj[11].equals("") ? "-" : temObj[11];
            newObj[14] = temObj[12] == null || temObj[12].equals("") ? "-" : temObj[12];
            newObj[15] = temObj[13] == null || temObj[13].equals("") ? "-" : temObj[13];

            newObj[16] = temObj[14] == null || temObj[14].equals("") ? "-" : temObj[14];
            newObj[17] = temObj[15] == null || temObj[15].equals("") ? "-" : temObj[15];
            newObj[18] = temObj[16] == null || temObj[16].equals("") ? "-" : temObj[16];
            newObj[19] = temObj[17] == null || temObj[17].equals("") ? "-" : temObj[17];
            newObj[20] = temObj[19] == null || temObj[19].equals("") ? "-" : temObj[19];
            // 绑定台区数不需要了
            //    newObj[20] = temObj[19] == null || temObj[19].equals("") ? "-" : temObj[19];
            if (temObj[21] != null) {
                switch (temObj[21].toString()) {
                    case "1":
                        newObj[21] = "已创建";
                        break;
                    case "2":
                        newObj[21] = "已绑定终端";
                        break;
                    case "3":
                        newObj[21] = "注册调试";
                        break;
                    case "4":
                        newObj[21] = "配送完成";
                        break;
                    case "5":
                        newObj[21] = "收货完成";
                        break;
                    case "6":
                        newObj[21] = "安装完成";
                        break;
                    case "7":
                        newObj[21] = "建档完成";
                        break;
                    case "8":
                        newObj[21] = "验收完成";
                        break;
                    default:
                        newObj[21] = "已创建";
                        break;
                }
            } else {
                newObj[21] = "-";
            }


            newObj[22] = temObj[21] == null || temObj[21].equals("") ? "-" : temObj[21];
            newObj[23] = temObj[22] == null || temObj[22].equals("") ? "-" : temObj[22];
            //newObj[23] = temObj[23] == null || temObj[23].equals("") ? "-" : temObj[23];
            if (temObj[24] != null) {
                if (temObj[24].toString().equals("0")) {
                    newObj[24] = "未关联";
                }
                if (temObj[24].toString().equals("1")) {
                    newObj[24] = "已关联";
                }
            }

            newObj[25] = temObj[24] == null || temObj[24].equals("") ? "-" : temObj[24];

            newObj[26] = temObj[25] == null || temObj[25].equals("") ? "-" : temObj[25];
            String terminal_quantity = temObj[4] == null ? "-" : temObj[4].toString();

            newObj[27] = temObj[27] == null ? "-" : temObj[27] + "/" + terminal_quantity;
            newObj[28] = temObj[28] == null ? "-" : temObj[28] + "/" + terminal_quantity;
            newObj[29] = temObj[29] == null ? "-" : temObj[29] + "/" + terminal_quantity;
            newObj[30] = temObj[30] == null ? "-" : temObj[30] + "/" + terminal_quantity;

            newObj[31] = temObj[31] == null || temObj[31].equals("") ? "-" : temObj[31];
            newObj[32] = temObj[32] == null || temObj[32].equals("") ? "-" : temObj[32];
            String sfcqs = temObj[34] == null ? "-" : temObj[34].toString();
            if(sfcqs.equals("0")){
                sfcqs="否";
            }else if(sfcqs.equals("1")){
                sfcqs="是";
            }
            newObj[33] = sfcqs;
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
    public Map<String, Object> addPO(HttpServletRequest request, Map<String, Object> mapP) {
        Map<String, Object> resultMap = new HashMap<>();
        String now = sdf.format(new Date());
        String tswlb = mapP.get("tswlb") == null || mapP.get("tswlb") == "" ? "1" : mapP.get("tswlb").toString();
        String unit = mapP.get("unit") == null || mapP.get("unit") == "" ? "" : mapP.get("unit").toString();
        String project_no = mapP.get("project_no") == null || mapP.get("project_no") == "" ? "" : mapP.get("project_no").toString();
        String project_name = mapP.get("project_name") == null || mapP.get("project_name") == "" ? "" : mapP.get("project_name").toString();
        String terminal_quantitys = mapP.get("terminal_quantity") == null || mapP.get("terminal_quantity") == "" ? "" : mapP.get("terminal_quantity").toString();
        int terminal_quantity = 0;
        if (StringUtils.isNotBlank(terminal_quantitys)) {
            terminal_quantity = Integer.parseInt(terminal_quantitys);
        }
        String cgghd_number = mapP.get("cgghd_number") == null || mapP.get("cgghd_number") == "" ? "" : mapP.get("cgghd_number").toString();
        String cgddh = mapP.get("cgddh") == null || mapP.get("cgddh") == "" ? "" : mapP.get("cgddh").toString();
        String gys_number = mapP.get("gys_number") == null || mapP.get("gys_number") == "" ? "" : mapP.get("gys_number").toString();
        String ht_number = mapP.get("ht_number") == null || mapP.get("ht_number") == "" ? "" : mapP.get("ht_number").toString();
        String gys_name = mapP.get("gys_name") == null || mapP.get("gys_name") == "" ? "" : mapP.get("gys_name").toString();
        String cgghd_create_date = mapP.get("cgghd_create_date") == null || mapP.get("cgghd_create_date") == "" ? "" : mapP.get("cgghd_create_date").toString();
        String handover_date = mapP.get("handover_date") == null || mapP.get("handover_date") == "" ? "" : mapP.get("handover_date").toString();
        String editor = mapP.get("editor") == null || mapP.get("editor") == "" ? "" : mapP.get("editor").toString();
        String county_id = mapP.get("county_id") == null || mapP.get("county_id") == "" ? "" : mapP.get("county_id").toString();
        String county_nm = mapP.get("county_nm") == null || mapP.get("county_nm") == "" ? "" : mapP.get("county_nm").toString();
        String unit_id = mapP.get("unit_id") == null || mapP.get("unit_id") == "" ? "" : mapP.get("unit_id").toString();
        String xmxz = mapP.get("xmxz") == null || mapP.get("xmxz") == "" ? "" : mapP.get("xmxz").toString();
        String jgrq = mapP.get("jgrq") == null || mapP.get("jgrq") == "" ? "" : mapP.get("jgrq").toString();
        String kgrq = mapP.get("kgrq") == null || mapP.get("kgrq") == "" ? "" : mapP.get("kgrq").toString();
        //招标批次
        String zbpc = mapP.get("zbpc") == null || mapP.get("zbpc") == "" ? "" : mapP.get("zbpc").toString();
        //技术id
        String jsId = mapP.get("jsId") == null || mapP.get("jsId") == "" ? "" : mapP.get("jsId").toString();
        //设备类型 （0融合终端、1成套设备、2综配箱3、箱变）
        String deviceType = mapP.get("deviceType") == null || mapP.get("deviceType") == "" ? "" : mapP.get("deviceType").toString();
            if(StringUtils.isBlank(deviceType))deviceType="1";
        if (StringUtils.isBlank(unit) ||
                StringUtils.isBlank(county_nm) ||
                StringUtils.isBlank(gys_number) ||
                StringUtils.isBlank(project_no) ||
                StringUtils.isBlank(terminal_quantitys) ||
                StringUtils.isBlank(ht_number) ||
                StringUtils.isBlank(project_name) ||
                StringUtils.isBlank(cgddh) ||
                StringUtils.isBlank(cgghd_number) ||
                StringUtils.isBlank(gys_name) ||
                StringUtils.isBlank(xmxz) ||

                StringUtils.isBlank(cgghd_create_date) ||
                StringUtils.isBlank(handover_date) ||
                StringUtils.isBlank(kgrq) ||
                StringUtils.isBlank(jgrq)

        ) {
            resultMap.put("value", "必填字段不许为空！");
            resultMap.put("total", "0");
            resultMap.put("isTrue", false);
        }
        String telephone = mapP.get("telephone") == null || mapP.get("telephone") == "" ? "" : mapP.get("telephone").toString();
        String address = mapP.get("address") == null || mapP.get("address") == "" ? "" : mapP.get("address").toString();
        String terminal_consignee = mapP.get("terminal_consignee") == null || mapP.get("terminal_consignee") == "" ? "" : mapP.get("terminal_consignee").toString();

        //获取esn总数
        String sqlEsnCount = "select term_esn from DMS_IOT_DEVICE_RESOURCE_INFO where link_order_no='" + cgddh + "'  and is_valid=1 ";
        List<Object[]> objectsZz = commonInterface.selectListBySql(sqlEsnCount);
        int esn_num = 0;
        if (objectsZz.size() > 0) {
            //创建几个终端
            Object[] objects1 = objectsZz.get(0);
            esn_num = objectsZz.size();
        }
        String pjId = "";
        String areaId = "";
        String city = unit;
        String city_id = "297ebd676610090d01661013d8a00008";
        if (city.lastIndexOf("常德") != -1) {
            city_id = "2c948a856614994b016614fce96d00fd";
            areaId = "43070000";
            pjId = "4216495151142404110";
            city = "国网湖南省电力有限公司常德供电分公司";
        }
        if (city.lastIndexOf("郴州") != -1) {
            city_id = "2c948a856614994b016615859ba602dc";
            areaId = "43100000";
            pjId = "4216495151142404098";
            city = "国网湖南省电力有限公司郴州供电分公司";
        }
        if (city.lastIndexOf("衡阳") != -1) {
            city_id = "2c948a856614994b0166153e43420203";
            areaId = "43040000";
            pjId = "4216495151142404099";
            city = "国网湖南省电力有限公司衡阳供电分公司";
        }
        if (city.lastIndexOf("怀化") != -1) {
            city_id = "2c948a856614994b0166158caaf40343";
            areaId = "43120000";
            pjId = "4216495151142404107";
            city = "国网湖南省电力有限公司怀化供电分公司";
        }
        if (city.lastIndexOf("娄底") != -1) {
            city_id = "2c948a856614994b0166150cf57c0159";
            areaId = "43130000";
            pjId = "4216495151142404106";
            city = "国网湖南省电力有限公司娄底供电分公司";
        }
        if (city.lastIndexOf("邵阳") != -1) {
            city_id = "2c948a856614994b0166151e8fc30191";
            areaId = "43050000";
            pjId = "4216495151142404108";
            city = "国网湖南省电力有限公司邵阳供电分公司";
        }
        if (city.lastIndexOf("湘潭") != -1) {
            city_id = "2c948a856614994b016614cbed6c0047";
            areaId = "43030000";
            pjId = "4216495151142404102";
            city = "国网湖南省电力有限公司湘潭供电分公司";
        }
        if (city.lastIndexOf("湘西") != -1) {
            city_id = "2c948a856614994b0166159fa4b50447";
            areaId = "43310000";
            pjId = "4216495151142404105";
            city = "国网湖南省电力有限公司湘西供电分公司";
        }
        if (city.lastIndexOf("益阳") != -1) {
            city_id = "2c948a856614994b016614eb5e8c00b9";
            areaId = "43090000";
            pjId = "4216495151142404104";
            city = "国网湖南省电力有限公司益阳供电分公司";
        }
        if (city.lastIndexOf("岳阳") != -1) {
            city_id = "2c948a856614994b016614d9c95f0066";
            areaId = "43060000";
            pjId = "4216495151142404109";
            city = "国网湖南省电力有限公司岳阳供电分公司";
        }
        if (city.lastIndexOf("张家界") != -1) {
            city_id = "2c948a856614994b016615993aa30407";
            areaId = "43080000";
            pjId = "4216495151142404101";
            city = "国网湖南省电力有限公司张家界供电分公司";
        }
        if (city.lastIndexOf("长沙") != -1) {
            city_id = "297ebd676610090d016610144d4b0009";
            areaId = "43010000";
            pjId = "4216495151142404097";
            city = "国网湖南省电力有限公司长沙供电分公司";
        }
        if (city.lastIndexOf("株洲") != -1) {
            city_id = "2c948a856614994b016615a445690477";
            areaId = "43020000";
            pjId = "4216495151142404100";
            city = "国网湖南省电力有限公司株洲供电分公司";
        }
        if (city.lastIndexOf("永州") != -1) {
            city_id = "2c948a856614994b016615486fc0024f";
            areaId = "43110000";
            pjId = "4216495151142404103";
            city = "国网湖南省电力有限公司永州供电分公司";
        }
        if (city.lastIndexOf("国网湖南省电力有限公司物资公司") != -1 || city_id.lastIndexOf("电科院") != -1 || areaId.equals("")) {
            city_id = "96b8401074dc974d01752a4d37d6013e";
            areaId = "43990000";
            pjId = "4216495151142404112";
            city = "国网湖南省电力有限公司物资公司";
        }
        String sql = " insert into d5000.dms_tr_project_order (unit,project_no,project_name,terminal_quantity,\n"
                + " cgghd_number,cgddh,gys_number,ht_number,gys_name,cgghd_create_date,handover_date,\n"
                + " terminal_consignee,telephone,address ,editor,editor_date,bind_tg_quntity,unit_id,county_id,county_nm,esn_num,xmxz,kgrq,jgrq ,zbpc,jsid,device_type "
                + " )\n" + " values ('" + city + "','" + project_no + "','" + project_name + "'," + terminal_quantity + ",\n"
                + " '" + cgghd_number + "','" + cgddh + "','" + gys_number + "','" + ht_number + "', '" + gys_name + "','"
                + cgghd_create_date + "','" + handover_date + "',\n"
                + " '" + terminal_consignee + "','" + telephone + "','" + address + "','"
                + editor + "','" + now + "','0','" + unit_id + "','" + county_id + "','" + county_nm
                + "','" + esn_num + "' , '" + xmxz + "','" + kgrq + "','" + jgrq + "','"+zbpc+"','"+jsId+"','"+deviceType+"')";
        try {
            log.info("订单新增sql" + sql);
            boolean b1 = commonInterface.dbAccess_insert(sql);

            if (b1) {
                //调用我来保接口
                HashMap map = new HashMap();
                map.put("unit", unit);
                map.put("projectNo", project_no);
                map.put("projectName", project_name);
                map.put("terminalQuantity", terminal_quantity);
                map.put("cgghdNumber", cgghd_number);
                map.put("cgddh", cgddh);
                map.put("gysNumber", gys_number);
                map.put("htNumber", ht_number);
                map.put("gysName", gys_name);
                map.put("cgghdCreateDate", cgghd_create_date);
                map.put("handoverDate", handover_date);
                map.put("terminalConsignee", terminal_consignee);
                map.put("telephone", telephone);
                map.put("address", address);
                map.put("editor", editor);
                map.put("editorDate", now);
                map.put("bindTgQuntity", 0);
                map.put("unitId", unit_id);
                map.put("countyId", county_id);
                map.put("countyNm", county_nm);
                map.put("esnNum", esn_num);
                map.put("jgrq", jgrq);
                map.put("kgrq", kgrq);
                map.put("xmxz", xmxz);
                map.put("zbpc", zbpc);
                map.put("jsid", jsId);
                map.put("devicetype",deviceType);
                try {
                    log.info("新增订单时、调用我来保新增订单接口————————————————————");
                    if(tswlb.equals("1")){
                        //订单新增
                        tbwlb.orderAddContractOrder(map);
                    }
                    log.info("调用我来保新增订单接口————————————————————");
                } catch (Exception e) {
                    log.info(e.getMessage());
                }

            /**
             *  生成虚拟配配变流程
             * String sql2 = "select max(id)+1 as maxid from d5000.dms_tr_device ";
                List<Map<String, Object>> list2 = jdbcTemplate.queryForList(sql2);
                Long id = Long.parseLong(list2.get(0).get("maxid").toString());

                for (int j = 0; j < terminal_quantity; j++) {
                    id = ++id;
                    String name = cgddh + "_" + terminal_quantity + "_" + (j + 1);
                    String dev_label = "";
                    if (j < objectsZz.size()) {
                        dev_label = objectsZz.get(j)[0].toString();
                    }
                    String insertSql = "insert into D5000.DMS_TR_PMS_LINK_INFO" + " (ID,NAME,FEEDER_ID,FEEDER_NAME,CITY_BASE_ORG_ID,CITY_ORG_NM," + " SAVE_TIME,IS_VIRTUAL,IS_VALID,TR_PROJECT_NO,DEV_LABEL )"
                            + " values ('" + id + "','" + name + "','" + "3799912185610633653" + "','" + "融合终端临时馈线" + "','" + city_id + "','" + city + "'," + "'" + now + "',1,1,'" + cgddh + "', '" + dev_label + "' )";
                    String insertSql2 = "insert into d5000.dms_tr_device (id,name,feeder_id) values ('" + id + "','" + name + "','" + "3799912185610633653" + "')";

                    commonInterface.dbAccess_insert(insertSql);
                    jdbcTemplate.update(insertSql2);
                    // 往云主站的  13505 插入   id、名称、馈线
                    commonInterface.dbAccess_insert(insertSql2);
                    //进行组合 (dev_label不为空的就是组合过的) 调用注册建档接口
                }*/

                String sqlSum = "select sum(esn_num) as  esn_num_sum,sum(bind_tg_quntity)as bind_tg_quntity_sum ,count(1) as gs,sum(terminal_quantity) as terminal_quantity   from DMS_TR_PROJECT_ORDER where ht_number='" + ht_number + "'";
                List<Object[]> devList = commonInterface.selectListBySql(sqlSum.toString());
                String esn_num_sum = "0";
                String bind_tg_quntity_sum = "0";
                String gs = "0";

                //合同下订单内融合终端总和
                String termSize = "0";
                if (devList != null && devList.get(0) != null && !devList.get(0).equals("")) {
                    Object[] objects = devList.get(0);
                    esn_num_sum = objects[0] != null ? objects[0].toString() : "0";
                    bind_tg_quntity_sum = objects[1] != null ? objects[1].toString() : "0";
                    gs = objects[2] != null ? objects[2].toString() : "0";
                    termSize = objects[3] != null ? objects[3].toString() : "0";
                    sql = "update DMS_IOT_HT_INFO set esn_num='" + esn_num_sum + "',link_tr_num='" + bind_tg_quntity_sum + "',order_num='" + gs + "' , order_ft_count='" + termSize + "' where ht_no='" + ht_number + "' and sb_device_type='"+deviceType+"'";
                }
                boolean b = commonInterface.dbAccess_insert(sql);
                try {
                    //我来保接口-合同修改
                    tbwlb.htUpdateContract(ht_number);
                } catch (Exception e) {
                    log.info("调用我来保合同修改接口报错了" + ht_number);
                }
                resultMap.put("value", "推送成功!");
                resultMap.put("total", "0");
                resultMap.put("isTrue", true);
                return resultMap;

            } else {
                resultMap.put("value", "新增失败,调用我来保接口超时!");
                resultMap.put("total", "0");
                resultMap.put("isTrue", false);
                return resultMap;
            }

        } catch (Exception e) {
            System.out.println("走到了报错里面");
            log.info(String.valueOf(e));
            resultMap.put("value", "新增失败，该订单号已存在");
            resultMap.put("total", "0");
            resultMap.put("isTrue", false);
            return resultMap;
        }

    }


    @Override
    public Map<String, Object> exportImport(MultipartFile file, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, String>> map = new ArrayList<>();
        boolean kfkproducestart = false;
        ObjectMapper objectMapper = new ObjectMapper();
        List<List<String>> list = new ArrayList<>();
        InputStream inputStream = null;
        //成功的条数
        int resultCount = 0;
        //失败的序号
        String xh = "";
        //为空不允许进行导入的序号
        String byxXh = "";
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
                for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {//从有数据的第行开始遍历
                    System.out.println("getFirstRowNum" + sheet.getFirstRowNum() + "---");
                    row = sheet.getRow(j);
                    //  if(row!=null&&row.getFirstCellNum()!=j){ //row.getFirstCellNum()!=j的作用是去除首行，即标题行，如果无标题行可将该条件去掉*/
                    // if(row!=null){ //row.getFirstCellNum()!=j的作用是去除首行，即标题行，如果无标题行可将该条件去掉*/
                    ArrayList tempList = new ArrayList();
                    for (int k = row.getFirstCellNum(); k < row.getLastCellNum(); k++) {//这里需要注意的是getLastCellNum()的返回值为“下标+1”
                        cell = row.getCell(k);
                        if (cell != null) {
                            if (cell.toString().equals("")) {
                                tempList.clear();
                                break;
                            }
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
                            }
                        } else {
                            tempList.add("-");
                            if (k == 6) {
                                tempList.clear();
                                break;
                            }

                        }

                    }

                    list.add(tempList);
                }
                //}
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            resultMap.put("total", "0");
            resultMap.put("isTrue", false);
            resultMap.put("value", "文件为空");
            return resultMap;
        }
        System.out.println(list.size() + "我是解析的Excel：" + list.toString());
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            Object[] params = new Object[18];
            boolean sf = true;
            for (int j = 0; j < list.get(i).size(); j++) {
                params[j] = list.get(i).get(j) != null ? list.get(i).get(j).toString() : "";
                log.info("输出当前" + params[j]);
                if (list.get(i).get(j) == null || list.get(i).get(j).toString().equals("") || list.get(i).get(j).toString().equals("-")) {
                    byxXh += params[0] + "、";
                    sf = false;
                }
            }
            //如果为空则不进行添加
            if (sf == false) {
                continue;
            }

            String pjId = "";
            String areaId = "";
            String city = params[1] == null ? "" : params[1].toString();
            String city_id = "297ebd676610090d01661013d8a00008";
            if (city.lastIndexOf("常德") != -1) {
                city_id = "2c948a856614994b016614fce96d00fd";
                areaId = "43070000";
                pjId = "4216495151142404110";
            }
            if (city.lastIndexOf("郴州") != -1) {
                city_id = "2c948a856614994b016615859ba602dc";
                areaId = "43100000";
                pjId = "4216495151142404098";
            }
            if (city.lastIndexOf("衡阳") != -1) {
                city_id = "2c948a856614994b0166153e43420203";
                areaId = "43040000";
                pjId = "4216495151142404099";
            }
            if (city.lastIndexOf("怀化") != -1) {
                city_id = "2c948a856614994b0166158caaf40343";
                areaId = "43120000";
                pjId = "4216495151142404107";
            }
            if (city.lastIndexOf("娄底") != -1) {
                city_id = "2c948a856614994b0166150cf57c0159";
                areaId = "43130000";
                pjId = "4216495151142404106";
            }
            if (city.lastIndexOf("邵阳") != -1) {
                city_id = "2c948a856614994b0166151e8fc30191";
                areaId = "43050000";
                pjId = "4216495151142404108";
            }
            if (city.lastIndexOf("湘潭") != -1) {
                city_id = "2c948a856614994b016614cbed6c0047";
                areaId = "43030000";
                pjId = "4216495151142404102";
            }
            if (city.lastIndexOf("湘西") != -1) {
                city_id = "2c948a856614994b0166159fa4b50447";
                areaId = "43310000";
                pjId = "4216495151142404105";
            }
            if (city.lastIndexOf("益阳") != -1) {
                city_id = "2c948a856614994b016614eb5e8c00b9";
                areaId = "43090000";
                pjId = "4216495151142404104";
            }
            if (city.lastIndexOf("岳阳") != -1) {
                city_id = "2c948a856614994b016614d9c95f0066";
                areaId = "43060000";
                pjId = "4216495151142404109";
            }
            if (city.lastIndexOf("张家界") != -1) {
                city_id = "2c948a856614994b016615993aa30407";
                areaId = "43080000";
                pjId = "4216495151142404101";
            }
            if (city.lastIndexOf("长沙") != -1) {
                city_id = "297ebd676610090d016610144d4b0009";
                areaId = "43010000";
                pjId = "4216495151142404097";
            }
            if (city.lastIndexOf("株洲") != -1) {
                city_id = "2c948a856614994b016615a445690477";
                areaId = "43020000";
                pjId = "4216495151142404100";
            }
            if (city.lastIndexOf("永州") != -1) {
                city_id = "2c948a856614994b016615486fc0024f";
                areaId = "43110000";
                pjId = "4216495151142404103";
            }
            if (city.lastIndexOf("国网湖南省电力有限公司物资公司") != -1 || city_id.lastIndexOf("电科院") != -1 || areaId.equals("")) {
                city_id = "96b8401074dc974d01752a4d37d6013e";
                areaId = "43990000";
                pjId = "4216495151142404112";
                city = "国网湖南省电力有限公司物资公司";
            }
            String sqlEsnCount = "select term_esn from DMS_IOT_DEVICE_RESOURCE_INFO where link_order_no='" + (params[6] == null ? "" : params[6]) + "' and  is_valid=1  order by term_esn";
            List<Object[]> objectsZz = commonInterface.selectListBySql(sqlEsnCount);
            int esn_num = 0;
            if (objectsZz.size() > 0) {
                esn_num = objectsZz.size();
            }
            String sql = "";
            sql = " insert into d5000.dms_tr_project_order (unit,project_no,project_name,terminal_quantity,\n"
                    + " cgghd_number,cgddh,gys_number,ht_number,gys_name,cgghd_create_date,handover_date,\n"
                    + " bind_tg_quntity,unit_id, kgrq,jgrq, xmxz,esn_num ) " + " values ('" + city + "','"
                    + (params[2] == null ? "" : params[2]) + "','" + (params[3] == null ? 0 : params[3]) + "',"
                    + (params[4] == null || params[4].equals("") ? 0 : params[4]) + ",\n" + " '" + (params[5] == null ? "" : params[5])
                    + "','" + (params[6] == null ? "" : params[6]) + "','" + (params[7] == null ? "" : params[7]) + "','"
                    + (params[8] == null ? "" : params[8]) + "', '" + (params[9] == null ? 0 : params[9]) + "','"
                    + (params[11] == null ? "" : params[11]) + "','" + (params[12] == null ? "" : params[12]) + "',\n" + "  "
                    + (params[4] == null || params[4].equals("") ? 0 : params[4]) + ", '" + city_id + "'  ,'"
                    + (params[13] == null ? "" : params[13]) + "', '" + (params[14] == null ? "" : params[14]) + "', '"
                    + (params[10] == null ? "" : params[10]) + "'," + esn_num + " ) ";

            try {
                //插入订单表
                boolean b = commonInterface.dbAccess_update(sql);
                log.info("新增订单成功");
                if (b == true) {
                    //调用我来保接口
                    HashMap maps = new HashMap();
                    maps.put("unit", city);
                    maps.put("projectNo", (params[2] == null ? "" : params[2]));
                    maps.put("projectName", (params[3] == null ? 0 : params[3]));
                    maps.put("terminalQuantity", (params[4] == null || params[4].equals("") ? 0 : params[4]));
                    maps.put("cgghdNumber", (params[5] == null ? "" : params[5]));
                    maps.put("cgddh", (params[6] == null ? "" : params[6]));
                    maps.put("gysNumber", (params[7] == null ? "" : params[7]));
                    maps.put("htNumber", (params[8] == null ? "" : params[8]));
                    maps.put("gysName", (params[9] == null ? 0 : params[9]));
                    maps.put("cgghdCreateDate", (params[11] == null ? "" : params[11]));
                    maps.put("handoverDate", (params[12] == null ? "" : params[12]));
                    maps.put("bindTgQuntity", (params[4] == null || params[4].equals("") ? 0 : params[4]));
                    maps.put("unitId", city_id);
                    maps.put("jgrq", (params[13] == null ? "" : params[13]));
                    maps.put("kgrq", (params[14] == null ? "" : params[14]));
                    maps.put("xmxz", (params[10] == null ? "" : params[10]));
                    maps.put("esnNum", esn_num);
                    try {
                        //调用我来保订单新增接口
                        tbwlb.orderAddContractOrder(maps);
                    } catch (Exception e) {
                        log.info(e.getMessage());
                    }
                    resultCount++;
                    //更新合同表
                    String sqlSum = "select sum(esn_num) as  esn_num_sum,sum(bind_tg_quntity)as bind_tg_quntity_sum ,count(1) as gs,sum(terminal_quantity)as terminal_quantity  from DMS_TR_PROJECT_ORDER where ht_number='" + (params[8] == null ? "" : params[8]) + "'";
                    List<Object[]> devList = commonInterface.selectListBySql(sqlSum.toString());
                    String esn_num_sum = "0";
                    String bind_tg_quntity_sum = "0";
                    String gs = "0";
                    //合同下订单内融合终端总和
                    String termSize = "0";
                    if (devList != null && devList.get(0) != null && !devList.get(0).equals("")) {
                        Object[] objects = devList.get(0);
                        esn_num_sum = objects[0] != null ? objects[0].toString() : "0";
                        bind_tg_quntity_sum = objects[1] != null ? objects[1].toString() : "0";
                        gs = objects[2] != null ? objects[2].toString() : "0";
                        termSize = objects[3] != null ? objects[3].toString() : "0";
                        sql = "update DMS_IOT_HT_INFO set esn_num='" + esn_num_sum + "',link_tr_num='" + bind_tg_quntity_sum + "',order_num='" + gs + "'  , order_ft_count='" + termSize + "' where ht_no='" + (params[8] == null ? "" : params[8]) + "'";
                        commonInterface.dbAccess_insert(sql);
                        try {
                            //合同修改-订单数量发生改变修合同下订单数
                            tbwlb.htUpdateContract((params[8] == null ? "" : params[8].toString()));
                        } catch (Exception e) {
                            log.info(e.getMessage());
                        }
                    }

                } else {
                    xh += params[0] + "、";
                }
            } catch (Exception e) {
                xh += params[0] + "、";
                System.out.println(e);
            }
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
            if (byxXh.length() > 0) {
                byxXh = byxXh.substring(0, byxXh.length() - 1);
                byxXh = "序号：" + byxXh + "，必填字段请填写完整";
            }
            if (resultCount > 0) {
                resultMap.put("value", "新增成功了" + resultCount + "条,其中序号" + xh + "未增加成功；" + byxXh);
                resultMap.put("total", resultCount + "");
                resultMap.put("isTrue", true);
            } else {
                resultMap.put("value", "导入数据无法识别，请把数据转换成文本格式");
                resultMap.put("total", resultCount + "");
                resultMap.put("isTrue", false);
                if (byxXh.length() > 0) {
                    resultMap.put("value", byxXh);
                }
            }

            return resultMap;
        }
    }

    @Override
    public Map<String, Object> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss") + ".xls";
        String targetFilePath = request.getSession().getServletContext().getRealPath("download") + File.separator + fileName;
        String sheet = "项目订单模板";

        String titles[] = new String[]{"序号", "需求单位", "项目编号", "项目名称", "订单内融合终端数量", "采购供货单编号", "采购订单号", "供应商编号", "协议库存标识符(合同编号)", "供应商名称", "项目性质", "采购供货单生成日期", "交接日期", "项目计划开工日期", "项目计划竣工日期"};
        List<Object[]> newlist = new ArrayList<Object[]>();


        Object[] newObj = new Object[15];
        newObj[0] = "1"; //需求单位
        newObj[1] = "国网湖南省电力有限公司长沙供电分公司"; //需求单位
        newObj[2] = "MODELXXX1";
        newObj[3] = "湖南35KV输变电工程"; //项目名称
        newObj[4] = "10"; //订单内融合终端数量
        newObj[5] = "SO164500456736";
        newObj[6] = "4500456736";//采购订单号
        newObj[7] = "1000151920";//供应商编号
        newObj[8] = "HN202000003822";//协议库存标识符

        newObj[9] = "北京智芯微电子科技有限公司";//供应商名称
        newObj[10] = "协议库存";//项目性质
        newObj[11] = "2022-03-04 00:00:00"; //采购供货单生成日期
        newObj[12] = "2022-03-07 00:00:00";
        newObj[13] = "2022-06-07 00:00:00";
        newObj[14] = "2022-12-17 00:00:00";
        newlist.add(newObj);
        try {
            createmsgExcelXLK(targetFilePath, sheet, titles, newlist);

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
//        Label label4 = null;
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
            String[] str = {"协议库存", "融资租赁", "业扩工程", "技改升级", "其他"};
            List<String> list2 = new ArrayList<>(Arrays.asList(str));
            WritableCellFeatures wcf = new WritableCellFeatures();
            wcf.setDataValidationList(list2);
            label = new Label(10, 1, null);
            label.setCellFeatures(wcf);
            worksheet.addCell(label);


//
//            String[] str4 = {"国网湖南省电力有限公司长沙供电分公司", "国网湖南省电力有限公司株洲供电分公司","国网湖南省电力有限公司湘潭供电分公司","国网湖南省电力有限公司衡阳供电分公司","国网湖南省电力有限公司常德供电分公司","国网湖南省电力有限公司岳阳供电分公司","国网湖南省电力有限公司娄底供电分公司","国网湖南省电力有限公司益阳供电分公司","国网湖南省电力有限公司邵阳供电分公司","国网湖南省电力有限公司郴州供电分公司","国网湖南省电力有限公司永州供电分公司","国网湖南省电力有限公司怀化供电分公司","国网湖南省电力有限公司湘西供电分公司","国网湖南省电力有限公司张家界供电分公司","国网湖南省电力有限公司物资公司"};
//            List<String> list5 = new ArrayList<>(Arrays.asList(str4));
//            WritableCellFeatures wcf4 = new WritableCellFeatures();
//            wcf4.setDataValidationList(list5);
//            label4 = new Label(1, 1, null);
//            label4.setCellFeatures(wcf4);
//            worksheet.addCell(label4);

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

    /*查询所属产品列表*/
    public List<String> getProduct() {
        String sql = "select id,pd_name from iot_product where is_valid='1' and out_iot_fac='2' and pd_mode='0'";
        List result = new ArrayList();
        log.info("来到了获取所属产品接口2"+sql);
        List<Object[]> list = commonInterface.selectListBySql(sql);
        log.info("数据为"+JSONObject.toJSONString(list)+"size为"+list.size());
        if (list.size() > 0) {
            for (Object[] objects : list) {
                result.add(objects[1]);

            }
        }
        return result;
    }

    @Override
    public LayJson getOrderZtPo(Map<String, Object> map) {
        String cgddh = map.get("cgddh") == null || map.get("cgddh") == "" ? null : map.get("cgddh").toString();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ///    String current_state_time = simpleDateFormat.format(new Date());
        String sql = "select order_state,max(cgghd_create_date||'') as cgghd_create_date ,max(glesntime||'')  as glesntime, \t \n" +
                "dis.term_dispatch_id,shipper,dis.shipper_phone,dis.recipitnt,dis.recipitnt_phone,\n" +
                "max(dis.ps_time||'') as ps_time, max(qs_time||'')  as qs_time,max(order_az_time||'')  as order_az_time," +
                "max(order_jd_time||'')  as order_jd_time,min(d.is_check||''),max(d.is_check_time||'')  as is_check_time ,count(1) as ensgs ," +
                " (pms_city_company_name ||'-'||pms_county_company_name) as dz from DMS_TR_PROJECT_ORDER as od\n" +
                " left join  dms_termesn_dispatch dis on dis.order_id=od.cgddh\n" +
                "   left join  DMS_IOT_DEVICE_RESOURCE_INFO  as info on info.link_order_no=od.cgddh   and info.is_valid=1 \n" +
                " left join iot_device                 as d\n" +
                "on\n" +
                "    d.dev_label   =info.term_esn\n" +
                "    and d.is_valid    =1\n" +
                "    and d.connect_mode=1\n" +
                "    and d.out_iot_fac =2\n" +
                " where cgddh='" + cgddh + "'\n" +
                " group  by order_state,dis.term_dispatch_id,shipper,dis.shipper_phone,dis.recipitnt,dis.recipitnt_phone, (pms_city_company_name ||'-'||pms_county_company_name) " +
                " ";
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        log.info("getOrderZtPo查询sql" + sql);
        String order_state = "1";
        String cgddhCreateDate = "";
        String glesntime = "";
        String term_dispatch_id = "";
        String shipper = "";
        String shipper_phone = "";
        String recipitnt = "";
        String recipitnt_phone = "";
        String ps_time = "";
        String qs_time = "";
        String order_az_time = "";
        String order_jd_time = "";
        String is_check = "";
        String is_check_time = "";
        String esngs = "";
        String useraddress = "";
        if (devList != null) {
            order_state = devList.get(0)[0] != null ? devList.get(0)[0].toString() : "0";
            cgddhCreateDate = devList.get(0)[1] != null ? devList.get(0)[1].toString() : "";
            glesntime = devList.get(0)[2] != null ? devList.get(0)[2].toString() : "";
            term_dispatch_id = devList.get(0)[3] != null ? devList.get(0)[3].toString() : "";
            shipper = devList.get(0)[4] != null ? devList.get(0)[4].toString() : "";
            shipper_phone = devList.get(0)[5] != null ? devList.get(0)[5].toString() : "";
            recipitnt = devList.get(0)[6] != null ? devList.get(0)[6].toString() : "";
            recipitnt_phone = devList.get(0)[7] != null ? devList.get(0)[7].toString() : "";
            ps_time = devList.get(0)[8] != null ? devList.get(0)[8].toString() : "";
            qs_time = devList.get(0)[9] != null ? devList.get(0)[9].toString() : "";
            order_az_time = devList.get(0)[10] != null ? devList.get(0)[10].toString() : "";
            order_jd_time = devList.get(0)[11] != null ? devList.get(0)[11].toString() : "";
            is_check = devList.get(0)[12] != null ? devList.get(0)[12].toString() : "";
            is_check_time = devList.get(0)[13] != null ? devList.get(0)[13].toString() : "";
            esngs = devList.get(0)[14] != null ? devList.get(0)[14].toString() : "";
            useraddress = devList.get(0)[15] != null ? devList.get(0)[15].toString() : "";
        }
        int orderState = Integer.parseInt(order_state);
        List<HashMap> resultList = new ArrayList<>();
        HashMap linkedHashMap2 = new HashMap();
        linkedHashMap2.put("key", "已绑定终端");
        linkedHashMap2.put("value", "");

        HashMap linkedHashMap3 = new HashMap();
        linkedHashMap3.put("key", "注册调试");
        linkedHashMap3.put("value", "");
        HashMap linkedHashMap6 = new HashMap();
        linkedHashMap6.put("key", "安装完成");
        linkedHashMap6.put("value", "");
        HashMap linkedHashMap7 = new HashMap();
        linkedHashMap7.put("key", "建档完成");
        linkedHashMap7.put("value", "");
        HashMap linkedHashMap8 = new HashMap();
        linkedHashMap8.put("key", "验收完成");
        linkedHashMap8.put("value", "");
        if (orderState >= 1) {
            HashMap linkedHashMap1 = new HashMap();
            linkedHashMap1.put("key", "已创建");
            linkedHashMap1.put("value", "采购供货单生成创建时间：" + cgddhCreateDate + " ");
            resultList.add(linkedHashMap1);
            if (orderState >= 2) {
                linkedHashMap2.put("value", "绑定时间:" + glesntime + ",\t \t 绑定终端数：" + esngs + " 个");
                resultList.add(linkedHashMap2);
                if (orderState >= 3) {
                    linkedHashMap3.put("value", "注册调试时间:" + glesntime + ", 注册数量 " + esngs + "个");
                    resultList.add(linkedHashMap3);

                    for(Object[] li:devList){
                        term_dispatch_id = "";
                        shipper =  "";
                        shipper_phone = "";
                        recipitnt ="";
                        recipitnt_phone ="";
                        ps_time =  "";
                        qs_time ="";
                        System.out.println("进来循环了"+JSONObject.toJSONString(devList));
                        term_dispatch_id = li[3] != null ? li[3].toString() : "";
                        shipper = li[4] != null ? li[4].toString() : "";
                        shipper_phone = li[5] != null ? li[5].toString() : "";
                        recipitnt = li[6] != null ? li[6].toString() : "";
                        recipitnt_phone = li[7] != null ? li[7].toString() : "";
                        ps_time = li[8] != null ? li[8].toString() : "";
                        qs_time = li[9] != null ? li[9].toString() : "";
                        HashMap linkedHashMap4 = new HashMap();
                        linkedHashMap4.put("key", "配送完成");
                        linkedHashMap4.put("value", "");

                        HashMap linkedHashMap5 = new HashMap();
                        linkedHashMap5.put("key", "收货完成");
                        linkedHashMap5.put("value", "");
                        if (orderState >= 4) {
                            linkedHashMap4.put("value", " 配送单号：" + term_dispatch_id + " ,  \t \t 配送时间：" + ps_time + "  , \t \t 配送发起人：" + shipper + " , \t \t   配送人联系方式：" + shipper_phone + "  ,收货地址: "+useraddress +"  ");
                            resultList.add(linkedHashMap4);
                        }
                        if (orderState >= 5) {
                            linkedHashMap5.put("value", " 配送单号: "+term_dispatch_id+" ,  接收人:" + recipitnt + "  ,   \t \t接收人联系方式:" + recipitnt_phone + "  \t \t  收货时间：" + qs_time + "  ");
                            resultList.add(linkedHashMap5);
                        }
                    }
                    if (orderState >= 6) {
                        linkedHashMap6.put("value", "安装完成时间:" + order_az_time);
                        resultList.add(linkedHashMap6);
                        if (orderState >= 7) {
                            linkedHashMap7.put("value", "建档完成时间:" + order_jd_time + " ");
                            resultList.add(linkedHashMap7);
                            if (is_check.equals("1")) {
                                linkedHashMap8.put("value", "验收时间:" + is_check_time + ",    验收结果：成功");
                                resultList.add(linkedHashMap8);
                            } else {
                                linkedHashMap8.put("value", "验收时间:" + is_check_time + ",    验收结果：失败");
                                resultList.add(linkedHashMap8);
                            }
                        }
                    }


                }
            }
        } else {
            HashMap linkedHashMap1 = new HashMap();
            linkedHashMap1.put("key", "已创建");
            linkedHashMap1.put("value", "该订单状态异常 ");
        }
        return new LayJson(200, "请求成功", resultList, resultList.size());
    }

    @Override
    public LayJson getOrderOneEsnPo(Map<String, Object> map) {
        String cgddh = map.get("cgddh") == null || map.get("cgddh") == "" ? null : map.get("cgddh").toString();
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        if (StringUtils.isBlank(cgddh)) {
            return new LayJson(200, "请求成功", value, value.size());
        }
        String sql = " select info.TERM_ESN,dis.term_dispatch_id,max(dis.ps_state),max(dis.ps_time)||'' as ps_time\n" +
                "  from DMS_IOT_DEVICE_RESOURCE_INFO as info\n" +
                " left  join   dms_termesn_dispatch as dis on  info.link_dispatch=dis.term_dispatch_id  " +
                " where info.link_order_no='" + cgddh + "'   and info.is_valid=1 \n" +
                " GROUP BY term_dispatch_id ,info.TERM_ESN  ";
        log.info("获取getOrderOneEsnPo-----" + sql);
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        for (Object[] objs : devList) {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("esn", objs[0] == null ? "-" : objs[0].toString());
            hashMap.put("term_dispatch_id", objs[1] == null ? "-" : objs[1].toString());
            if (objs[2] != null && objs[2].toString().equals("6")) {
                hashMap.put("ps_state", "已配送");
            } else if (objs[2] != null && objs[2].toString().equals("7")) {
                hashMap.put("ps_state", "已签收");
            } else {
                hashMap.put("ps_state", objs[2] == null ? "-" : objs[2].toString());
            }
            hashMap.put("ps_time", objs[3] == null ? "-" : objs[3].toString());
            value.add(hashMap);
        }
        return new LayJson(200, "请求成功", value, value.size());
    }

    @Override
    public LayJson getOrderOnePsdPo(Map<String, Object> map) {
        String cgddh = map.get("cgddh") == null || map.get("cgddh") == "" ? null : map.get("cgddh").toString();

        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        if (StringUtils.isBlank(cgddh)) {
            return new LayJson(200, "请求成功", value, value.size());
        }
        String sql = "select term_dispatch_id,term_number,ps_time||'',shipper,shipper_phone,recipitnt,recipitnt_phone, qs_time||'',qs_oa,qs_name, " + " qs_phone ,wld from dms_termesn_dispatch where order_id='" + cgddh + "'";
        log.info("获取订单管理-详情-配送单号信息接口sql------" + sql);
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        for (Object[] objs : devList) {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("esn", objs[0] == null ? "-" : objs[0].toString());
            hashMap.put("term_number", objs[1] == null ? "-" : objs[1].toString());
            hashMap.put("ps_time", objs[2] == null ? "-" : objs[2].toString());
            hashMap.put("shipper", objs[3] == null ? "-" : objs[3].toString());
            hashMap.put("shipper_phone", objs[4] == null ? "-" : objs[4].toString());
            hashMap.put("recipitnt", objs[5] == null ? "-" : objs[5].toString());
            hashMap.put("recipitnt_phone", objs[6] == null ? "-" : objs[6].toString());
            hashMap.put("qs_time", objs[7] == null ? "-" : objs[7].toString());
            hashMap.put("qs_oa", objs[8] == null ? "-" : objs[8].toString());
            hashMap.put("qs_name", objs[9] == null ? "-" : objs[9].toString());
            hashMap.put("qs_phone", objs[10] == null ? "-" : objs[10].toString());
            hashMap.put("wld", objs[11] == null ? "-" : objs[11].toString());
            value.add(hashMap);
        }
        return new LayJson(200, "请求成功", value, value.size());
    }


    /**
     * 生成
     */
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

    public void downloadOnlineExcel(HttpServletRequest request, HttpServletResponse response, String fileName) {
        String filePath = request.getSession().getServletContext().getRealPath("download") + File.separator + fileName;
        downloadFile(request, response, "项目订单管理-" + fileName, filePath);
    }


    /**
     * @description：判断该订单下的esn是否都到达了这一状态
     * @author：sunheng
     * @date：2022/12/7 18:06
     * @param：根据采购订单号，查询该订单下的esn是不是 这里最高只能搞到7 建档完成搞不到8（验收完成景河写的，没办法触发事件，只能通过查表来判别）
     */
    public void upOrderState(String cgddh, Integer esnState, Integer orderState) {
        try {

            //查询该订单下esn数量 、>=当前状态下的数量 是否相等
            //返回值 1代表满足 即可更新该接口
            String sql = "select if (od.terminal_quantity=count(1),1,0) as sf from DMS_IOT_DEVICE_RESOURCE_INFO as info\n"
                    + " left join  DMS_TR_PROJECT_ORDER  od  on info.link_order_no=od.cgddh \n";
            if (esnState == 9) {
                sql += " where cgddh='" + cgddh + "'  and info.is_valid=1 and tm_dqzt>='" + esnState + "' and info.jdsf=1 group by od.terminal_quantity";
            } else {
                sql += " where cgddh='" + cgddh + "'  and info.is_valid=1 and tm_dqzt>='" + esnState + "' group by od.terminal_quantity";
            }
            log.info("更新订单状态接口 " + sql);
            List<Object[]> devList = commonInterface.selectListBySql(sql);
            if (devList.size() == 0 && devList.get(0).length == 0) {
                log.info("绑定" + cgddh + "时，由于订单下esn没全部更新，订单当前无法更新为" + orderState);
            } else {
                if (Integer.parseInt(devList.get(0)[0].toString()) == 1) {
                    String sql2 = "";
                    //安装完成
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String current_state_time = simpleDateFormat.format(new Date());
                    if (orderState == 6) {
                        sql2 = "update DMS_TR_PROJECT_ORDER  set order_state='" + orderState + "' ,order_az_time='" + current_state_time + "'  where cgddh='" + cgddh + "'";
                    } else if (orderState == 7) {
                        sql2 = "update DMS_TR_PROJECT_ORDER  set order_state='" + orderState + "' ,order_jd_time='" + current_state_time + "'  where cgddh='" + cgddh + "'";
                    } else {
                        sql2 = "update DMS_TR_PROJECT_ORDER  set order_state='" + orderState + "'  where cgddh='" + cgddh + "'";

                    }
                    boolean b = commonInterface.dbAccess_update(sql2);
                    if (b) {
                        log.info("订单" + cgddh + ",成功更新为" + orderState);
                    }
                } else {
                    log.info("绑定" + cgddh + "时，由于订单下esn没全部更新，订单当前无法更新为" + orderState);
                }
            }
        } catch (NumberFormatException e) {
            log.info("订单状态接口出了" + e.getMessage());
        }
    }

    public LayJson del_term_from_order(HttpServletRequest request){
        String term_esn = "";
        String order_no = "";
        String del_time = "";
        String del_reason = "";
        String del_staff = "";

        StringBuilder sb = CommonUtil.getPostRaw(request);
        if (!sb.toString().equals("")) {
            JSONObject js = JSONObject.parseObject(sb.toString());
            term_esn = js.getString("term_esn");
            order_no = js.getString("order_no");
            del_time = js.getString("del_time");
            del_reason = js.getString("del_reason");
            del_staff = js.getString("del_staff");
        }else{
            return new LayJson(500, "无请求参数", null, 0);
        }

        try{
/** 调用吕飞删除接口            //华为删除设备接口
            String sql = "select\n" +
                    "        id||''          as id,\n" +
                    "        out_dev_id||''  as outDevId,\n" +
                    "        rely_type||''   as relyType,\n" +
                    "        connect_mode||'' as connectMode,\n" +
                    "        pj_id||''       as pjId,\n" +
                    "        dev_label||''   as devLabel,\n" +
                    "        dev_name||''    as devName\n" +
                    "from\n" +
                    "        iot_device\n" +
                    "where\n" +
                    "        is_valid    ='1'\n" +
                    "    and out_iot_fac ='2'\n" +
                    "    and connect_mode='1' " +
                    "    and dev_label='"+term_esn+"'";
            JSONArray queryList0 = commonInterface.dbAccess_selectList(sql);
//            String[] str_arr0 = new String[]{"ID","OUT_DEV_ID","RELY_TYPE","CONNECT_MODE","PJ_ID",
//            "DEV_LABEL","DEV_NAME"};
//            List<Object[]> list0 = CommonUtil.jsonArrayToList(queryList0, str_arr0);

            if(queryList0==null||queryList0.size()==0){
                return new LayJson(500, "IOTDEVICE无有效数据", null, 0);
            }
            JSONObject j = queryList0.getJSONObject(0);
            JSONObject newj = new JSONObject();
            newj.put("id",j.getString("ID"));
            newj.put("outDevId",j.getString("OUTDEVID"));
            newj.put("relyType",j.getString("RELYTYPE"));
            newj.put("connectMode",j.getString("CONNECTMODE"));
            newj.put("pjId",j.getString("PJID"));
            newj.put("devLabel",j.getString("DEVLABEL"));
            newj.put("devName",j.getString("DEVNAME"));
            JSONArray newja = new JSONArray();
            newja.add(newj);
            String addr = "http://25.212.172.39:23503/DevMaint/delDevMsg";
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            log.info("删除终端调用接口参数:"+newja.toJSONString());
            String ss = HttpUtil.httpPost(addr, newja.toJSONString(), headers);*/

            //记录删除事件
            String sql1 = "insert into d5000.DMS_IOT_RESOURCE_DEL_DETAIL " +
                    "(term_esn,order_no,del_time,del_reason,del_staff)values(" +
                    "'"+term_esn+"','"+order_no+"','"+del_time+"','"+del_reason+"','"+del_staff+"')";
            log.info(sql1);
            commonInterface.dbAccess_insert(sql1);

            //解除终端与项目订单绑定关系并将状态置为已检测
            String sql2 = "update d5000.DMS_IOT_DEVICE_RESOURCE_INFO " +
                    "set link_order_no ='',tm_dqzt=2 " +
                    "where term_esn='"+term_esn+"'";
            log.info(sql2);
            commonInterface.dbAccess_update(sql2);

            //虚拟配变注册状态改为未注册状态
            String sql3 = "update d5000.DMS_TR_PMS_LINK_INFO set dev_label='' where dev_label='"+term_esn+"'";
            log.info(sql3);
            commonInterface.dbAccess_update(sql3);
            return new LayJson(200, "删除终端成功", null, 1);
        }catch (Exception e){
            e.printStackTrace();
            return new LayJson(500, "内部错误", null, 0);
        }
    }
}
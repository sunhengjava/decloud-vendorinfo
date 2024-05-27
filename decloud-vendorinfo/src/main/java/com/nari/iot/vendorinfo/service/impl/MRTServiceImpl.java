package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.MRTService;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


@Service("MRTService")
@Slf4j
public class MRTServiceImpl implements MRTService {


    @Autowired
    CommonInterface commonInterface;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public LayJson upApplicationFrom(Map<String, Object> map) {

        String dataTime = simpleDateFormat.format(new Date());
        String applicaitonID = map.get("applicaitonID") != null ? map.get("applicaitonID").toString() : "";
        String WBName = map.get("WBName") != null ? map.get("WBName").toString() : "";
        String cgdd = map.get("cgdd") != null ? map.get("cgdd").toString() : "";
        String terminal = map.get("terminal") != null ? map.get("terminal").toString() : "";
        String batchYear = map.get("batchYear") != null ? map.get("batchYear").toString() : "";
        String entryName = map.get("entryName") != null ? map.get("entryName").toString() : "";
        String WBContacts = map.get("WBContacts") != null ? map.get("WBContacts").toString() : "";
        String WBPhoneNumber = map.get("WBPhoneNumber") != null ? map.get("WBPhoneNumber").toString() : "";
        String sqr = map.get("sqr") != null ? map.get("sqr").toString() : "";
        String sqTime = map.get("sqTime") != null ? map.get("sqTime").toString() : "";
        List<Map> shxx = map.get("shxx") != null ? (List<Map>) map.get("shxx") : new ArrayList<>();
        String sql = "insert into \"D5000\".\"MANUFACTURER_APPLY_FOR_TERMINAL\"(applicationid,wbname,cgddh,terminal,batchyear,entryname,wbcontacts,wbphonenumber,sqr,sqTime) " +
                " values ('" + applicaitonID + "','" + WBName + "','" + cgdd + "','" + terminal + "','" + batchYear + "','" + entryName + "','" + WBContacts + "','" + WBPhoneNumber + "','" + sqr + "','" + sqTime + "' )";
        log.info("当前sql为11:" + sql);
        boolean b = commonInterface.dbAccess_update(sql);
        if (b == true) {
            for (Map li : shxx) {
                String address = li.get("address") != null ? li.get("address").toString() : "";
                String consignee = li.get("consignee") != null ? li.get("consignee").toString() : "";
                String consigneePhone = li.get("consigneePhone") != null ? li.get("consigneePhone").toString() : "";
                String sqNumber = li.get("sqNumber") != null ? li.get("sqNumber").toString() : "";
                String sqTimes = li.get("sqTimes") != null ? li.get("sqTimes").toString() : "";
                log.info("当前sql为111:" + "insert into  \"D5000\".\"MANUFACTURER_APPLY_FOR_TERMINAL_DETAIL\"(ssd,address,consignee,sqnumbere,sqtime,consigneePhone,inserttime)" +
                        " values('" + applicaitonID + "','" + address + "','" + consignee + "','" + sqNumber + "','" + sqTimes + "','" + consigneePhone + "','" + dataTime + "') ");
                commonInterface.dbAccess_insert("insert into  \"D5000\".\"MANUFACTURER_APPLY_FOR_TERMINAL_DETAIL\"(ssd,address,consignee,sqnumbere,sqtime,consigneePhone,inserttime)" +
                        " values('" + applicaitonID + "','" + address + "','" + consignee + "','" + sqNumber + "','" + sqTimes + "','" + consigneePhone + "','" + dataTime + "') ");
            }
            return new LayJson(200, "插入成功", null, 1);
        } else {
            return new LayJson(500, "插入失败,该终端申请单已存在", null, 0);
        }
    }


    /**
     * 2 省公司或电科院下达终端二次转运通知
     *
     * @description：
     * @author：sunheng
     * @date：2024/2/23 10:22
     * @param：
     */
    @Override
    public LayJson secondaryTransports(Map<String, Object> map) {
        String sfgj = map.get("sfgj") != null ? map.get("sfgj").toString() : "";
        String terminalApplicaitonID = map.get("terminalApplicaitonID") != null ? map.get("terminalApplicaitonID").toString() : "";
        String supplier = map.get("supplier") != null ? map.get("supplier").toString() : "";
        String cgddh = map.get("cgddh") != null ? map.get("cgddh").toString() : "";
        String deviceType = map.get("deviceType") != null ? map.get("deviceType").toString() : "";
        String projectNo = map.get("projectNo") != null ? map.get("projectNo").toString() : "";
        String projectName = map.get("projectName") != null ? map.get("projectName").toString() : "";
        String noticeID = map.get("noticeID") != null ? map.get("noticeID").toString() : "";
        String xdr = map.get("xdr") != null ? map.get("xdr").toString() : "";
        String xdTime = map.get("xdTime") != null ? map.get("xdTime").toString() : "";
        String terminalNumber = map.get("terminalNumber") != null ? map.get("terminalNumber").toString() : "";
        String terminalType = map.get("terminalType") != null ? map.get("terminalType").toString() : "";
        String batchNumber = map.get("batchNumber") != null ? map.get("batchNumber").toString() : "";
        String technicalID = map.get("technicalID") != null ? map.get("technicalID").toString() : "";
        String TMName = map.get("TMName") != null ? map.get("TMName").toString() : "";
        String ZPXTermContacts = map.get("ZPXTermContacts") != null ? map.get("ZPXTermContacts").toString() : "";
        String ZPXPhoneNumber = map.get("ZPXPhoneNumber") != null ? map.get("ZPXPhoneNumber").toString() : "";

        boolean b = commonInterface.dbAccess_insert("insert into  \"D5000\".\"QLC_SECONDARY_TRANSPORTATION_NOTICE\"(TERMINALAPPLICATIONID,supplier,cgddh,devicetype,projectno,projectname,noticeid,xdr,xdtime,terminalnumber,terminaltype,batchnumber,technicalid,tmname,zpxtermcontacts,zpxphonenumber,sfgj,status)" +
                " values('" + terminalApplicaitonID + "','" + supplier + "','" + cgddh + "','" + deviceType + "','" + projectNo + "','" + projectName + "','" + noticeID + "','" + xdr + "','" + xdTime + "','" + terminalNumber + "','" + terminalType + "','" + batchNumber + "','" + technicalID + "','" + TMName + "','" + ZPXTermContacts + "','" + ZPXPhoneNumber + "','" + sfgj + "',1) ");
        if (b == true) {
            return new LayJson(200, "插入成功", null, 1);
        } else {
            return new LayJson(500, "插入失败,该通知单已存在", null, 0);
        }
    }


    /**
     * 3 终端供应商发给综配箱厂家请求信息
     *
     * @description：
     * @author：sunheng
     * @date：2024/2/23 10:22
     * @param："requestID":"请求单id", "supplierContacts":终端供应商联系人",
     * "supplierPhoneNumber":"终端供应商联系手机号",
     * "secondaryNumber"："二次转运数量",
     * "batch"："招标批次",
     * "technicalID"："技术ID",
     * "deliverTime"："预计送达时间"
     * "noticeID"："二次转运的通知id"
     */
    @Override
    public LayJson suppierRequest(Map<String, Object> map) {
        String requestID = map.get("requestID") != null ? map.get("requestID").toString() : "";
        String supplierContacts = map.get("supplierContacts") != null ? map.get("supplierContacts").toString() : "";
        String supplierPhoneNumber = map.get("supplierPhoneNumber") != null ? map.get("supplierPhoneNumber").toString() : "";
        String batch = map.get("batch") != null ? map.get("batch").toString() : "";
        String secondaryNumber = map.get("secondaryNumber") != null ? map.get("secondaryNumber").toString() : "";
        String technicalID = map.get("technicalID") != null ? map.get("technicalID").toString() : "";
        String deliverTime = map.get("deliverTime") != null ? map.get("deliverTime").toString() : "";
        String noticeID = map.get("noticeID") != null ? map.get("noticeID").toString() : "";
        String sqr = map.get("sqr") != null ? map.get("sqr").toString() : "";
        String sqTime = map.get("sqTime") != null ? map.get("sqTime").toString() : "";
        boolean b = commonInterface.dbAccess_insert("insert into  \"D5000\".\"QLC_SUPPLIER_REQUEST\"(requestID,ZPXContacts,ZPXPhoneNumber,secondaryNumber,technicalID,deliverTime,noticeID,batch,SQR,SQTIME)" +
                " values('" + requestID + "','" + supplierContacts + "','" + supplierPhoneNumber + "','" + secondaryNumber + "','" + technicalID + "','" + deliverTime + "','" + noticeID + "','" + batch + "','" + sqr + "','" + sqTime + "') ");

        commonInterface.dbAccess_insert("update  \"D5000\".\"QLC_SECONDARY_TRANSPORTATION_NOTICE\" set status=2  where  noticeID='" + noticeID + "'");

        if (b == true) {
            return new LayJson(200, "插入成功", null, 1);
        } else {
            return new LayJson(500, "插入失败,该请求单已存在", null, 0);
        }
    }


    /**
     * 4 综配箱厂家响应供应商的请求信息
     *
     * @description：
     * @author：sunheng
     * @date：2024/2/23 10:22
     * @param：
     */
    @Override
    public LayJson manufacturerResponse(Map<String, Object> map) {
        String responseID = map.get("responseID") != null ? map.get("responseID").toString() : "";
        String requestID = map.get("requestID") != null ? map.get("requestID").toString() : "";
        String supplierContacts = map.get("supplierContacts") != null ? map.get("supplierContacts").toString() : "";
        String supplierPhoneNumber = map.get("supplierPhoneNumber") != null ? map.get("supplierPhoneNumber").toString() : "";
        String secondaryNumber = map.get("secondaryNumber") != null ? map.get("secondaryNumber").toString() : "";
        String deliverTime = map.get("deliverTime") != null ? map.get("deliverTime").toString() : "";
        String noticeID = map.get("noticeID") != null ? map.get("noticeID").toString() : "";
        String address = map.get("address") != null ? map.get("address").toString() : "";
        String consignee = map.get("consignee") != null ? map.get("consignee").toString() : "";
        String consigneeTime = map.get("consigneeTime") != null ? map.get("consigneeTime").toString() : "";
        String consigneePhone = map.get("consigneePhone") != null ? map.get("consigneePhone").toString() : "";
        boolean b = commonInterface.dbAccess_insert("insert into  \"D5000\".\"QLC_MANUFACTURER_RESPONSE\"(responseID,requestID,supplierContacts,supplierPhoneNumber,secondaryNumber,deliverTime,noticeID,xy_shr,xy_address,xy_tel,xy_time)" +
                " values('" + responseID + "','" + requestID + "','" + supplierContacts + "','" + supplierPhoneNumber + "','" + secondaryNumber + "','" + deliverTime + "','" + noticeID + "','" + consignee + "','" + address + "','" + consigneePhone + "','" + consigneeTime + "') ");
        if (b == true) {
                commonInterface.dbAccess_insert("update  \"D5000\".\"QLC_SECONDARY_TRANSPORTATION_NOTICE\" set status=3  where  noticeID='" + noticeID + "'");
            return new LayJson(200, "插入成功", null, 1);
        } else {
            return new LayJson(500, "插入失败,该请求单已存在", null, 0);
        }
    }


    /**
     * 5 终端供应商发货给综配箱厂家
     *
     * @description：
     * @author：sunheng
     * @date：2024/2/23 10:22
     * @param：
     */
    @Override
    public LayJson terminalhipment(Map<String, Object> map) {
        String responseID = map.get("responseID") != null ? map.get("responseID").toString() : "";
        String noticeID = map.get("noticeID") != null ? map.get("noticeID").toString() : "";
        String secondaryNumber = map.get("secondaryNumber") != null ? map.get("secondaryNumber").toString() : "";


        String transportID = map.get("transportID") != null ? map.get("transportID").toString() : "";
        String address = map.get("address") != null ? map.get("address").toString() : "";
        String consignee = map.get("consignee") != null ? map.get("consignee").toString() : "";
        String consigneePhone = map.get("consigneePhone") != null ? map.get("consigneePhone").toString() : "";
        String sqTimes = map.get("sqTimes") != null ? map.get("sqTimes").toString() : "";
        String termEsn = map.get("termEsn") != null ? map.get("termEsn").toString() : "";
        String fhr = map.get("fhr") != null ? map.get("fhr").toString() : "";
        String fhTime = map.get("fhTime") != null ? map.get("fhTime").toString() : "";
        log.info("update \"D5000\".\"QLC_MANUFACTURER_RESPONSE\" set deliverysum='" + secondaryNumber + "' , fh_zzwld='" + transportID + "',fh_address='" + address + "',fh_shr='" + consignee + "',fh_tel='" + consigneePhone + "',fh_termesn='" + termEsn + "',fh_yjsdsj='" + sqTimes + "'  where responseid='" + responseID + "' ");
        boolean b = commonInterface.dbAccess_update("update \"D5000\".\"QLC_MANUFACTURER_RESPONSE\" set deliverysum='" + secondaryNumber + "' , fh_zzwld='" + transportID + "',fh_address='" + address + "',fh_shr='" + consignee + "',fh_tel='" + consigneePhone + "',fh_termesn='" + termEsn + "',fh_yjsdsj='" + sqTimes + "' ,fhr='" + fhr + "',fhtime='" + fhTime + "' where responseid='" + responseID + "' ");

        if (b == true) {
            commonInterface.dbAccess_insert("update  \"D5000\".\"QLC_SECONDARY_TRANSPORTATION_NOTICE\" set status=4  where  noticeID='" + noticeID + "'");
            return new LayJson(200, "插入成功", null, 1);
        } else {
            return new LayJson(500, "插入失败", null, 0);
        }
    }


    /**
     * 6 综配箱厂家进行收货确认
     *
     * @description：
     * @author：sunheng
     * @date：2024/2/23 10:22
     * @param：
     */
    @Override
    public LayJson receiptConfirmation(Map<String, Object> map) {
        String receiptID = map.get("receiptID") != null ? map.get("receiptID").toString() : "";
        String responseID = map.get("responseID") != null ? map.get("responseID").toString() : "";
        String wldID = map.get("wldID") != null ? map.get("wldID").toString() : "";
        String termEsn = map.get("termEsn") != null ? map.get("termEsn").toString() : "";
        String supplier = map.get("supplier") != null ? map.get("supplier").toString() : "";
        String noticeID = map.get("noticeID") != null ? map.get("noticeID").toString() : "";
        String transportID = map.get("transportID") != null ? map.get("transportID").toString() : "";
        String photo = map.get("photo") != null ? map.get("photo").toString() : "";
        String address = map.get("address") != null ? map.get("address").toString() : "";
        String consignee = map.get("consignee") != null ? map.get("consignee").toString() : "";
        String consigneePhone = map.get("consigneePhone") != null ? map.get("consigneePhone").toString() : "";
        String sqNumber = map.get("sqNumber") != null ? map.get("sqNumber").toString() : "";
        String sqTimes = map.get("sqTimes") != null ? map.get("sqTimes").toString() : "";
        String dataTime = simpleDateFormat.format(new Date());
        boolean b = commonInterface.dbAccess_insert(" insert into \"D5000\".\"QLC_RECEIPTCONFIRMATION\" " +
                "(receiptID,responseID,wldID,termEsn,supplier,noticeID,transportID,photo,address,consignee,consigneePhone,sqNumber,sqTimes,dataTime) " +
                " values('" + receiptID + "','" + responseID + "','" + wldID + "','" + termEsn + "','" + supplier + "','" + noticeID + "'," +
                " '" + transportID + "','" + photo + "','" + address + "','" + consignee + "','" + consigneePhone + "','" + sqNumber + "','" + sqTimes + "','" + dataTime + "' ) ");
        if (b == true) {
            String[] split = termEsn.split(",");
            for (int i = 0; i < split.length; i++) {
                commonInterface.dbAccess_insert("update  \"dms_iot_device_resource_info\" set sfeczy=1  where  term_esn='" + split[i] + "'");
            }
            commonInterface.dbAccess_insert("update  \"D5000\".\"QLC_SECONDARY_TRANSPORTATION_NOTICE\" set status=4  where  noticeID='" + noticeID + "'");
            return new LayJson(200, "插入成功", null, 1);
        } else {
            return new LayJson(500, "插入失败,该请求单已存在", null, 0);
        }
    }

    @Override
    public LayJson secondaryTransportation(Map<String, Object> map) {
        String requestID = map.get("requestID") != null ? map.get("requestID").toString() : "";
        String cgddh = map.get("cgddh") != null ? map.get("cgddh").toString() : "";
        String status = map.get("status") != null ? map.get("status").toString() : "";
        String projectName = map.get("projectName") != null ? map.get("projectName").toString() : "";
        int pageNo = map.get("pageNo") == null || map.get("pageNo") == "" ? 1 : Integer.parseInt(map.get("pageNo").toString());
        int pageSize = map.get("pageSize") == null || map.get("pageSize") == "" ? 50 : Integer.parseInt(map.get("pageSize").toString());
        String sql = " select  a.noticeid,a.cgddh,a.devicetype,a.projectno,a.projectname,a.terminalapplicationid,a.xdr,a.xdtime||'',a.terminalnumber,sum(c.secondarynumber) as xysl,sum(c.deliverysum) as fhsl,sum(d.sqnumber) as shsl,a.status,\n" +
                "a.terminaltype,a.batchnumber,a.technicalid,a.supplier,a.tmname,a.zpxtermcontacts,a.zpxphonenumber \n" +
                "from qlc_secondary_transportation_notice as a \n" +
                "left JOIN  \"D5000\".\"QLC_SUPPLIER_REQUEST\" as b on b.noticeid=a.noticeid\n" +
                "left join \"D5000\".\"QLC_MANUFACTURER_RESPONSE\" as c on c.requestid=b.requestid\n" +
                "left join  \"D5000\".\"QLC_RECEIPTCONFIRMATION\" as d on c.responseid=d.responseid  where 1=1 ";
        if (StringUtils.isNotBlank(requestID)) {
            sql += " and  a.terminalapplicationid= '" + requestID + "' ";
        }
        if (StringUtils.isNotBlank(cgddh)) {
            sql += " and a.cgddh='" + cgddh + "' ";
        }
        if (StringUtils.isNotBlank(status)) {
            sql += " and a.status in  (" + status + ") ";
        }
        if (StringUtils.isNotBlank(projectName)) {
            sql += " and a.projectname like '%" + projectName + "%' ";
        }
        sql += "group by a.noticeid,a.cgddh,a.devicetype,a.projectno,a.projectname,a.terminalapplicationid,a.xdr,a.xdtime,a.terminalnumber,a.status,\n" +
                "a.terminaltype,a.batchnumber,a.technicalid,a.supplier,a.tmname,a.zpxtermcontacts,a.zpxphonenumber  ";
        String sqlCount = "select  count(1) as gs from (  " + sql + ")";
        sql += "   limit " + (pageNo - 1) * pageSize + "," + pageSize;
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        List<Object[]> devCount = commonInterface.selectListBySql(sqlCount.toString());
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        for (Object[] objs : devList) {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("noticeid", objs[0] == null ? "-" : objs[0].toString());
            hashMap.put("cgddh", objs[1] == null ? "-" : objs[1].toString());
            hashMap.put("devicetype", objs[2] == null ? "-" : objs[2].toString());
            hashMap.put("projectno", objs[3] == null ? "-" : objs[3].toString());
            hashMap.put("projectname", objs[4] == null ? "-" : objs[4].toString());
            hashMap.put("terminalapplicationid", objs[5] == null ? "-" : objs[5].toString());
            hashMap.put("xdr", objs[6] == null ? "-" : objs[6].toString());
            hashMap.put("xdtime", objs[7] == null ? "-" : objs[7].toString());
            hashMap.put("terminalnumber", objs[8] == null ? "-" : objs[8].toString());
            hashMap.put("xysl", objs[9] == null ? "-" : objs[9].toString());
            hashMap.put("fhsl", objs[10] == null ? "-" : objs[10].toString());
            hashMap.put("shsl", objs[11] == null ? "-" : objs[11].toString());
            String statusRe = objs[12] == null ? "-" : objs[12].toString();
            if (statusRe.equals("1")) {
                statusRe = "已下达";
            } else if (statusRe.equals("2")) {
                statusRe = "已请求";
            } else if (statusRe.equals("3")) {
                statusRe = "已响应";
            } else if (statusRe.equals("4")) {
                statusRe = "已发货";
            } else if (statusRe.equals("5")) {
                statusRe = "已收货";
            }
            hashMap.put("status", statusRe);
            hashMap.put("terminaltype", objs[13] == null ? "-" : objs[13].toString());
            hashMap.put("batchnumber", objs[14] == null ? "-" : objs[14].toString());
            hashMap.put("technicalid", objs[15] == null ? "-" : objs[15].toString());
            hashMap.put("supplier", objs[16] == null ? "-" : objs[16].toString());
            hashMap.put("tmname", objs[17] == null ? "-" : objs[17].toString());
            hashMap.put("zpxtermcontacts", objs[18] == null ? "-" : objs[18].toString());
            hashMap.put("zpxphonenumber", objs[19] == null ? "-" : objs[19].toString());
            value.add(hashMap);
        }
        String s = devCount.get(0)[0] != null ? devCount.get(0)[0].toString() : "0";
        return new LayJson(200, "请求成功", value, Integer.parseInt(s));
    }

    @Override
    public Map<String, Object> exportST(HttpServletRequest request, HttpServletResponse response) {
        String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss") + ".xls";
        String targetFilePath = request.getSession().getServletContext().getRealPath("download") + File.separator + fileName;
        String sheet = "二次转运";
        String requestID = request.getParameter("requestID") == null || request.getParameter("requestID") == "" ? null : request.getParameter("requestID").toString();
        String cgddh = request.getParameter("cgddh") == null || request.getParameter("cgddh") == "" ? null : request.getParameter("cgddh").toString();
        String status = request.getParameter("status") == null || request.getParameter("status") == "" ? null : request.getParameter("status").toString();
        String projectName = request.getParameter("projectName") == null || request.getParameter("projectName") == "" ? null : request.getParameter("projectName").toString();

        String sql = " select  a.noticeid,a.cgddh,a.devicetype,a.projectno,a.projectname,a.terminalapplicationid,a.xdr,a.xdtime||'',a.terminalnumber,sum(c.secondarynumber) as xysl,sum(c.deliverysum) as fhsl,sum(d.sqnumber) as shsl,a.status,\n" +
                "a.terminaltype,a.batchnumber,a.technicalid,a.supplier,a.tmname,a.zpxtermcontacts,a.zpxphonenumber \n" +
                "from qlc_secondary_transportation_notice as a \n" +
                "left JOIN  \"D5000\".\"QLC_SUPPLIER_REQUEST\" as b on b.noticeid=a.noticeid\n" +
                "left join \"D5000\".\"QLC_MANUFACTURER_RESPONSE\" as c on c.requestid=b.requestid\n" +
                "left join  \"D5000\".\"QLC_RECEIPTCONFIRMATION\" as d on c.responseid=d.responseid  where 1=1 ";

        if (StringUtils.isNotBlank(requestID)) {
            sql += " and  a.terminalapplicationid= '" + requestID + "' ";
        }
        if (StringUtils.isNotBlank(cgddh)) {
            sql += " and a.cgddh='" + cgddh + "' ";
        }
        if (StringUtils.isNotBlank(status)) {
            sql += " and a.status in  (" + status + ") ";
        }
        if (StringUtils.isNotBlank(projectName)) {
            sql += " and a.projectname like '%" + projectName + "%' ";
        }
        sql += "group by a.noticeid,a.cgddh,a.devicetype,a.projectno,a.projectname,a.terminalapplicationid,a.xdr,a.xdtime,a.terminalnumber,a.status,\n" +
                "a.terminaltype,a.batchnumber,a.technicalid,a.supplier,a.tmname,a.zpxtermcontacts,a.zpxphonenumber  ";
        List<Object[]> devList = commonInterface.selectListBySql(sql);

        String titles[] = new String[]{"序号", "二次转运通知ID", "采购订单编号", "设备类型", "项目编号", "项目名称", "终端申请id", "二次转运下达人", //7
                "二次转运下达时间", "要求数量", "响应数量", "发货数量", "收货数量", "当前状态", "终端类型", "招标批次号"    //8
                , "技术ID", "终端厂家", "成套设备厂家", "成套设备联系人", "成套设备联系号码"}; //5
        List<Object[]> newlist = new ArrayList<Object[]>();
        int i = 0;
        for (Object[] temObj : devList) {
            i++;
            Object[] newObj = new Object[21];
            newObj[0] = i;
            newObj[1] = temObj[0] == null || temObj[0].equals("") ? "-" : temObj[0];
            newObj[2] = temObj[1] == null || temObj[1].equals("") ? "-" : temObj[1];
            newObj[3] = temObj[2] == null || temObj[2].equals("") ? "-" : temObj[2];
            newObj[4] = temObj[3] == null || temObj[3].equals("") ? "-" : temObj[3];
            newObj[5] = temObj[4] == null || temObj[4].equals("") ? "-" : temObj[4];
            newObj[6] = temObj[5] == null || temObj[5].equals("") ? "-" : temObj[5];
            newObj[7] = temObj[6] == null || temObj[6].equals("") ? "-" : temObj[6];
            newObj[8] = temObj[7] == null || temObj[7].equals("") ? "-" : temObj[7];
            newObj[9] = temObj[8] == null || temObj[8].equals("") ? "-" : temObj[8];
            newObj[10] = temObj[9] == null || temObj[9].equals("") ? "-" : temObj[9];
            newObj[11] = temObj[10] == null || temObj[10].equals("") ? "-" : temObj[10];
            newObj[12] = temObj[11] == null || temObj[11].equals("") ? "-" : temObj[11];
            //newObj[13] = temObj[12] == null || temObj[12].equals("") ? "-" : temObj[12];
            String statusRe = temObj[12] == null ? "-" : temObj[12].toString();
            if (statusRe.equals("1")) {
                statusRe = "已下达";
            } else if (statusRe.equals("2")) {
                statusRe = "已请求";
            } else if (statusRe.equals("3")) {
                statusRe = "已响应";
            } else if (statusRe.equals("4")) {
                statusRe = "已发货";
            } else if (statusRe.equals("5")) {
                statusRe = "已收货";
            }
            newObj[13] = statusRe;
            newObj[14] = temObj[13] == null || temObj[13].equals("") ? "-" : temObj[13];
            newObj[15] = temObj[14] == null || temObj[14].equals("") ? "-" : temObj[14];
            newObj[16] = temObj[15] == null || temObj[15].equals("") ? "-" : temObj[15];
            newObj[17] = temObj[16] == null || temObj[16].equals("") ? "-" : temObj[16];
            newObj[18] = temObj[17] == null || temObj[17].equals("") ? "-" : temObj[17];
            newObj[19] = temObj[18] == null || temObj[18].equals("") ? "-" : temObj[18];
            newObj[20] = temObj[19] == null || temObj[19].equals("") ? "-" : temObj[19];
            newlist.add(newObj);
        }
        try {
            createmsgExcel(targetFilePath, sheet, titles, newlist);
        } catch (Exception e) {
            return CommonUtil.returnMap(false, 0, "", false);
        }
        try {
            downloadExcel(request, response, fileName);
        } catch (Exception e) {
            return CommonUtil.returnMap(false, 0, "", null);
        }
        return CommonUtil.returnMap(true, 0, "", fileName);

    }

    @Override
    public LayJson getEsnDetail(Map<String, Object> map) {
        String noticeID = map.get("noticeID") != null ? map.get("noticeID").toString() : "";
        String sql = "select fh_termesn,fh_zzwld,status from qlc_secondary_transportation_notice as a" +
                " left join   \"D5000\".\"QLC_MANUFACTURER_RESPONSE\" as b on a.noticeid=b.noticeid  where a.noticeid='" + noticeID + "' ";
        log.info(sql);
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        for (Object[] objs : devList) {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            String esn = objs[0] == null ? "-" : objs[0].toString();
            String status = objs[2] == null ? "-" : objs[2].toString();
            if (status.equals("1")) {
                status = "已下达";
            } else if (status.equals("2")) {
                status = "已请求";
            } else if (status.equals("3")) {
                status = "已响应";
            } else if (status.equals("4")) {
                status = "已发货";
            } else if (status.equals("5")) {
                status = "已收货";
            }
            String[] split = esn.split(",");
            for (int i = 0; i < split.length; i++) {
                hashMap.put("esn", split[i]);
                hashMap.put("fh_zzwld", objs[1] == null ? "-" : objs[1].toString());
                hashMap.put("status", status);
                value.add(hashMap);
            }
        }
        return new LayJson(200, "请求成功", value, value.size());
    }

    @Override
    public LayJson getpsd(Map<String, Object> map) {
        String noticeID = map.get("noticeID") != null ? map.get("noticeID").toString() : "";
        String sql = " select fh_zzwld,deliverysum,fh_yjsdsj||'',suppliercontacts,fh_shr,fh_tel,b.sqtimes||'',b.sqnumber  from \"D5000\".\"QLC_MANUFACTURER_RESPONSE\" as a\n" +
                " left join  \"D5000\".\"QLC_RECEIPTCONFIRMATION\" as b on a.responseid=b.responseid\n" +
                " where a.noticeid='" + noticeID + "'";
        log.info(sql);
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        for (Object[] objs : devList) {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("psd", objs[0] == null ? "-" : objs[0].toString());
            hashMap.put("zdsl", objs[1] == null ? "-" : objs[1].toString());
            hashMap.put("yjsdsj", objs[2] == null ? "-" : objs[2].toString());
            hashMap.put("gys", objs[3] == null ? "-" : objs[3].toString());
            hashMap.put("jsr", objs[4] == null ? "-" : objs[4].toString());
            hashMap.put("jsrtel", objs[5] == null ? "-" : objs[5].toString());
            hashMap.put("sdsj", objs[6] == null ? "-" : objs[6].toString());
            hashMap.put("sdsl", objs[7] == null ? "-" : objs[7].toString());
            value.add(hashMap);

        }
        return new LayJson(200, "请求成功", value, value.size());
    }

    @Override
    public LayJson getStatusDetail(Map<String, Object> map) {
        String noticeID = map.get("noticeID") != null ? map.get("noticeID").toString() : "";
        String sql = "select  e.wbname,e.terminal,e.sqr as esqr,e.sqtime as esqtime,e.wbcontacts,e.wbphonenumber,f.sqnumbere as fsqnumbere,f.sqtime as fsqtime,f.consignee,f.consigneephone,f.address as faddress,\n" +
                "a.xdr as axdr,a.xdtime||'',\n" +
                "b.sqr as bsqr,b.sqtime||'' as bsqtime,\n" +
                "c.xy_shr,c.xy_time,c.secondarynumber,\n" +
                "c.fhr as cfhr,c.fhtime||'',c.deliverysum,\n" +
                "d.consignee as discou,d.sqtimes as dsqtimes,d.sqnumber as dsqnumber\n" +
                "from qlc_secondary_transportation_notice as a \n" +
                "left join  MANUFACTURER_APPLY_FOR_TERMINAL as e  on a.terminalapplicationid=e.applicationid\n" +
                "left join \"D5000\".\"MANUFACTURER_APPLY_FOR_TERMINAL_DETAIL\" as f on f.ssd=e.applicationid\n" +
                "left JOIN  \"D5000\".\"QLC_SUPPLIER_REQUEST\" as b on b.noticeid=a.noticeid\n" +
                "left join \"D5000\".\"QLC_MANUFACTURER_RESPONSE\" as c on c.requestid=b.requestid " +
                " left join \"D5000\".\"QLC_RECEIPTCONFIRMATION\" as d on c.responseid=d.responseid " +
                " where a.noticeid='" + noticeID + "' ";
        log.info("sql"+sql);
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        List<Map<String, String>> collect = new ArrayList<>();
        HashMap<String, String> hashMap1 = new HashMap<>();
        String zdsq = "";//终端申请
        String eczytz = "";//二次转运通知
        String zdzyqq = "";//终端转运请求
        String xy = "";//响应
        String fh = "";//发货
        String sh = "";
        for (int i = 0; i < devList.size(); i++) {
            String zdsqFactoryName = devList.get(i)[0] == null ? "-" : devList.get(i)[0].toString();
            String zdsqSblx = devList.get(i)[1] == null ? "-" : devList.get(i)[1].toString();
            String zdsqSqr = devList.get(i)[2] == null ? "-" : devList.get(i)[2].toString();
            String zdsqSqTime = devList.get(i)[3] == null ? "-" : devList.get(i)[3].toString();
            String zdsqZbslxr = devList.get(i)[4] == null ? "-" : devList.get(i)[4].toString();
            String zdsqZbsTel = devList.get(i)[5] == null ? "-" : devList.get(i)[5].toString();
            String zdsqXQEsnSum = devList.get(i)[6] == null ? "-" : devList.get(i)[6].toString();
            String zdsqXQsdsj = devList.get(i)[7] == null ? "-" : devList.get(i)[7].toString();
            String zdsqXQshr = devList.get(i)[8] == null ? "-" : devList.get(i)[8].toString();
            String zdsqXQshrTel = devList.get(i)[9] == null ? "-" : devList.get(i)[9].toString();
            String zdsqXQshrAddress = devList.get(i)[10] == null ? "-" : devList.get(i)[10].toString();
            //二次转运
            String eczyTzr = devList.get(i)[11] == null ? "-" : devList.get(i)[11].toString();
            String eczyTzTime = devList.get(i)[12] == null ? "-" : devList.get(i)[12].toString();
            //转运请求
            String qqSqr = devList.get(i)[13] == null ? "-" : devList.get(i)[13].toString();
            String qqSqTime = devList.get(i)[14] == null ? "-" : devList.get(i)[14].toString();
            //响应
            String xyr = devList.get(i)[15] == null ? "-" : devList.get(i)[15].toString();
            String xyTime = devList.get(i)[16] == null ? "-" : devList.get(i)[16].toString();
            String xySum = devList.get(i)[17] == null ? "-" : devList.get(i)[17].toString();
            //发货
            String fhr = devList.get(i)[18] == null ? "-" : devList.get(i)[18].toString();
            String fhTime = devList.get(i)[19] == null ? "-" : devList.get(i)[19].toString();
            String fhSum = devList.get(i)[20] == null ? "-" : devList.get(i)[20].toString();
            //收货
            String shr = devList.get(i)[21] == null ? "-" : devList.get(i)[21].toString();
            String shTime = devList.get(i)[22] == null ? "-" : devList.get(i)[22].toString();
            String shSum = devList.get(i)[23] == null ? "-" : devList.get(i)[23].toString();
            if (i == 0) {
                zdsq += " "+zdsqFactoryName+" 厂家主动申请融合终端，设备类型为："+zdsqSblx+", 申请人: "+zdsqSqr+",  申请时间："+zdsqSqTime+", 设备类型为："+zdsqSblx+" ,中标商联系人："+zdsqZbslxr+",中标商联系号码："+zdsqZbsTel+";";
                eczytz = "通知人: "+eczyTzr+" ,  通知时间: "+eczyTzTime+"";
                zdzyqq = "申请人: "+qqSqr+"  ,申请时间: "+qqSqTime+" ";
                xy = "响应人: "+xyr+"  ,响应时间: "+xyTime+" ,响应数量: "+xySum+" ";
                fh = "发货人:"+fhr+"  ,发货时间: "+fhTime+" ,发货数量： "+fhSum+" ; ";
                sh="收货人: "+shr+"  ,收货时间: "+shTime+" ,收货数量： "+shSum+" ; ";
            }else{
                zdsq += "    详情" + (i + 1) + "： 申请终端数量："+zdsqXQEsnSum+",  申请送达时间："+zdsqXQsdsj+" , 收货人:"+zdsqXQshr+" , 收货人手机号: "+zdsqXQshrTel+" ,收货地址: "+zdsqXQshrAddress+" ; ";
                xy += "响应人" + (i + 1) + ": "+xyr+"  ,响应时间" + (i + 1) + ": "+xyTime+" ,响应数量" + (i + 1) + ": "+xySum+" ;";
                fh += "发货人" + (i + 1) + ": "+fhr+"  ,发货时间" + (i + 1) + ": "+fhTime+" ,发货数量" + (i + 1) + "： "+fhSum+" ;";
                sh += "收货人"+ (i + 1) + ": "+shr+"  ,收货时间"+ (i + 1) +": "+shTime+" ,收货数量"+ (i + 1) +"： "+shSum+" ;";
            }
        }
        hashMap1.put("终端申请", zdsq);
        HashMap<String, String> hashMap2 = new HashMap<>();
        hashMap2.put("二次转运通知", eczytz);
        HashMap<String, String> hashMap3 = new HashMap<>();
        hashMap3.put("终端转运请求", zdzyqq);
        HashMap<String, String> hashMap4 = new HashMap<>();
        hashMap4.put("成套设备响应", xy);

        HashMap<String, String> hashMap5 = new HashMap<>();
        hashMap5.put("终端厂家发货", fh);
        HashMap<String, String> hashMap6 = new HashMap<>();
        hashMap6.put("成套设备收货", sh);
        collect.add(hashMap1);
        collect.add(hashMap2);
        collect.add(hashMap3);
        collect.add(hashMap4);
        collect.add(hashMap5);
        collect.add(hashMap6);
        return new LayJson(200, "请求成功", collect, collect.size());
    }

    public static String wlbOss = "http://25.212.172.50:9099/getOssQlc/";

    @Override
    public LayJson getPicture(Map<String, Object> map) {
        String noticeID = map.get("noticeID") == null || map.get("noticeID") == "" ? null : map.get("noticeID").toString();
        String sql = " select photo from qlc_receiptconfirmation where noticeid='" + noticeID + "' ";
        log.info("sql" + sql);
        List<Object[]> devCount = commonInterface.selectListBySql(sql.toString());
        if(devCount.size()>0){
            String s = devCount.get(0)[0] != null ? devCount.get(0)[0].toString() : "-";
            String[] split = s.split(",");
            for (int i = 0; i < split.length; i++) {
                split[i] = wlbOss + split[i];
            }
            return new LayJson(200, "请求成功", split, split.length);
        }else{
            return new LayJson(200, "未匹配到图片", null, 0);
        }

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

    public void downloadExcel(HttpServletRequest request, HttpServletResponse response, String fileName) {
        String filePath = request.getSession().getServletContext().getRealPath("download") + File.separator + fileName;
        downloadFile(request, response, "二次转运-" + fileName, filePath);
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

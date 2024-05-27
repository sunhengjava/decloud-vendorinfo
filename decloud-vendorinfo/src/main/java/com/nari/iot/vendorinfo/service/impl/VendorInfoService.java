package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.metadata.Table;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.nari.iot.vendorinfo.common.*;
import com.nari.iot.vendorinfo.controller.Tbwlb;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.IVendorInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


@Service("vendorInfoService")
@Slf4j
public class VendorInfoService implements IVendorInfoService {

    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    CommonInterface commonInterface;
    @Autowired
    Tbwlb tbwlb;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> queryVendorGrid(HttpServletRequest request) {
        logger.info("进入查询");
        String vendor_nm = request.getParameter("NAME");
        String vendor_credit_code = request.getParameter("CREDIT_CODE");
        String market_contact = request.getParameter("MARKET_CONTACT");
        String engineering_contact = request.getParameter("ENGINEERING_CONTACT");
        String deviceType = request.getParameter("deviceType");
        int pageNo = 0;
        int pageSize = 0;
        if (request.getParameter("pageSize") != null && !request.getParameter("pageSize").equals("")) {
            pageNo = Integer.parseInt(request.getParameter("pageNo"));
            pageSize = Integer.parseInt(request.getParameter("pageSize"));
            pageNo = (pageNo - 1) * pageSize;
        }
        String sql = "select t1.name,t1.jc_name,t1.credit_code,t1.address, " +
                "t1.market_contact,t1.market_contact_tel,t1.market_contact_email, " +
                "t1.engineering_contact,t1.engineering_contact_tel,t1.engineering_contact_email, " +
                "count(t2.ht_no) as htnum,sum(t2.goods_num) as zbnum ,sum(t2.esn_num) esn_num , " +
                "case when t2.sb_device_type='0' then '融合终端' when  t2.sb_device_type='1' then '成套设备'" +
                " when t2.sb_device_type='2' then '综配箱'  when t2.sb_device_type='3' then '箱变' end as SB_DEVICE_TYPE " +
                " from d5000.DMS_TERMINAL_VENDOR t1 " +
                " left join d5000.DMS_IOT_HT_INFO t2 on t2.vendor_credit_code = t1.credit_code " +
                "where 1=1 ";
        if (vendor_nm != null && !vendor_nm.equals("")) {
            sql += "and (t1.name like '%" + vendor_nm + "%' or t1.jc_name like '%" + vendor_nm + "%') ";
        }
        if (vendor_credit_code != null && !vendor_credit_code.equals("")) {
            sql += "and t1.credit_code like '%" + vendor_credit_code + "%' ";
        }
        if (market_contact != null && !market_contact.equals("")) {
            sql += "and t1.market_contact like '%" + market_contact + "%' ";
        }
        if (engineering_contact != null && !engineering_contact.equals("")) {
            sql += "and t1.engineering_contact like '%" + engineering_contact + "%' ";
        }
        if (deviceType != null && !deviceType.equals("")) {
            sql += "and t2.sb_device_type  in ( " + deviceType + ") ";
        }
        sql += "group by t1.name,t1.jc_name,t1.credit_code,t1.address, " +
                "t1.market_contact,t1.market_contact_tel,t1.market_contact_email, " +
                "t1.engineering_contact,t1.engineering_contact_tel,t1.engineering_contact_email,t2.sb_device_type ";
        logger.info("sql:" + sql);

        JSONArray queryList = commonInterface.dbAccess_selectList(sql);
        List<Object> list = queryList.subList(0, queryList.size());
        logger.info("list:" + list.size());
        if (list == null || list.size() <= 0) {
            logger.info("异常返回空");
            return null;
        }
        int total = list.size();
        logger.info("total:" + total);
        if (pageSize != 0) {
            if (pageNo == 0 && list.size() < pageSize) {

            } else {
                if (list.size() < pageNo + pageSize) {
                    if (pageNo > list.size()) {
                        if (pageSize < list.size()) {
                            list = list.subList(0, pageSize);
                        }
                    } else {
                        list = list.subList(pageNo, list.size());
                    }
                } else {
                    list = list.subList(pageNo, pageNo + pageSize);
                }
            }
        }

        Map<String, Object> m = new HashMap<>();
        m.put("data", list);
        m.put("total", total);
        return m;

    }

    @Override
    public Map<String, Object> insertVendor(Map<String, Object> map) {
        String NAME  = map.get("NAME") != null ? map.get("NAME").toString() : "";
        String MARKET_CONTACT  = map.get("MARKET_CONTACT") != null ? map.get("MARKET_CONTACT").toString() : "";
        String ENGINEERING_CONTACT  = map.get("ENGINEERING_CONTACT") != null ? map.get("ENGINEERING_CONTACT").toString() : "";
        String CREDIT_CODE  = map.get("CREDIT_CODE") != null ? map.get("CREDIT_CODE").toString() : "";
        String JC_NAME  = map.get("JC_NAME") != null ? map.get("JC_NAME").toString() : "";
        String ADDRESS  = map.get("ADDRESS") != null ? map.get("ADDRESS").toString() : "";
        String MARKET_CONTACT_TEL  = map.get("MARKET_CONTACT_TEL") != null ? map.get("MARKET_CONTACT_TEL").toString() : "";
        String MARKET_CONTACT_EMAIL  = map.get("MARKET_CONTACT_EMAIL") != null ? map.get("MARKET_CONTACT_EMAIL").toString() : "";
        String ENGINEERING_CONTACT_TEL  = map.get("ENGINEERING_CONTACT_TEL") != null ? map.get("ENGINEERING_CONTACT_TEL").toString() : "";
        String ENGINEERING_CONTACT_EMAIL  = map.get("ENGINEERING_CONTACT_EMAIL") != null ? map.get("ENGINEERING_CONTACT_EMAIL").toString() : "";

        String sql = "insert into d5000.DMS_TERMINAL_VENDOR " +
                "(NAME,MARKET_CONTACT,ENGINEERING_CONTACT,CREDIT_CODE," +
                "JC_NAME,ADDRESS,MARKET_CONTACT_TEL,MARKET_CONTACT_EMAIL," +
                "ENGINEERING_CONTACT_TEL,ENGINEERING_CONTACT_EMAIL,CREATE_TIME)" +
                "values('" + NAME + "','" + MARKET_CONTACT + "','" + ENGINEERING_CONTACT + "'," +
                "'" + CREDIT_CODE + "','" + JC_NAME + "','" + ADDRESS + "','" + MARKET_CONTACT_TEL + "'," +
                "'" + MARKET_CONTACT_EMAIL + "','" + ENGINEERING_CONTACT_TEL + "','" + ENGINEERING_CONTACT_EMAIL + "',now())";
        commonInterface.dbAccess_update(sql);
        HashMap maps = new HashMap();
        maps.put("creditCode", CREDIT_CODE);
        maps.put("oldCreditCode", CREDIT_CODE);
        maps.put("vendorName", NAME);
        maps.put("jcName", JC_NAME);
        maps.put("address", ADDRESS);
        maps.put("marketContact", MARKET_CONTACT);
        maps.put("marketContactTel", MARKET_CONTACT_TEL);
        maps.put("marketContactEmail", MARKET_CONTACT_EMAIL);
        maps.put("engineeringContact", ENGINEERING_CONTACT);
        maps.put("engineeringContactTel", ENGINEERING_CONTACT_TEL);
        maps.put("engineeringContactEmail", ENGINEERING_CONTACT_EMAIL);
        tbwlb.editSupplier(maps);
        return CommonUtil.returnMap(true, 0, "success", null);
    }

    @Override
    public Map<String, Object> updateVendor(HttpServletRequest request) {
        String NAME = request.getParameter("NAME");
        String MARKET_CONTACT = request.getParameter("MARKET_CONTACT");
        String ENGINEERING_CONTACT = request.getParameter("ENGINEERING_CONTACT");
        String CREDIT_CODE_OLD = request.getParameter("CREDIT_CODE_OLD");
        String CREDIT_CODE_NEW = request.getParameter("CREDIT_CODE_NEW");
        String JC_NAME = request.getParameter("JC_NAME");
        String ADDRESS = request.getParameter("ADDRESS");
        String MARKET_CONTACT_TEL = request.getParameter("MARKET_CONTACT_TEL");
        String MARKET_CONTACT_EMAIL = request.getParameter("MARKET_CONTACT_EMAIL");
        String ENGINEERING_CONTACT_TEL = request.getParameter("ENGINEERING_CONTACT_TEL");
        String ENGINEERING_CONTACT_EMAIL = request.getParameter("ENGINEERING_CONTACT_EMAIL");
        HashMap map = new HashMap();
        map.put("oldCreditCode", CREDIT_CODE_OLD);

        String sql0 = "update d5000.DMS_TERMINAL_VENDOR set update_time=now(),";
        String sql1 = " where CREDIT_CODE = '" + CREDIT_CODE_OLD + "'";
        String sql2 = "";
        if (NAME != null && !NAME.equals("")) {
            sql2 += "NAME='" + NAME + "',";
            map.put("vendorName", NAME);
        }
        if (CREDIT_CODE_NEW != null && !CREDIT_CODE_NEW.equals("")) {
            sql2 += "CREDIT_CODE='" + CREDIT_CODE_NEW + "',";
            map.put("creditCode", CREDIT_CODE_NEW);
        }
        if (MARKET_CONTACT != null && !MARKET_CONTACT.equals("")) {
            sql2 += "MARKET_CONTACT='" + MARKET_CONTACT + "',";
            map.put("marketContact", MARKET_CONTACT);
        }
        if (ENGINEERING_CONTACT != null && !ENGINEERING_CONTACT.equals("")) {
            sql2 += "ENGINEERING_CONTACT='" + ENGINEERING_CONTACT + "',";
            map.put("engineeringContact", ENGINEERING_CONTACT);
        }
        if (JC_NAME != null && !JC_NAME.equals("")) {
            sql2 += "JC_NAME='" + JC_NAME + "',";
            map.put("jcName", JC_NAME);
        }
        if (ADDRESS != null && !ADDRESS.equals("")) {
            sql2 += "ADDRESS='" + ADDRESS + "',";
            map.put("address", ADDRESS);
        }
        if (MARKET_CONTACT_TEL != null && !MARKET_CONTACT_TEL.equals("")) {
            sql2 += "MARKET_CONTACT_TEL='" + MARKET_CONTACT_TEL + "',";
            map.put("marketContactTel", MARKET_CONTACT_TEL);
        }
        if (MARKET_CONTACT_EMAIL != null && !MARKET_CONTACT_EMAIL.equals("")) {
            sql2 += "MARKET_CONTACT_EMAIL='" + MARKET_CONTACT_EMAIL + "',";
            map.put("marketContactEmail", MARKET_CONTACT_EMAIL);
        }
        if (ENGINEERING_CONTACT_TEL != null && !ENGINEERING_CONTACT_TEL.equals("")) {
            sql2 += "ENGINEERING_CONTACT_TEL='" + ENGINEERING_CONTACT_TEL + "',";
            map.put("engineeringContactTel", ENGINEERING_CONTACT_TEL);
        }
        if (ENGINEERING_CONTACT_EMAIL != null && !ENGINEERING_CONTACT_EMAIL.equals("")) {
            sql2 += "ENGINEERING_CONTACT_EMAIL='" + ENGINEERING_CONTACT_EMAIL + "',";
            map.put("engineeringContactEmail", ENGINEERING_CONTACT_EMAIL);
        }

        sql2 = sql2.substring(0, sql2.length() - 1);
        System.out.println("sql" + sql0 + sql2 + sql1);

        Boolean a = commonInterface.dbAccess_update(sql0 + sql2 + sql1);
        if (a) {
            tbwlb.editSupplier(map);
            return CommonUtil.returnMap(true, 0, "success", null);
        } else {
            return CommonUtil.returnMap(false, 0, "fail", null);
        }
    }

    /**
     * 下载Excel
     *
     * @param response
     */
    @Override
    public void exportmaintable(HttpServletRequest request, HttpServletResponse response) {
        ExcelWriter writer = null;
        OutputStream out = null;
        String fileName = "终端供应商导出_" + CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss");
        Map<String, Object> querymaintable = queryVendorGrid(request);
        List<LinkedHashMap> list = (List<LinkedHashMap>) querymaintable.get("data");
        List<List<String>> data = new ArrayList<>();
        List<String> data1 = new ArrayList<>();
        List<String> collist = new ArrayList<>();
        collist.add("NAME");
        collist.add("JC_NAME");
        collist.add("CREDIT_CODE");
        collist.add("ADDRESS");
        collist.add("MARKET_CONTACT");
        collist.add("MARKET_CONTACT_TEL");
        collist.add("MARKET_CONTACT_EMAIL");
        collist.add("ENGINEERING_CONTACT");
        collist.add("ENGINEERING_CONTACT_TEL");
        collist.add("ENGINEERING_CONTACT_EMAIL");
        collist.add("SB_DEVICE_TYPE");
        collist.add("HTNUM");
        collist.add("ZBNUM");
        collist.add("SB_DEVICE_TYPE");
        List<List<String>> headList = new ArrayList<>();
        headList.add(Arrays.asList("供应商名称"));
        headList.add(Arrays.asList("供应商简称"));
        headList.add(Arrays.asList("统一信用码"));
        headList.add(Arrays.asList("供应商地址"));
        headList.add(Arrays.asList("市场联系人"));
        headList.add(Arrays.asList("市场联系人电话"));
        headList.add(Arrays.asList("市场联系人邮箱"));
        headList.add(Arrays.asList("工程联系人"));
        headList.add(Arrays.asList("工程联系人电话"));
        headList.add(Arrays.asList("工程联系人邮箱"));
        headList.add(Arrays.asList("设备类型"));
        headList.add(Arrays.asList("合同数"));
        headList.add(Arrays.asList("中标终端总数量"));
        headList.add(Arrays.asList("厂家供货总数量"));
        for (LinkedHashMap m : list) {
            data1 = new ArrayList<>();

            for (String key : collist) {
                data1.add(m.containsKey(key) ? (m.get(key) == null ? "" : m.get(key).toString()) : "");
            }
            data.add(data1);
        }
        try {

            out = response.getOutputStream();
            writer = new ExcelWriter(out, ExcelTypeEnum.XLSX);
            Table table = new Table(0);
            table.setHead(headList);

            writer.write0(data, new Sheet(0), table);
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileName + ".xlsx").getBytes(), "ISO8859-1"));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.finish();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public LayJson queryGysPo(HttpServletRequest request) {
        List<Object[]> devList = commonInterface.selectListBySql("select distinct name from dms_terminal_vendor");

        ArrayList list = new ArrayList();
        for (Object[] objects : devList) {
            list.add(objects[0]);
        }

        return new LayJson(200, "请求成功", list, list.size());


    }
}

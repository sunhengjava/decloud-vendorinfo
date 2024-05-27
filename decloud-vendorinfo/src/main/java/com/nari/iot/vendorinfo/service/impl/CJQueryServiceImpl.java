package com.nari.iot.vendorinfo.service.impl;

import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.CJQueryService;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Service("CJQueryService")
@Slf4j
public class CJQueryServiceImpl implements CJQueryService {

    @Autowired
    CommonInterface commonInterface;

    @Override
    public LayJson getListPO(Map<String, Object> map) {
        String supplierName = map.get("supplier_name") == null || map.get("supplier_name") == "" ? null : map.get("supplier_name").toString();
        String currentState = map.get("current_state") == null || map.get("current_state") == "" ? null : map.get("current_state").toString();
        String startTime = map.get("startTime") == null || map.get("startTime") == "" ? null : map.get("startTime").toString();
        String endTime = map.get("endTime") == null || map.get("endTime") == "" ? null : map.get("endTime").toString();
        String gdId = map.get("gdId") == null || map.get("gdId") == "" ? null : map.get("gdId").toString();
        int pageNo = map.get("pageNo") == null || map.get("pageNo") == "" ? 1 : Integer.parseInt(map.get("pageNo").toString());
        int pageSize = map.get("pageSize") == null || map.get("pageSize") == "" ? 50 : Integer.parseInt(map.get("pageSize").toString());
        String sql = "select wk.work_order_id,wk.supplier_name,vendor_credit_code,wk.require_qty_shipped,wk.estimated_qty_shipped,wk.delivery_time||'' ,wk.current_state,\n" +
                "wk.qty_shipped,count(distinct yzht.YZ_HT_ID) as preset_contract,\n" +
                "count(distinct dispatch.id)as fhdxx,max(dispatch.consigner_name),max(dispatch.consigner_phone), WM_CONCAT( DISTINCT yzht.zbcgfs) as zbcgfspj,wk.actual_quantity_received,max(wk.sq_time||''),wk.sj_ht \n" +
                " from dms_work_order  as wk\n" +
                "left join  dms_yz_ht as yzht \n" +
                "on yzht.work_order_id=wk.work_order_id and wk.SJ_HT=yzht.sj_ht\n" +
                "left join dms_dispatch_list as dispatch on wk.WORK_ORDER_ID=dispatch.work_order_id\n" +
                "left join  DMS_IOT_HT_INFO as ht on ht.ht_id=yzht.yz_ht_id where 1=1  ";
        String sqlCount = "  select count(1) from dms_work_order where 1=1 ";

        if (!StringUtils.isBlank(gdId)) {
            sql += " and wk.work_order_id like '%" + gdId + "%' ";
            sqlCount += " and work_order_id  like '%" + gdId + "%' ";
        }
        if (!StringUtils.isBlank(supplierName)) {
            sql += " and wk.supplier_name like '%" + supplierName + "%' ";
            sqlCount += " and supplier_name like '%" + supplierName + "%' ";
        }
        if(StringUtils.isNotBlank(currentState)){
            currentState = currentState.replace(",", "','");
        }
        if (!StringUtils.isBlank(currentState)) {
            sql += "and current_state in('" + currentState + "') ";
            sqlCount += "and current_state in ('" + currentState + "')";
        }
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            sql += "and  sq_time>='" + startTime + "' and sq_time<='" + endTime + "' ";
            sqlCount += "and  sq_time>='" + startTime + "' and sq_time<='" + endTime + "' ";
        }
        sql += " group by  wk.work_order_id,wk.supplier_name,wk.require_qty_shipped,wk.estimated_qty_shipped,wk.delivery_time,\n" +
                "wk.current_state,wk.qty_shipped,wk.actual_quantity_received,vendor_credit_code ,wk.sj_ht limit  " + (pageNo - 1) * pageSize + "," + pageSize;
        System.out.println("厂家发货管理查询" + sql);
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        List<Object[]> devCount = commonInterface.selectListBySql(sqlCount.toString());
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        for (Object[] objs : devList) {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("work_order_id", objs[0] == null ? "-" : objs[0].toString());
            hashMap.put("supplier_name", objs[1] == null ? "-" : objs[1].toString());
            hashMap.put("vendor_credit_code", objs[2] == null ? "-" : objs[2].toString());
            hashMap.put("require_qty_shipped", objs[3] == null ? "-" : objs[3].toString());
            hashMap.put("estimated_qty_shipped", objs[4] == null ? "-" : objs[4].toString());
            hashMap.put("delivery_time", objs[5] == null ? "-" : objs[5].toString());
            hashMap.put("current_state", objs[6] == null ? "-" : objs[6].toString());
            hashMap.put("qty_shipped", objs[7] == null ? "-" : objs[7].toString());
            hashMap.put("preset_contract", objs[8] == null ? "-" : objs[8].toString());
            hashMap.put("fhdxx", objs[9] == null ? "-" : objs[9].toString());
            hashMap.put("consigner_name", objs[10] == null ? "-" : objs[10].toString());
            hashMap.put("consigner_phone", objs[11] == null ? "-" : objs[11].toString());
            hashMap.put("zbcg", objs[12] == null ? "-" : objs[12].toString());
            hashMap.put("actual_quantity_received", objs[13] == null ? "-" : objs[13].toString());
            hashMap.put("sq_time", objs[14] == null ? "-" : objs[14].toString());
            hashMap.put("sj_ht", objs[15] == null ? "-" : objs[15].toString());
            value.add(hashMap);
        }

        String s = devCount.get(0)[0].toString();
        return new LayJson(200, "请求成功", value, Integer.parseInt(s));
    }


    /**
     * @description：查询预置合同详情
     * @author：sunheng
     * @date：2022/11/16 9:17
     * @param：
     */
    @Override
    public LayJson getHtListPO(Map<String, Object> map) {
        String workOrderId = map.get("work_order_id") == null || map.get("work_order_id") == "" ? null : map.get("work_order_id").toString();
        String sjHt = map.get("sj_ht") == null || map.get("sj_ht") == "" ? null : map.get("sj_ht").toString();
        String sql = " select yz_ht_id,yz_ht_name,zbcgfs from dms_yz_ht where work_order_id='" + workOrderId + "' and sj_ht='" + sjHt + "' ";
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        for (Object[] objs : devList) {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("yz_ht_id", objs[0] == null ? "-" : objs[0].toString());
            hashMap.put("yz_ht_name", objs[1] == null ? "-" : objs[1].toString());
            hashMap.put("zbcgfs", objs[2] == null ? "-" : objs[2].toString());
            value.add(hashMap);
        }
        return new LayJson(200, "请求成功", value, value.size());
    }


    /**
     * @description：查询发货单详情
     * @author：sunheng
     * @date：2022/11/16 9:17
     * @param：
     */
    @Override
    public LayJson getFhdListPO(Map<String, Object> map) {
        String workOrderId = map.get("work_order_id") == null || map.get("work_order_id") == "" ? null : map.get("work_order_id").toString();
        String sql = "select shipments_time||'',wlid,logistics_company,qty_shipped,signer_oa,signer_name,reality_qty_shipped,delivery_time||'',apply_for_batch_number  from dms_dispatch_list  where work_order_id='" + workOrderId + "'";
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        for (Object[] objs : devList) {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("shipments_time", objs[0] == null ? "-" : objs[0].toString());
            hashMap.put("id", objs[1] == null ? "-" : objs[1].toString());
            hashMap.put("logistics_company", objs[2] == null ? "-" : objs[2].toString());
            hashMap.put("qty_shipped", objs[3] == null ? "-" : objs[3].toString());
            hashMap.put("signer_oa", objs[4] == null ? "-" : objs[4].toString());
            hashMap.put("shr_name", objs[5] == null ? "-" : objs[5].toString());
            hashMap.put("reality_qty_shipped", objs[6] == null ? "-" : objs[6].toString());
            hashMap.put("delivery_time", objs[7] == null ? "-" : objs[7].toString());
            hashMap.put("apply_for_batch_number", objs[8] == null ? "-" : objs[8].toString());
            value.add(hashMap);
        }
        return new LayJson(200, "请求成功", value, value.size());
    }

    /**
     * @description：查询发货状态
     * @author：sunheng
     * @date：2022/11/16 9:17
     * @param： 根据工单的id、状态 来进行相应的查询
     */
    @Override
    public LayJson getFhztListPO(Map<String, Object> map) {

        String workOrderId = map.get("work_order_id") == null || map.get("work_order_id") == "" ? null : map.get("work_order_id").toString();
        //进行判断工单的状态是不是等于5（已完成）  如果不是的话的话直接显示下面结果
        String sqlOrder = "select current_state,current_state_time||'' from dms_work_order where work_order_id='" + workOrderId + "' ";
        List<Object[]> orderList = commonInterface.selectListBySql(sqlOrder);
        int current_state = 0;
        String current_state_time = "";
        if (orderList.size() > 0&&orderList.get(0).length>0) {
            current_state = Integer.parseInt(orderList.get(0)[0] == null ? "" : orderList.get(0)[0].toString());
            current_state_time = orderList.get(0)[1] == null ? "" : orderList.get(0)[1].toString();
        } else {
            return new LayJson(200, "未找到该数据", null, 0);
        }
        //发货申请，发货审批
        String sql = "select work_order_time||'',sqr_name,sqr_phone,approval_finish_time||'',approval_staff_name,approval_staff_oa,approval_staff_phone,approval_state,xgnr,approval_remarks" +
                " from dms_requistion_work \n" +
                "  where work_order_id='" + workOrderId + "' " +
                " order by work_ORDER_TIME";
        List<HashMap> resultList = new ArrayList<>();
        System.out.println("状态查询1-" + sql);
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < devList.size(); i++) {
            String work_order_time = devList.get(i)[0] == null ? "" : devList.get(i)[0].toString();
            String sqr_name = devList.get(i)[1] == null ? "" : devList.get(i)[1].toString();
            String sqr_phone = devList.get(i)[2] == null ? "" : devList.get(i)[2].toString();
            String approval_finish_time = devList.get(i)[3] == null ? "" : devList.get(i)[3].toString();
            String approval_staff_name = devList.get(i)[4] == null ? "" : devList.get(i)[4].toString();
            String approval_staff_oa = devList.get(i)[5] == null ? "" : devList.get(i)[5].toString();
            String approval_staff_phone = devList.get(i)[6] == null ? "" : devList.get(i)[6].toString();
            String approval_state = devList.get(i)[7] == null ? "" : devList.get(i)[7].toString();
            if (approval_state.equals("1")) {
                approval_state = "通过";
            } else if (approval_state.equals("0")) {
                approval_state = "驳回";
            }
            String xgnr = devList.get(i)[8] == null ? "" : devList.get(i)[8].toString();
            String spbz = devList.get(i)[9] == null ? "" : devList.get(i)[9].toString();
            if (i >= 1) {

                HashMap linkedHashMap = new HashMap();
                linkedHashMap.put("key", "发货重新申请");
                linkedHashMap.put("value", "申请时间 " + work_order_time + " , 申请人:" + sqr_name + " ,手机号码:" + sqr_phone + ",修改内容: " + xgnr);
                resultList.add(linkedHashMap);
            } else {
                HashMap linkedHashMap = new HashMap();
                linkedHashMap.put("key", "发货申请");
                linkedHashMap.put("value", "申请时间 " + work_order_time + " , 申请人:" + sqr_name + " ,手机号码:" + sqr_phone);
                resultList.add(linkedHashMap);
            }

            if (StringUtils.isNotBlank(approval_state)) {
                HashMap linkedHashMap = new HashMap();
                linkedHashMap.put("key", "发货审批");
                linkedHashMap.put("value", "审批时间 " + approval_finish_time + " , 审批人:" + approval_staff_oa + approval_staff_name + " ,手机号码:" + approval_staff_phone + ", 审批结果:" + approval_state + ", 审批备注： " + spbz);
                resultList.add(linkedHashMap);
            } else {
                //已经关闭情况下
                if (current_state == 32) {
                    HashMap linkedHashMap = new HashMap();
                    linkedHashMap.put("key", "已关闭");
                    linkedHashMap.put("value", " 驳回后，经厂家确认，终结流程");
                    resultList.add(linkedHashMap);
                } else {
                    HashMap linkedHashMap = new HashMap();
                    linkedHashMap.put("key", "发货审批");
                    linkedHashMap.put("value", "正在审批中");
                    resultList.add(linkedHashMap);
                }
                return new LayJson(200, "请求成功", resultList, resultList.size());
            }
        }
        if (current_state == 32) {
            HashMap linkedHashMap = new HashMap();
            linkedHashMap.put("key", "已关闭");
            linkedHashMap.put("value", "\t\t\t 驳回后，经厂家确认，终结流程");
            resultList.add(linkedHashMap);
            return new LayJson(200, "请求成功", resultList, (resultList.size() - 1));
        }
        if (current_state == 2) {
            HashMap linkedHashMap = new HashMap();
            linkedHashMap.put("key", "供应商发货");
            linkedHashMap.put("value", "\t\t\t 供应商发货中");
            resultList.add(linkedHashMap);
            return new LayJson(200, "请求成功", resultList, resultList.size());
        }
        if (current_state == 3) {
            HashMap linkedHashMap = new HashMap();
            linkedHashMap.put("key", "待厂家确认");
            linkedHashMap.put("value", "\t\t\t 申请已被驳回-待厂家确认");
            resultList.add(linkedHashMap);
            return new LayJson(200, "请求成功", resultList, resultList.size());
        }
        //供应商发货
        String fhSql = "select shipments_time||'',wlid,consigner_name,consigner_phone,delivery_time,signer_oa,signer_name,signer_phone " +
                " from dms_dispatch_list where work_order_id='" + workOrderId + "' order by shipments_time ";
        List<Object[]> fhList = commonInterface.selectListBySql(fhSql);
        System.out.println("状态查询2-" + fhSql);
        String fh = "";
        for (Object[] objs : fhList) {
            String shipments_time = objs[0] == null ? "" : objs[0].toString();
            String id = objs[1] == null ? "" : objs[1].toString();
            String consigner_name = objs[2] == null ? "" : objs[2].toString();
            String consigner_phone = objs[3] == null ? "" : objs[3].toString();
            String delivery_time = objs[4] == null ? "" : objs[4].toString();
            String signer_oa = objs[5] == null ? "" : objs[5].toString();
            String signer_name = objs[6] == null ? "" : objs[6].toString();
            String signer_phone = objs[7] == null ? "" : objs[7].toString();
            log.info("手机号码为"+consigner_phone);
            fh += "物流单号:" + id + " ,  发货时间:" + shipments_time + ", 发货人:" + consigner_name + " , 手机号:" + consigner_phone ;
        }

        HashMap linkedHashMap1 = new HashMap();
        linkedHashMap1.put("key", "供应商发货");
        linkedHashMap1.put("value", fh);
        resultList.add(linkedHashMap1);
        //检测中心收货
        String shSql = "select shipments_time||'',wlid,consigner_name,consigner_phone,delivery_time||'',signer_oa,signer_name,signer_phone " +
                " from dms_dispatch_list where work_order_id='" + workOrderId + "' order by delivery_time ";
        List<Object[]> shList = commonInterface.selectListBySql(shSql);
        System.out.println("sh状态查询3-" + shSql);
        String sh = "";
        for (Object[] objs : shList) {
            String id = objs[1] == null ? "" : objs[1].toString();
            String delivery_time = objs[4] == null ? "" : objs[4].toString();
            String signer_oa = objs[5] == null ? "" : objs[5].toString();
            String signer_name = objs[6] == null ? "" : objs[6].toString();
            String signer_phone = objs[7] == null ? "" : objs[7].toString();
            if (StringUtils.isNotBlank(delivery_time)) {
                sh += "收货时间:" + delivery_time + " , 物流单:" + id + " , 收货人:" + signer_oa + signer_name + " ,手机号码:" + signer_phone + " \n";
            }
        }
        if (StringUtils.isNotBlank(sh)) {
            sh = sh.substring(0, sh.length() - 2);
        }
        if (StringUtils.isBlank(sh)) {
            if (current_state == 5) {
                HashMap linkedHashMap = new HashMap();
                linkedHashMap.put("key", "检测中心收货");
                linkedHashMap.put("value", "收货时间:    , 物流单:      , 发货人:     ,手机号码:    ");
                resultList.add(linkedHashMap);
                return new LayJson(200, "请求成功", linkedHashMap, resultList.size());
            } else if (current_state >= 4) {
                HashMap linkedHashMap2 = new HashMap();
                linkedHashMap2.put("key", "检测中心收货");
                linkedHashMap2.put("value", "\t\t\t 检测中心收货中");
                resultList.add(linkedHashMap2);
            }

            return new LayJson(200, "请求成功", resultList, resultList.size());
        } else {
            HashMap linkedHashMap2 = new HashMap();
            linkedHashMap2.put("key", "检测中心收货");
            linkedHashMap2.put("value", sh);
            resultList.add(linkedHashMap2);
            if (current_state == 5) {
                HashMap linkedHashMap3 = new HashMap();
                linkedHashMap3.put("key", "已完成");
                linkedHashMap3.put("value", "完成时间:" + current_state_time + ", 工单已完成。");
                resultList.add(linkedHashMap3);
            }

        }
        return new LayJson(200, "请求成功", resultList, resultList.size());
    }

    /**
     * 导出数据
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    public Map<String, Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response) {
        String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss") + ".xls";
        String targetFilePath = request.getSession().getServletContext().getRealPath("download") + File.separator + fileName;
        String sheet = "厂家发货管理";
        String supplier_name = request.getParameter("supplier_name");
        String current_state = request.getParameter("current_state");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String gdId = request.getParameter("gdId");
        StringBuffer sql = new StringBuffer();

        sql.append("select wk.work_order_id,wk.supplier_name,vendor_credit_code,wk.require_qty_shipped,wk.estimated_qty_shipped,wk.delivery_time||'' ,wk.current_state,\n" +
                "wk.qty_shipped,count(distinct yzht.YZ_HT_ID) as preset_contract,\n" +
                "count(distinct dispatch.id)as fhdxx,max(dispatch.consigner_name),max(dispatch.consigner_phone), WM_CONCAT( DISTINCT yzht.zbcgfs) as zbcgfspj,wk.actual_quantity_received,max(wk.sq_time||''),wk.sj_ht \n" +
                " from dms_work_order  as wk\n" +
                "left join  dms_yz_ht as yzht \n" +
                "on yzht.work_order_id=wk.work_order_id and wk.SJ_HT=yzht.sj_ht\n" +
                "left join dms_dispatch_list as dispatch on wk.WORK_ORDER_ID=dispatch.work_order_id\n" +
                "left join  DMS_IOT_HT_INFO as ht on ht.ht_id=yzht.yz_ht_id where 1=1 ");

        if (!StringUtils.isBlank(gdId)) {
            sql.append( " and wk.work_order_id like '%" + gdId + "%' ");
        }
        if (!StringUtils.isBlank(supplier_name)) {
            sql.append(" and wk.supplier_name like '%" + supplier_name + "%' ");
        }

        if(StringUtils.isNotBlank(current_state)){
            current_state = current_state.replace(",", "','");
        }
        if (!StringUtils.isBlank(current_state)) {
            sql.append("and current_state in('" + current_state + "') ");
        }
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            sql.append("and  sq_time>='" + startTime + "' and sq_time<='" + endTime + "' ");
        }
        sql.append("group by   wk.work_order_id,wk.supplier_name,wk.require_qty_shipped,wk.estimated_qty_shipped,wk.delivery_time,\n" +
                " wk.current_state,wk.qty_shipped,wk.actual_quantity_received,vendor_credit_code ,wk.sj_ht ");
        System.out.println("查询" + sql);
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());

        String titles[] = new String[]{"序号", "工单ID", "供应商名称", "统一信用码", "要求发货数量", "预计发货数量", "送达时间", "状态",
                "实际发货数量", "预置合同", "发货单信息", "发货人名称", "联系号码", "招标采购方式", "实际收货数量", "订单创建时间"};
        List<Object[]> newlist = new ArrayList<Object[]>();
        int i = 0;
        for (Object[] temObj : devList) {
            i++;
            Object[] newObj = new Object[16];
            newObj[0] = i;
            newObj[1] = temObj[0] == null || temObj[0].equals("") ? "-" : temObj[0];
            newObj[2] = temObj[1] == null || temObj[1].equals("") ? "-" : temObj[1];
            newObj[3] = temObj[2] == null || temObj[2].equals("") ? "-" : temObj[2];
            newObj[4] = temObj[3] == null || temObj[3].equals("") ? "-" : temObj[3];
            newObj[5] = temObj[4] == null || temObj[4].equals("") ? "-" : temObj[4];
            newObj[6] = temObj[5] == null || temObj[5].equals("") ? "-" : temObj[5];
            //  newObj[7] = temObj[6] == null || temObj[6].equals("") ? "-" : temObj[6];
            if (temObj[6] != null) {
                String ensState = temObj[6].toString();
                if (StringUtils.isBlank(ensState)) {
                    newObj[7] = "-";
                }
                if (ensState.equals("5")) {
                    newObj[7] = "已完成";
                }
                if (ensState.equals("1")) {
                    newObj[7] = "发货已申请";
                }
                if (ensState.equals("2")) {
                    newObj[7] = "发货审核通过";
                }
                if (ensState.equals("3")) {
                    newObj[7] = "发货驳回";
                }
                if (ensState.equals("31")) {
                    newObj[7] = "发货重新申请";
                }
                if (ensState.equals("32")) {
                    newObj[7] = "已关闭";
                }
                if (ensState.equals("4")) {
                    newObj[7] = "已发货";
                }
                if (ensState.equals("5")) {
                    newObj[7] = "已完成";
                }

            } else {
                newObj[7] = "";
            }
            newObj[8] = temObj[7] == null || temObj[7].equals("") ? "-" : temObj[7];
            newObj[9] = temObj[8] == null || temObj[8].equals("") ? "-" : temObj[8];
            newObj[10] = temObj[9] == null || temObj[9].equals("") ? "-" : temObj[9];
            newObj[11] = temObj[10] == null || temObj[10].equals("") ? "-" : temObj[10];
            newObj[12] = temObj[11] == null || temObj[11].equals("") ? "-" : temObj[11];
            newObj[13] = temObj[12] == null || temObj[12].equals("") ? "-" : temObj[12];
            newObj[14] = temObj[13] == null || temObj[13].equals("") ? "-" : temObj[13];
            newObj[15] = temObj[14] == null || temObj[14].equals("") ? "-" : temObj[14];
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


    /**
     * 下载
     *
     * @param request
     * @param response
     */
    public void downloadOnlineExcel(HttpServletRequest request, HttpServletResponse response, String fileName) {
        String filePath = request.getSession().getServletContext().getRealPath("download") + File.separator + fileName;
        downloadFile(request, response, "厂家发货管理-" + fileName, filePath);
    }

    /**
     * 生成
     *
     * @param targetFile
     * @param sheet
     * @param titles
     * @param rows
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


package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.Constant;
import com.nari.iot.vendorinfo.common.HttpUtil;
import com.nari.iot.vendorinfo.entity.Address;
import com.nari.iot.vendorinfo.entity.ApolloConfig;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.entity.LayJsonS;
import com.nari.iot.vendorinfo.service.OrderProjectService;
import com.nari.iot.vendorinfo.service.PurchaseOrdersService;
import jxl.write.DateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;


@Service("PurchaseOrdersService")
@Slf4j
public class PurchaseOrdersServiceImpl implements PurchaseOrdersService {


    @Autowired
    CommonInterface commonInterface;
    @Autowired
    ApolloConfig apolloConfig;
    @Autowired
    private OrderProjectService orderProjectService;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * @description：1 发货申请-新增
     * @author：sunheng
     * @date：2022/11/4 11:43
     * @param：
     */
    @Override
    public LayJson insertSqWorkOrder(Map<String, Object> map) {
        //工单ID
        String workOrderId = map.get("workOrderId") == null || map.get("workOrderId") == "" ? "" : map.get("workOrderId").toString();
        //供应商名称
        String supplierName = map.get("supplierName") == null || map.get("supplierName") == "" ? "" : map.get("supplierName").toString();
        //要求发货数量
        String requireQtyShipped = map.get("requireQtyShipped") == null || map.get("requireQtyShipped") == "" ? "" : map.get("requireQtyShipped").toString();
        //预计发货数量
        String estimatedQtyShipped = map.get("estimatedQtyShipped") == null || map.get("estimatedQtyShipped") == "" ? "" : map.get("estimatedQtyShipped").toString();
        //送达时间
        String deliveryTime = map.get("deliveryTime") == null || map.get("deliveryTime") == "" ? "" : map.get("deliveryTime").toString();
        //申请时间
        String currentStateTime = map.get("currentStateTime") == null || map.get("currentStateTime") == "" ? "" : map.get("currentStateTime").toString();

        String currentState = map.get("currentState") == null || map.get("currentState") == "" ? "" : map.get("currentState").toString();
        String sqr_name = map.get("sqr_name") == null || map.get("sqr_name") == "" ? "" : map.get("sqr_name").toString();
        String sqr_phone = map.get("sqr_phone") == null || map.get("sqr_phone") == "" ? "" : map.get("sqr_phone").toString();
        //插入工单表
        String insertWorkOrder = "insert into dms_work_order (work_order_id,supplier_name,REQUIRE_QTY_SHIPPED,ESTIMATED_QTY_SHIPPED,DELIVERY_TIME,CURRENT_STATE,CURRENT_STATE_TIME,SQ_TIME) \n" + "values ('" + workOrderId + "','" + supplierName + "'," + requireQtyShipped + "," + estimatedQtyShipped + ",'" + deliveryTime + "','" + currentState + "','" + currentStateTime + "','" + currentStateTime + "')";
        log.info("调用了4.2发货申请新增接口,插入工单表：" + insertWorkOrder);
        boolean b = commonInterface.dbAccess_insert(insertWorkOrder);
        if (b == true) {
            //插入申请表
            String inserRequistionWork = "insert into dms_requistion_work (work_order_id,work_order_time,ESTIMATED_QTY_SHIPPED,DELIVERY_TIME,sqr_name,sqr_phone) " + "values ('" + workOrderId + "','" + currentStateTime + "'," + estimatedQtyShipped + ",'" + deliveryTime + "','" + sqr_name + "','" + sqr_phone + "')";

            boolean b1 = commonInterface.dbAccess_insert(inserRequistionWork);
            log.info("调用了4.2发货申请新增接口,插入申请表：" + insertWorkOrder + "\n 结果为" + b1);
            if (b1 == true) {
                return new LayJson(200, "插入成功", null, 1);

            } else {
                return new LayJson(501, "插工单表成功，但申请表失败", null, 0);
            }
        }
        return new LayJson(500, "插入失败", null, 0);
    }

    /**
     * @description：1 发货申请-修改(修改的时候相当于重新申请了) 重新申请31 、闭环32
     * @author：sunheng
     * @date：2022/11/4 11:39
     * @param： {
     * workOrderId:            工单ID,
     * estimatedQtyShipped:    预计发货数量,
     * currentStateTime:       当前状态时间,
     * xgnr:                   修改内容
     * }
     */
    @Override
    public LayJson updateSqWorkOrder(Map<String, Object> map) {
        String workOrderId = map.get("workOrderId") == null || map.get("workOrderId") == "" ? "" : map.get("workOrderId").toString();
        String currentStateTime = map.get("currentStateTime") == null || map.get("currentStateTime") == "" ? "" : map.get("currentStateTime").toString();
        String currentState = map.get("currentState") == null || map.get("currentState") == "" ? "" : map.get("currentState").toString();
        String sqr_name = map.get("sqr_name") == null || map.get("sqr_name") == "" ? "" : map.get("sqr_name").toString();
        String sqr_phone = map.get("sqr_phone") == null || map.get("sqr_phone") == "" ? "" : map.get("sqr_phone").toString();
        if (currentState.equals("32")) {
            String updateWorkOrder = "update dms_work_order set   current_state='" + currentState + "' , current_state_time='" + currentStateTime + "' " + " where work_order_id='" + workOrderId + "' and is_valid=1";
            commonInterface.dbAccess_insert(updateWorkOrder);
            return new LayJson(200, "关闭成功", null, 1);
        } else {
            String estimatedQtyShipped = map.get("estimatedQtyShipped") == null || map.get("estimatedQtyShipped") == "" ? "" : map.get("estimatedQtyShipped").toString();
            String deliveryTime = map.get("deliveryTime") == null || map.get("deliveryTime") == "" ? "" : map.get("deliveryTime").toString();
            String xgnr = map.get("xgnr") == null || map.get("xgnr") == "" ? "" : map.get("xgnr").toString();
            //修改工单表、修改申请表
            String updateWorkOrder = "update dms_work_order  set estimated_qty_shipped='" + estimatedQtyShipped + "' , delivery_time='" + deliveryTime + "' ,  " + " current_state='" + currentState + "' , current_state_time='" + currentStateTime + "' " + " where work_order_id='" + workOrderId + "' and is_valid=1";
            boolean b = commonInterface.dbAccess_update(updateWorkOrder);
            if (b == true) {
                //插入申请表
                String inserRequistionWork = "insert into dms_requistion_work (work_order_id,work_order_time,ESTIMATED_QTY_SHIPPED,DELIVERY_TIME,XGNR,sqr_name,sqr_phone) \n" + "values ('" + workOrderId + "','" + currentStateTime + "'," + estimatedQtyShipped + ",'" + deliveryTime + "','" + xgnr + "','" + sqr_name + "','" + sqr_phone + "')";
                boolean b1 = commonInterface.dbAccess_insert(inserRequistionWork);
                if (b1 == true) {
                    return new LayJson(200, "插入成功", null, 1);

                } else {
                    return new LayJson(501, "插申请表失败", null, 0);
                }
            }
        }

        return new LayJson(500, "修改失败", null, 0);
    }

    /**
     * @description：2 发货审批-新增接口
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param： {approvalStaffName：审批人name
     * approvalStaffOA：审批人oa
     * approvalFinishTime:审批完成时间
     * approvalRemarks：审批备注
     * approvalState：审批状态   1 通过 0不通过
     * workOrderTime:工单时间
     * }
     */
    @Override
    public LayJson insertSpWorkOrder(Map<String, Object> map) {
        String workOrderId = map.get("workOrderId") == null || map.get("workOrderId") == "" ? "" : map.get("workOrderId").toString();
        String approvalStaffName = map.get("approvalStaffName") == null || map.get("approvalStaffName") == "" ? "" : map.get("approvalStaffName").toString();
        String approvalStaffOA = map.get("approvalStaffOA") == null || map.get("approvalStaffOA") == "" ? "" : map.get("approvalStaffOA").toString();
        String approvalFinishTime = map.get("approvalFinishTime") == null || map.get("approvalFinishTime") == "" ? "" : map.get("approvalFinishTime").toString();
        String approvalRemarks = map.get("approvalRemarks") == null || map.get("approvalRemarks") == "" ? "" : map.get("approvalRemarks").toString();
        String approvalStaffPhone = map.get("approvalStaffPhone") == null || map.get("approvalStaffPhone") == "" ? "" : map.get("approvalStaffPhone").toString();

        String approvalState = "1";
        String currentState = map.get("currentState") == null || map.get("currentState") == "" ? "" : map.get("currentState").toString();

        //currentState =3驳回、=2通过
        if (currentState.equals("3")) {
            approvalState = "0";
        }
        String sql = "update dms_requistion_work set approval_staff_name='" + approvalStaffName + "',approval_staff_oA='" + approvalStaffOA + "' ,APPROVAL_FINISH_TIME='" + approvalFinishTime + "' " + ",approval_remarks='" + approvalRemarks + "',approval_state='" + approvalState + "' ,approval_staff_phone='" + approvalStaffPhone + "'   where work_order_id='" + workOrderId + "' and work_order_time=(select max(work_order_time)from dms_requistion_work where work_order_id='" + workOrderId + "' ) ";
        boolean b = false;
        try {
            b = commonInterface.dbAccess_update(sql);
            if (b == true) {
                String sqlOrder = "update dms_work_order SET current_state='" + currentState + "' ,  current_state_time='" + approvalFinishTime + "',  sp_time='" + approvalFinishTime + "' where work_order_id='" + workOrderId + "' and is_valid=1";
                boolean b1 = commonInterface.dbAccess_update(sqlOrder);
                if (b1 == true) {
                    return new LayJson(200, "插入成功", null, 1);

                } else {
                    return new LayJson(501, "插入成功但更新work_order表失败", null, 0);
                }
            }
        } catch (Exception e) {
            log.info("5-3插入报错" + e.getMessage());
        }
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'5-3','" + JSONObject.toJSONString(map) + "','','" + b + "')");
        return new LayJson(500, "插入失败", null, 0);
    }


    /**
     * @description：2 发货审批-更新接口
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param： {
     * workOrderId:工单id
     * estimatedQtyShipped：预计发货数量
     * deliveryTime:送达时间
     * xgnr :修改内容
     * }
     */
    @Override
    public LayJson updateSpWorkOrder(Map<String, Object> map) {
        String workOrderId = map.get("workOrderId") == null || map.get("workOrderId") == "" ? "" : map.get("workOrderId").toString();
        String estimatedQtyShipped = map.get("estimatedQtyShipped") == null || map.get("estimatedQtyShipped") == "" ? "" : map.get("estimatedQtyShipped").toString();
        String deliveryTime = map.get("deliveryTime") == null || map.get("deliveryTime") == "" ? "" : map.get("deliveryTime").toString();
        String xgnr = map.get("xgnr") == null || map.get("xgnr") == "" ? null : map.get("xgnr").toString();
        String sql = " update dms_work_order  set ESTIMATED_QTY_SHIPPED='" + estimatedQtyShipped + "' , delivery_time='" + deliveryTime + "' where work_order_id='" + workOrderId + "' and is_valid=1";
        boolean b = false;
        try {
            b = commonInterface.dbAccess_update(sql);
            if (b == true) {
                String sqlOrder = " update dms_requistion_work set xgnr='" + xgnr + "' where work_order_id='" + workOrderId + "'and work_order_time=(" + " select MAX(work_order_time) from dms_requistion_work where work_order_id='" + workOrderId + "' ) ";
                boolean b1 = commonInterface.dbAccess_update(sqlOrder);
                if (b1 == true) {
                    return new LayJson(200, "更新成功", null, 1);

                } else {
                    return new LayJson(501, "更新成功但更新dms_requistion_work表失败", null, 0);
                }
            }
        } catch (Exception e) {
            log.info("5-4插入报错" + e.getMessage());
        }
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'5-4','" + JSONObject.toJSONString(map) + "','','" + b + "')");
        return new LayJson(500, "更新失败", null, 0);
    }

    /**
     * @description：2 发货审批-删除
     * @author：sunheng
     * @date：2022/11/4 16:33
     * @param：
     */
    @Override
    public LayJson deleteSpWorkOrder(HttpServletRequest request) {
        String workOrderId = request.getParameter("workOrderId") == null || request.getParameter("workOrderId") == "" ? "" : request.getParameter("workOrderId").toString();
        String sql = "update  dms_work_order set is_valid=0 where work_order_id='" + workOrderId + "'";
        boolean b1 = commonInterface.dbAccess_update(sql);
        if (b1) {
            return new LayJson(200, "删除成功", null, 1);
        }
        return new LayJson(500, "删除失败", null, 0);
    }

    /**
     * @description：3 供应商发货-新增
     * @author：sunheng
     * @date：2022/11/4 17:46
     * @param：{ fhdId:发货单ID, workOrderId:申请单ID,consignerName:发货人姓名,consignerPhone:发货人电话, shrName:收货人姓名, shrPhone:收货人电话
     * qtyShipped:发货数量, shipmentsTime:发货时间, logisticsCompany:物流公司 }
     */
    @Override
    public LayJson insertFhWorkOrder(Map<String, Object> map) {
        String fhdId = map.get("fhdId") == null || map.get("fhdId") == "" ? "" : map.get("fhdId").toString();
        String wlId = map.get("wlId") == null || map.get("wlId") == "" ? "" : map.get("wlId").toString();
        String workOrderId = map.get("workOrderId") == null || map.get("workOrderId") == "" ? "" : map.get("workOrderId").toString();
        String consignerName = map.get("consignerName") == null || map.get("consignerName") == "" ? "" : map.get("consignerName").toString();
        String consignerPhone = map.get("consignerPhone") == null || map.get("consignerPhone") == "" ? "" : map.get("consignerPhone").toString();
        String shrName = map.get("shrName") == null || map.get("shrName") == "" ? "" : map.get("shrName").toString();
        String shrPhone = map.get("shrPhone") == null || map.get("shrPhone") == "" ? "" : map.get("shrPhone").toString();
        String qtyShipped = map.get("qtyShipped") == null || map.get("qtyShipped") == "" ? "0" : map.get("qtyShipped").toString();
        String shipmentsTime = map.get("shipmentsTime") == null || map.get("shipmentsTime") == "" ? "" : map.get("shipmentsTime").toString();
        String logisticsCompany = map.get("logisticsCompany") == null || map.get("logisticsCompany") == "" ? "" : map.get("logisticsCompany").toString();

        String sql = " insert into dms_dispatch_list (id,work_order_id,consigner_name,consigner_phone,shr_name,shr_phone,qty_shipped,shipments_time,logistics_company,wlid)\n" + "        values ('" + fhdId + "','" + workOrderId + "','" + consignerName + "','" + consignerPhone + "','" + shrName + "','" + shrPhone + "','" + qtyShipped + "','" + shipmentsTime + "','" + logisticsCompany + "','" + wlId + "')";
        String sqlShipped = " select  qty_shipped from dms_work_order where  work_order_id='" + workOrderId + "' ";
        List<Object[]> list = commonInterface.selectListBySql(sqlShipped);
        log.info("供应商发货" + sql);
        if (list.size() < 1) {
            return new LayJson(500, "新增失败,未找到该申请单", null, 0);
        }
        Object[] objects = list.get(0);
        boolean b = false;
        try {
            b = commonInterface.dbAccess_insert(sql);
            if (b) {
                int qty_shipped = Integer.parseInt(objects[0] != null ? objects[0].toString() : "0") + Integer.parseInt(qtyShipped);
                //更新申请单中 发货数量
                String sqlOrder = "  update dms_work_order set  qty_shipped= " + qty_shipped + "  where  work_order_id='" + workOrderId + "'";
                commonInterface.dbAccess_update(sqlOrder);
                return new LayJson(200, "新增成功", null, 1);
            }
        } catch (Exception e) {
            log.info("6.3供应商发货新增接口报错" + e.getMessage());
        }
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'6-3','" + JSONObject.toJSONString(map) + "','','" + b + "')");
        return new LayJson(500, "新增失败", null, 0);
    }

    /**
     * @description：3 供应商发货-修改
     * @author：sunheng
     * @date：2022/11/4 17:46
     * @param：{ fhdId:发货单ID, work_order_id:申请单ID,shipmentsTime:发货时间, zbcgfs:招标采购方式,
     * yzht：{ "yzHtId":"预置合同id","yzHtName" :预置合同名称  }
     * }
     */
    @Override
    public LayJson updateFhWorkOrder(Map<String, Object> map) {
        String workOrderId = map.get("workOrderId") == null || map.get("workOrderId") == "" ? "" : map.get("workOrderId").toString();
        String shipmentsTime = map.get("shipmentsTime") == null || map.get("shipmentsTime") == "" ? "" : map.get("shipmentsTime").toString();
        String currentState = map.get("currentState") == null || map.get("currentState") == "" ? "" : map.get("currentState").toString();
        List<Map<String, Object>> yzht = map.get("yzht") == null || map.get("yzht") == "" ? new ArrayList<>() : (List<Map<String, Object>>) map.get("yzht");
        String sqlOrder = " update dms_work_order set current_state='" + currentState + "' ,CURRENT_STATE_TIME='" + shipmentsTime + "'  where  work_order_id='" + workOrderId + "'";
        boolean b = false;
        try {
            b = commonInterface.dbAccess_update(sqlOrder);
            if (b) {
                for (Map<String, Object> li : yzht) {
                    String yzHtId = li.get("yzHtId") == null || li.get("yzHtId") == "" ? "" : li.get("yzHtId").toString();
                    String yzHtName = li.get("yzHtName") == null || li.get("yzHtName") == "" ? "" : li.get("yzHtName").toString();
                    String zbcgfs = li.get("zbcgfs") == null || li.get("zbcgfs") == "" ? "" : li.get("zbcgfs").toString();
                    String sql = "insert  into dms_yz_ht(yz_ht_id,yz_ht_name,work_order_id,zbcgfs)values('" + yzHtId + "','" + yzHtName + "','" + workOrderId + "','" + zbcgfs + "')";
                    commonInterface.dbAccess_update(sql);
                }
                return new LayJson(200, "修改成功", null, 1);
            }
        } catch (Exception e) {
            log.info("6.4供应商发货-修改接口" + e.getMessage());
        }

        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'6-4','" + JSONObject.toJSONString(map) + "','','" + b + "')");
        return new LayJson(500, "修改失败", null, 0);
    }

    /**
     * @description：4、检测中心收货-新增
     * @author：sunheng
     * @date：2022/11/5 17:07
     * @param：{ fhdId:发货单id
     * workOrderId：申请单id
     * realityQtyShipped：实际发货数量
     * deliveryTime：入库时间
     * applyForBatchNumber：申请批次号
     * signerOa：签收人oa
     * signerName：签收人姓名
     * signerPhone：签收人电话
     * }
     */
    @Override
    public LayJson insertShWorkOrder(Map<String, Object> map) {
        String fhdId = map.get("fhdId") == null || map.get("fhdId") == "" ? "" : map.get("fhdId").toString();
        String workOrderId = map.get("workOrderId") == null || map.get("workOrderId") == "" ? "" : map.get("workOrderId").toString();
        String realityQtyShipped = map.get("realityQtyShipped") == null || map.get("realityQtyShipped") == "" ? "0" : map.get("realityQtyShipped").toString();
        String deliveryTime = map.get("deliveryTime") == null || map.get("deliveryTime") == "" ? "" : map.get("deliveryTime").toString();
        String applyForBatchNumber = map.get("applyForBatchNumber") == null || map.get("applyForBatchNumber") == "" ? "" : map.get("applyForBatchNumber").toString();
        String signerOa = map.get("signerOa") == null || map.get("signerOa") == "" ? "" : map.get("signerOa").toString();
        String signerName = map.get("signerName") == null || map.get("signerName") == "" ? "" : map.get("signerName").toString();
        String signerPhone = map.get("signerPhone") == null || map.get("signerPhone") == "" ? "" : map.get("signerPhone").toString();
        String sqlDispatch = "update dms_dispatch_list set reality_qty_shipped='" + realityQtyShipped + "' ,delivery_time='" + deliveryTime + "',apply_for_batch_number='" + applyForBatchNumber + "', " + "signer_oa='" + signerOa + "',signer_name='" + signerName + "',signer_phone='" + signerPhone + "' where id='" + fhdId + "'";
        log.info("输入sql" + sqlDispatch);
        String sqlShipped = " select  actual_quantity_received from dms_work_order where  work_order_id='" + workOrderId + "' ";
        List<Object[]> list = commonInterface.selectListBySql(sqlShipped);
        if (list.size() < 1) {
            return new LayJson(500, "新增失败,未找到该申请单", null, 0);
        }
        Object[] objects = list.get(0);
        boolean b = commonInterface.dbAccess_update(sqlDispatch);
        if (b) {
            //更新申请单中 发货数量
            int actual_quantity_received = Integer.parseInt(objects[0] != null ? objects[0].toString() : "0") + Integer.parseInt(realityQtyShipped);
            String sqlOrder = " update dms_work_order set  actual_quantity_received=" + actual_quantity_received + " where  work_order_id='" + workOrderId + "'";
            commonInterface.dbAccess_update(sqlOrder);
            return new LayJson(200, "新增成功", null, 1);
        }
        return new LayJson(500, "新增失败", null, 0);

    }

    /**
     * @description：4、检测中心收货-修改(修改申请单的 状态、状态时间)
     * @author：sunheng
     * @date：2022/11/5 17:08
     * @param：
     */
    @Override
    public LayJson updateShWorkOrder(Map<String, Object> map) {
        String workOrderId = map.get("workOrderId") == null || map.get("workOrderId") == "" ? "" : map.get("workOrderId").toString();
        String currentStateTime = map.get("currentStateTime") == null || map.get("currentStateTime") == "" ? "" : map.get("currentStateTime").toString();
        String currentState = map.get("currentState") == null || map.get("currentState") == "" ? "" : map.get("currentState").toString();
        String sql = "update dms_work_order set current_state='" + currentState + "' ,current_state_time='" + currentStateTime + "'  where  work_order_id='" + workOrderId + "'";
        boolean b = commonInterface.dbAccess_update(sql);

        if (b) {

            return new LayJson(200, "修改成功", null, 1);
        }
        return new LayJson(200, "修改失败", null, 0);
    }


    /**
     * @description：5、检测中心全检-新增(新增实际匹配合同信息)
     * @author：sunheng
     * @date：2022/11/5 17:08
     * @param：{ ”workOrderId”:xx”,		//申请单id
     * ”ht”:[{
     * “htId”:”合同Id”,
     * “htName”:”合同Nmae”,
     * “zbcgfs”:”招标采购方式”,
     * }....]
     * }
     */
    @Override
    public LayJson addQjWorkOrder(Map<String, Object> map) {
        String workOrderId = map.get("workOrderId") == null || map.get("workOrderId") == "" ? "" : map.get("workOrderId").toString();
        List<Map<String, Object>> yzht = map.get("ht") == null || map.get("ht") == "" ? new ArrayList<>() : (List<Map<String, Object>>) map.get("ht");
        String sqlOrder = " update dms_work_order set sj_ht='1' where  work_order_id='" + workOrderId + "'";
        boolean b = commonInterface.dbAccess_update(sqlOrder);
        if (b) {
            for (Map<String, Object> li : yzht) {
                String yzHtId = li.get("htId") == null || li.get("htId") == "" ? "" : li.get("htId").toString();
                String yzHtName = li.get("htName") == null || li.get("htName") == "" ? "" : li.get("htName").toString();
                String zbcgfs = li.get("zbcgfs") == null || li.get("zbcgfs") == "" ? "" : li.get("zbcgfs").toString();
                String sql = "insert  into dms_yz_ht(yz_ht_id,yz_ht_name,work_order_id,zbcgfs,sj_ht)values('" + yzHtId + "','" + yzHtName + "','" + workOrderId + "','" + zbcgfs + "',1)";
                log.info("进行更新了yzht表" + sql);
                commonInterface.dbAccess_update(sql);
            }
        } else {
            return new LayJson(501, "修改订单表中实际合同状态失败", null, 0);
        }

        return new LayJson(200, "新增成功", null, 0);
    }

    /**
     * @description：10.4 终端调试状-新增 （终端调试状态新增接口）
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @Override
    public LayJson addTsWorkOrder(Map<String, Object> map) {
        String terminal_debugging_status = map.get("terminalDebuggingStatus") == null || map.get("terminalDebuggingStatus") == "" ? "" : map.get("terminalDebuggingStatus").toString();
        String termEsn = map.get("termEsn") == null || map.get("termEsn") == "" ? "" : map.get("termEsn").toString();
        String tsTime = map.get("tsTime") == null || map.get("tsTime") == "" ? "" : map.get("tsTime").toString();

        String sqlOrder = " update dms_iot_device_resource_info  set terminal_debugging_status='" + terminal_debugging_status + "',ts_time='" + tsTime + "',tm_dqzt=5 where  term_esn='" + termEsn + "' and is_valid=1";
        log.info("终端调试状态插入sql" + JSON.toJSONString(map));
        boolean b = commonInterface.dbAccess_update(sqlOrder);
        if (b) {

            commonInterface.dbAccess_update("update iot_device set is_zdts='" + terminal_debugging_status + "' , is_zdts_time='" + tsTime + "'    where  dev_label='" + termEsn + "' and is_valid=1 and out_iot_fac=2 ");
            return new LayJson(200, "修改成功", null, 1);
        }
        return new LayJson(500, "修改失败", null, 0);
    }

    /*订单发货-插入终端配送单信息*/
    @Override
    public LayJson addZdpsTerm(Map<String, Object> map) {
        String termDispatchId = map.get("termDispatchId") == null || map.get("termDispatchId") == "" ? "" : map.get("termDispatchId").toString();
        String termNumber = map.get("termNumber") == null || map.get("termNumber") == "" ? "" : map.get("termNumber").toString();
        String esn = map.get("esn") == null || map.get("esn") == "" ? "" : map.get("esn").toString();
        String pmsCityCompanyId = map.get("pmsCityCompanyId") == null || map.get("pmsCityCompanyId") == "" ? "" : map.get("pmsCityCompanyId").toString();
        String pmsCityCompanyName = map.get("pmsCityCompanyName") == null || map.get("pmsCityCompanyName") == "" ? "" : map.get("pmsCityCompanyName").toString();
        String pmsCountyCompanyId = map.get("pmsCountyCompanyId") == null || map.get("pmsCountyCompanyId") == "" ? "" : map.get("pmsCountyCompanyId").toString();
        String pmsCountyCompanyName = map.get("pmsCountyCompanyName") == null || map.get("pmsCountyCompanyName") == "" ? "" : map.get("pmsCountyCompanyName").toString();
        String orderId = map.get("orderId") == null || map.get("orderId") == null ? "" : map.get("orderId").toString();
        String orderName = map.get("orderName") == null || map.get("orderName") == "" ? "" : map.get("orderName").toString();
        String htId = map.get("htId") == null || map.get("htId") == "" ? "" : map.get("htId").toString();
        String htName = map.get("htName") == null || map.get("htName") == "" ? "" : map.get("htName").toString();
        String zbcgfs = map.get("zbcgfs") == null || map.get("zbcgfs") == "" ? "" : map.get("zbcgfs").toString();
        String shipper = map.get("shipper") == null || map.get("shipper") == "" ? "" : map.get("shipper").toString();
        String shipperPhone = map.get("shipperPhone") == null || map.get("shipperPhone") == "" ? "" : map.get("shipperPhone").toString();
        String recipitnt = map.get("recipitnt") == null || map.get("recipitnt") == "" ? "" : map.get("recipitnt").toString();
        String recipitntPhone = map.get("recipitntPhone") == null || map.get("recipitntPhone") == "" ? "" : map.get("recipitntPhone").toString();
        String psState = map.get("psState") == null || map.get("psState") == "" ? "" : map.get("psState").toString();
        String psTime = map.get("psTime") == null || map.get("psTime") == "" ? "" : map.get("psTime").toString();
        String wld = map.get("wld") == null || map.get("wld") == "" ? "" : map.get("wld").toString();
        String sqlCity = "select org_id,name from d5000.dms_gf_org_info  where pms_recode in('" + pmsCityCompanyId + "')";
        List<Object[]> listCity = commonInterface.selectListBySql(sqlCity);
        String cityId = "";
        String cityName = "";
        String countyId = "";
        String countyName = "";
        /*查询pms对应公司id*/
        if (listCity.size() > 0 && listCity.get(0) != null) {
            cityId = listCity.get(0)[0].toString();
            cityName = listCity.get(0)[1].toString();
        }
        String sqlCounty = "select org_id,name from d5000.dms_gf_org_info  where pms_recode in('" + pmsCountyCompanyId + "')";
        List<Object[]> listCounty = commonInterface.selectListBySql(sqlCounty);
        if (listCounty.size() > 0 && listCounty.get(0) != null) {
            countyId = listCounty.get(0)[0].toString();
            countyName = listCounty.get(0)[1].toString();
        }


        String sqlOrder = " insert into dms_termesn_dispatch (TERM_DISPATCH_ID,\n" + "TERM_NUMBER,\n" + "CITY_COMPANY_ID,\n" + "\n" + "CITY_COMPANY_NAME,\n" + "COUNTY_COMPANY_ID,\n" + "COUNTY_COMPANY_NAME,\n" + "ORDER_ID,\n" + "ORDER_NAME,\n" + "HT_ID,\n" + "HT_NAME,\n" + "\n" + "ZBCGFS,\n" + "SHIPPER,\n" + "SHIPPER_PHONE,\n" + "RECIPITNT,\n" + "RECIPITNT_PHONE,\n" + "\n" + "PS_STATE,\n" + "PS_TIME,PMS_CITY_COMPANY_ID,PMS_CITY_COMPANY_NAME,PMS_COUNTY_COMPANY_ID,PMS_COUNTY_COMPANY_NAME,WLD) values('" + termDispatchId + "'," + termNumber + ",'" + cityId + "',\n" + "'" + cityName + "','" + countyId + "','" + countyName + "','" + orderId + "','" + orderName + "','" + htId + "','" + htName + "',\n" + "'" + zbcgfs + "','" + shipper + "','" + shipperPhone + "','" + recipitnt + "','" + recipitntPhone + "',\n" + " '" + psState + "','" + psTime + "','" + pmsCityCompanyId + "','" + pmsCityCompanyName + "','" + pmsCountyCompanyId + "','" + pmsCountyCompanyName + "','" + wld + "') ";
        boolean b = commonInterface.dbAccess_update(sqlOrder);
        if (StringUtils.isNotBlank(esn)) {
            String[] split = esn.split(",");
            for (int i = 0; i < split.length; i++) {
                commonInterface.dbAccess_update(" update dms_iot_device_resource_info  set  tm_dqzt='" + psState + "',link_dispatch='" + termDispatchId + "'  where  term_esn='" + split[i] + "' and is_valid=1");
            }
            //更新订单状态
            log.info("开始调用了订单状态修改接口6、4");
            orderProjectService.upOrderState(orderId, 6, 4);
        }

        try {
            commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'12-3','" + JSONObject.toJSONString(map) + "','新增配送单','" + b + "')");
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        if (b) {
            return new LayJson(200, "新增成功", null, 1);
        }
        return new LayJson(500, "新增失败", null, 0);
    }

    /*订单收货-修改终端配送单（状态、签收人、签收人oa、签收时间）*/
    @Override
    public LayJson addZdshTerm(Map<String, Object> map) {
        String psState = map.get("psState") == null || map.get("psState") == "" ? "" : map.get("psState").toString();
        String qsTime = map.get("qsTime") == null || map.get("qsTime") == "" ? "" : map.get("qsTime").toString();
        String qsName = map.get("qsName") == null || map.get("qsName") == "" ? "" : map.get("qsName").toString();
        String qsOa = map.get("qsOa") == null || map.get("qsOa") == "" ? "" : map.get("qsOa").toString();
        String qsPhone = map.get("qsPhone") == null || map.get("qsPhone") == "" ? "" : map.get("qsPhone").toString();
        String termDispatchId = map.get("termDispatchId") == null ? "" : map.get("termDispatchId").toString();
        String sqlOrder = " update  dms_termesn_dispatch  set " + " PS_STATE='" + psState + "' ," + " QS_NAME='" + qsName + "'," + " QS_OA='" + qsOa + "'," + " QS_TIME='" + qsTime + "',QS_PHONE='" + qsPhone + "' where   term_dispatch_id='" + termDispatchId + "' ";

        boolean b = commonInterface.dbAccess_update(sqlOrder);
        //String sqlEsn = "select esn from dms_termesn_dispatch   where term_dispatch_id='" + termDispatchId + "'  ";
        String sqlEsn = "select WM_CONCAT(DISTINCT TERM_ESN) from dms_iot_device_resource_info   where LINK_DISPATCH='" + termDispatchId + "'   and is_valid=1 ";

        List<Object[]> listEsn = commonInterface.selectListBySql(sqlEsn);
        System.out.println(sqlEsn);
        if (listEsn != null && listEsn.get(0) != null && listEsn.get(0)[0] != null) {
            String esn = listEsn.get(0)[0].toString();
            String[] split = esn.split(",");
            for (int i = 0; i < split.length; i++) {
                commonInterface.dbAccess_update("update dms_iot_device_resource_info  set  tm_dqzt='" + psState + "' where  term_esn='" + split[i] + "'  and is_valid=1 ");
            }
            //查询对应的订单
            String sql = "select order_id from dms_termesn_dispatch where term_dispatch_id='" + termDispatchId + "'";
            List<Object[]> list = commonInterface.selectListBySql(sql);
            if (list.size() > 0) {
                Object[] orderIdObject = list.get(0);
                if (orderIdObject.length > 0) {
                    orderProjectService.upOrderState(orderIdObject[0].toString(), 7, 5);
                }
            }
        }
        if (b) {
            return new LayJson(200, "修改成功", null, 1);
        }
        return new LayJson(500, "修改失败", null, 0);
    }

    /**
     * @description：参数配置-新增/修改接口 （配置是否成功标识，状态时间、 配变容量、CT变比）
     * @author：sunheng
     * @date：2022/11/22 19:25
     * @param：
     */
    @Override
    public LayJson addCsPzTerm(Map<String, Object> map) {
        String cspz_state = map.get("cspzState") == null || map.get("cspzState") == "" ? "" : map.get("cspzState").toString();
        String cspzStateTime = map.get("cspzStateTime") == null || map.get("cspzStateTime") == "" ? "" : map.get("cspzStateTime").toString();
        String pbrl = map.get("pbrl") == null || map.get("pbrl") == "" ? "" : map.get("pbrl").toString();
        String ctbb = map.get("ctbb") == null || map.get("ctbb") == "" ? "" : map.get("ctbb").toString();
        String termEsn = map.get("termEsn") == null || map.get("termEsn") == "" ? "" : map.get("termEsn").toString();
        String sqlOrder = " update dms_iot_device_resource_info  set cspz_state='" + cspz_state + "', cspz_state_time='" + cspzStateTime + "' , pbrl='" + pbrl + "',ctbb='" + ctbb + "'  where  term_esn='" + termEsn + "' and is_valid=1";
        boolean b = commonInterface.dbAccess_update(sqlOrder);
        if (b) {
            return new LayJson(200, "修改成功", null, 1);
        }
        return new LayJson(500, "修改失败", null, 0);
    }


    @Override
    public LayJsonS addCsPzTermSuper(Map<String, Object> map) {
        SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = SDF.format(new Date());
        //CT一次值
        String ct1 = map.get("ct1") == null || map.get("ct1") == "" ? "" : map.get("ct1").toString();
        String ct2 = map.get("ct2") == null || map.get("ct2") == "" ? "" : map.get("ct2").toString();
        //配变容量
        String dtCapacity = map.get("dtCapacity") == null || map.get("dtCapacity") == "" ? "" : map.get("dtCapacity").toString();
        String esn = map.get("esn") == null || map.get("esn") == "" ? "" : map.get("esn").toString();
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),16-3,'" + JSONObject.toJSONString(map) + "','" + esn + "',1)");
        String dataSourceName = apolloConfig.getDevice_mode_name();
        dataSourceName = "'" + dataSourceName.replace(",", "','") + "'";
        log.info("调用addCsPzTermSuper的dataSourceName参数值为——————" + dataSourceName);
        //对应iot_device表 out_dev_id、dms_region_id
        //查询边设备
        String sql = "select id from iot_device where  dev_label='" + esn + "'   and is_valid=1 and out_iot_fac =2 and connect_mode=1";
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        log.info("调用addCsPzTermSuper查询iot的sql1为——————" + sql);
        log.info("调用addCsPzTermSuper查询iot的sql1结果总条数——————" + devList.size());
        for (Object[] objs : devList) {
            log.info("调用addCsPzTermSuper循环消息为——————" + JSONObject.toJSONString(objs));
            if (objs[0] != null && StringUtils.isNotBlank(objs[0].toString())) {
                String sqlResult = "select out_dev_id,dms_region_id from iot_device where  direct_id='" + objs[0] + "'      and is_valid    =1 " + "   and out_iot_fac =2 and  pd_id in (select id From iot_product where is_valid=1 and out_iot_fac='2' and pd_mode='1' and device_mode_name in(" + dataSourceName + "))";
                List<Object[]> outList = commonInterface.selectListBySql(sqlResult);
                log.info("调用addCsPzTermSuper查询iot的sql2为——————" + sqlResult);
                log.info("调用addCsPzTermSuper查询iot的sql2结果总条数——————" + outList.size());
                //调用 1、参数设置，2、参数激活，3、参数召测 三个接口，若失败给予相对应的提示(先 1、设置 2、激活、3召测)
                for (Object[] ob : outList) {
                    if (ob[0] != null && StringUtils.isNotBlank(ob[0].toString())) {
                        HashMap<String, Object> parameter = new HashMap();
                        HashMap<String, String> parameterSon = new HashMap();
                        parameter.put("method", "ParameterSet");
                        parameter.put("deviceId", ob[0].toString());
                        parameter.put("areaId", ob[1].toString());
                        parameterSon.put("ARtg", ct1);
                        parameterSon.put("ARtgSnd", ct2);
                        parameterSon.put("Load", dtCapacity);
                        //   parameterSon.put("sendTime", date);
                        parameter.put("paras", parameterSon);
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json");
                        //调用召测接口
                        log.info("调用设置接口——————" + outList.size());
                        String ss = HttpUtil.httpPost(Address.ADDR1, JSONObject.toJSONString(parameter), headers);
                        Map parse1 = (Map) JSON.parse(ss);
                        log.info(JSONObject.toJSONString("调用设置接口——参数1：" + JSONObject.toJSONString(parameter) + "———————结果——————" + ss));
                        String request1 = parse1.get("code").toString();
                        if (request1.equals("2000")) {
                            log.info("调用设置接口成功开始调用参数激活接口——————");
                            parameter.put("method", "ParameterActive");
                            String ss2 = HttpUtil.httpPost(Address.ADDR1, JSONObject.toJSONString(parameter), headers);
                            Map parse2 = (Map) JSON.parse(ss2);
                            log.info(JSONObject.toJSONString("调用参数激活接口——参数2：" + JSONObject.toJSONString(parameter) + "———————结果——————" + ss2));

                            String code2 = parse2.get("code").toString();
                            if (code2.equals("2000")) {

                                parameter.put("method", "ParameterGet");
                                parameterSon.put("ARtg", "");
                                parameterSon.put("ARtgSnd", "");
                                parameterSon.put("Load", "");
                                parameter.put("paras", parameterSon);
                                String ss3 = HttpUtil.httpPost(Address.ADDR1, JSONObject.toJSONString(parameter), headers);
                                Map parse3 = (Map) JSON.parse(ss3);
                                log.info(JSONObject.toJSONString("参数召测接口——参数3：" + JSONObject.toJSONString(parameter) + "———————结果——————" + ss3));

                                String code3 = parse3.get("code").toString();
                                if (code3.equals("2000")) {
                                    log.info("调用激活接口成功了————————开始更新数据库");
                                    String sqlOrder = " update dms_iot_device_resource_info  set cspz_state=1, cspz_state_time=now() , pbrl='" + dtCapacity + "',ctbb='" + ct1 + "' , ctbb2='" + ct2 + "'  where  term_esn='" + esn + "' and is_valid=1";
                                    boolean b = commonInterface.dbAccess_update(sqlOrder);
                                    if (b) {
                                        return new LayJsonS(200, "修改成功", null, 1, "成功", 1);
                                    }
                                    return new LayJsonS(500, "修改失败", null, 0, "失败", 1);

                                } else {
                                    //参数激活失败
                                    return new LayJsonS(500, "参数激活失败", ss3, 0, "失败", 1);
                                }
                            } else {
                                //参数设置失败
                                return new LayJsonS(500, "参数设置失败", ss2, 0, "失败", 1);
                            }
                        } else {
                            //提示召测失败
                            return new LayJsonS(500, "提示召测失败", ss, 0, "失败", 1);
                        }
                    } else {
                        return new LayJsonS(500, "iot_device中id不正确", null, 0, "失败", 1);
                    }
                }
            } else {
                return new LayJsonS(500, "未找到direct_id为" + objs[0] + "的数据", null, 0, "失败", 1);
            }
        }
        // return new LayJson(500, "修改失败", null, 0);
        return new LayJsonS(200, "接口调用完成", null, 1, "成功", 1);
    }

    public static void main(String[] args) {

        //   SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        String s = "11,22,33,44";
        String s1="22";
        String[] split = s.split(",");
        String[] split2 = s1.split(",");
        System.out.println(JSONObject.toJSONString(split)+"==="+JSONObject.toJSONString(split2));

    }

    /*
     * 终端安装-修改图片信息
     * */
    @Override
    public LayJson addZdazOssTerm(Map<String, Object> map) {
        String termEsn = map.get("termEsn") == null || map.get("termEsn") == "" ? "" : map.get("termEsn").toString();
        String esn1 = map.get("esn1") == null || map.get("esn1") == "" ? "" : map.get("esn1").toString();
        String esn2 = map.get("esn2") == null || map.get("esn2") == "" ? "" : map.get("esn2").toString();
        String esn3 = map.get("esn3") == null || map.get("esn3") == "" ? "" : map.get("esn3").toString();
        String esn4 = map.get("esn4") == null || map.get("esn4") == "" ? "" : map.get("esn4").toString();
        String esn5 = map.get("esn5") == null || map.get("esn5") == "" ? "" : map.get("esn5").toString();
        String esn6 = map.get("esn6") == null || map.get("esn6") == "" ? "" : map.get("esn6").toString();
        // 1我来保、2工程管控
        String type = map.get("type") == null || map.get("type") == "" ? "1" : map.get("type").toString();
        StringBuffer stringBuffer = new StringBuffer("update dms_iot_device_resource_info set ");
        if (StringUtils.isNotBlank(esn1)) {

            stringBuffer.append(" esn1 ='" + esn1 + "',");
        }
        if (StringUtils.isNotBlank(esn2)) {
            stringBuffer.append(" esn2 ='" + esn2 + "',");
        }
        if (StringUtils.isNotBlank(esn3)) {
            stringBuffer.append(" esn3 ='" + esn3 + "',");
        }
        if (StringUtils.isNotBlank(esn4)) {
            stringBuffer.append(" esn4 ='" + esn4 + "',");
        }
        if (StringUtils.isNotBlank(esn5)) {
            stringBuffer.append(" esn5 ='" + esn5 + "',");
        }
        if (StringUtils.isNotBlank(esn6)) {
            stringBuffer.append(" esn6 ='" + esn6 + "',");
        }
        String sql = stringBuffer.substring(0, stringBuffer.length() - 1);
        sql += " where  term_esn='" + termEsn + "' and is_valid=1 and oss_type="+type+" ";
        if(type.equals("2")){
            //根据图片去下载本地，然后上传到本地oss文件夹下，把地址存储下来

        }
        log.info("终端安装-修改图片信息" + sql);
        boolean b = commonInterface.dbAccess_update(sql);
        if (b) {
            return new LayJson(200, "修改成功", null, 1);
        }
        return new LayJson(500, "修改失败", null, 0);
    }

    /**
     * @description：终端安装-修改资源库表状态
     * @author：sunheng
     * @date：2022/11/22 19:23
     * @param：
     */
    @Override
    public LayJson addZdazTerm(Map<String, Object> map) {
        String esnState = map.get("esnState") == null || map.get("esnState") == "" ? "" : map.get("esnState").toString();
        String azTime = map.get("azTime") == null || map.get("azTime") == "" ? "" : map.get("azTime").toString();
        String termEsn = map.get("termEsn") == null || map.get("termEsn") == "" ? "" : map.get("termEsn").toString();
        String dicLabel = map.get("dicLabel") == null || map.get("dicLabel") == "" ? "" : map.get("dicLabel").toString();
        String zoneProjectName = map.get("zoneProjectName") == null || map.get("zoneProjectName") == "" ? "" : map.get("zoneProjectName").toString();
        String feeder_name = map.get("feeder_name") == null || map.get("feeder_name") == "" ? "" : map.get("feeder_name").toString();
        String feeder_id = map.get("feeder_id") == null || map.get("feeder_id") == "" ? "" : map.get("feeder_id").toString();
        String projectName = map.get("projectName") == null || map.get("projectName") == "" ? "" : map.get("projectName").toString();
        String projectCode = map.get("projectCode") == null || map.get("projectCode") == "" ? "" : map.get("projectCode").toString();


        if (StringUtils.isNotBlank(termEsn)) {
            String[] split = termEsn.split(",");
            for (int i = 0; i < split.length; i++) {
                String sqlOrder = " update dms_iot_device_resource_info  set tm_dqzt='" + esnState + "',az_time='" + azTime + "' ,diclabel='" + dicLabel + "' , feederid='" + feeder_id + "',feedername='" + feeder_name + "',projectcode='" + projectCode + "',projectname='" + projectName + "',zoneprojectname='" + zoneProjectName + "'  where  term_esn='" + split[i] + "' and is_valid=1";
                boolean b = commonInterface.dbAccess_update(sqlOrder);
                log.info("终端状态修改sql" + sqlOrder);
                try {
                    //更新iot_device表的数据
                    String sqlDevice = " update iot_device  set run_state_time='" + azTime + "'  where  dev_label ='" + split[i] + "'  and is_valid    =1 " + "    and connect_mode=1" + "    and out_iot_fac =2";
                    boolean b2 = commonInterface.dbAccess_update(sqlDevice);
                    log.info("更新iot-device表中run_state_time字段" + b2);
                    //更  新订单状态-订单这个时候还没有关联
                    String sql = "select link_order_no from dms_iot_device_resource_info where term_esn='" + split[i] + "'  and is_valid=1 ";
                    log.info("查询" + sql);
                    List<Object[]> list = commonInterface.selectListBySql(sql);
                    if (list.size() > 0) {
                        log.info("查询出来的数量不为空");
                        if (list.get(0).length > 0) {
                            try {
                                orderProjectService.upOrderState(list.get(0)[0].toString(), 8, 6);
                            } catch (Exception e) {
                                log.info("调用我来保接口报错了" + list.get(0)[0].toString());
                            }

                        }
                    }

                } catch (Exception e) {
                    log.info("报错了" + e.getMessage());
                    return new LayJson(500, "修改终端状态失败", null, 0);
                }
            }
        }

        return new LayJson(200, "修改终端状态成功", null, 1);
    }

    /*子设备新增-全部的暂时没用到*/

    @Override
    public LayJson addChildDevice(Map<String, Object> map) {
        String ID = map.get("ID") != null ? map.get("termEsn").toString() : "";
        String TERMESN = map.get("TERMESN") != null ? map.get("TERMESN").toString() : "";
        String TERMID = map.get("TERMID") != null ? map.get("TERMID").toString() : "";
        String INSTALLTGNO = map.get("INSTALLTGNO") != null ? map.get("INSTALLTGNO").toString() : "";
        String INSTALLTGNM = map.get("INSTALLTGNM") != null ? map.get("INSTALLTGNM").toString() : "";
        String CITYID = map.get("CITYID") != null ? map.get("CITYID").toString() : "";
        String CITYNAME = map.get("CITYNAME") != null ? map.get("CITYNAME").toString() : "";
        String COUNTYID = map.get("COUNTYID") != null ? map.get("COUNTYID").toString() : "";
        String COUNTYNAME = map.get("COUNTYNAME") != null ? map.get("COUNTYNAME").toString() : "";
        String LINEPMSNO = map.get("LINEPMSNO") != null ? map.get("LINEPMSNO").toString() : "";
        String LINENAME = map.get("LINENAME") != null ? map.get("LINENAME").toString() : "";
        String TQNAME = map.get("TQNAME") != null ? map.get("TQNAME").toString() : "";
        String TQNO = map.get("TQNO") != null ? map.get("TQNO").toString() : "";
        String TQASSTNO = map.get("TQASSTNO") != null ? map.get("TQASSTNO").toString() : "";
        String TQPMSNO = map.get("TQPMSNO") != null ? map.get("TQPMSNO").toString() : "";
        String POWERONOFF = map.get("POWERONOFF") != null ? map.get("POWERONOFF").toString() : "";
        String SIGNALLAMP = map.get("SIGNALLAMP") != null ? map.get("SIGNALLAMP").toString() : "";
        String INDICATORLIGHT = map.get("INDICATORLIGHT") != null ? map.get("INDICATORLIGHT").toString() : "";
        String TQCAPACITY = map.get("TQCAPACITY") != null ? map.get("TQCAPACITY").toString() : "";
        String CTCHANGE = map.get("CTCHANGE") != null ? map.get("CTCHANGE").toString() : "";
        String TEAMNAME = map.get("TEAMNAME") != null ? map.get("TEAMNAME").toString() : "";
        String TEAMID = map.get("TEAMID") != null ? map.get("TEAMID").toString() : "";
        String FINISHDATE = map.get("FINISHDATE") != null ? map.get("FINISHDATE").toString() : "";
        String INSTALLLNG = map.get("INSTALLLNG") != null ? map.get("INSTALLLNG").toString() : "";
        String INSTALLLAT = map.get("INSTALLLAT") != null ? map.get("INSTALLLAT").toString() : "";
        String RATECAP = map.get("RATECAP") != null ? map.get("RATECAP").toString() : "";
        String CTRATE = map.get("CTRATE") != null ? map.get("CTRATE").toString() : "";
        String STATE = map.get("STATE") != null ? map.get("STATE").toString() : "";
        String INSTALLTIME = map.get("INSTALLTIME") != null ? map.get("INSTALLTIME").toString() : "";
        String INSTALLUSEROA = map.get("INSTALLUSEROA") != null ? map.get("INSTALLUSEROA").toString() : "";
        String INSTALLUSERNM = map.get("INSTALLUSERNM") != null ? map.get("INSTALLUSERNM").toString() : "";
        String INSTALLUSERTEL = map.get("INSTALLUSERTEL") != null ? map.get("INSTALLUSERTEL").toString() : "";
        String INSTALLORGID = map.get("INSTALLORGID") != null ? map.get("INSTALLORGID").toString() : "";
        String INSTALLORGNM = map.get("INSTALLORGNM") != null ? map.get("INSTALLORGNM").toString() : "";
        String DEVNAME = map.get("DEVNAME") != null ? map.get("DEVNAME").toString() : "";
        String RECORDUSEROA = map.get("RECORDUSEROA") != null ? map.get("RECORDUSEROA").toString() : "";
        String RECORDUSERNM = map.get("RECORDUSERNM") != null ? map.get("RECORDUSERNM").toString() : "";
        String RECORDUSERTEL = map.get("RECORDUSERTEL") != null ? map.get("RECORDUSERTEL").toString() : "";
        String RECORDORGID = map.get("RECORDORGID") != null ? map.get("RECORDORGID").toString() : "";
        String RECORDORGNM = map.get("RECORDORGNM") != null ? map.get("RECORDORGNM").toString() : "";
        String DEVTYPE = map.get("DEVTYPE") != null ? map.get("DEVTYPE").toString() : "";
        String FACTORYNAME = map.get("FACTORYNAME") != null ? map.get("FACTORYNAME").toString() : "";
        String DEVMODEL = map.get("DEVMODEL") != null ? map.get("DEVMODEL").toString() : "";
        String ASSETID = map.get("ASSETID") != null ? map.get("ASSETID").toString() : "";
        String HARDWAREVERSION = map.get("HARDWAREVERSION") != null ? map.get("HARDWAREVERSION").toString() : "";
        String FACTORYDATE = map.get("FACTORYDATE") != null ? map.get("FACTORYDATE").toString() : "";
        String ACCEPTREMARK = map.get("ACCEPTREMARK") != null ? map.get("ACCEPTREMARK").toString() : "";
        String ACCEPTTIME = map.get("ACCEPTTIME") != null ? map.get("ACCEPTTIME").toString() : "";
        String ACCEPTUSEROA = map.get("ACCEPTUSEROA") != null ? map.get("ACCEPTUSEROA").toString() : "";
        String ACCEPTUSERNM = map.get("ACCEPTUSERNM") != null ? map.get("ACCEPTUSERNM").toString() : "";
        String ACCEPTUSERTEL = map.get("ACCEPTUSERTEL") != null ? map.get("ACCEPTUSERTEL").toString() : "";
        String ACCEPTORGID = map.get("ACCEPTORGID") != null ? map.get("ACCEPTORGID").toString() : "";
        String ACCEPTORGNM = map.get("ACCEPTORGNM") != null ? map.get("ACCEPTORGNM").toString() : "";
        String CGDDH = map.get("CGDDH") != null ? map.get("CGDDH").toString() : "";

        String insert = "insert into \"D5000\".\"DMS_IOT_CHILDDEVICE\"(\"ID\", \"TERMESN\", \"TERMID\", \"INSTALLTGNO\", \"INSTALLTGNM\", \"CITYID\", \"CITYNAME\", \"COUNTYID\", \"COUNTYNAME\", \"LINEPMSNO\", \"LINENAME\", \"TQNAME\", \"TQNO\", \"TQASSTNO\", \"TQPMSNO\", \"POWERONOFF\", \"SIGNALLAMP\", \"INDICATORLIGHT\", \"TQCAPACITY\", \"CTCHANGE\", \"TEAMNAME\", \"TEAMID\", \"FINISHDATE\", \"INSTALLLNG\", \"INSTALLLAT\", \"RATECAP\", \"CTRATE\", \"STATE\", \"INSTALLTIME\", \"INSTALLUSEROA\", \"INSTALLUSERNM\", \"INSTALLUSERTEL\", \"INSTALLORGID\", \"INSTALLORGNM\", \"DEVNAME\", \"RECORDUSEROA\", \"RECORDUSERNM\", \"RECORDUSERTEL\", \"RECORDORGID\", \"RECORDORGNM\", \"DEVTYPE\", \"FACTORYNAME\", \"DEVMODEL\", \"ASSETID\", \"HARDWAREVERSION\", \"FACTORYDATE\", \"ACCEPTREMARK\", \"ACCEPTTIME\", \"ACCEPTUSEROA\", \"ACCEPTUSERNM\", \"ACCEPTUSERTEL\", \"ACCEPTORGID\", \"ACCEPTORGNM\", \"CGDDH\") \n" + "VALUES('" + ID + "', '" + TERMESN + "', '" + TERMID + "','" + INSTALLTGNO + "', " + " '" + INSTALLTGNM + "', '" + CITYID + "','" + CITYNAME + "', '" + COUNTYID + "'," + " '" + COUNTYNAME + "','" + LINEPMSNO + "','" + LINENAME + "', '" + TQNAME + "', '" + TQNO + "', " + " '" + TQASSTNO + "', '" + TQPMSNO + "', '" + POWERONOFF + "','" + SIGNALLAMP + "', " + " '" + INDICATORLIGHT + "', '" + TQCAPACITY + "', '" + CTCHANGE + "','" + TEAMNAME + "','" + TEAMID + "', " + " '" + FINISHDATE + "','" + INSTALLLNG + "', '" + INSTALLLAT + "', '" + RATECAP + "', '" + CTRATE + "', '" + STATE + "', '" + INSTALLTIME + "','" + INSTALLUSEROA + "', '" + INSTALLUSERNM + "', '" + INSTALLUSERTEL + "', '" + INSTALLORGID + "', '" + INSTALLORGNM + "', '" + DEVNAME + "', '" + RECORDUSEROA + "', '" + RECORDUSERNM + "', '" + RECORDUSERTEL + "', '" + RECORDORGID + "', '" + RECORDORGNM + "', '" + DEVTYPE + "', '" + FACTORYNAME + "', '" + DEVMODEL + "', '" + ASSETID + "', '" + HARDWAREVERSION + "', '" + FACTORYDATE + "', '" + ACCEPTREMARK + "', '" + ACCEPTTIME + "', '" + ACCEPTUSEROA + "', '" + ACCEPTUSERNM + "', '" + ACCEPTUSERTEL + "', '" + ACCEPTORGID + "', '" + ACCEPTORGNM + "', '" + CGDDH + "')";

        log.info("我来保调用共享中心15.7子设备新增sql" + insert);
        boolean b1 = commonInterface.dbAccess_insert(insert);
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-7','" + JSONObject.toJSONString(map) + "','','" + b1 + "')");
        if (b1) {
            return new LayJson(200, "子设备新增成功", null, 1);
        } else {
            return new LayJson(500, "子设备新增失败", null, 0);
        }

    }


    @Override
    public LayJson addChildDevicesTy(Map<String, Object> map) {
        log.info("我来保调用同源15.7来到了" + JSON.toJSONString(map));
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-7-1','" + JSONObject.toJSONString(map) + "','','" + true + "')");
        String esnNo = map.get("esnNo") != null ? map.get("esnNo").toString() : "";
        String devType = map.get("devType") != null ? map.get("devType").toString() : "";
        String factoryName = map.get("factoryName") != null ? map.get("factoryName").toString() : "";
        String devModel = map.get("devModel") != null ? map.get("devModel").toString() : "";
        String assetId = map.get("assetId") != null ? map.get("assetId").toString() : "";
        String hardwareVersion = map.get("hardwareVersion") != null ? map.get("hardwareVersion").toString() : "";
        String factoryDate = map.get("factoryDate") != null ? map.get("factoryDate").toString() : "";
        String type = map.get("type") != null ? map.get("type").toString() : "";
        String tgNo = map.get("tgNo") != null ? map.get("tgNo").toString() : "";
        String tgName = map.get("tgName") != null ? map.get("tgName").toString() : "";
        String tgPmsNo = map.get("tgPmsNo") != null ? map.get("tgPmsNo").toString() : "";
        String psrId = map.get("psrId") != null ? map.get("psrId").toString() : "";
        String isValid = map.get("isValid") != null ? map.get("isValid").toString() : "1";
        String czTime = map.get("czTime") != null ? map.get("czTime").toString() : "";
        String now = sdf.format(new Date());
        String check = "select ASSET_ID from d5000.DMS_IOT_CHILDDEVICES " + "where ASSET_ID='" + assetId + "' " +
//                "and esn_no='"+esnNo+"' " +
                "and is_valid =1 ";
        JSONArray ja = commonInterface.dbAccess_selectList(check);
        String sql = "";
        log.info("我来保调用同源15.7子设备新增sql" + sql);
        if (ja.size() > 0) {
            sql = " update dms_iot_childdevices set dev_type='" + devType + "',factory_name='" + factoryName + "',\n" + " devmodel='" + devModel + "',asset_id='" + assetId + "',hardware_version='" + hardwareVersion + "',factory_date='" + factoryDate + "',\n" + " type=" + type + " ,tgno='" + tgNo + "',tgname='',tgpmsno='" + tgPmsNo + "',nowdate='" + czTime + "',is_valid=" + isValid + "  where  psrid='" + psrId + "'";
            boolean b1 = commonInterface.dbAccess_insert(sql);
            commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-7','" + JSONObject.toJSONString(map) + "','','" + b1 + "')");
            if (b1) {
                return new LayJson(200, "子设备修改成功", null, 1);
            } else {
                return new LayJson(500, "子设备修改失败", null, 0);
            }

        } else {
            sql = "insert into \"D5000\".\"DMS_IOT_CHILDDEVICES\"( ESN_NO,DEV_TYPE,FACTORY_NAME,DEVMODEL,ASSET_ID,HARDWARE_VERSION,FACTORY_DATE,TYPE,TGNO,TGNAME,TGPMSNO,NOWDATE,is_valid,psrid ) \n" + "VALUES('" + esnNo + "', '" + devType + "', '" + factoryName + "','" + devModel + "', " + " '" + assetId + "', '" + hardwareVersion + "','" + factoryDate + "', '" + type + "','" + tgNo + "','" + tgName + "','" + tgPmsNo + "','" + czTime + "'," + isValid + ",'" + psrId + "')";
            boolean b1 = commonInterface.dbAccess_insert(sql);
            commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-7','" + JSONObject.toJSONString(map) + "','','" + b1 + "')");
            if (b1) {
                return new LayJson(200, "子设备新增成功", null, 1);
            } else {
                return new LayJson(500, "子设备新增失败", null, 0);
            }
        }


    }

    @Override
    public LayJson addChildDevices(Map<String, Object> map) {
        log.info("我来保调用共享中心15.7来到了" + JSON.toJSONString(map));
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-7-1','" + JSONObject.toJSONString(map) + "','','" + true + "')");
        String esnNo = map.get("esnNo") != null ? map.get("esnNo").toString() : "";
        String devType = map.get("devType") != null ? map.get("devType").toString() : "";
        String factoryName = map.get("factoryName") != null ? map.get("factoryName").toString() : "";
        String devModel = map.get("devModel") != null ? map.get("devModel").toString() : "";
        String assetId = map.get("assetId") != null ? map.get("assetId").toString() : "";
        String hardwareVersion = map.get("hardwareVersion") != null ? map.get("hardwareVersion").toString() : "";
        String factoryDate = map.get("factoryDate") != null ? map.get("factoryDate").toString() : "";
        String type = map.get("type") != null ? map.get("type").toString() : "";
        String tgNo = map.get("tgNo") != null ? map.get("tgNo").toString() : "";
        String tgName = map.get("tgName") != null ? map.get("tgName").toString() : "";
        String tgPmsNo = map.get("tgPmsNo") != null ? map.get("tgPmsNo").toString() : "";
        String psrId = map.get("psrId") != null ? map.get("psrId").toString() : "0";
        String isValid = map.get("isValid") != null ? map.get("isValid").toString() : "1";
        String now = sdf.format(new Date());
        String czTime = map.get("czTime") != null ? map.get("czTime").toString() : now;

        String check = "select ASSET_ID from d5000.DMS_IOT_CHILDDEVICES " + "where ASSET_ID='" + assetId + "' " +
//                "and esn_no='"+esnNo+"' " +
                "and is_valid =1 ";
        JSONArray ja = commonInterface.dbAccess_selectList(check);
        String sql = "";
        log.info("我来保调用共享中心15.7子设备新增sql" + sql);
        if (ja.size() > 0) {
            return new LayJson(201, "该设备已存在", null, 0);
        }else {
            try {
                commonInterface.dbAccess_delete("DELETE   \"D5000\".\"DMS_IOT_CHILDDEVICES\" where ESN='"+esnNo+"' and psrid='"+psrId+"' and lttno='0' ");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            sql = "insert into \"D5000\".\"DMS_IOT_CHILDDEVICES\"( ESN_NO,DEV_TYPE,FACTORY_NAME,DEVMODEL,ASSET_ID,HARDWARE_VERSION,FACTORY_DATE,TYPE,TGNO,TGNAME,TGPMSNO,NOWDATE,is_valid,psrid ) \n" + "VALUES('" + esnNo + "', '" + devType + "', '" + factoryName + "','" + devModel + "', " + " '" + assetId + "', '" + hardwareVersion + "','" + factoryDate + "', '" + type + "','" + tgNo + "','" + tgName + "','" + tgPmsNo + "','" + czTime + "',"+isValid+",'" + psrId + "')";
            boolean b1 = commonInterface.dbAccess_insert(sql);
            commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-7','" + JSONObject.toJSONString(map) + "','','" + b1 + "')");
            if (b1) {
                return new LayJson(200, "子设备新增成功", null, 1);
            } else {
                return new LayJson(500, "子设备新增失败", null, 0);
            }
        }
    }

    public LayJson delChildDevice(Map<String, Object> map) {
        String esnNo = map.get("esnNo") != null ? map.get("esnNo").toString() : "";
        String devType = map.get("devType") != null ? map.get("devType").toString() : "";
//        String factoryName = map.get("factoryName") != null ? map.get("factoryName").toString() : "";
//        String devModel = map.get("devModel") != null ? map.get("devModel").toString() : "";
        String assetId = map.get("assetId") != null ? map.get("assetId").toString() : "";
//        String hardwareVersion = map.get("hardwareVersion") != null ? map.get("hardwareVersion").toString() : "";
//        String factoryDate = map.get("factoryDate") != null ? map.get("factoryDate").toString() : "";
        String type = map.get("type") != null ? map.get("type").toString() : "";
//        String tgNo = map.get("tgNo") != null ? map.get("tgNo").toString() : "";
//        String tgName = map.get("tgName") != null ? map.get("tgName").toString() : "";
//        String tgPmsNo = map.get("tgPmsNo") != null ? map.get("tgPmsNo").toString() : "";
//        String now = sdf.format(new Date());
        String del_time = map.get("del_time") != null ? map.get("del_time").toString() : "";
        String del_reason = map.get("del_reason") != null ? map.get("del_reason").toString() : "";
        String del_staff = map.get("del_staff") != null ? map.get("del_staff").toString() : "";

        String del = "update d5000.DMS_IOT_CHILDDEVICES set is_valid=2 " + "where esn_no='" + esnNo + "' and ASSET_ID='" + assetId + "' and is_valid=1";

        log.info("我来保调用共享中心15.7子设备删除sql" + del);
        boolean b1 = commonInterface.dbAccess_delete(del);

//        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-7','" + JSONObject.toJSONString(map) + "','','" + b1 + "')");
        if (b1) {
            //记录删除事件
            commonInterface.dbAccess_insert("insert into d5000.DMS_IOT_CHILDDEVICE_DEL_DETAIL " + "(device_esn,asset_id,del_time,del_reason,del_staff)values(" + "'" + esnNo + "','" + assetId + "','" + del_time + "','" + del_reason + "','" + del_staff + "')");
            return new LayJson(200, "子设备删除成功", null, 1);
        } else {
            return new LayJson(500, "子设备删除失败", null, 0);
        }

    }





    /*
      * ltu_num  ltu数量
        humidity_sensor_num 温湿度传感器
        tg_temperature_sensor_num	配变桩头温度传感器
      * */

    @Override
    public LayJson addIotChildSize(Map<String, Object> map) {
        log.info("我来保调用共享中心15.8来到了" + JSON.toJSONString(map));
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-8-1','" + JSONObject.toJSONString(map) + "','','" + true + "')");
        String esnNo = map.get("esnNo") != null ? map.get("esnNo").toString() : "";
        String lowBranchNum = map.get("lowBranchNum") != null ? map.get("lowBranchNum").toString() : "";
        String nonReactiveNum = map.get("nonReactiveNum") != null ? map.get("nonReactiveNum").toString() : "";
        String voltageTapNum = map.get("voltageTapNum") != null ? map.get("voltageTapNum").toString() : "";
        String oilSensorNum = map.get("oilSensorNum") != null ? map.get("oilSensorNum").toString() : "";
        String ltu = map.get("ltu") != null ? map.get("ltu").toString() : "";
        String sdcgq = map.get("sdcgq") != null ? map.get("sdcgq").toString() : "";
        String pbztwdcgq = map.get("pbztwdcgq") != null ? map.get("pbztwdcgq").toString() : "";
        String insert = "update iot_device set low_branch_num='" + lowBranchNum + "',non_reactive_num='" + nonReactiveNum + "',voltage_tap_num='" + voltageTapNum + "',oil_sensor_num='" + oilSensorNum + "',ltu_mun='" + ltu + "',humidity_s_num='" + sdcgq + "' ,tg_tmp_s_mum='" + pbztwdcgq + "'  where dev_label='" + esnNo + "' and is_valid=1 and connect_mode=1 and out_iot_fac=2  ";
        log.info("我来保调用共享中心15.8子设备数量修改" + insert);
        boolean b1 = commonInterface.dbAccess_update(insert);
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'15-8','" + JSONObject.toJSONString(map) + "','','" + b1 + "')");
        if (b1) {
            return new LayJson(200, "子设备数量更新成功了", null, 1);
        } else {
            return new LayJson(500, "子设备数量更新失败,请核查网络原因", null, 0);
        }
    }

    /*
     * 获取终端工程管控信息
     * */
    @Override
    public LayJson addZdEngineeringControlInfo(Map<String, Object> map) {
        String termEsn = map.get("termEsn") == null || map.get("termEsn") == "" ? "" : map.get("termEsn").toString();
        String countyId = map.get("countyId") == null || map.get("countyId") == "" ? "" : map.get("countyId").toString();
        String countyName = map.get("countyName") == null || map.get("countyName") == "" ? "" : map.get("countyName").toString();
        String batchName = map.get("batchName") == null || map.get("batchName") == "" ? "" : map.get("batchName").toString();
        String batchCode = map.get("batchCode") == null || map.get("batchCode") == "" ? "" : map.get("batchCode").toString();
        String projectName = map.get("projectName") == null || map.get("projectName") == "" ? "" : map.get("projectName").toString();
        String projectCode = map.get("projectCode") == null || map.get("projectCode") == "" ? "" : map.get("projectCode").toString();
        String zoneTgNum = map.get("zoneTgNum") == null || map.get("zoneTgNum") == "" ? "" : map.get("zoneTgNum").toString();
        String zoneProjectName = map.get("zoneProjectName") == null || map.get("zoneProjectName") == "" ? "" : map.get("zoneProjectName").toString();
        String dicLabel = map.get("dicLabel") == null || map.get("dicLabel") == "" ? "" : map.get("dicLabel").toString();

        StringBuffer stringBuffer = new StringBuffer("update dms_iot_device_resource_info set ");
        if (StringUtils.isNotBlank(countyId)) {
            stringBuffer.append(" COUNTYID ='" + countyId + "',");
        }
        if (StringUtils.isNotBlank(countyName)) {
            stringBuffer.append(" COUNTYNAME ='" + countyName + "',");
        }
        if (StringUtils.isNotBlank(batchCode)) {
            stringBuffer.append(" BATCHCODE ='" + batchCode + "',");
        }
        if (StringUtils.isNotBlank(batchName)) {
            stringBuffer.append(" BATCHNAME ='" + batchName + "',");
        }
        if (StringUtils.isNotBlank(projectCode)) {
            stringBuffer.append(" PROJECTCODE ='" + projectCode + "',");
        }

        if (StringUtils.isNotBlank(projectName)) {
            stringBuffer.append(" PROJECTNAME ='" + projectName + "',");
        }
        if (StringUtils.isNotBlank(zoneTgNum)) {
            stringBuffer.append(" ZONETGNUM ='" + zoneTgNum + "',");
        }
        if (StringUtils.isNotBlank(zoneProjectName)) {
            stringBuffer.append(" ZONEPROJECTNAME ='" + zoneProjectName + "',");
        }
        if (StringUtils.isNotBlank(dicLabel)) {
            stringBuffer.append(" DICLABEL ='" + dicLabel + "',");
        }

        String sql = stringBuffer.substring(0, stringBuffer.length() - 1);
        sql += " where  term_esn='" + termEsn + "' and is_valid=1";
        boolean b = commonInterface.dbAccess_update(sql);
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'14-4','" + JSONObject.toJSONString(map) + "','','" + b + "')");
        if (b) {
            return new LayJson(200, "修改成功", null, 1);
        }
        return new LayJson(500, "修改失败", null, 0);
    }


        //更新订单接口（滞后天数）
    @Override
    public LayJson upOderLagdays(Map<String, Object> map) {
        String cgddh = map.get("cgddh") == null || map.get("cgddh") == "" ? "" : map.get("cgddh").toString();
        String zhts = map.get("zhts") == null || map.get("zhts") == "" ? "" : map.get("zhts").toString();
        String sql = " update dms_tr_project_order set zhts='"+zhts+"' where cgddh='"+cgddh+"' ";
        boolean b = commonInterface.dbAccess_update(sql);
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'22-1','" + JSONObject.toJSONString(map) + "','','" + b + "')");
        if (b) {
            return new LayJson(200, "修改成功", null, 1);
        }
        return new LayJson(500, "修改失败", null, 0);
    }

}

package com.nari.iot.vendorinfo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.HttpUtil;
import com.nari.iot.vendorinfo.entity.ApolloConfig;
import com.nari.iot.vendorinfo.service.PurchaseOrdersService;
import com.sgcc.uap.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/tbwlb")
public class Tbwlb {
    public static String Token;
    @Autowired
    private ApolloConfig apolloConfig;

    @Autowired
    CommonInterface commonInterface;

    @Autowired
    PurchaseOrdersService purchaseOrdersService;
    @RequestMapping("getApollo")
    public String getApollo() {
        String device_ode_name = apolloConfig.getDevice_mode_name();
        return "获取apoll对应的参数" + device_ode_name;
    }

    @RequestMapping("getToken")
    public String getToken() {
        if (StringUtils.isBlank(Token)) {
            String addr = "http://25.212.172.30:9001/pwserver/small/secretApi/getSmallSecret?secret=eG16X2d4eng=";
            Map<String, String> headers = new HashMap<String, String>();
            //headers.put("Content-Type", "application/json");
            String ss = HttpUtil.httpGet(addr, headers);
            Map parse1 = (Map) JSON.parse(ss);
            log.info(JSONObject.toJSONString("调用我来保tokne结果———————————" + parse1));
            String state = parse1.get("state").toString();
            if (state.equals("success")) {
                Map result = (Map) JSON.parse(parse1.get("result").toString());
                Map smallSecret = (Map) JSON.parse(result.get("smallSecret").toString());
                String smallToken = smallSecret.get("smallToken").toString();
                Token = smallToken;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(250000l);
                            Token = "";
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, "匿名类调用token方法").start();
                log.info(smallToken);
            }
        }
        return Token;
    }

    /**
     * 1.4  厂家供应商-新增、修改接口  ok
     */
    @PostMapping("/editSupplier")
    public JSONObject editSupplier(@RequestBody Map maps) {
        String addr = "http://25.212.181.38:18081/terminal/terminalProcess/vendor/editSupplier";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("tel", "15200855793");
        headers.put("smallToken", getToken());
        String ss = HttpUtil.httpPost(addr, JSONObject.toJSONString(maps), headers);
        Map parse1 = (Map) JSON.parse(ss);
        log.info("调用我来保1.4厂家新增接口---参数为{}得到的返回值为：" + JSONObject.toJSONString(parse1), JSONObject.toJSONString(maps));
        //写入日志表
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'1-4','" + JSONObject.toJSONString(maps) + "','','" + JSONObject.toJSONString(parse1) + "')");
        return new JSONObject(parse1);
    }


    /**
     * 2.4 合同管理-修改接口
     */
    @PostMapping("/htUpdateContract/{htNo}")
    public JSONObject htUpdateContract(@PathVariable String htNo) {
        String addr = "http://25.212.181.38:18081/terminal/terminalProcess/contract/updateContract";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("tel", "15200855793");
        headers.put("smallToken", getToken());
        String sql = "  select ht_no,ht_id,org_nm,package_no,buyer_nm,\n" + "  batch_year,vendor_nm,ht_goods_nm,goods_num,erp_no,batch_no,esn_num,link_tr_num,order_num,vendor_credit_code," + " ht_name,zb_date,zbcgfs,order_ft_count,oss_ref\n" + "   from DMS_IOT_HT_INFO where ht_no='" + htNo + "'";
        log.info("调用我来保合同管理修改接口" + sql);
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        HashMap maps = new HashMap();
        if (devList.get(0).length > 0) {
            maps.put("htNo", devList.get(0)[0]);
            maps.put("htId", devList.get(0)[1]);
            maps.put("orgNm", devList.get(0)[2]);
            maps.put("packageNo", devList.get(0)[3]);
            maps.put("buyerNm", devList.get(0)[4]);

            maps.put("batchYear", devList.get(0)[5]);
            maps.put("vendorNm", devList.get(0)[6]);
            maps.put("htGoodsNm", devList.get(0)[7]);
            maps.put("goodsNum", devList.get(0)[8]);
            maps.put("erpNo", devList.get(0)[9]);

            maps.put("batchNo", devList.get(0)[10]);
            maps.put("esnNum", devList.get(0)[11]);
            maps.put("linkTrNum", devList.get(0)[12]);
            maps.put("orderNum", devList.get(0)[13]);
            maps.put("vendorCreditCode", devList.get(0)[14]);

            maps.put("htName", devList.get(0)[15]);
            maps.put("zbDate", devList.get(0)[16]);
            maps.put("zbcgfs", devList.get(0)[17]);
            maps.put("orderFtCount", devList.get(0)[18]);
            maps.put("ossRef", devList.get(0)[19]);
        }
        String ss = HttpUtil.httpPost(addr, JSONObject.toJSONString(maps), headers);
        Map parse1 = (Map) JSON.parse(ss);
        log.info("调用我来保2.4合同管理修改接口---参数为{}得到的返回值为：" + JSONObject.toJSONString(parse1), JSONObject.toJSONString(maps));
        //写入日志表
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'2-4','" + JSONObject.toJSONString(maps) + "','','" + JSONObject.toJSONString(parse1) + "')");
        return new JSONObject(parse1);
    }

    /**
     * 3.3 订单管理-新增接口
     * {
     * "unit": "国网湖南省电力有限公司长沙供电分公司",
     * "projectNo": "1816A12000UX",
     * "projectName": "湖南长沙望城区供电公司乌山镇双丰村金塘学校（WYG1332）台区中低压配电网改造",
     * "terminalQuantity": "13",
     * "cgghdNumber": "SO164500435562Test",
     * "cgddh": "4500435562Test",
     * "gysNumber": "1000151983",
     * "htNumber": "HN2022000TEST",
     * "gysName": "北京智芯微电子科技有限公司",
     * "cgghdCreateDate": "2021-11-11 14:15:44",
     * "handoverDate": "2021-12-20 00:00:00",
     * "terminalConsignee": "",
     * "telephone": "",
     * "address": "",
     * "editor": "",
     * "editorDate": "",
     * "bindTgQuntity": "",
     * "unitId": "297ebd676610090d016610144d4b0009",
     * "countyId": "",
     * "countyNm": "",
     * "esnNum": "",
     * "jgrq": "",
     * "kgrq": "",
     * "xmxz": ""
     * }
     */
    @PostMapping("/orderAddContractOrder")
    public JSONObject orderAddContractOrder(@RequestBody Map maps) {
        String addr = "http://25.212.181.38:18081/terminal/terminalProcess/contractOrder/addContractOrder";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("tel", "15200855793");
        headers.put("smallToken", getToken());
        String ss = HttpUtil.httpPost(addr, JSONObject.toJSONString(maps), headers);
        Map parse1 = (Map) JSON.parse(ss);
        log.info("调用我来保3.3订单管理新增接口---参数为{}得到的返回值为：" + JSONObject.toJSONString(parse1), JSONObject.toJSONString(maps));
        //写入日志表
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'3-3','" + JSONObject.toJSONString(maps) + "','','" + JSONObject.toJSONString(parse1) + "')");
        return new JSONObject(parse1);
    }

    /**
     * 3.4订单管理-修改接口
     * 参数同上
     */
    @GetMapping("/orderUpdateContractOrder/{cgddh}")
    public JSONObject orderUpdateContractOrder(@PathVariable String cgddh) {
        String addr = "http://25.212.181.38:18081/terminal/terminalProcess/contractOrder/updateContractOrder";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("tel", "15200855793");
        headers.put("smallToken", getToken());
        //根据cgddh查询订单信息给我来保
        String sql = " select   unit,project_no,project_name,terminal_quantity, " + "  cgghd_number,cgddh,gys_number,ht_number,gys_name,cgghd_create_date,handover_date, " + "   terminal_consignee,address,editor,editor_date,bind_tg_quntity,unit_id,county_id,county_nm,esn_num,kgrq,jgrq,xmxz " + "  from dms_tr_project_order  where cgddh='" + cgddh + "'";
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        HashMap maps = new HashMap();
        if (devList.get(0).length > 0) {
            maps.put("unit", devList.get(0)[0]);
            maps.put("projectNo", devList.get(0)[1]);
            maps.put("projectName", devList.get(0)[2]);
            maps.put("terminalQuantity", devList.get(0)[3]);
            maps.put("cgghdNumber", devList.get(0)[4]);
            maps.put("cgddh", devList.get(0)[5]);
            maps.put("gysNumber", devList.get(0)[6]);
            maps.put("htNumber", devList.get(0)[7]);
            maps.put("gysName", devList.get(0)[8]);
            maps.put("cgghdCreateDate", devList.get(0)[9]);
            maps.put("handoverDate", devList.get(0)[10]);
            maps.put("terminalConsignee", devList.get(0)[11]);
            maps.put("address", devList.get(0)[12]);
            maps.put("editor", devList.get(0)[13]);
            maps.put("editorDate", devList.get(0)[14]);
            maps.put("bindTgQuntity", devList.get(0)[15]);
            maps.put("unitId", devList.get(0)[16]);
            maps.put("countyId", devList.get(0)[17]);
            maps.put("countyNm", devList.get(0)[18]);
            maps.put("esnNum", devList.get(0)[19]);
            maps.put("jgrq", devList.get(0)[20]);
            maps.put("kgrq", devList.get(0)[21]);
            maps.put("xmxz", devList.get(0)[22]);
        }
        String ss = HttpUtil.httpPost(addr, JSONObject.toJSONString(maps), headers);
        Map parse1 = (Map) JSON.parse(ss);
        log.info("调用我来保3.4订单管理新增接口---参数为{}得到的返回值为：" + JSONObject.toJSONString(parse1), JSONObject.toJSONString(maps));
        //  写入日志表
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'3-4','" + JSONObject.toJSONString(maps) + "','','" + JSONObject.toJSONString(parse1) + "')");
        return new JSONObject(parse1);
    }



    /**
     * 8.2 检测中心全检-新增接口(资源库)
     */
    @PostMapping("/qjInsertTerminalResourceInfo")
    public JSONObject qjInsertTerminalResourceInfo(@RequestBody List<Map> maps) {
        String addr = "http://25.212.181.38:18081/terminal/terminalProcess/terminal/insertTerminalResourceInfo";
        //工程管控
        String addrGcgk = "http://25.212.251.16:17801:/termInfo/saveTermEsn ";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("tel", "15200855793");
        headers.put("smallToken", getToken());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String ss2 = HttpUtil.httpPost(addrGcgk, JSONObject.toJSONString(maps), headers);
                    Map parse2 = (Map) JSON.parse(ss2);
                    //工程管控
                    commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'8-2-2','" + JSONObject.toJSONString(maps) + "','','" + JSONObject.toJSONString(parse2) + "')");
                } catch (Exception e) {
                    log.info("工程管控插入报错8.3"+e.getMessage());
                }
            }
        }, "调用功能管控").start();

        String ss = HttpUtil.httpPost(addr, JSONObject.toJSONString(maps), headers);
        Map parse1 = (Map) JSON.parse(ss);
        log.info("调用我来我来保8.2检测中心全检新增接口---参数为{}得到的返回值为：" + JSONObject.toJSONString(parse1), JSONObject.toJSONString(maps));
        //写入日志表 报错了
        try {
            commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'8-2-1','" + JSONObject.toJSONString(maps) + "','','" + JSONObject.toJSONString(parse1) + "')");
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return new JSONObject(parse1);
    }

    /**
     * 8.3 检测中心全检-修改
     */
    @PostMapping("/qjUpdateDetectionState")
    public JSONObject qjUpdateDetectionState(@RequestBody List<Map> maps) {
        String addr = "http://25.212.181.38:18081/terminal/terminalProcess/terminal/updateDetectionState";
        //工程管控
        String addr2 = "http://25.212.251.16:17801:/termInfo/saveTermEsnResult";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("tel", "15200855793");
        headers.put("smallToken", getToken());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String ss2 = HttpUtil.httpPost(addr2, JSONObject.toJSONString(maps), headers);
                    Map parse2 = (Map) JSON.parse(ss2);
                    commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'8-3-2','" + JSONObject.toJSONString(maps) + "','','" + JSONObject.toJSONString(parse2) + "')");

                } catch (Exception e) {
                    log.info("工程管控插入报错8.3"+e.getMessage());
                }
            }
        }, "调用功能管控").start();

                    String ss = HttpUtil.httpPost(addr, JSONObject.toJSONString(maps), headers);
                    Map parse1 = (Map) JSON.parse(ss);
                    log.info("调用我来我来保8.3检测中心全检修改接口---参数为{}得到的返回值为：" + JSONObject.toJSONString(parse1), JSONObject.toJSONString(maps));
                    //写入日志表
                    commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'8-3-1','" + JSONObject.toJSONString(maps) + "','','" + JSONObject.toJSONString(parse1) + "')");

        return new JSONObject(parse1);
    }



    /**
     * 10.3终端注册参数生成--新增接口（我来保说不需要）
     */
    @PostMapping("/zdzcUpdateIotDeviceInfo/{devLabel}")
    public JSONObject zdzcUpdateIotDeviceInfo(@PathVariable  String devLbel) {
        String addr = "http://25.212.181.38:18081/terminal/terminalProcess/terminal/updateIotDevice";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("tel", "15200855793");
        headers.put("smallToken", getToken());

        String sql2 = " select cert_flag,\n" +
                "connect_mode,\n" +
                "creat_time,\n" +
                "data_encrypt_id,\n" +
                "data_key,\n" +
                "dev_label,\n" +
                "dev_name,\n" +
                "dev_type,\n" +
                "device_key,\n" +
                "direct_id,\n" +
                "dms_region_id,\n" +
                "encrypt_id,\n" +
                "encrypt_key,\n" +
                "feeder_id,\n" +
                "fw_update_time,\n" +
                "fw_version,\n" +
                "id,\n" +
                "iot_secret,\n" +
                "is_check,\n" +
                "is_check_time,\n" +
                "is_jdys,\n" +
                "is_jdys_time,\n" +
                "is_online,\n" +
                "is_valid,\n" +
                "last_time,\n" +
                "low_branch_num,\n" +
                "modifiy_method,\n" +
                "non_reactive_num,\n" +
                "oil_sensor_num,\n" +
                "out_dev_id,\n" +
                "out_iot_fac,\n" +
                "pd_id,\n" +
                "pd_version,\n" +
                "pj_id,\n" +
                "project_type,\n" +
                "rely_id,\n" +
                "rely_mode,\n" +
                "rely_name,\n" +
                "rely_type,\n" +
                "run_state,\n" +
                "run_state_time,\n" +
                "server_info,\n" +
                "sim_ip,\n" +
                "sim_no,\n" +
                "status,\n" +
                "status_change_time,\n" +
                "ter_type,\n" +
                "voltage_tap_num  from iot_device    where is_valid    =1\n" +
                "    and connect_mode=1\n" +
                "    and out_iot_fac =2 and dev_label='" + devLbel + "' ";
        Map parse1 =new  HashMap();
        log.info("查询iot表中数据"+sql2);
        List<Object[]> list = commonInterface.selectListBySql(sql2);
        if (list.size() > 0 && list.get(0).length > 0) {
            Object[] resultObj = list.get(0);
            HashMap resultMapWlb = new HashMap();
            resultMapWlb.put("certFlag", resultObj[0]);
            resultMapWlb.put("connectMode", resultObj[1]);
            resultMapWlb.put("creatTime", resultObj[2]);
            resultMapWlb.put("dataEncryptId", resultObj[3]);
            resultMapWlb.put("dataKey", resultObj[4]);
            resultMapWlb.put("devLabel", resultObj[5]);
            resultMapWlb.put("devName", resultObj[6]);
            resultMapWlb.put("devType", resultObj[7]);
            resultMapWlb.put("deviceKey", resultObj[8]);
            resultMapWlb.put("directId", resultObj[9]);
            resultMapWlb.put("dmsRegionId", resultObj[10]);
            resultMapWlb.put("encryptId", resultObj[11]);
            resultMapWlb.put("encryptKey", resultObj[12]);
            resultMapWlb.put("feederId", resultObj[13]);
            resultMapWlb.put("fwUpdateTime", resultObj[14]);
            resultMapWlb.put("fwVersion", resultObj[15]);
            resultMapWlb.put("id", resultObj[16]);
            resultMapWlb.put("iotSecret", resultObj[17]);
            resultMapWlb.put("isCheck", resultObj[18]);
            resultMapWlb.put("isCheckTime", resultObj[19]);
            resultMapWlb.put("isJdys", resultObj[20]);
            resultMapWlb.put("isJdysTime", resultObj[21]);
            resultMapWlb.put("isOnline", resultObj[22]);
            resultMapWlb.put("isValid", resultObj[23]);
            resultMapWlb.put("lastTime", resultObj[24]);
            resultMapWlb.put("lowBranchNum", resultObj[25]);
            resultMapWlb.put("modifiyMethod", resultObj[26]);
            resultMapWlb.put("nonReactiveNum", resultObj[27]);
            resultMapWlb.put("oilSensorNum", resultObj[28]);
            resultMapWlb.put("outDevId", resultObj[29]);
            resultMapWlb.put("outIotFac", resultObj[30]);
            resultMapWlb.put("pdId", resultObj[31]);
            resultMapWlb.put("pdVersion", resultObj[32]);
            resultMapWlb.put("pjId", resultObj[33]);
            resultMapWlb.put("projectType", resultObj[34]);
            resultMapWlb.put("relyId", resultObj[35]);
            resultMapWlb.put("relyMode", resultObj[36]);
            resultMapWlb.put("relyName", resultObj[37]);
            resultMapWlb.put("relyType", resultObj[38]);
            resultMapWlb.put("runState", resultObj[39]);
            resultMapWlb.put("runStateTime", resultObj[40]);
            resultMapWlb.put("serverInfo", resultObj[41]);
            resultMapWlb.put("simIp", resultObj[42]);
            resultMapWlb.put("simNo", resultObj[43]);
            resultMapWlb.put("status", resultObj[44]);
            resultMapWlb.put("statusChangeTime", resultObj[45]);
            resultMapWlb.put("terType", resultObj[46]);
            resultMapWlb.put("voltageTapNum", resultObj[47]);

            log.info("开始调用--参数为----"+ JSONObject.toJSONString(resultMapWlb));
            String ss = HttpUtil.httpPost(addr, JSONObject.toJSONString(resultMapWlb), headers);
            parse1 = (Map) JSON.parse(ss);
            log.info("调用我来我来保10.3终端注册参数生成新增接口---参数为{}得到的返回值为：" + JSONObject.toJSONString(parse1), JSONObject.toJSONString(resultMapWlb));
            //写入日志表
            commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'10-3','" + JSONObject.toJSONString(resultMapWlb) + "','','" + JSONObject.toJSONString(parse1) + "')");
        }
        return new JSONObject(parse1);
    }

    /**
     * 11.3sim卡修改
     */
    @PostMapping("/simUpdateIotDeviceSimInfo")
    public JSONObject simUpdateIotDeviceSimInfo(@RequestBody List<Map> maps) {
        String addr = "http://25.212.181.38:18081/terminal/terminalProcess/terminal/updateSimInfo";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("tel", "15200855793");
        headers.put("smallToken", getToken());
        String ss = HttpUtil.httpPost(addr, JSONObject.toJSONString(maps), headers);
        Map parse1 = (Map) JSON.parse(ss);
        log.info("调用我来我来保11.3终端调试-状态新增几口---参数为{}得到的返回值为：" + JSONObject.toJSONString(parse1), JSONObject.toJSONString(maps));
        //写入日志表
        commonInterface.dbAccess_insert("insert into  dms_yzz_log (log_time,log_type,log_json,dev_label,status) values(now(),'11-3','" + JSONObject.toJSONString(maps) + "','','" + JSONObject.toJSONString(parse1) + "')");
        return new JSONObject(parse1);
    }

    public static void main(String[] args) {
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString().replace("-", ""); // 去掉"-"
        System.out.println(id);
    }
    /* //14.2查询子设备信息
                 termEsn 终端ID
                termId 终端ESN
                 cgddh   采购订单号
                terminalState   终端状态
                pageSize    页编号-1开始
                pageNum  页编号-1开始
    **/
    @PostMapping("/queryChildDevice")
    public JSONObject queryChildDevice(@RequestBody Map maps) {
        String addr = "http://25.212.181.38:18081/terminal/terminalProcess/terminal/getTerminalOperateListToForeign";
        Map<String, String> headers = new HashMap<String, String>();
        String termEsn = maps.get("termEsn") != null ? maps.get("termEsn").toString() : "";
        String termId = maps.get("termId") != null ? maps.get("termId").toString() : "";
        String cgddh = maps.get("cgddh") != null ? maps.get("cgddh").toString() : "";
        String terminalState = maps.get("terminalState") != null ? maps.get("terminalState").toString() : "";
        String pageSize = maps.get("pageSize") != null ? maps.get("pageSize").toString() : "";
        String pageNum = maps.get("pageNum") != null ? maps.get("pageNum").toString() : "";
        if (StringUtils.isBlank(termEsn)) {
            headers.put("termEsn", termEsn);
        }
        if (StringUtils.isBlank(termId)) {
            headers.put("termId", termId);
        }
        if (StringUtils.isBlank(cgddh)) {
            headers.put("cgddh", cgddh);
        }
        if (StringUtils.isBlank(terminalState)) {
            headers.put("terminalState", terminalState);
        }
        if (StringUtils.isBlank(pageSize)) {
            headers.put("pageSize", pageSize);
        }
        if (StringUtils.isBlank(pageNum)) {
            headers.put("pageNum", pageNum);
        }
        String ss = HttpUtil.httpGet(addr, headers);
        Map parse1 = (Map) JSON.parse(ss);
        log.info("调用我来我来保14.2查询子设备信息---参数为{}得到的返回值为：" + JSONObject.toJSONString(parse1), JSONObject.toJSONString(maps));

        //查询总条数、进行分页;

        //以子设备id为准
    /*
            String CGDDH = map.get("CGDDH") != null ? map.get("CGDDH").toString() : "";
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
 */
        //判断是否非空，封装成指定字段调用接口
        try {
            Map<String,String> ma=new HashMap<>();
           // ma.put("ID",ID);
          //  purchaseOrdersService.addChildDevice(null);
        } catch (Exception e) {
            log.info("新增子设备接口报错了"+e.getMessage());
        }
        return new JSONObject(parse1);
    }

}

package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.QlclProjectService;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.java2d.pipe.AAShapePipe;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


@Service("QlclProjectService")
@Slf4j
public class QlclProjectServiceImpl implements QlclProjectService {
    @Autowired
    CommonInterface commonInterface;
    //public   static String wlbOss="http://ggzj-center.oss-hn-1-a.ops.sgmc.sgcc.com.cn/%E9%85%8D%E7%BD%91%E6%88%91%E6%9D%A5%E4%BF%9D/";
    public static String wlbOss = "http://25.212.172.50:9099/getOssQlc/";


    /**
     * @description：
     * @author：sunheng
     * @date：2022/11/23 15:50
     * @param：
     */
    @Override
    public LayJson getQlcListPo(Map<String, Object> map) {
        String orgId = map.get("orgId") == null || map.get("orgId") == "" ? null : map.get("orgId").toString();
        //02省） 03 市）  04县 ） 05供电所） 06线路   07 台区
        String orgType = map.get("orgType") == null || map.get("orgType") == "" ? null : map.get("orgType").toString();

        String termFactory = map.get("termFactory") == null || map.get("termFactory") == "" ? null : map.get("termFactory").toString();
        String termEsn = map.get("termEsn") == null || map.get("termEsn") == "" ? null : map.get("termEsn").toString();

        String tmDqztList = map.get("tmDqzt") == null || map.get("tmDqzt") == "" ? "" : map.get("tmDqzt").toString();

        String tmDqzt = "";
        if (tmDqztList.length() >= 2) {
            tmDqzt = tmDqztList.toString().replace(" ", "");
            tmDqzt = tmDqzt.substring(1, tmDqzt.length() - 1);
        }
        //所属产品
        String pdNameList = map.get("pdName") == null || map.get("pdName") == "" ? "" : JSON.toJSONString(map.get("pdName"));
        log.info("输出" + pdNameList);
        String pdName = "";
        if (pdNameList.length() >= 2) {
            pdName = pdNameList.replace(" ", "");
        }
        String substringPdName = "";
        if (StringUtils.isNotBlank(pdName)) {

            List tmList = JSON.parseArray(pdName);
            for (Object li : tmList) {
                if (li != null) {
                    substringPdName += "'" + li + "',";
                }
            }
        }

        if (StringUtils.isNotBlank(substringPdName)) {
            substringPdName = substringPdName.substring(0, substringPdName.length() - 1);
        }

        String isOnline = map.get("isOnline") == null || map.get("isOnline") == "" ? null : map.get("isOnline").toString();

        String tgName = map.get("tgName") == null || map.get("tgName") == "" ? null : map.get("tgName").toString();
        //配变编号（16M）
        String tgId = map.get("tgId") == null || map.get("tgId") == "" ? null : map.get("tgId").toString();
        //台区编号
        String tgNo = map.get("tgNo") == null || map.get("tgNo") == "" ? null : map.get("tgNo").toString();
        String projectName = map.get("projectName") == null || map.get("projectName") == "" ? null : map.get("projectName").toString();

        //采购订单号
        String cgddh = map.get("cgddh") == null || map.get("cgddh") == "" ? null : map.get("cgddh").toString();
        //合同名称
        String htNo = map.get("htNo") == null || map.get("htNo") == "" ? null : map.get("htNo").toString();
        //融合终端名称
        String devName = map.get("devName") == null || map.get("devName") == "" ? null : map.get("devName").toString();
        String simIp = map.get("simIp") == null || map.get("simIp") == "" ? null : map.get("simIp").toString();
        String simNo = map.get("simNo") == null || map.get("simNo") == "" ? null : map.get("simNo").toString();

        String lineName = map.get("lineName") == null || map.get("lineName") == "" ? null : map.get("lineName").toString();
        String zbcgfs = map.get("zbcgfs") == null || map.get("zbcgfs") == "" ? null : map.get("zbcgfs").toString();

        int pageNo = map.get("pageNo") == null || map.get("pageNo") == "" ? 1 : Integer.parseInt(map.get("pageNo").toString());
        int pageSize = map.get("pageSize") == null || map.get("pageSize") == "" ? 50 : Integer.parseInt(map.get("pageSize").toString());

        String sql = " select  \n" + " re.term_factory,re.term_esn,re.term_id,re.dev_type,re.tm_dqzt,d.is_online,"
                + "project.project_name ,re.link_order_no,ht.ht_no,ht.ht_name,ht.zbcgfs,d.dev_name,"
                + "de.name as tg_name,tr_de.default_s as tg_id,tr_de.tg_no," + "info.name as line_name,     " +
                "   if(tr_de.city_org_nm is null,dis.city_company_name,tr_de.city_org_nm), " +
                "    if(tr_de.county_org_nm is null,dis.county_company_name,tr_de.county_org_nm),tr_de.gds_org_nm, "
                + " d.out_dev_id,p.pd_name,d.device_key,d.sim_no,d.sim_ip,re.batch_number,d.is_check,tr_de.start_life_date||'',zhbb,rate_cap,sfeczy,is_a_check,is_a_check_time "
                + "   from DMS_IOT_DEVICE_RESOURCE_INFO as re     "
                + "  left join dms_termesn_dispatch as dis on  re.link_dispatch=dis.term_dispatch_id "
                + "    left join iot_device as d on d.dev_label=re.term_esn  and   d.is_valid=1 and d.connect_mode=1 and d.out_iot_fac=2  "
                + "   left join iot_product as p on d.pd_id=p.id  and p.out_iot_fac='2' and p.is_valid='1' and p.pd_mode='0' "
                + "   left join dms_tr_device as de on de.id=d.rely_id "
                + "   left join (select * from d5000.dms_tr_account_info where run_st=20 and default_i is null) as tr_de  on de.device_asset_id=concat('PD_',tr_de.tr_pms_no)"
                + "   left join dms_feeder_account_info_new  as info on info.id=tr_de.feeder_id"
                + "   left join DMS_TR_PROJECT_ORDER as project on project.cgddh=re.link_order_no "
                + "   left join DMS_IOT_HT_INFO as ht on ht.ht_no =project.ht_number    where 1=1  and re.is_valid=1  ";

        String sqlCount = " select count(1) from DMS_IOT_DEVICE_RESOURCE_INFO as re   left join dms_termesn_dispatch as dis on  re.link_dispatch=dis.term_dispatch_id "
                + "  left join iot_device as d on d.dev_label=re.term_esn   and   d.is_valid=1 and d.connect_mode=1 and d.out_iot_fac=2  "
                + "   left join dms_tr_device as de on de.id=d.rely_id "
                + " left join iot_product as p on d.pd_id=p.id  and p.out_iot_fac='2' and p.is_valid='1' and p.pd_mode='0'"
                + "   left join (select * from d5000.dms_tr_account_info where run_st=20 and default_i is null) as tr_de  on de.device_asset_id=concat('PD_',tr_de.tr_pms_no)"
                + "   left join dms_feeder_account_info_new  as info on info.id=tr_de.feeder_id "
                + "   left join DMS_TR_PROJECT_ORDER as project on project.cgddh=re.link_order_no "
                + "   left join DMS_IOT_HT_INFO as ht on ht.ht_no =project.ht_number    where 1=1  and re.is_valid=1   ";


        if (!StringUtils.isBlank(termFactory)) {
            sql += "  and re.term_factory like'%" + termFactory + "%' ";
            sqlCount += "  and re.term_factory like'%" + termFactory + "%' ";
        }
        if (!StringUtils.isBlank(termEsn)) {
            sql += "   and re.term_esn='" + termEsn + "' ";
            sqlCount += "   and re.term_esn='" + termEsn + "' ";
        }
        if (!StringUtils.isBlank(substringPdName)) {
            sql += " and p.pd_name in (" + substringPdName + ")";
            sqlCount += "  and p.pd_name in (" + substringPdName + ") ";
        }

        if (!StringUtils.isBlank(tmDqzt)) {


            if (tmDqzt.contains("9")) {

                String replace = tmDqzt.replace('9', '0');
                sql += "   and ( re.tm_dqzt in (" + replace + ") or (re.tm_dqzt='9'  and (d.is_check is  null OR d.is_check =0 ))";
                sqlCount += "   and ( re.tm_dqzt in (" + replace + ") or ( re.tm_dqzt='9' and (d.is_check is  null OR d.is_check =0 ))";
            } else {
                sql += "   and  ( re.tm_dqzt in (" + tmDqzt + ")";
                sqlCount += "   and ( re.tm_dqzt in (" + tmDqzt + ")";
            }

            if (tmDqzt.contains("10")) { //已验收
                sql += "     or ( re.tm_dqzt>='9' and d.is_check =1 )";
                sqlCount += "    or (re.tm_dqzt>='9'  and d.is_check = 1 ) ";
            }
            sql += "   )";
            sqlCount += "   )";


        }
        if (!StringUtils.isBlank(isOnline)) {
            sql += "    and is_online='" + isOnline + "'";
            sqlCount += "    and is_online='" + isOnline + "'";
        }

        if (!StringUtils.isBlank(tgName)) {
            sql += "  and de.name like '%" + tgName + "%' ";
            sqlCount += "  and de.name like '%" + tgName + "%' ";
        }
        if (!StringUtils.isBlank(tgId)) {
            sql += "   and de.code='" + tgId + "'  ";
            sqlCount += "   and de.code='" + tgId + "'  ";
        }
        if (!StringUtils.isBlank(tgNo)) {
            sql += "  and tr_de.tg_no='" + tgNo + "'  ";
            sqlCount += "  and tr_de.tg_no='" + tgNo + "'  ";
        }
        if (!StringUtils.isBlank(projectName)) {
            sql += "   and project.project_name like '%" + projectName + "%' ";
            sqlCount += "   and project.project_name like '%" + projectName + "%' ";
        }


        if (!StringUtils.isBlank(cgddh)) {
            sql += "  and re.link_order_no='" + cgddh + "' ";
            sqlCount += "  and re.link_order_no='" + cgddh + "' ";
        }
        if (!StringUtils.isBlank(htNo)) {
            sql += " and ht.ht_no like '%" + htNo + "%' ";
            sqlCount += " and ht.ht_no like '%" + htNo + "%' ";
        }
        if (!StringUtils.isBlank(devName)) {
            sql += "  and  d.dev_name like '%" + devName + "%'  ";
            sqlCount += "  and  d.dev_name like '%" + devName + "%'  ";
        }
        if (!StringUtils.isBlank(simIp)) {
            sql += "  and d.sim_ip='" + simIp + "'";
            sqlCount += "  and d.sim_ip='" + simIp + "'";
        }
        if (!StringUtils.isBlank(simNo)) {
            sql += "  and d.sim_no='" + simNo + "' ";
            sqlCount += "  and d.sim_no='" + simNo + "' ";
        }

        if (!StringUtils.isBlank(lineName)) {
            sql += " and info.name like '%" + lineName + "%' ";
            sqlCount += " and info.name like '%" + lineName + "%' ";
        }
        if (!StringUtils.isBlank(zbcgfs)) {
            sql += "  and ht.zbcgfs='" + zbcgfs + "' ";
            sqlCount += "  and ht.zbcgfs='" + zbcgfs + "' ";
        }

        if (StringUtils.isNotBlank(orgType)) {
            if (orgType.equals("03")) {
                sql += "   and if(tr_de.city_base_org_id is not null, " +
                        " if(tr_de.city_base_org_id='" + orgId + "', 1, 0), " +
                        " if(dis.city_company_id='" + orgId + "', 1, 0) )";
                sqlCount += "   and if(tr_de.city_base_org_id is not null, " +
                        " if(tr_de.city_base_org_id='" + orgId + "', 1, 0), " +
                        " if(dis.city_company_id='" + orgId + "', 1, 0) )";
            }

            if (orgType.equals("04")) {
//                sql += " and (tr_de.county_base_org_id='" + orgId + "' or dis.county_company_id ='" + orgId + "')  ";
//                sqlCount += " and (tr_de.county_base_org_id='" + orgId + "' or dis.county_company_id='" + orgId + "')  ";
                sql += "   and if(tr_de.county_base_org_id is not null, " +
                        " if(tr_de.county_base_org_id='" + orgId + "', 1, 0), " +
                        " if(dis.county_company_id='" + orgId + "', 1, 0) )";
                sqlCount += "   and if(tr_de.county_base_org_id is not null, " +
                        " if(tr_de.county_base_org_id='" + orgId + "', 1, 0), " +
                        " if(dis.county_company_id='" + orgId + "', 1, 0) )";
            }

            if (orgType.equals("05")) {
                sql += " and (tr_de.gds_base_org_id='" + orgId + "' )  ";
                sqlCount += " and (tr_de.gds_base_org_id='" + orgId + "')  ";
            }

            if (orgType.equals("06")) {
                sql += "   and( tr_de.id='" + orgId + "' )  ";
                sqlCount += "   and ( tr_de.id='" + orgId + "' )  ";
            }

            if (orgType.equals("07")) {
                sql += "  and  (info.id='" + orgId + "' )  ";
                sqlCount += "  and ( info.id='" + orgId + "' )  ";
            }
        }
        sql += "   order by re.send_time desc ";
        sqlCount += "   order by re.send_time desc ";
        sql += "   limit " + (pageNo - 1) * pageSize + "," + pageSize;
        log.info("终端全流程sql————" + sql);
        System.out.println("终端全流程sqlcCount————" + sqlCount);
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        List<Object[]> devCount = commonInterface.selectListBySql(sqlCount.toString());
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        for (Object[] objs : devList) {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("term_factory", objs[0] == null ? "-" : objs[0].toString());
            hashMap.put("term_esn", objs[1] == null ? "-" : objs[1].toString());
            hashMap.put("term_id", objs[2] == null ? "-" : objs[2].toString());
            hashMap.put("dev_type", objs[3] == null ? "-" : objs[3].toString());
            String ic_check = objs[25] == null ? "-" : objs[25].toString();
            if (objs[4] != null) {

                switch (objs[4].toString()) {
                    case "1":
                        hashMap.put("tm_dqzt", "未检测");
                        break;
                    case "2":
                        hashMap.put("tm_dqzt", "已检测");
                        break;
                    case "3":
                        hashMap.put("tm_dqzt", "已绑定");
                        break;
                    case "4":
                        hashMap.put("tm_dqzt", "已注册");
                        break;
                    case "5":
                        hashMap.put("tm_dqzt", "已调试");
                        break;
                    case "6":
                        hashMap.put("tm_dqzt", "已配送");
                        break;
                    case "7":
                        hashMap.put("tm_dqzt", "已收货");
                        break;
                    case "8":
                        hashMap.put("tm_dqzt", "已安装");
                        break;
                    case "9":
                        if (!ic_check.equals("-")) {
                            log.info("ic_check" + ic_check);
                            if (ic_check.equals("1")) {
                                hashMap.put("tm_dqzt", "已验收");
                            } else {
                                hashMap.put("tm_dqzt", "已建档");
                            }
                        } else {
                            hashMap.put("tm_dqzt", "已建档");
                        }
                        break;

                    case "12":
                        hashMap.put("tm_dqzt", "试运行异常");
                        break;
                    case "13":
                        hashMap.put("tm_dqzt", "正式投运");
                        break;
                    default:
                        hashMap.put("tm_dqzt", "-");
                        break;
                }

            } else {
                hashMap.put("tm_dqzt", objs[4] == null ? "-" : objs[4].toString());
            }

            hashMap.put("is_online", "");
            if (objs[5] != null) {

                if (objs[5].toString().equals("1")) {
                    hashMap.put("is_online", "在线");
                }
                if (objs[5].toString().equals("2")) {
                    hashMap.put("is_online", "离线");
                }
                if (objs[5].toString().equals("3")) {
                    hashMap.put("is_online", "未连接");
                }
            }
            hashMap.put("project_name", objs[6] == null ? "-" : objs[6].toString());
            hashMap.put("link_order_no", objs[7] == null ? "-" : objs[7].toString());
            hashMap.put("ht_no", objs[8] == null ? "-" : objs[8].toString());
            hashMap.put("ht_name", objs[9] == null ? "-" : objs[9].toString());
            hashMap.put("zbcgfs", objs[10] == null ? "-" : objs[10].toString());
            hashMap.put("dev_name", objs[11] == null ? "-" : objs[11].toString());
            hashMap.put("tg_name", objs[12] == null ? "-" : objs[12].toString());
            hashMap.put("tg_id", objs[13] == null ? "-" : objs[13].toString());
            hashMap.put("tg_no", objs[14] == null ? "-" : objs[14].toString());
            hashMap.put("line_name", objs[15] == null ? "-" : objs[15].toString());
            hashMap.put("city_org_nm", objs[16] == null ? "-" : objs[16].toString());
            hashMap.put("county_org_nm", objs[17] == null ? "-" : objs[17].toString());
            hashMap.put("gds_org_nm", objs[18] == null ? "-" : objs[18].toString());
            hashMap.put("out_dev_id", objs[19] == null ? "-" : objs[19].toString());
            hashMap.put("pd_name", objs[20] == null ? "-" : objs[20].toString());
            hashMap.put("device_key", objs[21] == null ? "-" : objs[21].toString());
            hashMap.put("sim_no", objs[22] == null ? "-" : objs[22].toString());
            hashMap.put("sim_ip", objs[23] == null ? "-" : objs[23].toString());
            hashMap.put("apply_for_batch_number", objs[24] == null ? "-" : objs[24].toString());

            hashMap.put("start_life_date", objs[26] == null ? "-" : objs[26].toString());
            hashMap.put("zhbb", objs[27] == null ? "-" : objs[27].toString());
            hashMap.put("rate_cap", objs[28] == null ? "-" : objs[28].toString());
            //sfeczy,is_a_check,is_a_check_time
            String eczy = objs[29] == null ? "-" : objs[29].toString();
            if (eczy.equals("1")) {
                eczy = "是";
            } else {
                eczy = "否";
            }
            hashMap.put("sfeczy", eczy);
            String tszt = objs[30] == null ? "-" : objs[30].toString();
            if (tszt.equals("1")) {
                tszt = "成功";
            } else if (tszt.equals("0")) {
                tszt = "失败";
            } else {
                tszt = "未调试";
            }

            hashMap.put("tszt", tszt);
            hashMap.put("tssj", objs[31] == null ? "-" : objs[31].toString());
            value.add(hashMap);
        }
        String s = devCount.get(0)[0] != null ? devCount.get(0)[0].toString() : "0";
        return new LayJson(200, "请求成功", value, Integer.parseInt(s));
    }

    @Override
    public Map<String, Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response) {

        String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss") + ".xls";
        String targetFilePath = request.getSession().getServletContext().getRealPath("download") + File.separator + fileName;
        String sheet = "终端全流程展示";
        String orgType = request.getParameter("orgType") == null || request.getParameter("orgType") == "" ? null : request.getParameter("orgType").toString();
        String orgId = request.getParameter("orgId") == null || request.getParameter("orgId") == "" ? null : request.getParameter("orgId").toString();

        String termFactory = request.getParameter("termFactory") == null || request.getParameter("termFactory") == "" ? null : request.getParameter("termFactory").toString();
        String termEsn = request.getParameter("termEsn") == null || request.getParameter("termEsn") == "" ? null : request.getParameter("termEsn").toString();
        String tmDqzt = request.getParameter("tmDqzt") == null || request.getParameter("tmDqzt") == "" ? null : request.getParameter("tmDqzt");
        log.info("tmDzt为" + tmDqzt);
        if (StringUtils.isNotBlank(tmDqzt)) {
            tmDqzt = tmDqzt.replace(" ", "");
        }
        //所属产品  "XXX,XX"
        String pdNameList = request.getParameter("pdName") == null || request.getParameter("pdName") == "" ? "" : request.getParameter("pdName");
        String substringPdName = "";
        if (StringUtils.isNotBlank(pdNameList)) {
            pdNameList = pdNameList.replace(" ", "");
            pdNameList = pdNameList.replace(",", "','");
            substringPdName = "'" + pdNameList + "'";
        }
        String isOnline = request.getParameter("isOnline") == null || request.getParameter("isOnline") == "" ? null : request.getParameter("isOnline").toString();

        String tgName = request.getParameter("tgName") == null || request.getParameter("tgName") == "" ? null : request.getParameter("tgName").toString();
        //配变编号（16M）
        String tgId = request.getParameter("tgId") == null || request.getParameter("tgId") == "" ? null : request.getParameter("tgId").toString();
        //台区编号
        String tgNo = request.getParameter("tgNo") == null || request.getParameter("tgNo") == "" ? null : request.getParameter("tgNo").toString();
        String projectName = request.getParameter("projectName") == null || request.getParameter("projectName") == "" ? null : request.getParameter("projectName").toString();

        //采购订单号
        String cgddh = request.getParameter("cgddh") == null || request.getParameter("cgddh") == "" ? null : request.getParameter("cgddh").toString();
        //合同名称
        String htNo = request.getParameter("htNo") == null || request.getParameter("htNo") == "" ? null : request.getParameter("htNo").toString();
        //融合终端名称
        String devName = request.getParameter("devName") == null || request.getParameter("devName") == "" ? null : request.getParameter("devName").toString();
        String simIp = request.getParameter("simIp") == null || request.getParameter("simIp") == "" ? null : request.getParameter("simIp").toString();
        String simNo = request.getParameter("simNo") == null || request.getParameter("simNo") == "" ? null : request.getParameter("simNo").toString();

        String lineName = request.getParameter("lineName") == null || request.getParameter("lineName") == "" ? null : request.getParameter("lineName").toString();
        String zbcgfs = request.getParameter("zbcgfs") == null || request.getParameter("zbcgfs") == "" ? null : request.getParameter("zbcgfs").toString();

        String sql = " select  \n" + " re.term_factory,re.term_esn,re.term_id,re.dev_type,re.tm_dqzt,d.is_online,\n"
                + "project.project_name ,re.link_order_no,ht.ht_no,ht.ht_name,ht.zbcgfs,d.dev_name,\n"
                + "de.name as tg_name,tr_de.default_s as tg_id,tr_de.tg_no,\n"
                + "info.name as line_name,  " +
                "   if(tr_de.city_org_nm is null,dis.city_company_name,tr_de.city_org_nm), " +
                "    if(tr_de.county_org_nm is null,dis.county_company_name,tr_de.county_org_nm),tr_de.gds_org_nm, "
                + " d.out_dev_id,p.pd_name,d.device_key,d.sim_no,d.sim_ip,re.batch_number,d.is_check,tr_de.start_life_date||'',zhbb,rate_cap,sfeczy,is_a_check,is_a_check_time||'' "
                + "   from DMS_IOT_DEVICE_RESOURCE_INFO as re "
                + "  left join dms_termesn_dispatch as dis on  re.link_dispatch=dis.term_dispatch_id "
                + "    left join iot_device as d on d.dev_label=re.term_esn  and   d.is_valid=1 and d.connect_mode=1 and d.out_iot_fac=2  "
                + "   left join iot_product as p on d.pd_id=p.id   and p.out_iot_fac='2' and p.is_valid='1' and p.pd_mode='0'"
                + "   left join dms_tr_device as de on de.id=d.rely_id\n"
                + "   left join (select * from d5000.dms_tr_account_info where run_st=20 and default_i is null) as tr_de  on de.device_asset_id=concat('PD_',tr_de.tr_pms_no)"
                + "   left join dms_feeder_account_info_new  as info on info.id=tr_de.feeder_id"
                + "   left join DMS_TR_PROJECT_ORDER as project on project.cgddh=re.link_order_no"
                + "   left join DMS_IOT_HT_INFO as ht on ht.ht_no =project.ht_number    where 1=1  and re.is_valid=1   ";

        if (!StringUtils.isBlank(termFactory)) {
            sql += "  and re.term_factory like'%" + termFactory + "%' ";
        }
        if (!StringUtils.isBlank(termEsn)) {
            sql += "   and re.term_esn='" + termEsn + "' ";
        }
        if (!StringUtils.isBlank(substringPdName)) {
            sql += " and p.pd_name in (" + substringPdName + ")";
        }
        if (StringUtils.isNotBlank(tmDqzt)) {
            if (tmDqzt.contains("9")) {
                String replace = tmDqzt.replace('9', '0');
                sql += "   and ( re.tm_dqzt in (" + replace + ") or (re.tm_dqzt='9'  and (d.is_check is  null OR d.is_check =0))";
            } else {
                sql += "   and  ( re.tm_dqzt in (" + tmDqzt + ")";
            }

//            //试运行
//            if (tmDqzt.contains("11")) {
//                sql += "    or (re.tm_dqzt>='9' and d.is_check =1 )";
//
//            }
            if (tmDqzt.contains("10")) { //已验收
                sql += "     or ( re.tm_dqzt>='9' and d.is_check =1 )";
            }
            sql += "   )";
        }
        if (!StringUtils.isBlank(isOnline)) {
            sql += "    and is_online='" + isOnline + "'";
        }
        if (!StringUtils.isBlank(tgName)) {
            sql += "  and de.name like '%" + tgName + "%' ";
        }
        if (!StringUtils.isBlank(tgId)) {
            sql += "   and de.code='" + tgId + "'  ";
        }
        if (!StringUtils.isBlank(tgNo)) {
            sql += "  and tr_de.tg_no='" + tgNo + "'  ";
        }
        if (!StringUtils.isBlank(projectName)) {
            sql += "   and project.project_name like '%" + projectName + "%' ";
        }


        if (!StringUtils.isBlank(cgddh)) {
            sql += "  and re.link_order_no='" + cgddh + "' ";
        }
        if (!StringUtils.isBlank(htNo)) {
            sql += " and ht.ht_no like '%" + htNo + "%' ";
        }
        if (!StringUtils.isBlank(devName)) {
            sql += "  and  d.dev_name like '%" + devName + "%'  ";
        }
        if (!StringUtils.isBlank(simIp)) {
            sql += "  and d.sim_ip='" + simIp + "'";
        }

        if (!StringUtils.isBlank(simNo)) {
            sql += "  and d.sim_no='" + simNo + "'";
        }

        if (!StringUtils.isBlank(lineName)) {
            sql += " and info.name like '%" + lineName + "%' ";
        }
        if (!StringUtils.isBlank(zbcgfs)) {
            sql += "  and ht.zbcgfs='" + zbcgfs + "' ";
        }

        if (StringUtils.isNotBlank(orgType)) {
            if (orgType.equals("03")) {
                // sql += " and (tr_de.city_base_org_id='" + orgId + "' or  dis.city_company_id='" + orgId + "' )  ";

                sql += "   and if(tr_de.city_base_org_id is not null, " +
                        " if(tr_de.city_base_org_id='" + orgId + "', 1, 0), " +
                        " if(dis.city_company_id='" + orgId + "', 1, 0) )";
            }
            if (orgType.equals("04")) {
                sql += "   and if(tr_de.county_base_org_id is not null, " +
                        " if(tr_de.county_base_org_id='" + orgId + "', 1, 0), " +
                        " if(dis.county_company_id='" + orgId + "', 1, 0) )";
            }
            if (orgType.equals("05")) {
                sql += " and (tr_de.gds_base_org_id='" + orgId + "' )  ";
            }


            if (orgType.equals("06")) {
                sql += "  and ( tr_de.id='" + orgId + "' )  ";
            }
            if (orgType.equals("07")) {
                sql += "  and ( info.id='" + orgId + "' )  ";
            }
        }
        sql += "   order by re.send_time desc ";
        log.info("终端全流程导出-" + sql);
        List<Object[]> devList = commonInterface.selectListBySql(sql);

        String titles[] = new String[]{"序号", "供应商名称", "核心板ESN", "终端ID", "设备型号", "终端当前状态", "终端在线状态", "发货申请批次号", //7
                "项目名称", "采购订单号", "合同编号", "合同名称", "招标采购方式", "融合终端名称", "所属配变", "配变编号（16M）"    //8
                , "台区编号", "配变投运时间", "综合倍率", "配变容量", "所属馈线", "市公司", "县公司", "供电所", "客户端ID", "所属产品", "用户名", "物联卡识别号", "物联卡IP", "是否二次转运", "调试状态", "联调体调试时间"}; //10
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

            newObj[5] = temObj[4] == null || temObj[4].equals("") ? "-" : temObj[4];
            String ic_check = temObj[25] == null ? "-" : temObj[25].toString();
            if (temObj[4] != null) {
                switch (temObj[4].toString()) {
                    case "1":
                        newObj[5] = "未检测";
                        break;
                    case "2":
                        newObj[5] = "已检测";
                        break;
                    case "3":
                        newObj[5] = "已绑定";
                        break;
                    case "4":
                        newObj[5] = "已注册";
                        break;
                    case "5":
                        newObj[5] = "已调试";
                        break;
                    case "6":
                        newObj[5] = "已配送";
                        break;
                    case "7":
                        newObj[5] = "已收货";
                        break;
                    case "8":
                        newObj[5] = "已安装";
                        break;
                    case "9":
                        if (!ic_check.equals("-")) {
                            if (ic_check.equals("1")) {

                                newObj[5] = "已验收";
                            } else {
                                newObj[5] = "已建档";
                            }
                        } else {
                            newObj[5] = "已建档";
                        }
                        break;
                    case "12":
                        newObj[5] = "试运行异常";
                        break;
                    case "13":
                        newObj[5] = "正式投运";
                        break;
                    default:
                        newObj[5] = "-";
                        break;
                }

            }

            if (temObj[5] != null) {
                if (temObj[5].toString().equals("1")) {
                    newObj[6] = "在线";
                }
                if (temObj[5].toString().equals("2")) {
                    newObj[6] = "离线";
                }
                if (temObj[5].toString().equals("3")) {
                    newObj[6] = "未连接";
                }
            } else {
                newObj[6] = "";
            }
            //   newObj[6] = temObj[5] == null || temObj[5].equals("") ? "-" : temObj[5];
            newObj[7] = temObj[24] == null || temObj[24].equals("") ? "-" : temObj[24];
            newObj[8] = temObj[6] == null || temObj[6].equals("") ? "-" : temObj[6];
            newObj[9] = temObj[7] == null || temObj[7].equals("") ? "-" : temObj[7];
            newObj[10] = temObj[8] == null || temObj[8].equals("") ? "-" : temObj[8];
            newObj[11] = temObj[9] == null || temObj[9].equals("") ? "-" : temObj[9];
            newObj[12] = temObj[10] == null || temObj[10].equals("") ? "-" : temObj[10];
            newObj[13] = temObj[11] == null || temObj[11].equals("") ? "-" : temObj[11];
            newObj[14] = temObj[12] == null || temObj[12].equals("") ? "-" : temObj[12];
            newObj[15] = temObj[13] == null || temObj[13].equals("") ? "-" : temObj[13];
            newObj[16] = temObj[14] == null || temObj[14].equals("") ? "-" : temObj[14];

            newObj[17] = temObj[26] == null || temObj[26].equals("") ? "-" : temObj[26];
            newObj[18] = temObj[27] == null || temObj[27].equals("") ? "-" : temObj[27];
            newObj[19] = temObj[28] == null || temObj[28].equals("") ? "-" : temObj[28];

            newObj[20] = temObj[15] == null || temObj[15].equals("") ? "-" : temObj[15];
            newObj[21] = temObj[16] == null || temObj[16].equals("") ? "-" : temObj[16];
            newObj[22] = temObj[17] == null || temObj[17].equals("") ? "-" : temObj[17];
            newObj[23] = temObj[18] == null || temObj[18].equals("") ? "-" : temObj[18];
            newObj[24] = temObj[19] == null || temObj[19].equals("") ? "-" : temObj[19];
            newObj[25] = temObj[20] == null || temObj[20].equals("") ? "-" : temObj[20];
            newObj[26] = temObj[21] == null || temObj[21].equals("") ? "-" : temObj[21];
            newObj[27] = temObj[22] == null || temObj[22].equals("") ? "-" : temObj[22];
            newObj[28] = temObj[23] == null || temObj[23].equals("") ? "-" : temObj[23];

            String eczy = temObj[29] == null || temObj[29].equals("") ? "-" : temObj[29].toString();
            if (eczy.equals("1")) {
                eczy = "是";
            } else {
                eczy = "否";
            }
            newObj[29] = eczy;
            String ys = temObj[30] == null || temObj[30].equals("") ? "-" : temObj[30].toString();
            if (ys.equals("1")) {
                ys = "成功";
            } else if (ys.equals("0")) {
                ys = "失败";
            } else {
                ys = "未调试";
            }
            newObj[30] = ys;
            newObj[31] = temObj[31] == null || temObj[31].equals("") ? "-" : temObj[31];


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
    public LayJson getQlcTpPo(Map<String, Object> map) {
        String termEsn = map.get("termEsn") == null || map.get("termEsn") == "" ? null : map.get("termEsn").toString();
        String sql = " select esn1,esn2,esn3,esn4 from DMS_IOT_DEVICE_RESOURCE_INFO where is_valid=1 and term_esn='" + termEsn + "' and is_valid=1 ";
        JSONArray objects = commonInterface.dbAccess_selectList(sql.toString());
        List<Map> listMaps = JSONObject.parseArray(objects.toJSONString(), Map.class);
        Map<String, String> map1 = listMaps.get(0);
        List list = new ArrayList();
        /*esn1（融合终端） esn2(无功补偿)、esn3（低压开关）、esn4(台区整体)*/
        //修改图片展示顺序 1整体、 2融合终端安装 3、低压开关安装  4、无功补偿安装图片
        String esn4 = map1.get("ESN4");
        if (StringUtils.isNotBlank(esn4)) {
            HashMap<String, String> obIn = new HashMap<>();
            obIn.put("key", "台区整体");
            obIn.put("value", wlbOss + esn4);
            list.add(obIn);
        }

        String esn1 = map1.get("ESN1");
        if (StringUtils.isNotBlank(esn1)) {
            HashMap<String, String> obIn = new HashMap<>();
            obIn.put("key", "融合终端");
            obIn.put("value", wlbOss + esn1);
            list.add(obIn);
        }

        String esn3 = map1.get("ESN3");
        if (StringUtils.isNotBlank(esn3)) {
            HashMap<String, String> obIn = new HashMap<>();
            obIn.put("key", "低压开关");
            obIn.put("value", wlbOss + esn3);
            list.add(obIn);
        }

        String esn2 = map1.get("ESN2");
        if (StringUtils.isNotBlank(esn2)) {
            HashMap<String, String> obIn = new HashMap<>();
            obIn.put("key", "无功补偿");
            obIn.put("value", wlbOss + esn2);
            list.add(obIn);
        }

      /*  for (Map.Entry<String, String> ob : map1.entrySet()) {
            if (StringUtils.isNotBlank(ob.getValue())) {
                HashMap<String, String> obIn = new HashMap<>();

                if (ob.getKey().equals("ESN4")) {

                }
                if (ob.getKey().equals("ESN1")) {
                    obIn.put("key", "融合终端");
                    obIn.put("value", ob.getValue());
                }
                if (ob.getKey().equals("ESN2")) {
                    obIn.put("key", "无功补偿");
                    obIn.put("value", ob.getValue());
                }
                if (ob.getKey().equals("ESN3")) {
                    obIn.put("key", "低压开关");
                    obIn.put("value", ob.getValue());
                }

            }
        }*/
        LayJson layJson = new LayJson(200, "请求成功", list, list.size());
        return layJson;
    }

    /**
     * esn表里并没有10 、11状态 需要查询的时候做处理
     *
     * @description：状态查询（这里已收货默认收货的，所以收货人是配送时候的收货人而不是签收人）
     * @author：sunheng
     * @date：2022/11/24 11:20
     * @param：
     */
    @Override
    public LayJson getQlcZtPo(Map<String, Object> map) {
        String termId = map.get("termId") == null || map.get("termId") == "" ? null : map.get("termId").toString();
        String jdParam = "";
        String rc = "select log_json from dms_yzz_log where  log_type='3' and dev_label =(select term_esn from dms_iot_device_resource_info where term_id='" + termId + "' and is_valid=1) order by log_time desc ";
        log.info("查询对应的参数" + rc);
        List<Object[]> list = commonInterface.selectListBySql(rc);
        if (list.size() > 0) {
            Object[] rcObjects = commonInterface.selectListBySql(rc).get(0);
            jdParam = rcObjects[0] == null ? "" : rcObjects[0].toString();
        }
        //进行判断工单的状态是不是等于5（已完成）  如果不是的话的话直接显示下面结果
        String sqlOrder = "select  (case \n" + " when d.is_check =1 and tm_dqzt=9 then 11 \n" + " else tm_dqzt \t\n" + " end)as tm_dqzt,\n"
                + "terminal_detection_status,terminal_detection_status_time||'',terminal_detection_result,terminal_detection_result_time||'',\n" + //6
                "order_push_date||'',\n" + "zc_time||'',\n" + "terminal_debugging_status,ts_time||'', \n" +//10
                "  dis.ps_time||'' ,\n" + "  qs_time||'',\n" + "  az_time||'',\n" + "  is_jdys,is_jdys_time||'',\n" + "  is_check_time||'' ,\n"
                + " dis.term_dispatch_id,dis.shipper,dis.shipper_phone,dis.recipitnt,dis.recipitnt_phone,dis.qs_name,dis.qs_phone,dis.qs_oa ,is_check,"
                + "      od.sq_time||'',wk.sqr_name,wk.sqr_phone,wk.approval_staff_oa,wk.approval_staff_name, wk.approval_staff_phone,wk.approval_state,wk.approval_remarks, "
                + "    li.shipments_time||'',li.id,li.consigner_name,li.consigner_phone,li.delivery_time||'',li.signer_oa,li.signer_name,li.signer_phone,wk.approval_finish_time||'',"
                + " info.link_order_no,zcsf,zcresult1,jdresult1,dis.pms_city_company_name,dis.pms_county_company_name,  "
                + " info.zoneprojectname,info.diclabel,info.projectname,info.projectcode,info.batchname,info.batchcode, "
                + "  info.zonetgnum,info.feedername,info.feederid,info.oss_type "
                + " from DMS_IOT_DEVICE_RESOURCE_INFO  as info \n"
                + " left join dms_termesn_dispatch as dis on  info.link_dispatch=dis.term_dispatch_id\n" + "left join iot_device                 as d\n"
                + "on " + "        d.dev_label   =info.term_esn\n" + "    and d.is_valid    =1 "
                + "    and d.connect_mode=1 " + "    and d.out_iot_fac =2 "
                + "   left join dms_dispatch_list as li on li.apply_for_batch_number=info.batch_number\n"
                + "   left join dms_work_order as od on od.work_order_id=li.work_order_id\n"
                + "   left join dms_requistion_work as wk on od.work_order_id=wk.WORK_ORDER_ID " + " where "
                + " info.term_id='" + termId + "'  and info.is_valid=1   order by wk.work_order_time DESC limit 0,1 ";
        System.out.println(sqlOrder);
        Object[] objects = commonInterface.selectListBySql(sqlOrder).get(0);
        List<HashMap> resultList = new ArrayList<>();
        int tm_dqzt = objects[0] == null ? 0 : Integer.parseInt(objects[0].toString());
        System.out.println("用户当前状态为" + tm_dqzt);
        String terminal_detection_status = objects[1] == null ? "-" : objects[1].toString();
        if (terminal_detection_status.equals("0")) {
            terminal_detection_status = "未检测";
        } else if (terminal_detection_status.equals("1")) {
            terminal_detection_status = "已检测";
        }
        //终端检测状态时间
        String terminal_detection_status_time = objects[2] == null ? "-" : objects[2].toString();
        //终端检测是结果 1合格 0 不合格
        String terminal_detection_result = objects[3] == null ? "-" : objects[3].toString();
        if (terminal_detection_result.equals("0")) {
            terminal_detection_result = "不合格";
        } else if (terminal_detection_result.equals("1")) {
            terminal_detection_result = "合格";
        }
        //终端检测结果时间
        String terminal_detection_result_time = objects[4] == null ? "-" : objects[4].toString();
        //订单esn绑定时间
        String order_push_date = objects[5] == null ? "-" : objects[5].toString();
        //注册时间
        String zc_time = objects[6] == null ? "-" : objects[6].toString();
        //终端调试状态 0未调试  1成功 2失败
        String terminal_debugging_status = objects[7] == null ? "-" : objects[7].toString();
        if (terminal_debugging_status.equals("1")) {
            terminal_debugging_status = "成功";
        } else if (terminal_debugging_status.equals("2")) {
            terminal_debugging_status = "失败";
        } else {
            terminal_debugging_status = "未知";
        }
        //推送时间
        String ts_time = objects[8] == null ? "-" : objects[8].toString();
        String ps_time = objects[9] == null ? "-" : objects[9].toString();
        String qs_time = objects[10] == null ? "-" : objects[10].toString();
        String az_time = objects[11] == null ? "-" : objects[11].toString();
        String is_jdys = objects[12] == null ? "-" : objects[12].toString();
        //建档时间
        String is_jdys_time = objects[13] == null ? "-" : objects[13].toString();
        //验收时间 也是试运行时间
        String is_check_time = objects[14] == null ? "-" : objects[14].toString();
        //配送单id
        String term_dispatch_id = objects[15] == null ? "-" : objects[15].toString();
        //配送发起人
        String shipper = objects[16] == null ? "-" : objects[16].toString();
        //配送发起人电话
        String shipper_phone = objects[17] == null ? "-" : objects[17].toString();
        //配送接收人
        String recipitnt = objects[18] == null ? "-" : objects[18].toString();
        //配送接收人电话
        String recipitnt_phone = objects[19] == null ? "-" : objects[19].toString();
        //签收人（用默认发货时候的接收人，而不是用的签收人）
        String qs_name = objects[20] == null ? "-" : objects[20].toString();
        //签收人电话
        String qs_phone = objects[21] == null ? "-" : objects[21].toString();
        //签收人oa
        String qs_oa = objects[22] == null ? "-" : objects[22].toString();
        String is_check = objects[23] == null ? "-" : objects[23].toString();
        if (is_check.equals("1")) {
            is_check = "验收成功";
        } else if (is_check.equals("0")) {
            is_check = "验收失败 ";
        }

        String sqTime = objects[24] == null ? "-" : objects[24].toString();
        String sqrName = objects[25] == null ? "-" : objects[25].toString();
        String sqrPhone = objects[26] == null ? "-" : objects[26].toString();
        String approvalStaffOa = objects[27] == null ? "-" : objects[27].toString();
        String approvalStaffName = objects[28] == null ? "-" : objects[28].toString();
        String approvalStaffPhone = objects[29] == null ? "-" : objects[29].toString();
        //审批状态 1通过 0驳回
        String approvalState = objects[30] == null ? "-" : objects[30].toString();
        if (approvalState.equals("0")) {
            approvalState = "驳回";
        } else if (approvalState.equals("1")) {
            approvalState = "同意";
        }
        //审批备注
        String approvalRemarks = objects[31] == null ? "-" : objects[31].toString();
        //发货时间
        String shipmentsTime = objects[32] == null ? "-" : objects[32].toString();
        //发货单ID
        String id = objects[33] == null ? "-" : objects[33].toString();
        //发货人姓名
        String consignerName = objects[34] == null ? "" : objects[34].toString();
        //发货人电话
        String consignerPhone = objects[35] == null ? "-" : objects[35].toString();
        //入库时间
        String deliveryTime = objects[36] == null ? "-" : objects[36].toString();
        //签收人Oa
        String singerOa = objects[37] == null ? "-" : objects[37].toString();
        //签收人姓名
        String signerName = objects[38] == null ? "-" : objects[38].toString();
        //签收人手机号
        String signerPhone = objects[39] == null ? "-" : objects[39].toString();
        //审批时间
        String approvalFinshTime = objects[40] == null ? "-" : objects[40].toString();
        String cgddh = objects[41] == null ? "-" : objects[41].toString();

        String zcsf = objects[42] == null ? "-" : objects[42].toString();
        String zcresult1 = objects[43] == null ? "-" : objects[43].toString();
        if (zcsf.equals("1")) {
            zcsf = "注册成功";
        } else if (zcsf.equals("2")) {
            zcsf = "注册失败，失败原因：" + zcresult1;
        }

        String jdresult1 = objects[44] == null ? "-" : objects[44].toString();
        if (is_jdys.equals("1")) {
            is_jdys = "建档成功";
        } else if (is_jdys.equals("0")) {
            is_jdys = "建档失败,失败原因：" + jdresult1;
        }
        String cityName = objects[45] == null ? "" : objects[45].toString();
        String countyName = objects[46] == null ? "" : objects[46].toString();
//        info.zoneprojectname,info.diclabel,info.projectname,info.projectcode,info.batchname,info.batchcode,
//                + info.zonetgnum,info.feedername,info.feederid
        /*
        * 我来保		: 	建设台区名称、建设性质、单项工程名称，项目编码、线路名称，线路id，
        工程管控项目	：	建设台区名称、建设性质、单项工程名称、项目编码、批次工程名称、批次工程编码、台区数量、
        * */
        String zoneprojectname = objects[47] == null ? "" : objects[47].toString();
        String diclabel = objects[48] == null ? "" : objects[48].toString();
        if (StringUtils.isNotBlank(diclabel)) {
            if (diclabel.equals("10")) {
                diclabel = "新建";
            } else {
                diclabel = "改造";
            }
        }
        String projectname = objects[49] == null ? "" : objects[49].toString();
        String projectcode = objects[50] == null ? "" : objects[50].toString();
        String batchname = objects[51] == null ? "" : objects[51].toString();
        String batchcode = objects[52] == null ? "" : objects[52].toString();
        String zonetgnum = objects[53] == null ? "" : objects[53].toString();
        String feedername = objects[54] == null ? "" : objects[54].toString();
        String feederid = objects[55] == null ? "" : objects[55].toString();
        String oss_type = objects[56] == null ? "" : objects[56].toString();
        String azMessg ="";
        if (StringUtils.isNotBlank(oss_type)&&oss_type.equals("1")) {
            azMessg = "数据来源：配网我来保， 建设台区名称 ： " + zoneprojectname + " ，线路名称：" + feedername + "，线路ID：" + feederid + "，单项工程名称 ：" + projectname + " ， 项目编码：" + projectcode + "  ，  台区建设性质 ： " + diclabel + " ";
        }else {
            azMessg = "数据来源:配电网工程全过程管控， 建设台区名称 ： " + zoneprojectname + " ，所属单位："+countyName+"，单项工程名称 ：" + projectname + " ， 项目编码：" + projectcode + "  ，  台区建设性质 ： " + diclabel + "  ";
        }
            /*获取发货申请开始的流程*/
        HashMap linkedHashMapFh1 = new HashMap();
        HashMap linkedHashMapFh2 = new HashMap();
        HashMap linkedHashMapFh3 = new HashMap();
        HashMap linkedHashMapFh4 = new HashMap();
        if (StringUtils.isNotBlank(sqTime) && !sqTime.equals("-")) {
            linkedHashMapFh1.put("key", "发货申请");
            linkedHashMapFh1.put("value", "申请时间 : " + sqTime + " ,          申请人: " + sqrName + " ,           手机号码: " + sqrPhone);
        } else {
            linkedHashMapFh1.put("key", "发货申请");
            linkedHashMapFh1.put("value", "未关联到相关流程");
        }

        if (StringUtils.isNotBlank(approvalFinshTime) && !approvalFinshTime.equals("-")) {
            linkedHashMapFh2.put("key", "发货审批");
            linkedHashMapFh2.put("value", "审批时间 : " + approvalFinshTime + " ,            审批人 : " + approvalStaffOa + approvalStaffName + " ,          手机号码 : " + approvalStaffPhone + "    ,           审批结果: " + approvalState + " ,           审批备注:  " + approvalRemarks);
        } else {
            linkedHashMapFh2.put("key", "发货审批");
            linkedHashMapFh2.put("value", "未关联到相关流程");
        }
        if (StringUtils.isNotBlank(shipmentsTime) && !shipmentsTime.equals("-")) {
            linkedHashMapFh3.put("key", "供应商发货");
            linkedHashMapFh3.put("value", "物流单号 :" + id + " ,            发货时间 : " + shipmentsTime + "    ,   发货人 : " + consignerName + " ,           手机号 : " + consignerPhone);
        } else {
            linkedHashMapFh3.put("key", "供应商发货");
            linkedHashMapFh3.put("value", "未关联到相关流程");
        }
        if (StringUtils.isNotBlank(deliveryTime) && !deliveryTime.equals("-")) {
            linkedHashMapFh4.put("key", "检测中心收货");
            linkedHashMapFh4.put("value", "收货时间 :" + deliveryTime + " ,         签收单号 :" + id + " ,          收货人 :" + singerOa + signerName + " ,        手机号码 : " + signerPhone);
        } else {
            linkedHashMapFh4.put("key", "检测中心收货");
            linkedHashMapFh4.put("value", "未检测到相关流程");
        }


        //
        HashMap linkedHashMap2 = new HashMap();
        HashMap linkedHashMap3 = new HashMap();
        HashMap linkedHashMap4 = new HashMap();
        HashMap linkedHashMap5 = new HashMap();
        HashMap linkedHashMap6 = new HashMap();
        HashMap linkedHashMap7 = new HashMap();
        HashMap linkedHashMap8 = new HashMap();
        HashMap linkedHashMap9 = new HashMap();
        HashMap linkedHashMap10 = new HashMap();
        HashMap linkedHashMap11 = new HashMap();
        HashMap linkedHashMap12 = new HashMap();
        HashMap linkedHashMap13 = new HashMap();

        if (tm_dqzt >= 1) {
            linkedHashMap2.put("key", "已检测");
            linkedHashMap2.put("value", "检测状态 : " + terminal_detection_status + " ，   检测状态时间 : " + terminal_detection_status_time + "  ，   检测结果 : " + terminal_detection_result + " ，   检测结果时间为 : " + terminal_detection_result_time);
            if (tm_dqzt >= 2) {
                linkedHashMap3.put("key", "已绑定");
                linkedHashMap3.put("value", " 绑定采购订单号 : " + cgddh + " ，   绑定时间 : " + order_push_date);
                if (tm_dqzt >= 3) {
                    linkedHashMap4.put("key", "已注册");
                    linkedHashMap4.put("value", "注册时间 : " + zc_time + "， 注册结果：" + zcsf);
                    System.out.println("来到了这里" + terminal_debugging_status);
                    if (tm_dqzt >= 4) {
                        linkedHashMap5.put("key", "已调试");
                        linkedHashMap5.put("value", "调试时间：" + ts_time + " ，   调试结果：" + terminal_debugging_status + " ");
                        if (tm_dqzt >= 5) {
                            linkedHashMap6.put("key", "已配送");
                            linkedHashMap6.put("value", " 配送单号：" + term_dispatch_id + "  ，  配送时间 ：" + ps_time + "  ，   配送发起人 ：" + shipper + " ， 联系方式 ：" + shipper_phone + " ，配送地址 : " + cityName + "-" + countyName);
                            if (tm_dqzt >= 6) {
                                linkedHashMap7.put("key", "已收货");
                                linkedHashMap7.put("value", "  配送接收人:" + recipitnt + "  ，      联系方式:" + recipitnt_phone + " ，   收货时间：" + qs_time + "  ");
                                if (tm_dqzt >= 7) {
                                    linkedHashMap8.put("key", "已安装");

                                    if (!az_time.equals("-")) {
                                        linkedHashMap8.put("value", "  安装时间：" + az_time + "   ，  安装结果:  安装成功 ，" + azMessg);

                                    } else {
                                        linkedHashMap8.put("value", "安装时间：" + az_time + "   ，        安装结果: -");
                                    }
                                    if (tm_dqzt >= 8) {
                                        linkedHashMap9.put("key", "已建档");
                                        linkedHashMap9.put("value", "建档时间：" + is_jdys_time + "  ，  建档结果 : " + is_jdys + "  ，建档参数：" + jdParam);
                                        if (tm_dqzt >= 9) {
                                            linkedHashMap10.put("key", "已验收");
                                            linkedHashMap10.put("value", "验收时间：" + is_check_time + " ，         验收结果 : " + is_check);
                                            if (is_check.equals("验收成功")) {
                                                linkedHashMap11.put("key", "试运行中");
                                                linkedHashMap11.put("value", "试运行一周，若无出现问题，则自动调整为正式投运状态 ");
                                                if (tm_dqzt == 12) {
                                                    linkedHashMap12.put("key", "试运行异常");
                                                    linkedHashMap12.put("value", "该终端试运行异常");
                                                }
                                                if (tm_dqzt == 13) {
                                                    linkedHashMap13.put("key", "正式投运");
                                                    linkedHashMap13.put("value", "试运行成功-已正式投运");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        } else {
            linkedHashMap2.put("key", "未检测");
            linkedHashMap2.put("value", "暂未检测");
        }


        resultList.add(linkedHashMapFh1);
        resultList.add(linkedHashMapFh2);
        resultList.add(linkedHashMapFh3);
        resultList.add(linkedHashMapFh4);
        resultList.add(linkedHashMap2);
        resultList.add(linkedHashMap3);
        resultList.add(linkedHashMap4);
        resultList.add(linkedHashMap5);
        resultList.add(linkedHashMap6);
        resultList.add(linkedHashMap7);
        resultList.add(linkedHashMap8);
        resultList.add(linkedHashMap9);
        resultList.add(linkedHashMap10);
        resultList.add(linkedHashMap11);
        resultList.add(linkedHashMap12);
        resultList.add(linkedHashMap13);

        List<Map> collect = resultList.stream().map(e -> {
            if (e.isEmpty()) {
                return null;
            }
            return e;
        }).filter(Objects::nonNull).collect(Collectors.toList());
//        if (collect.size() - 1 >= 1||tm_dqzt!=13) {
//            return new LayJson(200, "请求成功", collect, collect.size() - 1);
//        }
        return new LayJson(200, "请求成功", collect, collect.size());
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
        downloadFile(request, response, "终端全流程展示-" + fileName, filePath);
    }

}
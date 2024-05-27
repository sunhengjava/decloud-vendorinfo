package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSON;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.JDBCUtils;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.entity.TerminalStatusAccountingDO;
import com.nari.iot.vendorinfo.service.TerminalStatusAccountingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.text.DateFormat;
import java.util.*;

/**
 * @program: decloud-vendorinfo
 * @description: 终端状态统计页面
 * @author: sunheng
 * @create: 2023-10-25 10:45
 **/
@Service
@Slf4j
public class TerminalStatusAccountingServiceIpml implements TerminalStatusAccountingService {


    @Autowired
    CommonInterface commonInterface;


    @Override
    public LayJson searchListPO(String year, String terminalFactory, String batch, String product, String orgType, String orgId) {
        String substringPdName = "";
        if (StringUtils.isNotBlank(product)) {
            product = product.replace(" ", "");
            product = product.replace(",", "','");
            substringPdName = "'" + product + "'";
        }


        //先把对应的数据 统计好、由于需要 走过该状态的数据，最底层需要上面相加，需要进行再次匹配一下
        LinkedHashMap<String, TerminalStatusAccountingDO> termMap = new LinkedHashMap<>();

        StringBuffer sql = new StringBuffer();
        //查询配送、安装、建档数量
        sql.append("select ");
        //如何全省的就查询市级数据，否则查询省数据x
        if (orgType.equals("02")) {
            String sql1 = "select id,name from   osp.isc_baseorg  where func_type='03'  and id !='96b8401074dc974d01752a4d37d6013e' order by  unicode ASC ";
            List<Object[]> devList = commonInterface.selectListBySql(sql1);
            //声明个
            for(Object[] li:devList){
                TerminalStatusAccountingDO terminalStatusAccountingDO=new TerminalStatusAccountingDO();
                String id = li[0] != null ? li[0].toString() : "";
                String name = li[1] != null ? li[1].toString() : "";
                terminalStatusAccountingDO.setOrgName(name);
                terminalStatusAccountingDO.setOrgId(id);
                termMap.put(id,terminalStatusAccountingDO);
            }

            //       sql.append(" dis.city_company_name, dis.city_company_id, ");
                 sql.append( "   if(tr_de.city_org_nm is null,dis.city_company_name,tr_de.city_org_nm), if(tr_de.city_org_nm is null,dis.city_company_id,tr_de.city_base_org_id) ,");

        } else {
            if(orgType.equals("03")){
                String sql1 = "select id,name from   osp.isc_baseorg  where    parent_id='"+orgId+"' order by  unicode ASC";
                List<Object[]> devList = commonInterface.selectListBySql(sql1);
                //声明个
                for(Object[] li:devList){
                    TerminalStatusAccountingDO terminalStatusAccountingDO=new TerminalStatusAccountingDO();
                    String id = li[0] != null ? li[0].toString() : "";
                    String name = li[1] != null ? li[1].toString() : "";
                    terminalStatusAccountingDO.setOrgName(name);
                    terminalStatusAccountingDO.setOrgId(id);
                    termMap.put(id,terminalStatusAccountingDO);
                }
            }
            //        sql.append(" dis.county_company_name,dis.county_company_id, ");
           sql.append("    if(tr_de.county_org_nm is null,dis.county_company_name,tr_de.county_org_nm) , if(tr_de.county_org_nm is null,dis.county_company_id,tr_de.county_base_org_id), ");
        }

        sql.append("  re.tm_dqzt,d.is_check , count(1) as gs   " + "from " + "        DMS_IOT_DEVICE_RESOURCE_INFO as re " + "left join dms_termesn_dispatch       as dis on re.link_dispatch=dis.term_dispatch_id " + "left join DMS_TR_PROJECT_ORDER as project on project.cgddh=re.link_order_no   " + "left join iot_device as d on d.dev_label   =re.term_esn " + "    and d.is_valid    =1 " + "    and d.connect_mode=1 " + "    and d.out_iot_fac =2 " + "left join iot_product as p " + "on   d.pd_id=p.id  and p.out_iot_fac='2' and p.is_valid='1' and p.pd_mode='0' " + "left join DMS_IOT_HT_INFO as ht " + "on   ht.ht_no =project.ht_number " + "   left join dms_tr_device as de on de.id=d.rely_id " + "   left join (select * from d5000.dms_tr_account_info where run_st=20 and default_i is null) as tr_de  on de.device_asset_id=concat('PD_',tr_de.tr_pms_no)"

                + "where " + "        1              =1 ");
        String sqlDateFormat="'%Y'";
        if (StringUtils.isNotBlank(year)) {
            if(year.length()>4){
                sqlDateFormat="'%Y-MM'";
            }

            sql.append("   and DATE_FORMAT(dis.ps_time,"+sqlDateFormat+") ='" + year + "' " + //  配送时间
                    "     and DATE_FORMAT(re.az_time,"+sqlDateFormat+") ='" + year + "' " +    //安装
                    "        and  DATE_FORMAT(d.is_jdys_time,"+sqlDateFormat+") ='" + year + "' " +   //建档
                    " and DATE_FORMAT(d.is_check_time,"+sqlDateFormat+") ='" + year + "' " //验收时间
            );
        }
        if (StringUtils.isNotBlank(terminalFactory)) {
            sql.append(" and re.term_factory ='" + terminalFactory + "' "); //厂家
        }
        if (StringUtils.isNotBlank(product)) {//所属产品
            sql.append(" and p.pd_name in (" + substringPdName + ")");
        }
        if (StringUtils.isNotBlank(batch)) {
            sql.append("    and ht.batch_no_desc='" + batch + "' "); //22年第一批次
        }
        //市查询条件，查询市
        if (orgType.equals("03")) {
            sql.append("   and if(tr_de.city_base_org_id is not null, " + " if(tr_de.city_base_org_id='" + orgId + "', 1, 0), " + " if(dis.city_company_id='" + orgId + "', 1, 0) )");
        } else if (orgType.equals("04")) {
            //县查询条件查询县
            sql.append("   and if(tr_de.county_base_org_id is not null, " + " if(tr_de.county_base_org_id='" + orgId + "', 1, 0), " + " if(dis.county_company_id='" + orgId + "', 1, 0) ) and county_company_name is not null ");
        }
        //未验收逻辑 状态=9 ，d.is_check is  null or d.is_check =0
        sql.append("    and re.is_valid    =1 " + "    and re.tm_dqzt    in (6,7,8,9,10)  "
                + "     and if(re.tm_dqzt=6, if(dis.ps_time is not null, 1, 0), 1) " +
                "        and if(re.tm_dqzt=8, if(re.az_time is not null, 1, 0),1)  " +
                "        and if(re.tm_dqzt=9, if(d.is_jdys is not null, 1, 0), 1)  " +
                "       and if(d.is_check=1 , if(d.is_check_time is not null, 1, 0), 1) ");

        if (orgType.equals("02")) {
            sql.append(" group by if(tr_de.city_org_nm is null,dis.city_company_name,tr_de.city_org_nm), if(tr_de.city_org_nm is null,dis.city_company_id,tr_de.city_base_org_id)   ,d.is_check,  re.tm_dqzt   ");
        } else {
            sql.append(" group by if(tr_de.county_org_nm is null,dis.county_company_name,tr_de.county_org_nm) , if(tr_de.county_org_nm is null,dis.county_company_id,tr_de.county_base_org_id)  ,d.is_check , re.tm_dqzt    ");
        }
        log.info("searchListPO:" + sql.toString());
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());

        for (Object[] ob : devList) {
            String name = ob[0] != null ? ob[0].toString() : "";
            String id = ob[1] != null ? ob[1].toString() : "";
            TerminalStatusAccountingDO terminalStatusAccountingDO = termMap.get(id);
            if (terminalStatusAccountingDO != null) {
                int gs = 0;
                if (ob[4] != null) {


                    gs = Integer.parseInt(ob[4].toString());
                }
                if (ob[2].toString().equals("6") || (ob[2].toString().equals("7"))) {
                    int ps = gs + terminalStatusAccountingDO.getDelivery();
                    terminalStatusAccountingDO.setDelivery(ps);
                }
                if ((ob[2].toString().equals("8"))) {
                    int install = gs + terminalStatusAccountingDO.getInstaill();
                    terminalStatusAccountingDO.setInstaill(install);
                }
                if ((ob[2].toString().equals("9"))) {
                    //建档
                    if (ob[3] == null || ob[3].toString().equals("0")) {
                        int putonrecord = gs + terminalStatusAccountingDO.getPutOnRecord();
                        terminalStatusAccountingDO.setPutOnRecord(putonrecord);
                    } else {
                        //验收的
                        int check = gs + terminalStatusAccountingDO.getCheck();
                        terminalStatusAccountingDO.setCheck(check);
                    }
                }
                termMap.put(id, terminalStatusAccountingDO);
            } else {
                TerminalStatusAccountingDO terminalStatusAccountingDO2 = new TerminalStatusAccountingDO();
                terminalStatusAccountingDO2.setOrgName(name);
                terminalStatusAccountingDO2.setOrgId(id);
                if (ob[2].toString().equals("6") || (ob[2].toString().equals("7"))) {
                    int ps = Integer.parseInt(ob[4].toString());
                    terminalStatusAccountingDO2.setDelivery(ps);
                }
                if ((ob[2].toString().equals("8"))) {
                    int install = Integer.parseInt(ob[4].toString());
                    terminalStatusAccountingDO2.setInstaill(install);
                }
                if ((ob[2].toString().equals("9"))) {
                    //建档
                    if (ob[3] == null || ob[3].toString().equals("0")) {
                        int putonrecord = Integer.parseInt(ob[4].toString());
                        terminalStatusAccountingDO2.setPutOnRecord(putonrecord);
                    } else {
                        //验收的
                        int check = Integer.parseInt(ob[4].toString());
                        terminalStatusAccountingDO2.setCheck(check);
                    }
                }
                termMap.put(id, terminalStatusAccountingDO2);
            }

        }
        List<TerminalStatusAccountingDO> list = new ArrayList<>();
        TerminalStatusAccountingDO sumTerm = new TerminalStatusAccountingDO();
        sumTerm.setOrgId("200");
        sumTerm.setOrgName("总计");
        termMap.forEach((key, values) -> {
            values.setDelivery(values.getCheck() + values.getPutOnRecord() + values.getInstaill() + values.getDelivery());
            values.setInstaill(values.getCheck() + values.getPutOnRecord() + values.getInstaill());
            values.setPutOnRecord(values.getCheck() + values.getPutOnRecord());

            sumTerm.setDelivery(sumTerm.getDelivery() + values.getDelivery());
            sumTerm.setInstaill(sumTerm.getInstaill() + values.getInstaill());
            sumTerm.setCheck(sumTerm.getCheck() + values.getCheck());
            sumTerm.setPutOnRecord(sumTerm.getPutOnRecord() + values.getPutOnRecord());
            if (values.getOrgId().equals("")) {
                values.setOrgName("未匹配组织");
            } else {
                list.add(values);
            }
        });
        if (termMap.get("") != null) {
            list.add(termMap.get(""));
        }
        list.add(sumTerm);

        return new LayJson(200, "查询成功", list, list.size());
    }

    public static void main(String[] args) {
        System.out.println("sss".length());
    }
    @Override
    public LayJson searchBatch() {
        StringBuffer sql = new StringBuffer();
        sql.append("select  distinct batch_no_desc from  DMS_IOT_HT_INFO order by batch_no_desc desc");
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        log.info("searchBatch" + devList);
        List<String> list = new ArrayList<>();
        for (Object[] li : devList) {
            if (li[0] != null) {
                list.add(li[0].toString());
            }
        }
        return new LayJson(200, "查询成功", list, list.size());
    }

    @Override
    public LayJson searchProduct() {
        StringBuffer sql = new StringBuffer();
        sql.append("select pd_name  from iot_product where out_iot_fac='2' and is_valid='1' and  pd_mode='0'");
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        log.info("searchProduct" + devList);
        List<String> list = new ArrayList<>();
        for (Object[] li : devList) {
            if (li[0] != null) {
                list.add(li[0].toString());
            }
        }
        return new LayJson(200, "查询成功", list, list.size());
    }
}

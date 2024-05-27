package com.nari.iot.vendorinfo.service.impl;

import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.StockNtificationService;
import io.lettuce.core.ScriptOutputType;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: decloud-vendorinfo
 * @description:
 * @author: sunheng
 * @create: 2024-03-06 17:19
 **/
@Service("StockNtificationService")
@Slf4j
public class StockNtificationImpl implements StockNtificationService {

    @Autowired
    CommonInterface commonInterface;
    @Override
    public LayJson getQlcListPo(Map<String, Object> map) {
        String vendorNm = map.get("vendorNm") == null || map.get("vendorNm") == "" ? null : map.get("vendorNm").toString();
        String terminaltype = map.get("terminaltype") == null || map.get("literminaltypeneName") == "" ? null : map.get("terminaltype").toString();
        String tenderbatch = map.get("tenderbatch") == null || map.get("tenderbatch") == "" ? null : map.get("tenderbatch").toString();
        String bhdid = map.get("bhdid") == null || map.get("bhdid") == "" ? null : map.get("bhdid").toString();
        int pageNo = map.get("pageNo") == null || map.get("pageNo") == "" ? 1 : Integer.parseInt(map.get("pageNo").toString());
        int pageSize = map.get("pageSize") == null || map.get("pageSize") == "" ? 50 : Integer.parseInt(map.get("pageSize").toString());
        String sql="\n" +
                "select bhdId,vendorNm,stockData,deliveryTime,terminaltype,tenderbatch,promoter,promoteroa,fqrtel,fzrname,fzrtel,initiatetime||'', \n" +
                "notes,xystockdata,xydeliverytime||'',xyterminaltype,xytime||'',xyrname,xyroa,xyrtel,xytenderbatch " +
                "from stocklist_ask   \n" +

                "where  1=1 ";
        if(StringUtils.isNotBlank(vendorNm)){
            sql+=" and vendorNm like '%"+vendorNm+"%' ";
        }
        if(StringUtils.isNotBlank(terminaltype)){
            sql+=" and terminaltype='"+terminaltype+"' ";
        }
        if(StringUtils.isNotBlank(tenderbatch)){
            sql+=" and tenderbatch like '%"+tenderbatch+"%' ";
        }
        if(StringUtils.isNotBlank(bhdid)){
            sql+= " and bhdid='"+bhdid+"'";
        }
        String sqlCount="select  count(1) as gs from (  "+sql+")";

        sql += "   limit " + (pageNo - 1) * pageSize + "," + pageSize;
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        List<Object[]> devCount = commonInterface.selectListBySql(sqlCount.toString());
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        for (Object[] objs : devList) {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("bhdID", objs[0] == null ? "-" : objs[0].toString());
            hashMap.put("vendorNm", objs[1] == null ? "-" : objs[1].toString());
            hashMap.put("stockData", objs[2] == null ? "-" : objs[2].toString());
            hashMap.put("deliveryTime", objs[3] == null ? "-" : objs[3].toString());
            hashMap.put("terminalType", objs[4] == null ? "-" : objs[4].toString());
            hashMap.put("tenderBatch", objs[5] == null ? "-" : objs[5].toString());
            hashMap.put("promoter", objs[6] == null ? "-" : objs[6].toString());
            hashMap.put("promoterOA", objs[7] == null ? "-" : objs[7].toString());
            hashMap.put("fqrtel", objs[8] == null ? "-" : objs[8].toString());
            hashMap.put("fzrname", objs[9] == null ? "-" : objs[9].toString());
            hashMap.put("fzrtel", objs[10] == null ? "-" : objs[10].toString());
            hashMap.put("initiatetime", objs[11] == null ? "-" : objs[11].toString());
            hashMap.put("notes", objs[12] == null ? "-" : objs[12].toString());
            hashMap.put("xystockdata", objs[13] == null ? "-" : objs[13].toString());
            hashMap.put("xydeliverytime", objs[14] == null ? "-" : objs[14].toString());
            hashMap.put("xyterminaltype", objs[15] == null ? "-" : objs[15].toString());
            hashMap.put("xytime", objs[16] == null ? "-" : objs[16].toString());
            hashMap.put("xyrname", objs[17] == null ? "-" : objs[17].toString());
            hashMap.put("xyroa", objs[18] == null ? "-" : objs[18].toString());
            hashMap.put("xyrtel", objs[19] == null ? "-" : objs[19].toString());
            hashMap.put("xytenderbatch", objs[20] == null ? "-" : objs[20].toString());
            value.add(hashMap);
        }
        String s = devCount.get(0)[0] != null ? devCount.get(0)[0].toString() : "0";
        return new LayJson(200, "请求成功", value, Integer.parseInt(s));
    }

    @Override
    public LayJson getStockDetail(Map<String, Object> map) {

        String bhdid = map.get("bhdId") == null || map.get("bhdId") == "" ? null : map.get("bhdId").toString();
        int pageNo = map.get("pageNo") == null || map.get("pageNo") == "" ? 1 : Integer.parseInt(map.get("pageNo").toString());
        int pageSize = map.get("pageSize") == null || map.get("pageSize") == "" ? 50 : Integer.parseInt(map.get("pageSize").toString());
        String sql="select \n" +
                " promoter,term_id,term_esn,dev_type,send_time||'',term_factory,batch_number,bhdid,\n" +
                " terminal_detection_result,terminal_detection_result_time||'',terminal_detection_status,\n" +
                " terminal_detection_status_time||'',ts_time||'' \n" +
                " from stocklist_ask as ask\n" +
                "left join dms_work_order as workoder on workoder.work_order_id=ask.sqdid\n" +
                "left join   dms_dispatch_list as li on li.work_order_id=workoder.work_order_id \n" +
                "left join dms_iot_device_resource_info as info on info.batch_number=li.apply_for_batch_number\n" +
                "where  1=1   " ;
        if(StringUtils.isNotBlank(bhdid)){
            sql+= " and bhdid='"+bhdid+"'";
        }
        sql+= "order by term_id " ;
        String sqlCount="select  count(1) as gs from (  "+sql+")";
        sql += "   limit " + (pageNo - 1) * pageSize + "," + pageSize;
        List<Object[]> devList = commonInterface.selectListBySql(sql.toString());
        List<Object[]> devCount = commonInterface.selectListBySql(sqlCount.toString());
        List<Map<String, Object>> value = new ArrayList<Map<String, Object>>();
        for (Object[] objs : devList) {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("promoter", objs[0] == null ? "-" : objs[0].toString());
            hashMap.put("termId", objs[1] == null ? "-" : objs[1].toString());
            hashMap.put("termEsn", objs[2] == null ? "-" : objs[2].toString());
            hashMap.put("devType", objs[3] == null ? "-" : objs[3].toString());
            hashMap.put("sendTime", objs[4] == null ? "-" : objs[4].toString());
            hashMap.put("termFactory", objs[5] == null ? "-" : objs[5].toString());
            hashMap.put("batchNumber", objs[6] == null ? "-" : objs[6].toString());
            hashMap.put("bhdId", objs[7] == null ? "-" : objs[7].toString());
            hashMap.put("terminalDetectionSesult", objs[8] == null ? "-" : objs[8].toString());
            hashMap.put("terminalDetectionSesultTime", objs[9] == null ? "-" : objs[9].toString());
            hashMap.put("terminalDetectionStatus", objs[10] == null ? "-" : objs[10].toString());
            hashMap.put("terminalDetectionStatusTime", objs[11] == null ? "-" : objs[11].toString());
            hashMap.put("tsTime", objs[12] == null ? "-" : objs[12].toString());
            value.add(hashMap);
        }
        String s = devCount.get(0)[0] != null ? devCount.get(0)[0].toString() : "0";
        return new LayJson(200, "请求成功", value, Integer.parseInt(s));
    }


    @Override
    public Map<String, Object> exportAllExcel(HttpServletRequest request, HttpServletResponse response) {

        String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss") + ".xls";
        String targetFilePath = request.getSession().getServletContext().getRealPath("download") + File.separator + fileName;
        String sheet = "备货通知管理";
        String vendorNm = request.getParameter("vendorNm") == null || request.getParameter("vendorNm") == "" ? null : request.getParameter("vendorNm").toString();
        String terminaltype = request.getParameter("terminaltype") == null || request.getParameter("terminaltype") == "" ? null : request.getParameter("terminaltype").toString();
        String tenderbatch = request.getParameter("tenderbatch") == null || request.getParameter("tenderbatch") == "" ? null : request.getParameter("tenderbatch").toString();
        String bhdid = request.getParameter("bhdid") == null || request.getParameter("bhdid") == "" ? null : request.getParameter("bhdid").toString();

        String sql="\n" +
                "select bhdId,vendorNm,stockData,deliveryTime||'',terminaltype,tenderbatch,promoter,promoteroa,fqrtel,fzrname,fzrtel,initiatetime||'', \n" +
                "notes,xystockdata,xydeliverytime||'',xyterminaltype,xytime||'',xyrname,xyroa,xyrtel,xytenderbatch " +
                "from stocklist_ask\n" +
                "where  1=1 ";
        if(StringUtils.isNotBlank(vendorNm)){
            sql+=" and vendorNm like '%"+vendorNm+"%' ";
        }
        if(StringUtils.isNotBlank(terminaltype)){
            sql+=" and terminaltype='"+terminaltype+"' ";
        }

        if(StringUtils.isNotBlank(tenderbatch)){
            sql+=" and tenderbatch like '%"+tenderbatch+"%' ";
        }
        if(StringUtils.isNotBlank(bhdid)){
            sql+= " and bhdid='"+bhdid+"'";
        }
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        log.info("打印了："+sql);

        String titles[] = new String[]{"序号", "备货单ID", "终端供应商名称", "要求备货终端数量", "要求送达时间", "终端类型", "招标批次", "发起人名称", //7
                "发起人OA", "发起人电话", "发送负责人名称", "发送负责人手机号", "发起时间", "备注", "响应备货数量", "响应-要求送达时间"    //8
                , "响应-终端类型", "响应-响应时间", "响应人名称", "响应人OA", "响应人电话", "响应招标批次" }; //6
        List<Object[]> newlist = new ArrayList<Object[]>();
        int i = 0;
        for (Object[] temObj : devList) {
            i++;
            Object[] newObj = new Object[22];
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
            newObj[13] = temObj[12] == null || temObj[12].equals("") ? "-" : temObj[12];
            newObj[14] = temObj[13] == null || temObj[13].equals("") ? "-" : temObj[13];
            newObj[15] = temObj[14] == null || temObj[14].equals("") ? "-" : temObj[14];
            newObj[16] = temObj[15] == null || temObj[15].equals("") ? "-" : temObj[15];
            newObj[17] = temObj[16] == null || temObj[16].equals("") ? "-" : temObj[16];
            newObj[18] = temObj[17] == null || temObj[17].equals("") ? "-" : temObj[17];
            newObj[19] = temObj[18] == null || temObj[18].equals("") ? "-" : temObj[18];
            newObj[20] = temObj[19] == null || temObj[19].equals("") ? "-" : temObj[19];
            newObj[21] = temObj[20] == null || temObj[20].equals("") ? "-" : temObj[20];
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
    public Map<String, Object> exportStockDetail(HttpServletRequest request, HttpServletResponse response) {
        String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss") + ".xls";
        String targetFilePath = request.getSession().getServletContext().getRealPath("download") + File.separator + fileName;
        String sheet = "备货通知管理";
        String bhdId = request.getParameter("bhdId") == null || request.getParameter("bhdId") == "" ? null : request.getParameter("bhdId").toString();
        String sql="select  " +
                " promoter,term_id,term_esn,dev_type,send_time||'',term_factory,batch_number,bhdid, " +
                " terminal_detection_result,terminal_detection_result_time||'',terminal_detection_status, " +
                " terminal_detection_status_time||'',ts_time||'' " +
                " from stocklist_ask as ask " +
                "left join dms_work_order as workoder on workoder.work_order_id=ask.sqdid " +
                "left join   dms_dispatch_list as li on li.work_order_id=workoder.work_order_id  " +
                "left join dms_iot_device_resource_info as info on info.batch_number=li.apply_for_batch_number " +
                "where  1=1   " ;

        if(StringUtils.isNotBlank(bhdId)){
            sql+= " and bhdid='"+bhdId+"'";
        }
        List<Object[]> devList = commonInterface.selectListBySql(sql);
        log.info("打印了："+sql);
        //序号、客户名称、终端ID、核心板ESN、设备型号、发货日期、终端厂家、发货申请批次号、备货通知ID、终端检测结果、终端检测结果时间、终端检测状态、终端检测状态时间、调试时间
        String titles[] = new String[]{"序号", "客户名称", "终端ID", "核心板ESN", "设备型号", "发货日期", "终端厂家", "发货申请批次号", //7
                "备货通知ID", "终端检测结果", "终端检测结果时间", "终端检测状态", "终端检测状态时间", "调试时间"};
        List<Object[]> newlist = new ArrayList<Object[]>();
        int i = 0;
        for (Object[] temObj : devList) {
            i++;
            Object[] newObj = new Object[14];
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
            newObj[13] = temObj[12] == null || temObj[12].equals("") ? "-" : temObj[12];
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
        downloadFile(request, response, "备货通知管理-" + fileName, filePath);
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

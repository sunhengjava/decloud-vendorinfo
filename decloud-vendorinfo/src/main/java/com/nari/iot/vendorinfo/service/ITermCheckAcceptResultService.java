package com.nari.iot.vendorinfo.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface ITermCheckAcceptResultService {
    /**
     * 获取终端验收结果表格数据
     * @param request
     * @return
     */
   /* List<Map<String, Object>> getList(HttpServletRequest request,Map<String, Object> map);*/

    /**
     * 获取单个设备每项验收结果
     * @param request
     * @return
     */
    List<List<Object>> getDetail(HttpServletRequest request);

    /**
     * 获取终端验收结果子设备表格数据
     * @param request
     * @return
     */
    List<Map<String, Object>> getDevList(HttpServletRequest request);

    /**
     * 导出表格接口
     * @param request
     * @param response
     * @return
     */
   /* public Map<String, Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response);
    *//**
     * 导出word接口
     * @param request
     * @param response
     * @return
     */
    public Map<String, Object> exportWordReport(HttpServletRequest request, HttpServletResponse response)throws Exception;
}

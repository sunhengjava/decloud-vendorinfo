package com.nari.iot.vendorinfo.controller;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nari.iot.vendorinfo.common.JDBCUtils;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.ITerminalProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *@description：2、终端资源库管理
 *@author：sunheng
 *@date：2022/11/23 9:07
 *@param：
 */
@RestController
@RequestMapping(value = "terminal")
public class TerminalProjectController {
    @Autowired(required = false)
    private ITerminalProjectService terminalProjectService;

    @Autowired
    JdbcTemplate jdbcTemplate = new JdbcTemplate(JDBCUtils.getDataSource());
    /**
     * 1
     * 查询厂家信息
     * @description：
     * @author：sunheng
     * @date：2022/11/21 15:22
     * @param：
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    // @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @PostMapping("/getListPO")
    public LayJson getListPO(HttpServletRequest request, @RequestBody Map<String, Object> map) {
        LayJson listPO = terminalProjectService.getListPO(request, map);
        return listPO;
    }

    /** 2
     * @description：导入excel 终端明细1
     * @author：sunheng
     * @date：2022/11/21 15:22
     * @param：
     */
    @RequestMapping("/exportImport")
    public Map<String, Object> exportImport(MultipartFile file, HttpServletResponse httpServletResponse) throws Exception {
        Map<String, Object> queryListInfo = terminalProjectService.exportImport(file, httpServletResponse);
        return queryListInfo;
    }

    /**
     * @description：导入excel 终端结果2
     * @author：sunheng、7
     * @date：2022/11/21 15:22
     * @param：
     */
    @RequestMapping("/exportDetectionResultImport")
    public Map<String, Object> exportDetectionResultImport(MultipartFile file, HttpServletResponse httpServletResponse) throws Exception {
        Map<String, Object> queryListInfo = terminalProjectService.exportDetectionResultImport(file, httpServletResponse);
        return queryListInfo;
    }

    /**
     * @description：检测线传入
     * @author：sunheng
     * @date：2022/11/21 15:22
     * @param：
     */
    @RequestMapping("upTerminalCheck")
    public LayJson upTerminalCheck( @RequestBody Map<String, Object> map)  {
        LayJson queryListInfo = terminalProjectService.upTerminalCheck(map);
        return queryListInfo;
    }

    /**
     * @description：导出详情数据
     * @author：sunheng
     * @date：2022/11/21 15:25
     * @param：
     */
    @RequestMapping("/exportAllExcelDetail")
    public Map<String, Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object>  queryListInfo = terminalProjectService.exportAllExcelDetail(request, response);
        return queryListInfo;
    }

    /**
     * @description：导出模板一
     * @author：sunheng
     * @date：2022/11/21 15:25
     * @param：
     */
    @RequestMapping("/exportTemplate")
    public Map<String, Object> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> queryListInfo = terminalProjectService.exportTemplate(request, response);
        return queryListInfo;
    }

    /**
     * @description：导出模板二
     * @author：sunheng
     * @date：2022/11/21 15:25
     * @param：
     */
    @RequestMapping("/exportTemplate2")
    public Map<String, Object> exportTemplate2(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> queryListInfo = terminalProjectService.exportTemplate2(request, response);
        return queryListInfo;
    }
        //终端注册建档
    @PostMapping("/getHttpDeviceBatchRegister")
    public Map getHttpDeviceBatchRegister(@RequestBody List<Map> list ) {
        Map queryListInfo = terminalProjectService.getHttpDeviceBatchRegister(list);
        return queryListInfo;
    }
}
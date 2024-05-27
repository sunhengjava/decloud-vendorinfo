package com.nari.iot.vendorinfo.service;

import com.nari.iot.vendorinfo.entity.LayJson;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface ITerminalProjectService {


    /**
     * 获取合同基础数据
     * @param request
     * @return
     */
    LayJson getListPO(HttpServletRequest request, Map<String, Object> map);
    LayJson upTerminalCheck( Map<String, Object> map);

    /**
     * excle导入终端

     * @return
     */
    Map<String,Object> exportImport(MultipartFile file, HttpServletResponse httpServletResponse) ;


    /*
    * excel检测结果导入
    * */

    Map<String,Object> exportDetectionResultImport(MultipartFile file, HttpServletResponse httpServletResponse);


    /**
     * excle导出终端
     * @param request
     * @return
     */
    Map<String,Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response);

    /**
     * excle导出模板
     * @param request
     * @return
     */
    Map<String,Object> exportTemplate(HttpServletRequest request, HttpServletResponse response);
    Map<String,Object> exportTemplate2(HttpServletRequest request, HttpServletResponse response);


    Map getHttpDeviceBatchRegister(List<Map> list);
}

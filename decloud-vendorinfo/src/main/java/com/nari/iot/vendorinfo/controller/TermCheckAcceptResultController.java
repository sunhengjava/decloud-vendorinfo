package com.nari.iot.vendorinfo.controller;

import com.nari.iot.vendorinfo.service.ITermCheckAcceptResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "result/accept")
public class TermCheckAcceptResultController {
    @Autowired
    @Qualifier(value="TermCheckAcceptResultService")
    private ITermCheckAcceptResultService termCheckAcceptResultService;

  /*  @RequestMapping("/list")
    public List<Map<String, Object>> getResultList(HttpServletRequest request, @RequestBody Map<String, Object> map){
        List<Map<String, Object>> queryListInfo = termCheckAcceptResultService.getList(request, map);
        return queryListInfo;
    }*/
    @RequestMapping("/detail")
    public List<List<Object>> getDetail(HttpServletRequest request){
        List<List<Object>> queryListInfo = termCheckAcceptResultService.getDetail(request);
        return queryListInfo;
    }
    @RequestMapping("/detailList")
    public List<Map<String, Object>> getDetailList(HttpServletRequest request){
        List<Map<String, Object>> queryListInfo = termCheckAcceptResultService.getDevList(request);
        return queryListInfo;
    }
  /*  @RequestMapping("/exportAllExcelDetail")
    public Map<String, Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response){
        Map<String, Object> queryListInfo = termCheckAcceptResultService.exportAllExcelDetail(request,response);
        return queryListInfo;
    }*/
    @RequestMapping("/exportWordReport")
    public Map<String, Object> exportWordReport(HttpServletRequest request, HttpServletResponse response) throws Exception{
        Map<String, Object> queryListInfo = termCheckAcceptResultService.exportWordReport(request,response);
        return queryListInfo;
    }
}

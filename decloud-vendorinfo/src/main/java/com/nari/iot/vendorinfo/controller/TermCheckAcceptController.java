package com.nari.iot.vendorinfo.controller;

import com.nari.iot.vendorinfo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping(value = "check/accept")
public class TermCheckAcceptController {
    @Autowired
    @Qualifier(value="DeviceOnlineCheckAcceptService")
    private IDeviceOnlineCheckAcceptService deviceOnlineCheckAcceptService;
    @Autowired
    @Qualifier(value="ReportMsgCheckAcceptService")
    private IReportMsgCheckAcceptService reportMsgCheckAcceptService;
    @Autowired
    @Qualifier(value = "DeviceCountCheckAcceptService")
    private IDeviceCountCheckAcceptService deviceCountCheckAcceptService;
    @Autowired
    @Qualifier(value = "ParamSetCheckAcceptService")
    private IParamSetCheckAcceptService paramSetCheckAcceptService;
    @Autowired
    @Qualifier(value = "DataMeasureCheckAcceptService")
    private IDataMeasureCheckAcceptService dataMeasureCheckAcceptService;
    @Autowired
    @Qualifier(value = "RemoteControlCheckAcceptService")
    private  IRemoteControlCheckAcceptService remoteControlCheckAcceptService;

    @Autowired
    @Qualifier(value = "TermCheckAcceptAllService")
    private ITermCheckAcceptAllService termCheckAcceptAllService;

    @RequestMapping("/devOnline")
    public Map<String, Object> checkOnline(HttpServletRequest request){
        Map<String, Object> queryListInfo = deviceOnlineCheckAcceptService.deviceOnlineCheck(request);
        return queryListInfo;
    }

    @RequestMapping("/reportMsg")
    public Map<String, Object> checkReportMsg(HttpServletRequest request){
        Map<String, Object> queryListInfo = reportMsgCheckAcceptService.reportMsgCheck(request);
        return queryListInfo;
    }
    @RequestMapping("/devCount")
    public Map<String, Object> checkDevCount(HttpServletRequest request){
        Map<String, Object> queryListInfo = deviceCountCheckAcceptService.deviceCountCheck(request);
        return queryListInfo;
    }

    @RequestMapping("/paramSet")
    public Map<String, Object> checkParamSet(HttpServletRequest request){
        Map<String, Object> queryListInfo = paramSetCheckAcceptService.paramSetCheck(request);
        return queryListInfo;
    }

    @RequestMapping("/dataMeasure")
    public Map<String, Object> checkDataMeasure(HttpServletRequest request){
        Map<String, Object> queryListInfo = dataMeasureCheckAcceptService.
                dataMeasureCheck(request);
        return queryListInfo;
    }

    @RequestMapping("/remoteControl")
    public Map<String, Object> checkRemoteControl(HttpServletRequest request){
        Map<String, Object> queryListInfo = remoteControlCheckAcceptService.remoteControlCheck(request);
        return queryListInfo;
    }

    @RequestMapping("/termAll")
    public Map<String, Object> checkTermAll(HttpServletRequest request){
        Map<String, Object> queryListInfo = termCheckAcceptAllService.termAllCheck(request);
        return queryListInfo;
    }
    @RequestMapping("/termAll2")
    public Map<String, Object> checkTermAll2(HttpServletRequest request){
        Map<String, Object> queryListInfo = termCheckAcceptAllService.termAllCheck2(request);
        return queryListInfo;
    }

    @RequestMapping("/feedback")
    public Map<String, Object> termFeedBack(HttpServletRequest request,@RequestBody Map<String, Object> map){
        Map<String, Object> queryListInfo = termCheckAcceptAllService.termFeedBack(request,map);
        return queryListInfo;
    }

    @RequestMapping("/test")
    public Map<String,Object> test(HttpServletRequest request){
        String param = request.getParameter("param");
        Map<String,Object> map = new HashMap<>();
        map.put("param",param);
        return map;
    }

}

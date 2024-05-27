package com.nari.iot.vendorinfo.service;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nari.iot.vendorinfo.entity.LayJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface QlclProjectService {
    LayJson getQlcListPo( @RequestBody Map<String, Object> map) ;

    Map<String, Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response);
    LayJson getQlcTpPo( @RequestBody Map<String, Object> map) ;

    LayJson getQlcZtPo( @RequestBody Map<String, Object> map) ;

}
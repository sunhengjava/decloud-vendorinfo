package com.nari.iot.vendorinfo.controller;


import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.BsicsQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 *@description：基础查询表数据
 *@author：sunheng
 *@date：2022/10/31 17:18
 */
@Controller
@RequestMapping("/BasicsQuery")
public class BasicsQueryController {
    @Autowired
    private BsicsQueryService bsicsQueryService;

    @Autowired
    CommonInterface commonInterface;



    /*
    * 10.1 查询iot_device表数据
    *  参数：dev_label ,dev_name,rely_id,rely_name
    * */

    @RequestMapping(value="/queryIotDevcie", method = RequestMethod.GET)
    @ResponseBody
    public LayJson queryIotDevcie(HttpServletRequest request) {
        try {
            return bsicsQueryService.queryIotDevcie(request);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
         return   new LayJson(500,"请求失败",null,0);
        }

    }
}

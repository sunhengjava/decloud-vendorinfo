package com.nari.iot.vendorinfo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.ITerminalProjectService;
import com.nari.iot.vendorinfo.service.QlclProjectService;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description：3 终端全流程管理页面
 * @author：
 * @date：2022/11/23 9:06
 * @param：
 */
@RestController
@RequestMapping(value = "qlcprojcet")
public class QlclProjectController {
    @Autowired(required = false)
    private QlclProjectService qlclProjectService;


    /**
     * @description：查询全流程首页
     * @author：sunheng
     * @date：2022/11/23 9:09
     * @param：
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    // @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @PostMapping("/getQlcListPo")
    public LayJson getListPO(@RequestBody Map<String, Object> map) {
        LayJson listPO = qlclProjectService.getQlcListPo(map);
        return listPO;
    }

    /**
     * @description：导出详情数据
     * @author：sunheng
     * @date：2022/11/21 15:25
     * @param：
     */
    @RequestMapping("/exportQlcExcelDetail")
    public Map<String, Object> exportAllExcelDetail(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> queryListInfo = qlclProjectService.exportAllExcelDetail(request, response);
        return queryListInfo;
    }

    /**
     * @description：现场安装图片
     * @author：sunheng
     * @date：2022/11/23 18:11
     * @param：
     */
    @PostMapping("/getQlcTpPo")
    public LayJson getQlcTpPo(@RequestBody Map<String, Object> map) {
        LayJson listPO = qlclProjectService.getQlcTpPo(map);
        return listPO;
    }

    /**
     * @description：状态
     * @author：sunheng
     * @date：2022/11/23 18:11
     * @param：esn表里并没有10 、11状态 需要查询的时候做处理
     */
    @PostMapping("/getQlcZtPo")
    public LayJson
    getQlcZtPo(@RequestBody Map<String, Object> map) {
        LayJson listPO = qlclProjectService.getQlcZtPo(map);
        return listPO;
    }

    public static void main(String[] args) {
        String re="[[1,2,3],[1,2,3]]";
        String str = "[{\"name\":\"张三\",\"age\":\"1\"},{\"name\":\"李四\",\"age\":\"4\"}]";
        Map a=new HashMap();
        a.put("s","b");
        List listt = JSON.parseArray(str,Map.class);
        List listt2 = JSON.parseArray(re,List.class);
        System.out.println(listt2+"_"+listt+"__"+a);
    }


}
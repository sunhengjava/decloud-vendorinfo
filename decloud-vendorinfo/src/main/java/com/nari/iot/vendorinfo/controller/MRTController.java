package com.nari.iot.vendorinfo.controller;

import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.IotTerminalService;
import com.nari.iot.vendorinfo.service.MRTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @program:
 * @description: 厂家申请终端  申请表
 * @author: sunheng
 * @create: 2024-01-13 11:51
 **/
@Controller
@RequestMapping("/mrt")
public class MRTController {


    //1 综配箱厂家主动申请融合终端
    @Autowired
    MRTService mrtService;

    @PostMapping(value = "upApplicationFrom")
    @ResponseBody
    public LayJson upApplicationFrom(@RequestBody Map<String, Object> map) {
        try {
            return mrtService.upApplicationFrom(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }
    //2 电科院下达二次转运通知
    @PostMapping(value = "secondaryTransports")
    @ResponseBody
    public LayJson secondaryTransports(@RequestBody Map<String, Object> map) {
        try {
            return mrtService.secondaryTransports(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }

    // 3 终端供应商发给  请求  综配箱厂家
    @PostMapping(value = "suppierRequest")
    @ResponseBody
    public LayJson suppierRequest(@RequestBody Map<String, Object> map) {
        try {
            return mrtService.suppierRequest(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }

    //4 综配箱厂家响应供应商的请求信息
    @PostMapping(value = "manufacturerResponse")
    @ResponseBody
    public LayJson manufacturerResponse(@RequestBody Map<String, Object> map) {
        try {
            return mrtService.manufacturerResponse(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }
    //5 终端供应商发货给综配箱厂家
    @PostMapping(value = "terminalhipment")
    @ResponseBody
    public LayJson terminalhipment(@RequestBody Map<String, Object> map) {
        try {
            return mrtService.terminalhipment(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }
    //6 综配箱厂家进行收货确认
    @PostMapping(value = "receiptConfirmation")
    @ResponseBody
    public LayJson receiptConfirmation(@RequestBody Map<String, Object> map) {
        try {
            return mrtService.receiptConfirmation(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }


    //7 页面查询接口
    @PostMapping(value = "secondaryTransportation")
    @ResponseBody
    public LayJson secondaryTransportation(@RequestBody Map<String, Object> map) {
        try {
            return mrtService.secondaryTransportation(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }

    @RequestMapping("/exportST")
    public Map<String, Object> exportST(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> queryListInfo = mrtService.exportST(request, response);
        return queryListInfo;
    }

    /**
     * @description：二次转运详情中 查询esn详情
     */
    @PostMapping(value = "getEsnDetail")
    @ResponseBody
    public LayJson getEsnDetail(@RequestBody Map<String, Object> map) {
        try {
            return mrtService.getEsnDetail(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }
    /**
     * @description：二次转运详情中 配送单
     */
    @PostMapping(value = "getpsd")
    @ResponseBody
    public LayJson getpsd(@RequestBody Map<String, Object> map) {
        try {
            return mrtService.getpsd(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }
    /**
     * @description：二次转运详情中 查询状态详情
     */
    @PostMapping(value = "getStatusDetail")
    @ResponseBody
    public LayJson getStatusDetail(@RequestBody Map<String, Object> map) {
        try {
            return mrtService.getStatusDetail(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }
    /**
     * @description：二次转运详情中 查询图片详情
     */
    @PostMapping(value = "getPicture")
    @ResponseBody
    public LayJson getPicture(@RequestBody Map<String, Object> map) {
        try {
            return mrtService.getPicture(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }
}


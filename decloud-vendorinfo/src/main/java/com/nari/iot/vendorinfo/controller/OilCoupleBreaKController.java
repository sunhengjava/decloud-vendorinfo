package com.nari.iot.vendorinfo.controller;

import com.alibaba.fastjson.JSONException;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.IdConvert;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.OilCoupleBreakService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Properties;


/*
 * 异常监测油温数据低于下限值 (如5度) 或高于上限值 (如120度) ， 阑值可人工设置 (当日)
 * 进行统计
 * */


@RestController
@RequestMapping(value = "oilcouplebreak")
public class OilCoupleBreaKController  {

    @Autowired
    OilCoupleBreakService oilCoupleBreakService;

    /**
     *@description： 油温异常检测统计页面
     *@author：sunheng
     *@date：2023/4/20 11:46
     *@param：
     */
    @PostMapping(value = "getAnomalyCountVO")
    @ResponseBody
    public LayJson getAnomalyCountVO(@RequestBody Map<String, Object> map) {
        try {
           return oilCoupleBreakService.getAnomalyCountVO(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }


    //详情的
    @PostMapping(value = "getAnomalyXqVO")
    @ResponseBody
    public LayJson getAnomalyDetailsVO(@RequestBody Map<String, Object> map) {
        try {
            return oilCoupleBreakService.getAnomalyDetailsVO(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }


    //详情excel 写的暂时不管
    @RequestMapping("/AnomalyXqVOExcel")
    public Map<String, Object> exportAllExcel(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> queryListInfo = oilCoupleBreakService.AnomalyXqVOExcel(request, response);
        return queryListInfo;
    }


    //这个是弹窗的
    @PostMapping(value = "getAnomalyDetailsVO")
    @ResponseBody
    public LayJson getAnomalyXqVO(@RequestBody Map<String, Object> map) {
        try {
            return oilCoupleBreakService.getAnomalyXqVO(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }



}

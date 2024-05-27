package com.nari.iot.vendorinfo.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Component
@FeignClient(value = "jsiot-alarmInterface", path = "/dyiot-alarmlnterface/pushAlarm")
public interface PushAlarmClient {
    /**
     * 推送供服告警
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/pushAlarm/")
    Map<String, Object> pushAlarm(List<Map<String, Object>> param);



}

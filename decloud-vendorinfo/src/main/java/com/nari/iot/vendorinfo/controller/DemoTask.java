package com.nari.iot.vendorinfo.controller;

import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DemoTask {

    @Scheduled(fixedDelay = 5000)   // 每隔5秒执行一次
    public void demoTask() {
        System.out.println("当前时间：" + new Date());
    }

}
package com.nari.iot.vendorinfo.scheduletask;

import com.nari.iot.vendorinfo.controller.OilCoupleBreakComsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 油温异常检测数据原
 * 消费kafka中油温异常数据（陶定元 发送的kafka）
 **/
@Component
@Slf4j
public class OilCoupleBreakRunner implements ApplicationRunner {

    @Autowired
    private OilCoupleBreakComsumer oilCoupleBreakComsumer;
    @Override
    public void run(ApplicationArguments args) {

        try {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 6, 30, TimeUnit.MINUTES, new SynchronousQueue<Runnable>());
                    threadPoolExecutor.execute(oilCoupleBreakComsumer);
        } catch (Exception e) {

        }


    }

}
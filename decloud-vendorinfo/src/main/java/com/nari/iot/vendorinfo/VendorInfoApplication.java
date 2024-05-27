package com.nari.iot.vendorinfo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages={"com.nari.iot","com.nari.cas.sso"})
@EnableEurekaClient
@EnableFeignClients
@EnableApolloConfig
@EnableCaching
@EnableScheduling
public class VendorInfoApplication {

    public static void main(String[] args) {
        SpringApplication.run(VendorInfoApplication.class, args);

        System.out.println("======>>>> ApolloApplication started...");
        /**
         * 	从Apollo配置中心公共namespace（TEST1.CommonConf）中获取 Eureka节点配置：
         *
         */
        // 客户端获取非 "application" Namespace配置
        Config commonConf = ConfigService.getConfig("TEST1.CommonConf");
        String eurekaUrl = commonConf.getProperty("eureka.client.serviceUrl.defaultZone", null);
        if (eurekaUrl == null) {
            //从Apollo公共Namespace读取配置失败会自动读取项目下application中的eureka配置
            System.err.println("======>>>> From Apollo TEST1.CommonConf namespace get eurekaUrl fail...  "
                    + "Now read the Eureka address under the current project...");
        }
        System.out.println("======>>>> From Apollo TEST1.CommonConf namespace get eurekaUrl success...  eurekaUrl: "
                + eurekaUrl);

        /**
         * 	添加Apollo监听配置变化事件日志：
         *
         */
        // 客户端获取 "application" Namespace配置
        Config config = ConfigService.getAppConfig();
        // 监听配置变化事件
        config.addChangeListener(changeEvent -> {
            System.out.println("======>>>> Changes for namespace " + changeEvent.getNamespace());
            for (String key : changeEvent.changedKeys()) {
                ConfigChange change = changeEvent.getChange(key);
                System.out.println(String.format("======>>>> Found change - key: %s, oldValue: %s, newValue: %s, changeType: %s",
                        change.getPropertyName(), change.getOldValue(), change.getNewValue(), change.getChangeType()));
            }
        });
    }

}

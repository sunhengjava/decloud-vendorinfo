package com.nari.iot.vendorinfo.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.netflix.discovery.EurekaClientConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/*** 从Apollo配置中心公共namespace（TEST1.CommonConf）中获取 Eureka节点配置： **/
//@Configuration
public class ApolloCommonConf {
    private static Config config = ConfigService.getConfig("TEST1.CommonConf");

    @Bean
    @ConditionalOnMissingBean(value = {EurekaClientConfig.class}, search = SearchStrategy.CURRENT)
    public EurekaClientConfig eurekaClientConfigBean() {
        EurekaClientConfigBean eurekaClientConfigBean = new EurekaClientConfigBean();
        //注册中心集群配置节点配置
        String eurekaUrl = config.getProperty("eureka.client.serviceUrl.defaultZone", null);
        if (eurekaUrl == null) {
            //从Apollo读取配置失败会自动读取项目下application中的eureka配置
            // 方便本地调试用
            System.err.println("======>>>> From Apollo TEST1.CommonConf namespace get eurekaUrl fail... " + "Now read the Eureka address under the current project...");
            return eurekaClientConfigBean;
        }
        Map<String, String> serviceUrls = new HashMap<>();
        serviceUrls.put("defaultZone", eurekaUrl);
        eurekaClientConfigBean.setServiceUrl(serviceUrls);
        System.out.println("======>>>> From Apollo TEST1.CommonConf namespace get eurekaUrl success... eurekaUrl:" + eurekaUrl);
        return eurekaClientConfigBean;
    }
}

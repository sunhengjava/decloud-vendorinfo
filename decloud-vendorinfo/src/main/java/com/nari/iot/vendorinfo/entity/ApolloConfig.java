package com.nari.iot.vendorinfo.entity;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class ApolloConfig {


    /**
     * 商用库restTemplate调用地址
     */
    @Value("${dbAccess.url}")
    private String dbAccess_url;

    /**
     * 商用库数据库
     */
    @Value("${dbAccess.dataSourceName}")
    private String dataSourceName;
    /**
     * 是否使用feign方式调用微服务接口
     */
    @Value("${call_type_is_feign}")
    private String call_type_is_feign;

    @Value("${device_mode_name}")
    private String device_mode_name;
    @Value("${reload_pd_id}")
    private String reload_pd_id;

}

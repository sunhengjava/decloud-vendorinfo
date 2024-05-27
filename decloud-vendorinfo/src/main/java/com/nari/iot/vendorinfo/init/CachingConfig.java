package com.nari.iot.vendorinfo.init;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class CachingConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache("sampleCache")));
        return cacheManager;
    }

//    @Bean
//    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
//        return new RedisCacheManager(
//                RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
//                this.getRedisCacheConfigurationWithTtl( 60),// 默认策略，未配置的 key 会使用这个
//                this.getRedisCacheConfigurationMap() // 指定 key 策略
//        );
//    }
//
//    private Map<String, RedisCacheConfiguration> getRedisCacheConfigurationMap() {
//        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
//        //SsoCache和BasicDataCache进行过期时间配置
//        redisCacheConfigurationMap.put("sampleCache", this.getRedisCacheConfigurationWithTtl(60));//自定义设置缓存时间
//
//        return redisCacheConfigurationMap;
//    }
//
//    private RedisCacheConfiguration getRedisCacheConfigurationWithTtl(Integer seconds) {
//        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
//        ObjectMapper om = new ObjectMapper();
//        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        jackson2JsonRedisSerializer.setObjectMapper(om);
//        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
//        redisCacheConfiguration = redisCacheConfiguration.serializeValuesWith(
//                RedisSerializationContext
//                        .SerializationPair
//                        .fromSerializer(jackson2JsonRedisSerializer)
//        ).entryTtl(Duration.ofSeconds(seconds));
//
//        return redisCacheConfiguration;
//    }
}
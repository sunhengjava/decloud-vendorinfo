package com.nari.iot.vendorinfo.common;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Cache {
    /**
     * 键值对集合
     */
    private static final Map<String, Entity> MAP = new HashMap<>();
    /**
     * 定时器线程池，用于清除过期缓存
     */
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    /**
     * 添加缓存
     *
     * @param key  键
     * @param data 值
     */
    public static synchronized void put(String key, Object data) {
        Cache.put(key, data, 0);
    }

    /**
     * 添加缓存
     *
     * @param key    键
     * @param data   值
     * @param expire 过期时间，单位：毫秒， 0表示无限长
     */
    public static synchronized void put(String key, Object data, long expire) {
        //清除原键值对
        Cache.remove(key);
        //设置过期时间
        if (expire > 0) {
            Future future = EXECUTOR.schedule(new Runnable() {
                @Override
                public void run() {
                    //过期后清除该键值对
                    synchronized (Cache.class) {
                        MAP.remove(key);
                    }
                }
            }, expire, TimeUnit.MILLISECONDS);
            MAP.put(key, new Entity(data, future));
        } else {
            //不设置过期时间
            MAP.put(key, new Entity(data, null));
        }
    }

    /**
     * 读取缓存
     *
     * @param key 键
     * @return
     */
    public static synchronized <T> T get(String key) {
        Entity entity = MAP.get(key);
        return entity == null ? null : (T) entity.value;
    }

    /**
     * 清除缓存
     *
     * @param key 键
     * @return
     */
    public static synchronized <T> T remove(String key) {
        //清除原缓存数据
        Entity entity = MAP.remove(key);
        if (entity == null) {
            return null;
        }
        //清除原键值对定时器
        if (entity.future != null) {
            entity.future.cancel(true);
        }
        return (T) entity.value;
    }

    /**
     * 查询当前缓存的键值对数量
     *
     * @return
     */
    public static synchronized int size() {
        return MAP.size();
    }

    /**
     * 缓存实体类
     */
    private static class Entity {
        /**
         * 键值对的value
         */
        private Object value;
        /**
         * 定时器Future
         */
        private Future future;

        Entity(Object value, Future future) {
            this.value = value;
            this.future = future;
        }
    }
}

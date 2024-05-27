package com.nari.iot.vendorinfo.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Component
public interface IDeviceInfo {

    /**
     * 查询多条
     *
     * @param sql
     * @return
     * @throws Exception
     */
    JSONArray selectList(String sql) throws Exception;

    /**
     * 查询单条
     *
     * @param sql
     * @return
     * @throws Exception
     */
    JSONObject selectOne(String sql) throws Exception;

    /**
     * 插入
     *
     * @param sql
     * @return
     * @throws Exception
     */
    Boolean insert(String sql) throws Exception;

    /**
     * 更新
     *
     * @param sql
     * @return
     * @throws Exception
     */
    Boolean update(String sql) throws Exception;

    /**
     * 删除
     *
     * @param sql
     * @return
     * @throws Exception
     */
    Boolean delete(String sql) throws Exception;
}

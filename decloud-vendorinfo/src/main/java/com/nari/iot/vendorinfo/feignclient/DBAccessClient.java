package com.nari.iot.vendorinfo.feignclient;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(value = "dbserver", path = "/dbserver")
public interface DBAccessClient {
    /**
     * 查找多条结果
     *
     * @param sql:查找的sql
     * @param dataSourceName:配置的数据源名称
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/selectList", method = {RequestMethod.POST})
    JSONArray selectList(@RequestParam("sql") String sql, @RequestParam("dataSourceName") String dataSourceName) throws Exception;

    /**
     * 删除一条数据
     *
     * @param sql
     * @param dataSourceName
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/delete", method = {RequestMethod.POST})
    boolean delete(@RequestParam("sql") String sql, @RequestParam("dataSourceName") String dataSourceName) throws Exception;

    /**
     * 修改一条数据
     *
     * @param sql
     * @param dataSourceName
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/update", method = {RequestMethod.POST})
    boolean update(@RequestParam("sql") String sql, @RequestParam("dataSourceName") String dataSourceName) throws Exception;

    /**
     * 插入一条数据
     * @param sql
     * @param dataSourceName
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/insert", method = {RequestMethod.POST})
    boolean insert(@RequestParam("sql") String sql, @RequestParam("dataSourceName") String dataSourceName) throws Exception;

    /**
     * 批量插入或修改
     * @param param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/batchUpdate", method = {RequestMethod.POST})
    long batchUpdate(@RequestParam("sql") String sql, @RequestParam("dataSourceName") String dataSourceName, @RequestParam("param") JSONObject param) throws Exception;
}

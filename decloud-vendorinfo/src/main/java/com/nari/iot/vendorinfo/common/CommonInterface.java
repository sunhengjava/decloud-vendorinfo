package com.nari.iot.vendorinfo.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.feignclient.DBAccessClient;
import com.nari.iot.vendorinfo.feignclient.PushAlarmClient;
import com.nari.iot.vendorinfo.feignclient.RtdbClient;
import com.nari.iot.vendorinfo.init.InitIocBean;
import com.nari.iot.vendorinfo.entity.ApolloConfig;
import com.sgcc.uap.rtdb.criteria.RtdbEnv;
import com.sgcc.uap.rtdb.criteria.TableGetCriteria;
import com.sgcc.uap.rtdb.domain.GraphRealReq;
import com.sgcc.uap.rtdb.domain.GraphRealRsp;
import com.sgcc.uap.rtdb.domain.GraphSimpleReq;
import com.sgcc.uap.rtdb.domain.GraphSimpleRsp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j

public class CommonInterface {
    @Autowired
    private ApolloConfig apolloConfig;


    @Autowired
    private DBAccessClient dbAccessClient;

    @Autowired
    private RtdbClient rtdbClient;

    @Autowired
    private PushAlarmClient pushAlarmClient;

    @Autowired
    private static RestTemplate restTemplate = new RestTemplate();


    /**
     * sql查询
     *
     * @param sql
     * @return
     */
    public List<Object[]> selectListBySql(String sql) {
        JSONArray listJA = dbAccess_selectList(sql);
        return CommonUtil.toListBuJsonArr(listJA,sql);
    }
    /**
     * dbAccess_selectList
     *
     * @param sql
     * @return
     */
    public JSONArray dbAccess_selectList(String sql) {
        JSONArray result = new JSONArray();
        try {
            if (Boolean.parseBoolean(apolloConfig.getCall_type_is_feign())) {
                result = dbAccessClient.selectList(sql, apolloConfig.getDataSourceName());
            } else {
                String restUrl = apolloConfig.getDbAccess_url() + "/selectList?sql={sql}&dataSourceName={dataSourceName}";
                result = restTemplate.postForObject(restUrl, null, JSONArray.class, sql, apolloConfig.getDataSourceName());
                /*RestTemplate restTemplate = getRestTemplate();
                if (restTemplate != null) {
                    result = getRestTemplate().postForObject(restUrl, null, JSONArray.class, sql, apolloConfig.getDataSourceName());
                }*/
//                result = getRestTemplate().postForObject(restUrl, null, JSONArray.class, sql, apolloConfig.getDataSourceName());
                log.info("dbAccess_selectList_url:" + restUrl);
            }

        } catch (Exception e) {
            log.error("dbAccess_selectList-exception,sql：" + sql, e);
        }

        return result;
    }

    /**
     * dbAccess_delete
     *
     * @param sql
     * @return
     */
    public boolean dbAccess_delete(String sql) {
        boolean result = false;
        try {
            if (Boolean.parseBoolean(apolloConfig.getCall_type_is_feign())) {
                result = dbAccessClient.delete(sql, apolloConfig.getDataSourceName());
            } else {
                String restUrl = apolloConfig.getDbAccess_url() + "/delete?sql={sql}&dataSourceName={dataSourceName}";
                Object resultObject = restTemplate.postForObject(restUrl, null, Boolean.class, sql, apolloConfig.getDataSourceName());
                if (resultObject != null) {
                    result = Boolean.parseBoolean(resultObject.toString());
                }
                /*RestTemplate restTemplate = getRestTemplate();
                if (restTemplate != null) {
                    result = getRestTemplate().postForObject(restUrl, null, Boolean.class, sql, apolloConfig.getDataSourceName());
                }*/
            }
        } catch (Exception e) {
            log.error("dbAccess_delete-Exception,sql：" + sql, e);
        }
        return result;
    }

    /**
     * dbAccess_insert
     *
     * @param sql
     * @return
     */
    public boolean dbAccess_insert(String sql) {
        boolean result = false;
        try {
            if (Boolean.parseBoolean(apolloConfig.getCall_type_is_feign())) {
                result = dbAccessClient.insert(sql, apolloConfig.getDataSourceName());
            } else {
                String restUrl = apolloConfig.getDbAccess_url() + "/insert?sql={sql}&dataSourceName={dataSourceName}";
                Object resultObject = restTemplate.postForObject(restUrl, null, Boolean.class, sql, apolloConfig.getDataSourceName());
                if (resultObject != null) {
                    result = Boolean.parseBoolean(resultObject.toString());
                }
                /*RestTemplate restTemplate = getRestTemplate();
                if (restTemplate != null) {
                    result = getRestTemplate().postForObject(restUrl, null, Boolean.class, sql, apolloConfig.getDataSourceName());
                }*/
            }
        } catch (Exception e) {
            log.error("dbAccess_insert-Exception,sql：" + sql, e);
        }

        return result;
    }

    /**
     * dbAccess_update
     *
     * @param sql
     * @return
     */
    public boolean dbAccess_update(String sql){
        boolean result = false;
        try{
            if(Boolean.parseBoolean(apolloConfig.getCall_type_is_feign())){
                result = dbAccessClient.update(sql,apolloConfig.getDataSourceName());
            }else{
                String restUrl = apolloConfig.getDbAccess_url()+"/update?sql={sql}&dataSourceName={dataSourceName}";
                Object resultObject = restTemplate.postForObject(restUrl, null, Boolean.class, sql, apolloConfig.getDataSourceName());
                if (resultObject != null) {
                    result=Boolean.parseBoolean(resultObject.toString());
                }
            }
        }catch (Exception e){
            log.error("dbAccess_update-Exception,sql："+sql,e);
        }

        return result;
    }



    /**
     * 批量修改或插入
     * dbAccess_batchUpdate
     *
     * @param sql
     * @param params
     * @return
     */
    public long dbAccess_batchUpdate(String sql, @RequestBody List<Map<Integer, Object>> params) {
        JSONObject param = new JSONObject();
        param.put("sql", sql);
        param.put("dataSourceName", apolloConfig.getDataSourceName());
        param.put("params", params);
        long result = 0;
        try {
            if (Boolean.parseBoolean(apolloConfig.getCall_type_is_feign())) {
                result = dbAccessClient.batchUpdate(sql, apolloConfig.getDataSourceName(), param);
            } else {
                String restUrl = apolloConfig.getDbAccess_url() + "/batchUpdate";
                Object resultObject = restTemplate.postForObject(restUrl, param, Long.class);
                if (resultObject != null) {
                    result = Long.parseLong(resultObject.toString());
                }
                /*RestTemplate restTemplate = getRestTemplate();
                if (restTemplate != null) {
                    result = getRestTemplate().postForObject(restUrl, param, Long.class);
                }*/
            }
        } catch (Exception e) {
            log.error("dbAccess_batchUpdate-Exception,sql：" + sql, e);
        }

        return result;
    }


    /**
     * 获取Spring上下文RestTemplate
     *
     * @return
     */
    public RestTemplate getRestTemplate() {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(InitIocBean.class);
        RestTemplate restTemplate = ctx.getBean(RestTemplate.class);
        return restTemplate;
    }


}

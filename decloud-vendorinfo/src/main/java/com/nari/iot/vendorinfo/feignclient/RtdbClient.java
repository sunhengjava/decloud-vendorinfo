package com.nari.iot.vendorinfo.feignclient;

import com.sgcc.uap.rtdb.criteria.TableGetCriteria;
import com.sgcc.uap.rtdb.domain.GraphRealReq;
import com.sgcc.uap.rtdb.domain.GraphRealRsp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.util.List;

@Component
@FeignClient(value = "rtdbserver", path = "/rtdb")
public interface RtdbClient {

    /**
     * 根据域名数组取域值
     *
     * @param criteria
     * @return 域值数组的list
     */
    @RequestMapping(value = "/tableGet", method = RequestMethod.POST)
    List<List<Object>> tableGet(@RequestBody TableGetCriteria criteria);

    /**
     * 批量获取redis数据接口
     *
     * @param real_req
     * @return 成功返回GraphRealRsp，失败返回null
     */
    @RequestMapping(value = "/graphGetData2", method = RequestMethod.POST)
    GraphRealRsp graphGetData(@RequestBody GraphRealReq real_req);

    /**
     * 根据关键字和域名数组来取某条记录的多个域的值
     *
     * @param pk
     * @param criteria
     * @return
     */
    @RequestMapping(value = "/tableGetByKey/{pk}", method = RequestMethod.POST)
    Object[] tableGetByKey(@PathVariable("pk") Serializable pk, @RequestBody TableGetCriteria criteria);
}
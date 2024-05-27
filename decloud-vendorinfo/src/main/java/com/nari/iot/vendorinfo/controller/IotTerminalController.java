package com.nari.iot.vendorinfo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.HttpUtil;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.IotTerminalService;
import com.nari.iot.vendorinfo.service.PurchaseOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *@description： 终端注册
 *@author：sunheng
 *@date：2022/11/1 16:29
 *@param：
 */
@RestController
@RequestMapping("iotTerminal")
public class IotTerminalController {
    @Autowired
     IotTerminalService iotTerminalService;
    /**
     * @description：15.3 终端注册建档新增接口
     * @author：sunheng
     * @date：2022/11/1 9:49
     * @param： 我来保主要传过来的数据为：
     * 融合终端esn
     * 融合终端名称：长沙市+天心区+配变名称（大体命名格式）
     * 配变信息：配变名称、配变pms编号(16M…)、台区编号、配变资源id
     * 建档申请状态、时间
     */
    @RequestMapping(value = "/insertTerminalRegister", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object>  insertTerminalRegister(@RequestBody Map<String, Object> map) throws UnsupportedEncodingException {
        return  iotTerminalService.insertTerminalRegister(map);
    }

    @RequestMapping(value = "/upRegion", method = RequestMethod.POST)
    @ResponseBody
    public void upRegion(@RequestBody Map<String, String> map) {
          iotTerminalService.upRegion(map.get("city"),map.get("devLabel"));
    }


    /**
     * tets
     */
    @GetMapping("/test1")
    @ResponseBody
    public JSONObject test1() {

        String test="[{\"batchNumber\":\"DKYSCU2022111001\",\n" +
                "\"termId\":\"T231202SC201202110000001\",\n" +
                "\"termEsn\":\"1301021240000001\",\n" +
                "\"devType\":\"SCT230A\",\n" +
                "\"sendTime\":\"2021-11-11 00:00:00\",\n" +
                "\"termFactory\":\"北京智芯微电子科技有限公司\",\n" +
                "\"orgNm\":\"国网湖南综合能源服务有限公司\",\n" +
                "\"isValid\":\"1\"\n" +
                "}]";
        List listt = JSON.parseArray(test,Map.class);
        //工程管控
        String addrGcgk = "http://25.212.251.16:17801/termInfo/saveTermEsn";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("tel", "15200855793");
        Tbwlb tb=new Tbwlb();
        headers.put("smallToken",tb.getToken());
        String ss2 = HttpUtil.httpPost(addrGcgk, JSONObject.toJSONString(listt), headers);
        Map parse2 = (Map) JSON.parse(ss2);
        return new JSONObject(parse2);
    }

    public static void main(String[] args) {
        String test="{\"batchNumber\":\"DKYSCU2022111001\",\n" +
                "\"termId\":\"T231202SC201202110000001\",\n" +
                "\"termEsn\":\"1301021240000001\",\n" +
                "\"devType\":\"SCT230A\",\n" +
                "\"sendTime\":\"2021-11-11 00:00:00\",\n" +
                "\"termFactory\":\"北京智芯微电子科技有限公司\",\n" +
                "\"orgNm\":\"国网湖南综合能源服务有限公司\",\n" +
                "\"isValid\":\"1\"\n" +
                "}";
        String test1="[{\"batchNumber\":\"DKYSCU2022111001\",\n" +
                "\"termId\":\"T231202SC201202110000001\",\n" +
                "\"termEsn\":\"1301021240000001\",\n" +
                "\"devType\":\"SCT230A\",\n" +
                "\"sendTime\":\"2021-11-11 00:00:00\",\n" +
                "\"termFactory\":\"北京智芯微电子科技有限公司\",\n" +
                "\"orgNm\":\"国网湖南综合能源服务有限公司\",\n" +
                "\"isValid\":\"1\"\n" +
                "}]";
        List listt = JSON.parseArray(test1,Map.class);
        String s = JSONObject.toJSONString(listt);
        System.out.println(s);

        Map parse =(Map) JSON.parse(test);
        ArrayList arrayList=new ArrayList();
        arrayList.add(parse);
        System.out.println(arrayList);
    }
}

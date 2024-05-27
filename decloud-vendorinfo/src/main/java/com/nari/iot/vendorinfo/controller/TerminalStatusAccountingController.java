package com.nari.iot.vendorinfo.controller;

import com.nari.iot.vendorinfo.common.JDBCUtils;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.TerminalStatusAccountingService;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @program: decloud-vendorinfo
 * @description: 终端状态统计页面
 * @author: sunheng
 * @create: 2023-10-25 10:36
 **/
@RestController
@RequestMapping("termianlStatusAccounting")
public class TerminalStatusAccountingController {
    @Autowired(required = false)
    private TerminalStatusAccountingService terminalStatusAccountingService;


    /**
     * @description： 1查询 统计列表
     * @author：sunheng
     * @date：2023/10/25 11:44
     * @param：
     */
    @GetMapping("/searchListPO")
    public LayJson getListPO(@RequestParam(value = "year", required = false) String year,
                             @RequestParam(value = "terminalFactory", required = false) String terminalFactory,
                             @RequestParam(value = "batch", required = false) String batch,
                             @RequestParam(value = "product", required = false) String product,
                             @RequestParam("orgType") String orgType,
                             @RequestParam("orgId")String orgId ) {
        LayJson listPO = terminalStatusAccountingService.searchListPO(year, terminalFactory, batch, product,orgType,orgId);
        return listPO;
    }



    /**
     * @description： 2 查询 招标批次
     * @author：sunheng
     * @date：2023/10/25 14:14
     * @param：
     */
    @GetMapping("/searchBatch")
    public LayJson searchBatch() {
        LayJson listPO = terminalStatusAccountingService.searchBatch();
        return listPO;
    }

    /**
     * @description： 3 查询 所属产品
     * @author：sunheng
     * @date：2023/10/25 14:14
     * @param：
     */
    @GetMapping("/searchProduct")
    public LayJson searchProduct() {
        LayJson listPO = terminalStatusAccountingService.searchProduct();
        return listPO;
    }

    /**
     *@description：查询终端厂家信息、
     *@author：sunheng
     *@date：2023/10/26 9:21
     * post
         *@param： 调用地址 /vendor/queryGysPo
     */


}

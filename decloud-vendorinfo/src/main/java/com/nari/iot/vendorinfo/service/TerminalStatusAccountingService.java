package com.nari.iot.vendorinfo.service;

import com.nari.iot.vendorinfo.entity.LayJson;

import java.util.List;
import java.util.Map;

/**
 * @program: decloud-vendorinfo
 * @description: 终端状态统计页面
 * @author: sunheng
 * @create: 2023-10-25 10:45
 **/
public interface TerminalStatusAccountingService {
    public LayJson searchListPO(String year, String terminalFactory, String batch, String product, String orgType, String orgId);

    public LayJson searchBatch();
    public LayJson searchProduct();
}

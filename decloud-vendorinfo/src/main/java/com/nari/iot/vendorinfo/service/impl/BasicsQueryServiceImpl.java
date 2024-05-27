package com.nari.iot.vendorinfo.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.service.BsicsQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service("BsicsQueryService")
@Slf4j
public class BasicsQueryServiceImpl implements BsicsQueryService {

    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    CommonInterface commonInterface;

    @Override
    public LayJson queryIotDevcie(HttpServletRequest request) {

        String dev_label = request.getParameter("dev_label") != null ? request.getParameter("dev_label") : "";
        String dev_name = request.getParameter("dev_name") != null ? request.getParameter("dev_name") : "";
        String rely_id = request.getParameter("rely_id") != null ? request.getParameter("rely_id") : "";
        String rely_name = request.getParameter("rely_name") != null ? request.getParameter("rely_name") : "";

        int pageNo = request.getParameter("pageNo") == null || request.getParameter("pageNo") == "" ? 1 : Integer.parseInt(request.getParameter("pageNo").toString());
        int pageSize = request.getParameter("pageSize") == null || request.getParameter("pageSize") == "" ? 50 : Integer.parseInt(request.getParameter("pageSize").toString());

        StringBuffer sql = new StringBuffer("select*from iot_device where  is_valid=1 and connect_mode=1 and out_iot_fac=2  ");
        StringBuffer sqlCount = new StringBuffer("select count(1) from iot_device  where  is_valid=1 and connect_mode=1 and out_iot_fac=2  ");
        if (dev_label != null && !dev_label.equals("")) {
            sql.append(" and dev_label = '" + dev_label + "'\n");
            sqlCount.append("and dev_label = '" + dev_label + "'\n");
        }
        if (dev_name != null && !dev_name.equals("")) {
            sql.append(" and dev_name = '" + dev_name + "'\n");
            sqlCount.append("and dev_name = '" + dev_name + "'\n");
        }

        if (rely_id != null && !rely_id.equals("")) {
            sql.append(" and rely_id = '" + rely_id + "'\n");
            sqlCount.append("and rely_id = '" + rely_id + "'\n");
        }

        if (rely_name != null && !rely_name.equals("")) {
            sql.append(" and rely_name = '" + rely_name + "'\n");
            sqlCount.append("and rely_name = '" + rely_name + "'\n");
        }

        sql.append(" order by id limit " + (pageNo - 1) * pageSize + "," + pageSize);
        JSONArray objects1 = commonInterface.dbAccess_selectList(sql.toString());
        logger.info("查询iot表中的数据" + sql + "返回结果值：" + objects1);
        List<Object[]> devCount = commonInterface.selectListBySql(sqlCount.toString());
        log.info("查询总条数sql"+sqlCount+"返回值结果："+ JSONObject.toJSONString(devCount));
        LayJson layJson = new LayJson(200, "请求成功", objects1, Integer.parseInt(devCount.get(0)[0].toString()));
        return layJson;
    }
}

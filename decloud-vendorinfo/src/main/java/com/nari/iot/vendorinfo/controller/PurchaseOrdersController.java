package com.nari.iot.vendorinfo.controller;

import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.entity.LayJson;
import com.nari.iot.vendorinfo.entity.LayJsonS;
import com.nari.iot.vendorinfo.service.PurchaseOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @description：0、于我来保数据交互
 * @author：sunheng
 * @date：2022/11/23 9:08
 * @param：
 */
@RestController
@RequestMapping("/purchaseOrders")
public class PurchaseOrdersController {
    @Autowired
    private PurchaseOrdersService purchaseOrdersService;

    @Autowired
    CommonInterface commonInterface;

    /**
     * @description：4.3 发货申请-新增
     * @author：sunheng
     * @date：2022/10/31 17:38
     * @param：
     */
    @PostMapping(value = "/addSqWorkOrder")
    @ResponseBody
    public LayJson addSqWorkOrder(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.insertSqWorkOrder(map);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return new LayJson(500, "请求失败", null, 0);
        }

    }


    /**
     * @description：4.3 发货申请-修改
     * @author：sunheng
     * @date：2022/11/03 17:38
     * @param：{ *              workOrderId:            工单ID,
     * *              estimatedQtyShipped:    送达时间,
     * *              currentStateTime:       当前状态时间,
     * *              xgnr:                   修改内容
     * *          }
     */
    @PostMapping(value = "/updateSqWorkOrder")
    @ResponseBody
    public LayJson updateSqWorkOrder(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.updateSqWorkOrder(map);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return new LayJson(500, "请求失败", null, 0);
        }

    }


    /**
     * @description：5.3发货审批-新增接口
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "addSpWorkOrder")
    @ResponseBody
    public LayJson addSpWorkOrder(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.insertSpWorkOrder(map);
        } catch (Exception e) {
            e.printStackTrace();
            return new LayJson(500, "请求失败", null, 0);
        }
    }


    /**
     * @description：5.4发货审批-修改接口
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "updateSpWorkOrder")
    @ResponseBody
    public LayJson updateSpWorkOrder(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.updateSpWorkOrder(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @description：5.5发货审批-删除接口
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "delSpWorkOrder")
    @ResponseBody
    public LayJson deleteSpWorkOrder(HttpServletRequest request) {
        try {
            return purchaseOrdersService.deleteSpWorkOrder(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }


    /**
     * @description：6.3 供应商发货-新增
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "addFhWorkOrder")
    @ResponseBody
    public LayJson addFhWorkOrder(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.insertFhWorkOrder(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }


    /**
     * @description：6.3 供应商发货-修改
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "updateFhWorkOrder")
    @ResponseBody
    public LayJson updateFhWorkOrder(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.updateFhWorkOrder(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }


    /**
     * @description：7.3 检测中心收货-新增
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "insertShWorkOrder")
    @ResponseBody
    public LayJson insertShWorkOrder(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.insertShWorkOrder(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }


    /**
     * @description：7.4 检测中心收货-修改
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "updateShWorkOrder")
    @ResponseBody
    public LayJson updateShWorkOrder(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.updateShWorkOrder(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }


    /**
     * @description：8.8 检测中全检-新增 （新增实际匹配合同信息）
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "addQjWorkOrder")
    @ResponseBody
    public LayJson addQjWorkOrder(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addQjWorkOrder(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }

    /**
     * @description：11.4 终端调试状-新增 （终端调试状态新增接口）
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "addTsWorkOrder")
    @ResponseBody
    public LayJson addTsWorkOrder(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addTsWorkOrder(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }

    /**
     * @description：12.3 终端配送-新增（配送单信息）
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "addZdpsTerm")
    @ResponseBody
    public LayJson addZdpsTerm(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addZdpsTerm(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }


    /**
     * @description：13.3 终端收货-新增（配送单信息）
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "addZdshTerm")
    @ResponseBody
    public LayJson addZdshTerm(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addZdshTerm(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }


    /**
     * @description：14.3 终端安装-修改资源库的-图片信息
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "addZdazOssTerm")
    @ResponseBody
    public LayJson addZdazOssTerm(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addZdazOssTerm(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }

    /**
     * @description：14.3 终端安装-状态
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "addZdazTerm")
    @ResponseBody
    public LayJson addZdazTerm(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addZdazTerm(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }

    /**
     * @description：14.4 获取终端工程管控信息接口
     * @author：xuzhongyuan
     * @date：2023/10/13 16:00
     * @param：
     */
    @PostMapping(value = "addZdEngineeringControlInfo")
    @ResponseBody
    public LayJson addZdEngineeringControlInfo(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addZdEngineeringControlInfo(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }

    /**
     * @description：16.3 参数配置-新增/修改接口 （配置是否成功标识，状态时间、 配变容量、CT变比）
     * 旧代码----------
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "addCsPzTerm")
    @ResponseBody
    public LayJson addCsPzTerm(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addCsPzTerm(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }

    /**
     * @description：16.3 参数配置-新增/修改接口 （配置是否成功标识，状态时间、 配变容量、CT变比）
     * 参数配置----------新
     * @author：sunheng
     * @date：2022/11/4 10:59
     * @param：
     */
    @PostMapping(value = "addCsPzTermSuper")
    @ResponseBody
    public LayJsonS addCsPzTermSuper(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addCsPzTermSuper(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJsonS(500, "请求失败", null, 0, "失败", 0);
    }

    /*  *//**
     * @description：15.7子设备新增
     * @author：sunheng
     * @date：2023/3/1 19:02
     * @param：
     *//*
    @PostMapping(value = "addChildDevice")
    @ResponseBody
    public LayJson addChildDevice(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addChildDevice(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }*/

    /**
     * @description：更新iot_defice表中子设备数量 32
     * @author：sunheng
     * @date：2023/3/1 19:02
     * @param：
     */
    @PostMapping(value = "addChildDevices")
    @ResponseBody
    public LayJson addChildDevices(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addChildDevices(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }
    @PostMapping(value = "addChildDevicesTy")
    @ResponseBody
    public LayJson addChildDevicesTy(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addChildDevicesTy(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }
    @PostMapping(value = "addIotChildSize")
    @ResponseBody
    public LayJson addIotChildSize(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.addIotChildSize(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }

    /**
     * @description：删除DMS_IOT_CHILDDEVICES表中子设备数量
     * @author：zhangzhihao
     * @date：2023/8/1
     * @param：
     */
    @PostMapping(value = "delChildDevice")
    @ResponseBody
    public LayJson delChildDevice(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.delChildDevice(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }


    @PostMapping(value = "sendMessage")
    @ResponseBody
    public LayJson sendMessage(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.delChildDevice(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }



        /*2、更新订单接口（滞后天数）
        * {
            "cgddh"："采购订单号",
            "zhts"："滞后天数"
            }
        * */
    @PostMapping(value = "upOderLagdays")
    @ResponseBody
    public LayJson upOderLagdays(@RequestBody Map<String, Object> map) {
        try {
            return purchaseOrdersService.upOderLagdays(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LayJson(500, "请求失败", null, 0);
    }


}

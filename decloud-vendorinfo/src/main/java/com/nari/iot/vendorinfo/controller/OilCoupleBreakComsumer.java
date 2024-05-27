package com.nari.iot.vendorinfo.controller;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.IdConvert;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;


/* 消费kafka中油温异常数据（陶定元 发送的kafka）
 * 异常监测油温数据低于下限值 (如5度) 或高于上限值 (如120度) ， 阑值可人工设置 (当日)
 * */


@Slf4j
@Controller
public class OilCoupleBreakComsumer implements Runnable {

    @Autowired
    CommonInterface commonInterface;

    public static void main(String[] args) {
//        Long lo=1681879500000L;
//        String s = stampToDate(lo);
//        System.out.println(s);
        String te="789890";
        String[] split = te.split(",");
        System.out.println(JSONObject.toJSONString(split));
        if(split.length>1){
            System.out.println(split[0]+"____"+split[1]);
        }
    }

    @Override
    public void run() {
        Properties consumerProperties = new Properties();
        consumerProperties.put("bootstrap.servers", "25.212.172.5:20540,25.212.172.6:20540,25.212.172.7:20540");
        consumerProperties.put("group.id", "group8");
        consumerProperties.put("enable.auto.commit", "true");
//        consumerProperties.put("auto.commit.interval.ms", "100");
        consumerProperties.put("session.timeout.ms", "30000");
        /*
        如果有已提交的offset，从已提交的offset之后消费消息。如果无提交的offset,
            lastest: 从最后的offset之后消费数据
           earliest: 从最早的offset消费消息
        * */
        consumerProperties.put("auto.offset.reset", "earliest");
        consumerProperties.put("max.poll.interval.ms", "600000");
        consumerProperties.put("max.poll.records", "50");
        consumerProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        Consumer consumer = new KafkaConsumer<Integer, String>(consumerProperties);
        consumer.subscribe(Arrays.asList("TRS_MEAS_LIMIT_ALARM"));
        while (true) {
            //log.info("在拉");
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
         /* 1681869515647 3801319758079461724 13505 46 141.54  0  0  160.0*/
                try {
                    //JSONObject faultFlip = JSONObject.parseObject(consumerRecord.value());
                    String result = consumerRecord.value();
                    String[] resultList = result.split(" ");
                    if (resultList.length > 1 && resultList[2].equals("13505") && resultList[3].equals("176")) {
                        //log.info("TRS_MEAS_LIMIT_ALARM消息体" + consumerRecord.value());
                        long idByKeyId = IdConvert.getIdByKeyId(Long.parseLong(resultList[1]));
                        String time = stampToDate(Long.parseLong(resultList[0]));
                        String[] split = resultList[7].split(",");
                        String restricts="0";
                        String restricts2="0";
                        if(split.length>1){
                            restricts2 = split[1];
                            restricts=split[0];
                        }

                        commonInterface.dbAccess_insert("insert into \"D5000\".\"DMS_TEMPERATURE_UPANDDOWN\"(timestamps,measured_value,measured_id," +
                                "table_no,region_no,outof_pattern,outof_value,restricts,tgid,timestamp_date,restricts2)\n"
                                + "values('" + resultList[0] + "'," + resultList[4] + ",'" + resultList[1]
                                + "'," + resultList[2] + ","+ resultList[3]
                                + "," + resultList[5] + ","+resultList[6]+" ," + restricts2 + ","+idByKeyId+",'"+time+"' ,"+restricts+" )");
                    }
                } catch (JSONException e) {
                    log.info("出现了问题" + e.getMessage());
                    continue;
                }
            }
        }


    }


    public static String dateToStamp(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stamp = "";
        if (!"".equals(time)) {//时间不为空
            try {
                stamp = String.valueOf(sdf.parse(time).getTime() / 1000);
            } catch (Exception e) {
                System.out.println("参数为空！");
            }
        } else {    //时间为空
            long current_time = System.currentTimeMillis();  //获取当前时间
            stamp = String.valueOf(current_time / 1000);
        }
        return stamp;
    }

    public static String stampToDate(Long time) {
        //若是1681879500000L 多则不用乘
        //若少  则用乘
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time_Date = sdf.format(new Date(time));
        return time_Date;
    }

}

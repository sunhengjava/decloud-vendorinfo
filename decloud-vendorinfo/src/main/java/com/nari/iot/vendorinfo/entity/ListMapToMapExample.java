package com.nari.iot.vendorinfo.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
 
public class ListMapToMapExample {
    public static void main(String[] args) {
        // 创建一个List<Map>
        List<Map<String, Object>> list = Arrays.asList(
            new HashMap<String, Object>() {{
                put("id", 1);
                put("name", "Alice");
            }},
            new HashMap<String, Object>() {{
                put("id", 2);
                put("name", "Bob");
            }},
                new HashMap<String, Object>() {{
                    put("id", 2);
                    put("name", "33");
                }}

        );
 
        // 使用Stream API从List<Map>中提取数据集合并创建为一个新的Map
        Map<Integer, String> resultMap = list.stream()
            .collect(Collectors.toMap(
                map -> (Integer) map.get("id"), // 使用map中的"id"作为key
                map -> (String) map.get("name")  // 使用map中的"name"作为value
            ));
 
        // 打印结果
        resultMap.forEach((key, value) -> System.out.println(key + " -> " + value));
    }
}
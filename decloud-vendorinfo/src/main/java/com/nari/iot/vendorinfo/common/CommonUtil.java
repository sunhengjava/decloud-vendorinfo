package com.nari.iot.vendorinfo.common;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class CommonUtil {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CommonUtil.class);

    /**
     * 获取当前月份
     *
     * @return
     */
    public int getMonth() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        return month;
    }

    /**
     * 获取当前年份
     *
     * @return
     */
    public int getYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        return year;
    }

    /**
     * 获取上月年份
     *
     * @return
     */
    public int getLastMonthOfYear() {
        Date date = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1); //当前月的上个月
        int year = cal.get(Calendar.YEAR);
        return year;
    }

    /**
     * 获取上月月份
     *
     * @return
     */
    public int getLastMonth() {
        Date date = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1); //当前月的上个月
        int year = cal.get(Calendar.MONTH) + 1;
        return year;
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getTime(String format) {
        SimpleDateFormat sf = new SimpleDateFormat(format);

        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());
        return sf.format(ca.getTime());
    }

    /**
     * 字符串转日期格式
     *
     * @param strDate
     * @return
     */
    public Date parseDateFormat(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        Date date = null;
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        try {
            date = sdf.parse(strDate);
        } catch (Exception localException) {
        }
        return date;
    }

    /**
     * 检查文件是否存在，存在则删除
     */
    public static void checkFile(String target) {
        int index = target.lastIndexOf("/");
        if (index <= 0) {
            index = target.lastIndexOf("\\");
        }
        String path = target.substring(0, index);
        checkDir(path);
        File file = new File(target);
        if (file.exists()) {
            boolean b = file.delete();
        }
    }

    /**
     * 检查文件夹是否存在， 不存在则创建
     *
     * @param path
     */
    public static void checkDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean b = dir.mkdirs();
        }
    }

    /**
     * 获得指定日期的前或后几天的时间 yyyy-MM-dd HH:mm:ss
     *
     * @param format
     * @param i
     */
    public static String getDayStrBeforeOrAfter(int i, String format) {
        String dateStr = "";
        if ("".equals(format)) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        final int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day + i);
        dateStr = sdf.format(c.getTime());

        return dateStr;
    }

    /**
     * 日期format格式转毫秒级time
     *
     * @param dataStr
     * @param format
     * @return
     */
    public static long dateStrToTime(String dataStr, String format) {
        Date date = strTodate(dataStr, format);
        if (date == null) {
            return 0L;
        }
        return date.getTime();
    }

    /**
     * jsonArray转换成List
     *
     * @param jsonArray
     * @param strs
     * @return
     */
    public static List<Object[]> jsonArrayToList(JSONArray jsonArray, String[] strs) {
        List<Object[]> resultList = new ArrayList<Object[]>();

        if (jsonArray.size() == 0) {
            return resultList;
        }

        for (Object obj : jsonArray) {
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(obj));
            Object[] objs = new Object[strs.length];
            for (int i = 0; i < strs.length; i++) {
                objs[i] = jsonObject.get(strs[i].toUpperCase());
            }
            resultList.add(objs);
        }
        return resultList;
    }

    /**
     *  jsonArray转List<Object[]>
     * @return
     */
    public static List<Object[]>  toListBuJsonArr(JSONArray jsonArray, String sql){
        List<Object[]> list=new ArrayList<Object[]>();
        try{

            if(jsonArray!=null&&jsonArray.size()>0){
                for(Object object : jsonArray){
                    LinkedHashMap<String,Object> map=(LinkedHashMap<String,Object>)object;
                    //    com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(com.alibaba.fastjson.JSONObject.toJSONString(object));
                    Object[] o=new Object[map.entrySet().size()];
                    int i=0;
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        o[i]=entry.getValue();
                        i++;
                    }
                    list.add(o);
                }
            }else{
                return list;
            }
            return list;
        }catch(Exception e){
            System.out.println("此sql报错"+sql);
            LOGGER.info("此sql报错"+sql);
        }
        return list;
    }

    public static List<Map<String, Object>> getListMapResult(String[] cols, List<?> data) {
        List<Map<String, Object>> resultListMap = new ArrayList<Map<String, Object>>();

        if (cols != null && data != null) {
            int recordNum = data.size();
            for (int i = 0; i < recordNum; i++) {

                Map<String, Object> map = new HashMap<String, Object>();
                Object[] record = (Object[]) data.get(i);

                for (int j = 0; j < cols.length; j++) {
                    map.put(cols[j], record[j]);
                }
                resultListMap.add(map);
            }
        }
        return resultListMap;
    }

    /**
     * 返回map的数据结构
     *
     * @param isSuc
     * @param total
     * @param message
     * @param rows
     * @return success total message value
     */
    public static Map<String, Object> returnMap(boolean isSuc, int total,
                                                String message, Object rows) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", isSuc);
        result.put("total", total);
        result.put("message", message);
        result.put("value", rows);

        return result;
    }
    public static StringBuilder getPostRaw(HttpServletRequest request){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }
    /**
     * 融合终端验收接口返回
     * @param isSuc
     * @param result
     * @param data
     * @return
     */
    public static Map<String, Object> returnMap2(boolean isSuc, String result, Object data) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("success", isSuc);
        resultMap.put("result", result);
        resultMap.put("value", data);

        return resultMap;
    }


    /**
     * 将网络图片编码为base64
     *
     * @param url
     * @return
     */
    public static String encodeImageToBase64(URL url) throws Exception {
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        System.out.println("图片的路径为:" + url.toString());
        //打开链接
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            //设置请求方式为"GET"
            conn.setRequestMethod("GET");
            //超时响应时间为5秒
            conn.setConnectTimeout(5 * 1000);
            //通过输入流获取图片数据
            InputStream inStream = conn.getInputStream();
            //得到图片的二进制数据，以二进制封装得到数据，具有通用性
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            //创建一个Buffer字符串
            byte[] buffer = new byte[1024];
            //每次读取的字符串长度，如果为-1，代表全部读取完毕
            int len = 0;
            //使用一个输入流从buffer里把数据读取出来
            while ((len = inStream.read(buffer)) != -1) {
                //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
                outStream.write(buffer, 0, len);
            }
            //关闭输入流
            inStream.close();
            byte[] data = outStream.toByteArray();
            //对字节数组Base64编码
            BASE64Encoder encoder = new BASE64Encoder();
            String base64 = encoder.encode(data);
            return base64;//返回Base64编码过的字节数组字符串
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("图片上传失败,请联系客服!");
        }
    }
    public static String parseDateStrToNewStr(String dateStr, String oldFormat,
                                              int i, String newFormat) {
        String newStr = "";
        if (isEmpty(oldFormat)) {
            oldFormat = "yyyy-MM-dd";
        }

        if (isEmpty(newFormat)) {
            newFormat = oldFormat;
        }
        Date date1 = strTodate(dateStr, oldFormat);
        Date date2 = getDateBeforeOrAfter(date1, i);
        newStr = dateToStr(date2, newFormat);

        return newStr;
    }

    public static Date getDateBeforeOrAfter(Date date, int i) {
        if (date == null) {
            return null;
        }

        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        final int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day + i);

        return c.getTime();
    }

    /**
     * 根据键名查询键值
     *
     * @param key
     * @return
     */
    public static String readPropertisByName(String key) {
        String value = "";
        if (isEmpty(key)) {
            return value;
        }
        Properties prop = new Properties();
        try {
            String ospHomePath = System.getenv("OSP_HOME");
            String fileName = ospHomePath + File.separator + "conf"
                    + File.separator + "params.properties";
            InputStream in = new BufferedInputStream(new FileInputStream(
                    fileName));
            prop.load(in);
            Iterator<String> it = prop.stringPropertyNames().iterator();
            while (it.hasNext()) {
                if (key.equals(it.next())) {
                    value = prop.getProperty(key);
                    break;
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static boolean isEmpty(Object obj) {
        return (obj == null || "".equals(obj.toString()) || "null".equals(obj.toString()));
    }

    /**
     * 获取下一个月第一天
     *
     * @return
     */
    public static String getFirstDayOfNextMonth(String dateStr, String format,
                                                String outFormat) {
        String outStr = "";

        if (isEmpty(outFormat)) {
            outFormat = format;
        }

        SimpleDateFormat sf = new SimpleDateFormat(outFormat);
        Date date = strTodate(dateStr, format);
        if (date == null) {
            return outStr;
        }

        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(Calendar.MONTH, +1);
        ca.set(Calendar.DAY_OF_MONTH,
                ca.getActualMinimum(Calendar.DAY_OF_MONTH));
        outStr = sf.format(ca.getTime());
        return outStr;
    }

    /**
     * 日期转字符串 yyyy-MM-dd
     */
    public static String dateToStr(Date date, String format) {
        if ("".equals(format)) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        String str = "";
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        str = sdf.format(date);
        return str;
    }


    /**
     * parseDouble
     *
     * @param o
     * @return
     */
    public double parseDouble(Object o) {

        BigDecimal order = new BigDecimal(o + "");
        double d = order.doubleValue();
        return d;
    }

    /**
     * 获取某月的最后一天
     *
     * @throws
     * @Title:getLastDayOfMonth
     * @Description:
     * @param:@param year
     * @param:@param month
     * @param:@return
     * @return:String
     */
    public String getLastDayOfMonth(String str) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(strTodate(str, ""));
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        //设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfMonth = sdf.format(cal.getTime());
        return lastDayOfMonth;
    }


    /**
     * 字符串转date
     *
     * @param str
     * @param format
     * @return
     */
    public static Date strTodate(String str, String format) {
        if ("".equals(format)) {
            format = "yyyy-MM-dd";
        }

        Date date = null;
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }


    /**
     * 获取当月第一天
     *
     * @return
     */
    public String getMonthFirstDay() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());
        ca.add(Calendar.MONTH, 0);
        ca.set(Calendar.DAY_OF_MONTH,
                ca.getActualMinimum(Calendar.DAY_OF_MONTH));

        return sf.format(ca.getTime());
    }

    /**
     * 设置让浏览器弹出下载对话框的Header. 根据浏览器的不同设置不同的编码格式 防止中文乱码
     *
     * @param fileName
     *            下载后的文件名.
     */
    public static void setFileDownloadHeader(HttpServletRequest request, HttpServletResponse response, String fileName) {
        try {
            String encodedfileName = null;
            String agent = request.getHeader("USER-AGENT");
            if (null != agent && -1 != agent.indexOf("MSIE")) {
                encodedfileName = java.net.URLEncoder.encode(fileName, "UTF-8");
            } else if (null != agent && -1 != agent.indexOf("Mozilla")) {
                encodedfileName = new String(fileName.getBytes("UTF-8"),
                        "iso-8859-1");
            } else {
                encodedfileName = java.net.URLEncoder.encode(fileName, "UTF-8");
            }
            response.setHeader("Content-Disposition", "attachment; filename=\""
                    + encodedfileName + "\"");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }














}

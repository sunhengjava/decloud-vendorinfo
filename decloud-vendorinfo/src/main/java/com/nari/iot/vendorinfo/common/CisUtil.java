package com.nari.iot.vendorinfo.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CisUtil {

    public static String getID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private final static String[] chars = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
            "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6",
            "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z"};

    /**
     * 生成相应位数的校验码
     *
     * @param bit
     * @return
     */
    public static String getCode(int bit) {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < bit; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();
    }

    /**
     * 生成1000~9999 之间的4位随机数
     *
     * @return long
     */
    public static long getFourCode() {
        long sj = 0;
        sj = Math.round(Math.random() * 8999 + 1000);
        return sj;
    }

    /**
     * service 层返回的数据结构 <br/>
     * Map<String,Object> result;
     *
     * @param sucCode
     * @param msg
     * @return
     */
    public static Map<String, Object> backResult(String sucCode, String msg, Object obj) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("suc", sucCode);
        result.put("msg", msg);
        result.put("obj", obj);
        return result;
    }

    /**
     * 根据xxx,xxx,xxx 结构的字符串生成对应list
     *
     * @param ids
     * @return
     */
    public static List<String> idsToList(String ids) {
        List<String> idsList = new ArrayList<String>();
        try {
            String[] batchId = ids.split(",");
            Collections.addAll(idsList, batchId);
        } catch (Exception e) {
            System.out.println(e.toString());
            return idsList;
        }
        return idsList;
    }

    /**
     * 获取当前年份
     *
     * @return
     */
    public static String getCurYear() {
        Calendar a = Calendar.getInstance();
        int year = a.get(Calendar.YEAR);
        return String.valueOf(year);
    }

    /**
     * 获取系统当前时间
     *
     * @return
     */
    public static String getCurTime() {
        long time = System.currentTimeMillis();
        return String.valueOf(time);
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
     * 检查文件是否存在，存在则删除
     */
    public static void checkFile(String target) {
        String path = target.substring(0, target.lastIndexOf("/"));
        checkDir(path);
        File file = new File(target);
        if (file.exists()) {
            boolean b = file.delete();
        }
    }

    /**
     * 获取当前年月字符串
     */
    public static String getYYYYMM() {
        String dateStr = "";
        Date date = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        dateStr = sdf.format(date);
        return dateStr;
    }

    public static String dateToStr(Date date) {
        String str = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        str = format.format(date);
        return str;
    }

    /**
     * 设置让浏览器弹出下载对话框的Header. 根据浏览器的不同设置不同的编码格式 防止中文乱码
     *
     * @param fileName 下载后的文件名.
     */
    public static void setFileDownloadHeader(HttpServletRequest request, HttpServletResponse response,
                                             String fileName) {
        try {
            String encodedfileName = null;
            String agent = request.getHeader("USER-AGENT");
            if (null != agent && -1 != agent.indexOf("MSIE")) {// IE
                encodedfileName = java.net.URLEncoder.encode(fileName, "UTF-8");
            } else if (null != agent && -1 != agent.indexOf("Mozilla")) {
                encodedfileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
            } else {
                encodedfileName = java.net.URLEncoder.encode(fileName, "UTF-8");
            }
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String stringToIds(String id) {
        String ids = "";
        ids = "'" + id.replaceAll(",", "','") + "'";
        return ids;
    }

    /**
     * 测试方法
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(getID());
//		System.out.println(getCurYear());
//		System.out.println(getCurTime());
//		System.out.println(stringToIds("8777f66d41c847bca92bbefd390a4778,857f66d41c847bca92bbefd390a4778"));

        // System.out.println(backResult("0000", "查询成功！", new Date()));
        // System.out.println(getCode(6));
        // System.out.println(getFourCode());
        // float f1 = 1.5f;
        // float f2 = 2.5f;
        // System.out.println(f1+f2);
    }
}

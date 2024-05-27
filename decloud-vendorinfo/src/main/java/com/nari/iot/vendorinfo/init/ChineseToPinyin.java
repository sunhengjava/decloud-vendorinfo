package com.nari.iot.vendorinfo.init;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChineseToPinyin {
 
    public static String[] toPinyin(String chinese) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
 
        char[] chars = chinese.toCharArray();
        String[] pinyinArray = new String[chinese.length()];
 
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isWhitespace(c)) {
                pinyinArray[i] = "";
            } else if (c >= 0x4E00 && c <= 0x9FA5) { // 中文字符判断
                try {
                    pinyinArray[i] = PinyinHelper.toHanyuPinyinStringArray(c, format)[0];
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                    pinyinArray[i] = "";
                }
            } else {
                pinyinArray[i] = String.valueOf(c);
            }
        }
        return pinyinArray;
    }
 
    public static void main(String[] args) {
        String chineseName = "";
        String[] pinyin = toPinyin(chineseName);
        String result = Arrays.stream(pinyin).filter(Objects::nonNull).collect(Collectors.joining());
        System.out.println(result);
    }
}
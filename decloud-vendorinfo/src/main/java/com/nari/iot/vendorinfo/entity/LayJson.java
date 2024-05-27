package com.nari.iot.vendorinfo.entity;

public class LayJson {
    private int code;
    private String msg;
    private Object data;
    private int count;
    //返回数据符合layui需要的数据格式；第一个是状态码，0表示成功，第二个是提示信息，第三个是要返回的数据，第四个是数据的总数量
    public LayJson(int code, String msg, Object data, int count) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.count = count;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

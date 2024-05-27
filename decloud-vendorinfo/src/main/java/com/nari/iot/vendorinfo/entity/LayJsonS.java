package com.nari.iot.vendorinfo.entity;

public class LayJsonS {
    private int code;
    private String msg;
    private Object data;
    private int result;
    private String resultValue;
    private int count;
    //返回数据符合layui需要的数据格式；第一个是状态码，0表示成功，第二个是提示信息，第三个是要返回的数据，第四个是数据的总数量


    public LayJsonS(int code, String msg, Object data, int result, String resultValue, int count) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.result = result;
        this.resultValue = resultValue;
        this.count = count;
    }

    public LayJsonS() {
        super();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
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

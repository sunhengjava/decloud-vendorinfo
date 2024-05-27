package com.nari.iot.vendorinfo.common;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;
//import org.apache.http.annotation.NotThreadSafe;

/**
 * 重定义httpdelete对象，使能够传参
 */
//@NotThreadSafe
class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "DELETE";

    /**
     * 重载父类方法
     */
    public String getMethod() {
        return METHOD_NAME;
    }

    public HttpDeleteWithBody(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    public HttpDeleteWithBody(final URI uri) {
        super();
        setURI(uri);
    }

    public HttpDeleteWithBody() {
        super();
    }
}

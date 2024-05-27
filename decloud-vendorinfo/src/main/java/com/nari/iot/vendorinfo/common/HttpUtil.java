package com.nari.iot.vendorinfo.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HttpUtil {

	protected static final Log logger = LogFactory.getLog(HttpUtil.class);

	/**
	 * post方式调用外部http接口
	 * 
	 *            传参（json字符串）
	 * @param urlStr
	 *            接口
	 * @return
	 */
	public static String jsonPost(String dataStr, String urlStr) {
		String respStr = "";
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// 设置读取超时，单位：ms
			conn.setReadTimeout(2000);
			// 设置连接超时，单位：ms
			conn.setConnectTimeout(2000);
			// 是否可以发送参数
			conn.setDoOutput(true);
			// 是否可以接受参数
			conn.setDoInput(true);
			// 是否缓存
			conn.setUseCaches(false);
			// 请求方式
			conn.setRequestMethod("POST");
			// 发送的参数为json格式
			conn.setRequestProperty("Content-type",
					"application/json;charset=UTF-8");

			// 设置请求参数
			OutputStreamWriter writer = null;
			if (!dataStr.isEmpty()&&dataStr!=null) {
				writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
				writer.write(dataStr);
				writer.flush();
			}

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException(
						"Http Post Request Failed with Error Code: "
								+ conn.getResponseCode());
			}

			// 接受返回参数
			BufferedReader response = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "UTF-8"));
			String line = "";
			while ((line = response.readLine()) != null) {
				respStr += line;
			}

			// 关闭连接，输入，输出流
			if (conn != null) {
				conn.disconnect();
			}
			if (writer != null) {
				writer.close();
			}
			if (response != null) {
				response.close();
			}
		} catch (Exception e) {
			e.toString();
		}

		return respStr;
	}

	/**
	 * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址, 参考文章：
	 * http://developer.51cto.com/art/201111/305181.htm
	 * 
	 * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
	 * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
	 * 
	 * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130,
	 * 192.168.1.100
	 * 
	 * 用户真实IP为： 192.168.1.110
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 设置让浏览器弹出下载对话框的Header. 根据浏览器的不同设置不同的编码格式 防止中文乱码
	 * 
	 * @param fileName
	 *            下载后的文件名.
	 */
	public static void setFileDownloadHeader(HttpServletRequest request,
			HttpServletResponse response, String fileName) {
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

	/**
	 * 创建发送https请求的client
	 * 
	 * @return
	 */
	protected static CloseableHttpClient createSSLClient() {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(
					null, new TrustStrategy() {
						// 信任所有
						public boolean isTrusted(X509Certificate[] chain,
								String authType) throws CertificateException {
							return true;
						}
					}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslContext, new String[] { "SSLv2Hello", "SSLv3", "TLSv1",
							"TLSv1.2" }, null, NoopHostnameVerifier.INSTANCE);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (KeyManagementException e) {
			logger.error(e.toString());
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.toString());
		} catch (KeyStoreException e) {
			logger.error(e.toString());
		}
		return HttpClients.createDefault();
	}

	/**
	 * 发送http get请求
	 * 
	 * @param url
	 * @param headers
	 * @return
	 */
	public static String httpGet(String url, Map<String, String> headers) {
		return httpGet(url, headers, "utf-8");
	}

	/**
	 * 发送http get请求
	 * 
	 * @param url
	 * @param headers
	 * @param encode
	 * @return
	 */
	public static String httpGet(String url, Map<String, String> headers,
			String encode) {
		String respStr = "";
		if (encode == null) {
			encode = "utf-8";
		}
		// since 4.3 不再使用 DefaultHttpClient
		CloseableHttpClient closeableHttpClient = HttpClientBuilder.create()
				.build();
		HttpGet httpGet = new HttpGet(url);
		// 设置header
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpGet.setHeader(entry.getKey(), entry.getValue());
			}
		}
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = closeableHttpClient.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			respStr = EntityUtils.toString(entity, encode);
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			try {
				httpResponse.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
		try {
			// 关闭连接、释放资源
			closeableHttpClient.close();
		} catch (IOException e) {
			logger.error(e.toString());
		}
		return respStr;
	}

	/**
	 * 发送 http post 请求，参数以josn字符串进行提交
	 * 
	 * @param url
	 * @param stringJson
	 * @return
	 */
	public static String httpPost(String url, String stringJson) {
		return httpPost(url, stringJson, null, "utf-8");
	}

	/**
	 * 发送 http post 请求，参数以josn字符串进行提交
	 * 
	 * @param url
	 * @param stringJson
	 * @param headers
	 * @return
	 */
	public static String httpPost(String url, String stringJson,
			Map<String, String> headers) {
		return httpPost(url, stringJson, headers, "utf-8");
	}

	/**
	 * 发送 http post 请求，参数以josn字符串进行提交
	 * 
	 * @param url
	 * @param stringJson
	 * @param headers
	 * @param encode
	 * @return
	 */
	public static String httpPost(String url, String stringJson,
			Map<String, String> headers, String encode) {
		String respStr = "";
		if (encode == null) {
			encode = "utf-8";
		}
		CloseableHttpClient closeableHttpClient = HttpClients.createDefault();

		/*RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(15000).setConnectionRequestTimeout(15000)
				.setSocketTimeout(15000).build();*/
		HttpPost httpost = new HttpPost(url);

		// 设置header
		httpost.setHeader("Content-type", "application/json");
		httpost.setHeader("Accept", "application/json");
//		httpost.setConfig(requestConfig);
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpost.setHeader(entry.getKey(), entry.getValue());
			}
		}
		// 组织请求参数
		StringEntity stringEntity = new StringEntity(stringJson, encode);
		httpost.setEntity(stringEntity);
		CloseableHttpResponse httpResponse = null;
		try {
			// 响应信息
			httpResponse = closeableHttpClient.execute(httpost);
			HttpEntity entity = httpResponse.getEntity();
			respStr = EntityUtils.toString(entity, encode);
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			try {
				httpResponse.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
		try {
			// 关闭连接、释放资源
			closeableHttpClient.close();
		} catch (IOException e) {
			logger.error(e.toString());
		}
		return respStr;
	}

	/**
	 * 发送 http put 请求，参数以原生字符串进行提交
	 * 
	 * @param url
	 * @param stringJson
	 * @return
	 */
	public static String httpPut(String url, String stringJson) {
		return httpPut(url, stringJson, null, "utf-8");
	}

	/**
	 * 发送 http put 请求，参数以原生字符串进行提交
	 * 
	 * @param url
	 * @param stringJson
	 * @param headers
	 * @return
	 */
	public static String httpPut(String url, String stringJson,
			Map<String, String> headers) {
		return httpPut(url, stringJson, headers, "utf-8");
	}

	/**
	 * 发送 http put 请求，参数以原生字符串进行提交
	 * 
	 * @param url
	 * @param stringJson
	 * @param headers
	 * @param encode
	 * @return
	 */
	public static String httpPut(String url, String stringJson,
			Map<String, String> headers, String encode) {
		String respStr = "";
		if (encode == null) {
			encode = "utf-8";
		}
		CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
		HttpPut httpput = new HttpPut(url);

		// 设置header
		httpput.setHeader("Content-type", "application/json");
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpput.setHeader(entry.getKey(), entry.getValue());
			}
		}
		// 组织请求参数
		StringEntity stringEntity = new StringEntity(stringJson, encode);
		httpput.setEntity(stringEntity);
		CloseableHttpResponse httpResponse = null;
		try {
			// 响应信息
			httpResponse = closeableHttpClient.execute(httpput);
			HttpEntity entity = httpResponse.getEntity();
			respStr = EntityUtils.toString(entity, encode);
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			try {
				httpResponse.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
		try {
			// 关闭连接、释放资源
			closeableHttpClient.close();
		} catch (IOException e) {
			logger.error(e.toString());
		}
		return respStr;
	}

	/**
	 * 发送http delete请求
	 * 
	 * @param url
	 * @param headers
	 * @return
	 */
	public static String httpDelete(String url, String stringJson,
			Map<String, String> headers) {
		return httpDelete(url, stringJson, headers, "utf-8");
	}

	/**
	 * 发送http delete请求
	 * 
	 * @param url
	 * @param headers
	 * @param encode
	 * @return
	 */
	public static String httpDelete(String url, String stringJson,
			Map<String, String> headers, String encode) {
		String respStr = "";
		if (encode == null) {
			encode = "utf-8";
		}
		// since 4.3 不再使用 DefaultHttpClient
		CloseableHttpClient closeableHttpClient = HttpClientBuilder.create()
				.build();
		HttpDeleteWithBody httpdelete = new HttpDeleteWithBody(url);
		// 设置header
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpdelete.setHeader(entry.getKey(), entry.getValue());
			}
		}
		httpdelete.setHeader("Content-type", "application/json");

		// 组织请求参数
		StringEntity stringEntity = new StringEntity(stringJson, encode);
		httpdelete.setEntity(stringEntity);

		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = closeableHttpClient.execute(httpdelete);
			HttpEntity entity = httpResponse.getEntity();
			respStr = EntityUtils.toString(entity, encode);
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			try {
				httpResponse.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
			try {
				// 关闭连接、释放资源
				closeableHttpClient.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}

		return respStr;
	}

	/**
	 * 发送 http post 请求，支持文件上传
	 * 
	 * @param url
	 * @param params
	 * @param files
	 * @param headers
	 * @return
	 */
	public static String httpPostMultipartFileUpload(String url,
			Map<String, String> params, List<File> files,
			Map<String, String> headers) {
		return httpPostMultipartFileUpload(url, params, files, headers, "utf-8");
	}

	/**
	 * 发送 http post 请求，支持文件上传
	 * 
	 * @param url
	 * @param params
	 * @param files
	 * @param headers
	 * @param encode
	 * @return
	 */
	public static String httpPostMultipartFileUpload(String url,
			Map<String, String> params, List<File> files,
			Map<String, String> headers, String encode) {
		String respStr = "";
		if (encode == null) {
			encode = "utf-8";
		}
		CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
		HttpPost httpost = new HttpPost(url);

		// 设置header
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpost.setHeader(entry.getKey(), entry.getValue());
			}
		}
		MultipartEntityBuilder mEntityBuilder = MultipartEntityBuilder.create();
		mEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		mEntityBuilder.setCharset(Charset.forName(encode));

		// 普通参数
		ContentType contentType = ContentType.create("text/plain",
				Charset.forName(encode));// 解决中文乱码
		if (params != null && params.size() > 0) {
			Set<String> keySet = params.keySet();
			for (String key : keySet) {
				mEntityBuilder.addTextBody(key, params.get(key), contentType);
			}
		}
		// 二进制参数
		if (files != null && files.size() > 0) {
			for (File file : files) {
				mEntityBuilder.addBinaryBody("file", file);
			}
		}
		httpost.setEntity(mEntityBuilder.build());
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = closeableHttpClient.execute(httpost);
			HttpEntity entity = httpResponse.getEntity();
			respStr = EntityUtils.toString(entity, encode);
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			try {
				httpResponse.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
		try {
			// 关闭连接、释放资源
			closeableHttpClient.close();
		} catch (IOException e) {
			logger.error(e.toString());
		}
		return respStr;
	}

	/**
	 * 发送https get请求
	 * 
	 * @param url
	 * @param headers
	 * @return
	 */
	public static String httpsGet(String url, Map<String, String> headers) {
		return httpsGet(url, headers, "utf-8");
	}

	/**
	 * 发送https get请求
	 * 
	 * @param url
	 * @param headers
	 * @param encode
	 * @return
	 */
	public static String httpsGet(String url, Map<String, String> headers,
			String encode) {
		String respStr = "";
		if (encode == null) {
			encode = "utf-8";
		}
		CloseableHttpClient closeableHttpClient = createSSLClient();
		HttpGet httpGet = new HttpGet(url);
		// 设置header
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpGet.setHeader(entry.getKey(), entry.getValue());
			}
		}
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = closeableHttpClient.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			respStr = EntityUtils.toString(entity, encode);
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			try {
				httpResponse.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
		try {
			// 关闭连接、释放资源
			closeableHttpClient.close();
		} catch (IOException e) {
			logger.error(e.toString());
		}
		return respStr;
	}

	/**
	 * 发送 https post 请求，参数以josn字符串进行提交
	 * 
	 * @param url
	 * @param stringJson
	 * @return
	 */
	public static String httpsPost(String url, String stringJson) {
		return httpsPost(url, stringJson, null, "utf-8");
	}

	/**
	 * 发送 https post 请求，参数以josn字符串进行提交
	 * 
	 * @param url
	 * @param stringJson
	 * @param headers
	 * @return
	 */
	public static String httpsPost(String url, String stringJson,
			Map<String, String> headers) {
		return httpsPost(url, stringJson, headers, "utf-8");
	}

	/**
	 * 发送 https post 请求，参数以josn字符串进行提交
	 * 
	 * @param url
	 * @param stringJson
	 * @param headers
	 * @param encode
	 * @return
	 */
	public static String httpsPost(String url, String stringJson,
			Map<String, String> headers, String encode) {
		String respStr = "";
		if (encode == null) {
			encode = "utf-8";
		}
		CloseableHttpClient closeableHttpClient = createSSLClient();
		HttpPost httpost = new HttpPost(url);

		// 设置header
		httpost.setHeader("Content-type", "application/json");
		httpost.setHeader("Accept", "application/json");
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpost.setHeader(entry.getKey(), entry.getValue());
			}
		}

		// 组织请求参数
		StringEntity stringEntity = new StringEntity(stringJson, encode);
		httpost.setEntity(stringEntity);

		CloseableHttpResponse httpResponse = null;
		try {
			// 响应信息
			httpResponse = closeableHttpClient.execute(httpost);
			HttpEntity entity = httpResponse.getEntity();
			respStr = EntityUtils.toString(entity, encode);
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			try {
				httpResponse.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
			try {
				// 关闭连接、释放资源
				closeableHttpClient.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
		return respStr;
	}

	/**
	 * 发送 https put 请求，参数以原生字符串进行提交
	 * 
	 * @param url
	 * @param stringJson
	 * @return
	 */
	public static String httpsPut(String url, String stringJson) {
		return httpsPut(url, stringJson, null, "utf-8");
	}

	/**
	 * 发送 https put 请求，参数以原生字符串进行提交
	 * 
	 * @param url
	 * @param stringJson
	 * @param headers
	 * @return
	 */
	public static String httpsPut(String url, String stringJson,
			Map<String, String> headers) {
		return httpsPut(url, stringJson, headers, "utf-8");
	}

	/**
	 * 发送 https put 请求，参数以原生字符串进行提交
	 * 
	 * @param url
	 * @param stringJson
	 * @param headers
	 * @param encode
	 * @return
	 */
	public static String httpsPut(String url, String stringJson,
			Map<String, String> headers, String encode) {
		String respStr = "";
		if (encode == null) {
			encode = "utf-8";
		}
		CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
		HttpPut httpput = new HttpPut(url);

		// 设置header
		httpput.setHeader("Content-type", "application/json");
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpput.setHeader(entry.getKey(), entry.getValue());
			}
		}
		// 组织请求参数
		StringEntity stringEntity = new StringEntity(stringJson, encode);
		httpput.setEntity(stringEntity);
		CloseableHttpResponse httpResponse = null;
		try {
			// 响应信息
			httpResponse = closeableHttpClient.execute(httpput);
			HttpEntity entity = httpResponse.getEntity();
			respStr = EntityUtils.toString(entity, encode);
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			try {
				httpResponse.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
		try {
			// 关闭连接、释放资源
			closeableHttpClient.close();
		} catch (IOException e) {
			logger.error(e.toString());
		}
		return respStr;
	}

	/**
	 * 发送https delete请求
	 * 
	 * @param url
	 * @param headers
	 * @return
	 */
	public static String httpsDelete(String url, String stringJson,
			Map<String, String> headers) {
		return httpsDelete(url, stringJson, headers, "utf-8");
	}

	/**
	 * 发送https delete请求
	 * 
	 * @param url
	 * @param headers
	 * @param encode
	 * @return
	 */
	public static String httpsDelete(String url, String stringJson,
			Map<String, String> headers, String encode) {
		String respStr = "";
		if (encode == null) {
			encode = "utf-8";
		}
		CloseableHttpClient closeableHttpClient = createSSLClient();
		HttpDeleteWithBody httpdelete = new HttpDeleteWithBody(url);
		// 设置header
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpdelete.setHeader(entry.getKey(), entry.getValue());
			}
		}
		httpdelete.setHeader("Content-type", "application/json");
		// 组织请求参数
		StringEntity stringEntity = new StringEntity(stringJson, encode);
		httpdelete.setEntity(stringEntity);

		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = closeableHttpClient.execute(httpdelete);
			HttpEntity entity = httpResponse.getEntity();
			respStr = EntityUtils.toString(entity, encode);
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			try {
				httpResponse.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
			try {
				// 关闭连接、释放资源
				closeableHttpClient.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}

		return respStr;
	}

	/**
	 * 发送 https post 请求，支持文件上传
	 * 
	 * @param url
	 * @param params
	 * @param files
	 * @param headers
	 * @return
	 */
	public static String httpsPostMultipartFileUpload(String url,
			Map<String, String> params, List<File> files,
			Map<String, String> headers) {
		return httpsPostMultipartFileUpload(url, params, files, headers,
				"utf-8");
	}

	/**
	 * 发送 https post 请求，支持文件上传
	 * 
	 * @param url
	 * @param params
	 * @param files
	 * @param headers
	 * @param encode
	 * @return
	 */
	public static String httpsPostMultipartFileUpload(String url,
			Map<String, String> params, List<File> files,
			Map<String, String> headers, String encode) {
		String respStr = "";
		if (encode == null) {
			encode = "utf-8";
		}
		CloseableHttpClient closeableHttpClient = createSSLClient();
		HttpPost httpost = new HttpPost(url);

		// 设置header
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpost.setHeader(entry.getKey(), entry.getValue());
			}
		}
		MultipartEntityBuilder mEntityBuilder = MultipartEntityBuilder.create();
		mEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		mEntityBuilder.setCharset(Charset.forName(encode));

		// 普通参数
		ContentType contentType = ContentType.create("text/plain",
				Charset.forName(encode));// 解决中文乱码
		if (params != null && params.size() > 0) {
			Set<String> keySet = params.keySet();
			for (String key : keySet) {
				mEntityBuilder.addTextBody(key, params.get(key), contentType);
			}
		}
		// 二进制参数
		if (files != null && files.size() > 0) {
			for (File file : files) {
				mEntityBuilder.addBinaryBody("file", file);
			}
		}
		httpost.setEntity(mEntityBuilder.build());
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = closeableHttpClient.execute(httpost);
			HttpEntity entity = httpResponse.getEntity();
			respStr = EntityUtils.toString(entity, encode);
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			try {
				httpResponse.close();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
		try {
			// 关闭连接、释放资源
			closeableHttpClient.close();
		} catch (IOException e) {
			logger.error(e.toString());
		}
		return respStr;
	}

	public static void main(String[] args) {
		// String filePath = "%2Fvar%2Fdfes%2Fdfes_fserv.bak%2F";
		// filePath = filePath.replace("%2F", "/");
		// System.out.println(filePath);
		
		String rootPath = "/home/d5000/test";
		String filePath = "/home/d5000/test/var/huawei/pzwj/xxx.cfg";
		
		
		System.out.println(filePath.substring(rootPath.length()));
		
		String d5000Home = System.getenv("D5000_HOME");
		System.out.println(d5000Home.length());
		System.out.println(d5000Home);
	}

}

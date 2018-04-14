package com.flow.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.*;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({ "unused" })
public class HC {
	private final static Logger logger = LoggerFactory.getLogger(HC.class);
	private static CloseableHttpClient httpclient = null;
	private static IdleConnectionMonitorThread scanThread = null;
	private final static int socketTimeout = 300;
	private final static int connectTimeout = 300;
	private final static int connectionRequestTimeout = 200;
	private final static int maxTotal = 100;
	private final static int maxPerRoute = 100;

	private HC() {
		init();
	}

	private static final class HttpClient4UtilsInstanceHolder {
		private static HC instance = new HC();
	}

	public static HC getInstance() {
		return HttpClient4UtilsInstanceHolder.instance;
	}

	private void init() {
		try {
			SSLContext sslContext = SSLContexts.createSystemDefault();
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", new SSLConnectionSocketFactory(sslContext)).build();
			PoolingHttpClientConnectionManager poolManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			httpclient = HttpClients.custom().setConnectionManager(poolManager).build();
			SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
			poolManager.setDefaultSocketConfig(socketConfig);
			MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(200).setMaxLineLength(2000).build();
			ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE).setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).setMessageConstraints(messageConstraints).build();
			poolManager.setDefaultConnectionConfig(connectionConfig);
			poolManager.setMaxTotal(maxTotal);
			poolManager.setDefaultMaxPerRoute(maxPerRoute);
			scanThread = new IdleConnectionMonitorThread(poolManager);
			scanThread.start();
			logger.info("httpclint pool init ok---------");
		} catch (Exception e) {
			logger.error("httpclient init error:", e);
		}
	}

	/**
	 * 关闭连接池.
	 */
	@PreDestroy
	public void close() {
		if (httpclient != null) {
			try {
				httpclient.close();
			} catch (IOException ignored) {
			}
		}
		if (scanThread != null) {
			scanThread.shutdown();
		}
		logger.info("httpclint pool close ok---------");
	}

	private static HttpPost constructPost(String url, List<NameValuePair> paramList, int socketTimeout, int connectionRequestTimeout) {
		if (logger.isDebugEnabled())
			logger.debug("httpPost invoke url:" + url + " , params:" + paramList);

		HttpPost post = new HttpPost(url);
		post.setEntity(new UrlEncodedFormEntity(paramList, Consts.UTF_8));

		post.setConfig(RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectionRequestTimeout).setExpectContinueEnabled(false).build());

		return post;
	}

	private static String executePost(HttpPost post, String encoding) throws IOException {
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		try {

			if (logger.isDebugEnabled())
				logger.debug("httpPost invoke uri:{} --^_^-- postBody:{}, --^_^-- queryString:{} ", post.getURI(), post.getEntity().toString(), post.getRequestLine().getUri());
			response = httpclient.execute(post);
			entity = response.getEntity();
			if (entity != null) {
				String str = EntityUtils.toString(entity, encoding);
				if (logger.isDebugEnabled())
					logger.debug("httpPost response uri:{} --^_^-- response:{}, --^_^-- ", post.getURI(), str);
				return str;
			}
		} finally {
			if (entity != null) {
				entity.getContent().close();
			}
			if (response != null) {
				response.close();
			}
			post.releaseConnection();
		}
		return "";
	}

	private static String httpPost(String url, int socketTimeout, int connectionRequestTimeout, List<NameValuePair> paramList, Header header, String encoding) throws IOException {

		HttpPost post = constructPost(url, paramList, socketTimeout, connectionRequestTimeout);
		if (null != header) {
			post.setHeader(header);
		}
		return executePost(post, encoding);
	}

	private String httpPost(String url, int socketTimeout, int connectionRequestTimeout, List<NameValuePair> paramList, List<Header> headers, String encoding) throws IOException {
		HttpPost post = constructPost(url, paramList, socketTimeout, connectionRequestTimeout);
		if (null != headers) {
			Header[] headerArray = headers.toArray(new Header[headers.size()]);
			post.setHeaders(headerArray);
		}
		return executePost(post, encoding);
	}

	private String httpPost(String url, int socketTimeout, int connectionRequestTimeout, List<NameValuePair> paramList, Map<String, String> headerMap, String encoding) throws IOException {
		HttpPost post = constructPost(url, paramList, socketTimeout, connectionRequestTimeout);
		if (null != headerMap) {
			for (Map.Entry<String, String> stringEntry : headerMap.entrySet()) {
				post.setHeader(stringEntry.getKey(), stringEntry.getValue());
			}
		}
		post.setHeader("Content-type", "application/json");
		return executePost(post, encoding);
	}

	public static String httpPost(String url, int socketTimeout, int connectionRequestTimeout, List<NameValuePair> paramList, String encoding) throws IOException {
		return httpPost(url, socketTimeout, connectionRequestTimeout, paramList, (Header) null, encoding);
	}

	public String httpPost(String url, List<NameValuePair> paramList, String encoding) throws IOException {
		return httpPost(url, socketTimeout, connectionRequestTimeout, paramList, encoding);
	}

	public static String httpPost(String url, List<NameValuePair> paramList) throws IOException {
		return httpPost(url, socketTimeout, connectionRequestTimeout, paramList, Consts.UTF_8.name());
	}

	public String httpPost(String url, List<NameValuePair> paramList, Header header) throws IOException {
		return httpPost(url, socketTimeout, connectionRequestTimeout, paramList, header, Consts.UTF_8.name());
	}

	public String httpPost(String url, List<NameValuePair> paramList, Map<String, String> headerMap) throws IOException {
		return httpPost(url, socketTimeout, connectionRequestTimeout, paramList, headerMap, Consts.UTF_8.name());
	}

	public static String postResponse(String url, List<NameValuePair> params, Charsets charset, Header oHeader) throws IOException {
		Header header = null;
		if (null != oHeader) {
			header = new BasicHeader(oHeader.getName(), oHeader.getValue());
		}
		return httpPost(url, socketTimeout, connectionRequestTimeout, params, header, charset.encoding);
	}

	public static String postResponse(String url, Map<String, String> paramMap) throws IOException {
		return postResponse(url, paramMap, Charsets.UTF8, null);
	}

	public static String postResponse(String url, Map<String, String> paramMap, Charsets charset, Header oHeader) throws IOException {
		Header header = null;
		if (null != oHeader) {
			header = new BasicHeader(oHeader.getName(), oHeader.getValue());
		}
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (null != paramMap) {
			for (Map.Entry<String, String> entry : paramMap.entrySet()) {
				if (StringUtils.isNotEmpty(entry.getKey())) {
					params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
			}
		}
		return httpPost(url, socketTimeout, connectionRequestTimeout, params, header, charset.encoding);
	}

	private static String httpGet(String url, List<NameValuePair> paramList, int socketTimeout, int connectionRequestTimeout, String encode) throws IOException {
		String responseString = null;
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectionRequestTimeout).build();
		StringBuilder sb = new StringBuilder();
		sb.append(url);
		int i = 0;
		for (NameValuePair nameValuePair : paramList) {
			if (i == 0 && !url.contains("?")) {
				sb.append("?");
			} else {
				sb.append("&");
			}
			sb.append(nameValuePair.getName());
			sb.append("=");
			String value = nameValuePair.getValue();
			try {
				sb.append(URLEncoder.encode(value, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.warn("encode http get params error, value is " + value, e);
				sb.append(URLEncoder.encode(value));
			}
			i++;
		}

		logger.info("invokeGet begin invoke:" + sb.toString());
		HttpGet get = new HttpGet(sb.toString());
		get.setConfig(requestConfig);
		try {
			/*
			 * if (null != headers && headers.length > 0) {
			 * get.setHeaders(headers); }
			 */
			response = httpclient.execute(get);
			entity = response.getEntity();
			if (entity != null) {
				responseString = EntityUtils.toString(entity, encode);
				return responseString;
			}
		} finally {
			if (entity != null) {
				entity.getContent().close();
			}
			if (response != null) {
				response.close();
			}
			get.releaseConnection();
		}
		return responseString;
	}

	private String httpGet(String url, List<NameValuePair> paramList) throws IOException {
		return httpGet(url, paramList, socketTimeout, connectionRequestTimeout, Consts.UTF_8.toString());
	}

	private String httpGet(String url, List<NameValuePair> paramList, int socketTimeout, int connectionRequestTimeout) throws IOException {
		return httpGet(url, paramList, socketTimeout, connectionRequestTimeout, Consts.UTF_8.toString());
	}

	public String getResponse(String url) throws IOException {
		return getResponse(url, null, Charsets.UTF8);
	}

	public String getResponse(String url, NameValuePair[] arrayParams, Charsets charsets) throws IOException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (null != arrayParams) {
			Collections.addAll(params, arrayParams);
		}
		return httpGet(url, params, socketTimeout, connectionRequestTimeout, charsets.encoding);
	}

	/**
	 * HTTPS请求，默认超时为5S
	 *
	 * @param reqURL
	 * @param params
	 * @return
	 */

	public String connectPostHttps(String reqURL, Map<String, String> params) {
		String responseContent = null;
		HttpPost httpPost = new HttpPost(reqURL);
		try {
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(connectTimeout).setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectTimeout).build();

			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			httpPost.setEntity(new UrlEncodedFormEntity(formParams, Consts.UTF_8));
			httpPost.setConfig(requestConfig);
			// 绑定到请求 Entry
			for (Map.Entry<String, String> entry : params.entrySet()) {

				formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}

			CloseableHttpResponse response = httpclient.execute(httpPost);
			// noinspection Duplicates
			try {
				// 执行POST请求
				HttpEntity entity = response.getEntity(); // 获取响应实体
				try {
					if (null != entity) {
						responseContent = EntityUtils.toString(entity, Consts.UTF_8);
					}
				} finally {
					if (entity != null) {
						entity.getContent().close();
					}
				}
			} finally {
				if (response != null) {
					response.close();
				}
			}
			logger.info("requestURI : " + httpPost.getURI() + ", responseContent: " + responseContent);
		} catch (ClientProtocolException e) {
			logger.error("ClientProtocolException", e);
		} catch (IOException e) {
			logger.error("IOException", e);
		} finally {
			httpPost.releaseConnection();
		}
		return responseContent;

	}

	static class IdleConnectionMonitorThread extends Thread {
		private final HttpClientConnectionManager connMgr;
		private volatile boolean shutdown;

		IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
			super();
			this.connMgr = connMgr;
		}

		@Override
		public void run() {
			while (!shutdown) {
				synchronized (scanThread) {
					try {
						connMgr.closeExpiredConnections();
						connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
						scanThread.wait(5000);
					} catch (Exception e) {
						logger.error("IdleConnectionMonitorThread", e);
					}
				}
			}
		}

		void shutdown() {
			synchronized (scanThread) {
				shutdown = true;
				scanThread.notifyAll();
			}
		}

	}

	public static String nameValuePairArraytoString(NameValuePair[] array) {
		if (null == array) {
			return null;
		}
		if (array.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (NameValuePair a : array) {
			sb.append(a.getName()).append("=").append(a.getValue());
			sb.append("&");
		}
		String result = sb.toString();
		return result.substring(0, result.length() - 1);
	}
}

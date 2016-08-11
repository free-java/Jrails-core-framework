package net.rails.support.worker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Call HTTP/HTTPS worker.
 * Sample:
 * WebClientWorker client = new WebClientWorker("https://www.google.com");
 * int code = client.doGet();
 * System.out.println(client.getResponseText());
 * @author Jack
 *
 */
public class WebClientWorker {

	protected boolean isHttps = false;
	protected HttpURLConnection conn;
	protected String method = "POST";
	protected String url;
	protected String qs;

	/**
	 * @param url 请求网址
	 */
	public WebClientWorker(String url) {
		this.url = url;
	}
	
	/**
	 * @param url is request remote link,sample: http://www.domain.com/path.
	 * @param qs is query string,sample: name=Jack&addr=hongkong
	 */
	public WebClientWorker(String url, String qs) {
		this.url = url;
		this.qs = qs;
	}
	
	/**
	 * Use GET method rquest server.
	 * @throws Exception
	 */
	public int get() throws Exception{
		setMethod("GET");
		return connect();
	}
	
	/**
	 * Use POST method request server.
	 * @throws Exception
	 */
	public int post() throws Exception{
		setMethod("POST");
		return connect();
	}

	/**
	 * 连接请求，须指定请求方法GET或者POSt。
	 * @throws Exception
	 */
	public int connect() throws Exception {
		URL connUrl = new URL(url);
		isHttps = connUrl.toString().indexOf("https://") == 0 ? true : false;
		if (isHttps) {
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null,
					new TrustManager[] { new TrustAnyTrustManager() },
					new SecureRandom());
			HttpsURLConnection httpsConn = null;
			if (method.equals("POST")) {
				conn = (HttpURLConnection) connUrl.openConnection();
				initConn();
				httpsConn = (HttpsURLConnection) conn;
				httpsConn.setRequestMethod(method);
				httpsConn.setSSLSocketFactory(context.getSocketFactory());
				httpsConn.setHostnameVerifier(new TrustAnyHostnameVerifier());
				httpsConn.setDoInput(true);
				httpsConn.setDoOutput(true);
				httpsConn.setUseCaches(false);
				if(qs != null){
					httpsConn.getOutputStream().write(qs.getBytes());
					httpsConn.getOutputStream().flush();
					httpsConn.getOutputStream().close();
				}
			} else if (method.equals("GET")) {
				connUrl = new URL(url
						+ (qs == null ? "" : "?" + qs));
				conn = (HttpURLConnection) connUrl.openConnection();
				initConn();
				httpsConn = (HttpsURLConnection) conn;
				httpsConn.setRequestMethod(method);
				httpsConn.setSSLSocketFactory(context.getSocketFactory());
				httpsConn.setHostnameVerifier(new TrustAnyHostnameVerifier());
				httpsConn.setDoInput(true);
				httpsConn.setDoOutput(true);
				httpsConn.setUseCaches(false);
			}
			conn.connect();
		} else {
			if (method.toUpperCase().equals("POST")) {
				conn = (HttpURLConnection) connUrl.openConnection();
				initConn();
				conn.setRequestMethod(method);
				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);
				if(qs != null){
					conn.getOutputStream().write(qs.getBytes());
					conn.getOutputStream().flush();
					conn.getOutputStream().close();
				}
			} else if (method.toUpperCase().equals("GET")) {
				connUrl = new URL(url
						+ (qs == null ? "" : "?" + qs));
				conn = (HttpURLConnection) connUrl.openConnection();
				conn.setRequestMethod(method);
				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);
			}			
			conn.connect();
		}
		return conn.getResponseCode();
	}
	
	/**
	 * 获取连接对象。
	 * @return HttpURLConnection
	 */
	public HttpURLConnection getConn(){
		return conn;
	}

	/**
	 * Setting request method GET/POST/PUT/DELETE.
	 * @param method GET/POST/PUT/DELETE.
	 */
	public void setMethod(String method) {
		this.method = method.toUpperCase();
	}
	
	/**
	 * Get request method.
	 * @return GET/POST/PUT/DELETE.
	 */
	public String getMethod(){
		return method;
	}
	
	/**
	 * Get request URL.
	 * @return URL string.
	 */
	public String getUrl(){
		return url;
	}

	/**
	 * Get request content type charset value.
	 * @return default is UTF-8.
	 */
	public String getCharset() {
		String ct = conn.getContentType();
		if (ct != null) {
			Pattern pattern = Pattern
					.compile("(?<=(?i)(charset)=['\"]{0,1})([a-zA-Z0-9-/]+)(?=(['\"]{0,1}))");
			Matcher matcher = pattern.matcher(ct);
			return matcher.find() ? matcher.group().toUpperCase() : "UTF-8";
		} else
			return null;
	}

	/**
	 * Setting query string.
	 * Sample: name=Jack&website=www.jrails.net
	 * @param qs
	 */
	public void setQs(String qs) {
		this.qs = qs;
	}

	/**
	 * Get query string.
	 * @return
	 */
	public String getQs() {
		return qs;
	}

	/**
	 * Get response bytes
	 * @return byte[]
	 * @throws IOException
	 */
	public byte[] getResponseBytes() throws IOException {		
		InputStream input = null;
		ByteArrayOutputStream out = null;
		try {
			input = conn.getInputStream();
			out = new ByteArrayOutputStream(1024);
			byte[] buf = new byte[1024];
			int len = -1;
			while ((len = input.read(buf)) != -1) {
				out.write(buf, 0, len);
				out.flush();
			}
			return out.toByteArray();
		} finally {
			if (input != null)
				input.close();
			if (out != null)
				out.close();
		}
	}
	
	/**
	 * Get error response bytes
	 * @return byte[]
	 * @throws IOException
	 */
	public byte[] getErrorBytes() throws IOException {		
		InputStream input = null;
		ByteArrayOutputStream out = null;
		try {
			input = conn.getErrorStream();
			out = new ByteArrayOutputStream(1024);
			byte[] buf = new byte[1024];
			int len = -1;
			while ((len = input.read(buf)) != -1) {
				out.write(buf, 0, len);
				out.flush();
			}
			return out.toByteArray();
		} finally {
			if (input != null)
				input.close();
			if (out != null)
				out.close();
		}
	}

	/**
	 * Get response text.
	 * @return
	 * @throws Exception
	 */
	public String getResponseText() throws Exception {
		byte[] data = getResponseBytes();
		if (data != null)
			return new String(data, getCharset());
		else
			return null;
	}
	
	/**
	 * 获取请求失败后返回的文本内容。
	 * @return
	 * @throws Exception
	 */
	public String getErrorText() throws Exception {
		byte[] data = getErrorBytes();
		if (data != null)
			return new String(data, getCharset());
		else
			return null;
	}
	
	/**
	 * 若要控制连接对象请重写这方法。
	 */
	protected void initConn(){
		
	}

}

class TrustAnyHostnameVerifier implements HostnameVerifier {
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}
}

class TrustAnyTrustManager implements X509TrustManager {
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
	}

	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[] {};
	}
}

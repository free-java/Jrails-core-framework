package net.rails.web;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.rails.active_record.ActiveRecord;
import net.rails.ext.AbsGlobal;
import net.rails.ext.IndexMap;
import net.rails.ext.Json;
import net.rails.support.Support;
import net.rails.support.worker.TokenWorker;
import net.rails.support.worker.UserAgentWorker;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 控制器的基类,所有控制器必须继承这类，并必须在app.controller包下面。
 * @author Jack
 *
 */
@SuppressWarnings("unchecked")
public abstract class Controller {

	private String name;
	protected FilterConfig config;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected String serverPath;
	protected String contextPath;
	protected String action;
	protected UserAgentWorker userAgent;
	protected boolean isPost;
	protected boolean ajax = false;
	protected HttpSession session;
	protected Route route;
	protected ServletFileUpload upload;
	protected abstract AbsGlobal getGlobal();
	protected final Map<String, String> headers = new IndexMap<String, String>();
	protected final Map<String, String> cookies = new IndexMap<String, String>();
	protected final Map<String, Object> params = new IndexMap<String, Object>();
	protected final Map<String, Object> queies = new IndexMap<String, Object>();
	
	protected Logger log;

	public Controller(FilterConfig config, HttpServletRequest request,
			HttpServletResponse response, Route route) throws Exception {
		super();
		log = LoggerFactory.getLogger(getClass());
		this.config = config;
		this.request = request;
		this.response = response;
		this.session = request.getSession();
		this.route = route;
		userAgent = new UserAgentWorker(request.getHeader("user-agent"));
		ajax = request.getHeader("X-Requested-With") != null;
		final Enumeration<String> ens = request.getHeaderNames();
		Map<String, Object> qs = QueryString.parse(request.getQueryString());
		if (qs != null)
			queies.putAll(qs);

		String en = null;
		if (ens != null) {
			while ((en = ens.nextElement()) != null) {
				headers.put(en, request.getHeader(en));
			}
		}
		Cookie[] cks = request.getCookies();
		if (cks != null) {
			for (Cookie ck : cks) {
				cookies.put(ck.getName(), ck.getValue());
			}
		}
		if (route.getParams() != null)
			params.putAll(route.getParams());

		if (request.getAttribute(Route.ROUTE_PARAMS) != null)
			params.putAll((Map<String, Object>) request
					.getAttribute(Route.ROUTE_PARAMS));

		serverPath = request.getServletPath();
//		contextPath = config.getServletContext().getContextPath();
		contextPath = request.getContextPath();
		name = route.getController();
		action = route.getAction();
		isPost = this.request.getMethod().toUpperCase().equals("POST") ? true
				: false;

		if (ServletFileUpload.isMultipartContent(request)) {
			upload = new ServletFileUpload();
			parseDataParams();
		} else {
			parseParams(request.getParameterMap());
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Headers : " + headers);
			log.debug("UserAgent : " + headers.get("user-agent"));
			log.debug("Cookies : " + cookies);
			log.debug("Controller : " + route.getController());
			log.debug("Action : " + route.getAction());
			log.debug("Method : " + request.getMethod());
			log.debug("Ajax : " +  ajax);
			log.debug("Params : " + params);
			log.debug("Queies : " + queies);
		}
	}

	/** 以下是私有方法 **/

	private void parseFormField(String fieldName, FileItemStream item)
			throws IOException {
		InputStream stream = item.openStream();
		String value = Streams.asString(stream, AbsGlobal.getApplicationCharset());
		Matcher m = Pattern.compile("\\[\\]$").matcher(fieldName);
		boolean keyarr = m.find();
		String key = null;
		if (keyarr)
			key = m.replaceFirst("");
		else
			key = fieldName;
		if (params.containsKey(key)) {
			Object obj = params.get(key);
			List<String> list = null;
			if (obj instanceof List) {
				list = (List<String>) obj;
			} else {
				list = new ArrayList<String>();
				list.add((String) obj);
			}
			list.add(value);
			params.put(key, list);
		} else {
			if (keyarr) {
				List<String> list = new ArrayList<String>();
				list.add((String) value);
				params.put(key, list);
			} else
				params.put(key, value);
		}
	}

	private void parseFileField(String fieldName, String fileName,
			FileItemStream item) throws IOException {
		InputStream value = item.openStream();
		Matcher m = Pattern.compile("\\[\\]$").matcher(fieldName);
		boolean keyarr = m.find();
		String key = null;
		if (keyarr)
			key = m.replaceFirst("");
		else
			key = fieldName;
		if (params.containsKey(key)) {
			Object obj = params.get(key);
			List<ClientFile> list = null;
			if (obj instanceof List) {
				list = (List<ClientFile>) obj;
			} else {
				list = new ArrayList<ClientFile>();
				list.add((ClientFile) obj);
			}
			list.add(new ClientFile(fileName, value));
			params.put(key, list);
		} else {
			if (keyarr) {
				List<ClientFile> list = new ArrayList<ClientFile>();
				if (fileName != null && !fileName.equals(""))
					list.add(new ClientFile(fileName, value));
				else
					list.add(null);

				params.put(key, list);
			} else {
				if (fileName != null && !fileName.equals(""))
					params.put(key, new ClientFile(fileName, value));
				else
					params.put(key, null);
			}
		}
	}
	
	private void parseParams(Map<String, String[]> paramsMap)
			throws UnsupportedEncodingException {
		final List<String> keys = Support.map(paramsMap).keys();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			Matcher m = Pattern.compile("\\[\\]$").matcher(key);
			boolean keyarr = m.find();
			if (keyarr)
				params.put(m.replaceFirst(""),
						parseStringArray(keyarr, paramsMap.get(key)));
			else
				params.put(key, parseStringArray(keyarr, paramsMap.get(key)));
		}
	}

	/** 以下是受保护方法 **/

	/**
	 * 重写此方法可实现控制上传文件大小和文件类型限制
	 * 
	 * @throws Exception
	 */
	protected void parseDataParams() throws Exception {
		final FileItemIterator iter = upload.getItemIterator(request);
		upload.setHeaderEncoding(request.getCharacterEncoding());
		while (iter.hasNext()) {
			FileItemStream item = iter.next();
			String fieldName = item.getFieldName();
			if (item.isFormField()) {
				parseFormField(fieldName, item);
			} else {
				parseFileField(fieldName, item.getName(), item);
			}
		}
	}
	
	/**
	 * 重写此方法可实现对请求参数重新编码。
	 * @param param
	 * @return
	 */
	protected String encoding(String param) {
		if (isPost){
			return param;
		}else{
			try {
				return new String(param.getBytes(), AbsGlobal.getApplicationCharset());
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage(), e);
			}
			return param;
		}
	}


	/** 以下是公用方法 **/

	/**
	 * 获取请求路由。
	 * @return
	 */
	public Route getRoute() {
		return route;
	}

	/**
	 * 获取客户端浏览器UserAgent字符串。
	 * @return
	 */
	public UserAgentWorker getUserAgent() {
		return userAgent;
	}
		
	/**
	 * 下载文件。
	 * @param is
	 * @param filename 重新指定文件名称
	 * @throws IOException
	 */
	public void file(InputStream is, String filename) throws IOException {
		if (route.isActive()) {
			route.setActive(false);
			String charset = Support.config().env().getApplicationCharset();
			filename = URLEncoder.encode(filename, charset);
			filename = new String(filename.getBytes(charset), "ISO-8859-1");
			TokenWorker token = userAgent.browser();
			String disp = "attachment; filename*=\"" + filename + "\"";
			if (token.getGroup() == null) {
				disp = "attachment; filename*=\"" + filename + "\"";
			} else if (token.getGroup().equals("MSIE")) {
				disp = "attachment; filename=\"" + filename + "\"";
			} else if (token.getGroup().equals("Chrome")) {
				disp = "attachment; filename=\"" + filename + "\"";
			} else {
				disp = "attachment; filename=*\"" + filename + "\"";
			}
			response.addHeader("Content-Disposition", disp);
			response.setContentType("application/x-msdownload");
			BufferedOutputStream bos = null;
			bos = new BufferedOutputStream(response.getOutputStream());
			byte[] buff = new byte[1024];
			int bytesRead;
			while (-1 != (bytesRead = is.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
			is.close();
			bos.close();
		}
	}

	/**
	 * 下载文件。
	 * @param data 文件的Bytes形式
	 * @param filename 重新指定文件名称
	 * @throws IOException
	 */
	public void file(byte[] data, String filename) throws IOException {
		file(new ByteArrayInputStream(data), filename);
	}

	/**
	 * 下载文件。
	 * @param file
	 * @param filename 重新指定文件名称
	 * @throws IOException
	 */
	public void file(File file, String filename) throws IOException {
		file(FileUtils.readFileToByteArray(file), filename);
	}

	/**
	 * 下载文件。
	 * @param file
	 * @throws IOException
	 */
	public void file(File file) throws IOException {
		file(file, file.getName());
	}

	/**
	 * 跳转到另一个路由(同一请求,浏览器URL不会发生变化)。
	 * @param route
	 * @throws IOException
	 * @throws ServletException
	 */
	public void forwardRoute(String route) throws IOException, ServletException {
		Route r = new Route(request, route);
		forward(r.getController(), r.getAction());
	}

	/**
	 * 跳转到另一个路由(新的请求,浏览器URL会发生变化)。
	 * @param route
	 * @param qs
	 * @throws IOException
	 */
	public void redirectRoute(String route, QueryString qs) throws IOException {
		Route r = new Route(request, route);
		QueryString q = new QueryString();
		if (r.getParams() != null)
			q.putAll(r.getParams());
		if (qs != null)
			q.putAll(qs);
		redirect(r.getController(), r.getAction(), q);
	}

	/**
	 * 渲染到页面(对应view/controName/action.jsp)。
	 * @throws IOException
	 * @throws ServletException
	 */
	public void render() throws IOException, ServletException {
		render(name, action);
	}

	/**
	 * 渲染到页面(对应view/path/action.jsp)。
	 * @param path
	 * @param action
	 * @throws IOException
	 * @throws ServletException
	 */
	public void render(String path, String action) throws IOException,
			ServletException {
		if (route.isActive()) {
			route.setActive(false);
			final String view = MessageFormat.format(
					"/WEB-INF/view/{0}/{1}.jsp", path, action);
			request.getRequestDispatcher(view).forward(request, response);
			return;
		}
	}

	/**
	 * 跳转到另一个Action(同一请求,浏览器URL不会发生变化)。
	 * @param controller
	 * @param action
	 * @throws IOException
	 * @throws ServletException
	 */
	public void forward(String controller, String action) throws IOException,
			ServletException {
		if (route.isActive()) {
			route.setActive(false);
			request.getRequestDispatcher(
					MessageFormat.format("/{0}/{1}/", controller, action))
					.forward(request, response);
			return;
		}
	}

	/**
	 * 跳转到另一个Action(同一请求,浏览器URL不会发生变化)。
	 * @param action
	 * @throws IOException
	 * @throws ServletException
	 */
	public void forward(String action) throws IOException, ServletException {
		forward(name, action);
	}

	/**
	 * 跳转到另一个Action(同一请求,浏览器URL不会发生变化)。
	 * @param controller
	 * @param action
	 * @param qs
	 * @throws IOException
	 */
	public void redirect(String controller, String action, QueryString qs)
			throws IOException {
		String qstr = "";
		if (!Support.object(qs).blank())
			qstr = "?" + qs.toQueryString();

		redirect(MessageFormat.format("{0}/{1}/{2}{3}", contextPath,
				controller, action, qstr));
	}

	/**
	 * 重定向到另一个Action(URL被重写)。
	 * @param action
	 * @param qs
	 * @throws IOException
	 */
	public void redirect(String action, QueryString qs) throws IOException {
		redirect(name, action, qs);
	}

	/**
	 * 重定向到另一个Action(URL被重写)。
	 * @param url
	 * @throws IOException
	 */
	public void redirect(String url) throws IOException {
		if (route.isActive()) {
			route.setActive(false);
			response.sendRedirect(url);
			return;
		}
	}

	/**
	 * 
	 * @param status
	 *            301 or 302
	 * @param url
	 *            target url
	 */
	public void location(int status, String url) {
		response.setStatus(status);
		response.addHeader("Location", url);
		response.addHeader("Connection", "close");
		route.setActive(false);
	}

	/**
	 * document.write("<script>...</script>")
	 * 
	 * @param url
	 * @throws IOException
	 */
	public void location(String url) throws IOException {
		out().write(
				"document.write(\"<script>window.location.href='" + url
						+ "';</script>\");");
	}

	/**
	 * 向客户端输出流。
	 * @return PrintWriter
	 * @throws IOException
	 */
	public PrintWriter out() throws IOException {
		if (route.isActive()) {
			route.setActive(false);
			response.setContentType(MessageFormat.format(
					"text/html; charset={0}", AbsGlobal.getApplicationCharset()));
			return response.getWriter();
		} else
			return null;
	}

	/**
	 * 设置request级别的属性，可以页面获取。
	 * @param key
	 * @param value
	 */
	public void set(String key, Object value) {
		request.setAttribute(key, value);
	}

	/**
	 * 设置request级别的属性，可以页面获取。
	 * @param model
	 */
	public void bind(ActiveRecord model) {
		List<String> att = null;
		att = model.getAttributes();
		String key = new String();
		final Map<String, Object> m = new IndexMap<String, Object>();
		for (Iterator<String> iterator = att.iterator(); iterator.hasNext();) {
			key = iterator.next();
			m.put(key, model.get(key));
		}
		set(model.getClass().getSimpleName(), m);
	}

	/**
	 * 设置request级别的属性，可以页面获取。
	 * @param params
	 */
	public void bind(Map<String, Object> params) {
		final List<String> att = Support.map(params).keys();
		String key;
		Map<String, Object> m = new IndexMap<String, Object>();
		for (Iterator<String> iterator = att.iterator(); iterator.hasNext();) {
			String val = iterator.next();
			String[] temp = val.split("\\.");
			key = temp[0];
			if (temp.length == 1)
				set(key, params.get(val));
			else {
				m.put(temp[1], params.get(val));
				set(key, m);
			}
		}
	}

	public boolean isAjax() {
		return ajax;
	}

	public FilterConfig getConfig() {
		return config;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public String getName() {
		return name;
	}

	public Map<String, Object> getParams() {
		return params;
	}

//	public Object getValue(String name, Object def) {
//		return Support.map(params).get(name, def);
//	}
//
//	public Number getNumber(String name, Number def) {
//		Object v = getValue(name, def);
//		if (v == null)
//			return null;
//		else if (v instanceof String) {
//			try {
//				return new DecimalFormat().parse(v.toString());
//			} catch (ParseException e) {
//				return def;
//			}
//		}
//		return (Number) v;
//	}

	public Map<String, Object> getQueies() {
		return queies;
	}

	public Map<String, Object> form(String name, Map<String, Object> params) {
		final List<String> paramKeys = Support.map(params).keys();
		final Map<String, Object> m = new IndexMap<String, Object>();
		for (Iterator<String> iterator = paramKeys.iterator(); iterator
				.hasNext();) {
			String key = iterator.next();
			String regex = MessageFormat.format("^({0}\\[\\w+\\])", name);
			Pattern p = Pattern.compile(regex);
			Matcher mat = p.matcher(key);
			if (mat.find()) {
				p = Pattern.compile("\\[\\w+\\]");
				mat = p.matcher(key);
				if (mat.find())
					m.put(mat.group().replaceFirst("\\[", "")
							.replaceFirst("\\]", ""), params.get(key));
			}
		}
		return m;
	}

	public Map<String, ? extends Object> form(String name) {
		return form(name, params);
	}

	private Object parseStringArray(boolean keyarr, String[] values) {
		if (keyarr || values.length > 1) {
			List<String> arr = new ArrayList<String>();
			for (int i = 0; i < values.length; i++) {
				arr.add(encoding(values[i]));
			}
			return arr;
		} else {
			return encoding(values[0]);
		}
	}
	
	public List<Object> parseArray(String name,String def){
		Object v = params.get(name);
		if(v instanceof List)
			return (List<Object>)v;
		else
			return parseJsonArray(name,def);
	}
	
	public List<Object> parseArray(String name){
		Object v = params.get(name);
		if(v == null)
			return null;
		if(v instanceof List)
			return (List<Object>)v;
		else
			return parseJsonArray(name);
	}
	
	/**
	 * 获取Json Array字符串格式的参数
	 * @param name 参数名称
	 * @return
	 */
	public List<Object> parseJsonArray(String name){
		return (List<Object>)Json.parse(parseString(name));
	}
	
	/**
	 * 获取Json Array字符串格式的参数
	 * @param name 参数名称
	 * @param def 默认值
	 * @return
	 */
	public List<Object> parseJsonArray(String name,String def){
		return (List<Object>)Json.parse(parseString(name,def));
	}
	
	/**
	 * 获取Json字符串格式的参数
	 * @param name 参数名称
	 * @return
	 */
	public Map<String,Object> parseJson(String name){
		return (Map<String,Object>)Json.parse(parseString(name));
	}
	
	/**
	 * 获取Json字符串格式的参数
	 * @param name 参数名称
	 * @param def 默认值
	 * @return
	 */
	public Map<String,Object> parseJson(String name,String def){
		return (Map<String,Object>)Json.parse(parseString(name,def));
	}

	/**
	 * 获取请求参数(String类型)。
	 * @param name 参数名称
	 * @param def 默认值
	 * @return
	 */
	public String parseString(String name, String def) {
		return Support.string(parseString(name)).def(def);
	}

	/**
	 * 获取请求参数(String类型)。
	 * @param name 参数名称
	 * @return String
	 */
	public String parseString(String name) {
		return (String) params.get(name);
	}

	/**
	 * 获取请求参数(Number类型)。
	 * @param name 参数名称
	 * @param def 默认值
	 * @return Number
	 * @throws ParseException
	 */
	public Number parseNumber(String name, Number def) throws ParseException {
		DecimalFormat df = new DecimalFormat();
		return df.parse(parseString(name, def.toString()));
	}

	/**
	 *  获取请求参数(Number类型)。
	 * @param name 参数名称
	 * @return Number
	 * @throws ParseException
	 */
	public Number parseNumber(String name) throws ParseException {
		return parseNumber(name, null);
	}

	/**
	 * 获取请求参数(Boolean类型)。
	 * @param name 参数名称
	 * @param def 默认值
	 * @return Boolean
	 */
	public Boolean parseBoolean(String name, Boolean def) {
		return Boolean.parseBoolean(parseString(name));
	}

	/**
	 * 获取请求参数(Boolean类型)。
	 * @param name 参数名称
	 * @return Boolean
	 */
	public Boolean parseBoolean(String name) {
		return parseBoolean(name, null);
	}

	/**
	 * 获取请求参数(java.sql.Timestamp类型)。
	 * @param name 参数名称
	 * @param def 默认值
	 * @return Timestamp
	 * @throws ParseException
	 */
	public Timestamp parseTimestamp(String name, Timestamp def) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(getGlobal().t("formates",
				"datetime"));
		if (Support.string(parseString(name)).blank())
			return def;

		java.util.Date d = df.parse(parseString(name));
		return new Timestamp(d.getTime());
	}

	/**
	 * 获取请求参数(java.sql.Timestamp类型)。
	 * @param name 参数名称
	 * @return Timestamp
	 * @throws ParseException
	 */
	public Timestamp parseTimestamp(String name) throws ParseException {
		return parseTimestamp(name, null);
	}

	/**
	 * 获取请求参数(java.sql.Date类型)。
	 * @param name 参数名称
	 * @param def 默认值
	 * @return Date
	 * @throws ParseException
	 */
	public Date parseDate(String name, Date def) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(getGlobal().t("formates",
				"date"));
		if (Support.string(parseString(name)).blank())
			return def;

		java.util.Date d = df.parse(parseString(name));
		return new Date(d.getTime());
	}

	/**
	 * 获取请求参数(java.sql.Date类型)。
	 * @param name 参数名称
	 * @return Date
	 * @throws ParseException
	 */
	public Date parseDate(String name) throws ParseException {
		return parseDate(name, null);
	}

	/**
	 * 获取请求参数(java.sql.Time类型)。
	 * @param name 参数名称
	 * @param def 默认值
	 * @return Time
	 * @throws ParseException
	 */
	public Time parseTime(String name, Time def) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(getGlobal().t("formates",
				"time"));
		if (Support.string(parseString(name)).blank())
			return def;

		java.util.Date d = df.parse(parseString(name));
		return new Time(d.getTime());
	}

	/**
	 * 获取请求参数(java.sql.Time类型)。
	 * @param name 参数名称
	 * @return Time
	 * @throws ParseException
	 */
	public Time parseTime(String name) throws ParseException {
		return parseTime(name, null);
	}

	/**
	 * 向客户端响应文本内容。
	 * @param text
	 * @throws IOException
	 */
	public void text(String text) throws IOException {
		if (route.isActive()) {
			route.setActive(false);
			response.setContentType(MessageFormat.format(
					"text/html; charset={0}",
					Support.config().env().getApplicationCharset()));
			response.getWriter().write(text);
		}
	}
	
	/**
	 * 向客户端响应文本内容。
	 * @param contentType (e.g. text/css , text/javascript, text/html) 
	 * @param text
	 * @throws IOException
	 */
	public void text(String contentType,String text) throws IOException {
		if (route.isActive()) {
			route.setActive(false);
			response.setContentType(MessageFormat.format(
					"{0}; charset={1}",
					contentType,
					Support.config().env().getApplicationCharset()));
			response.getWriter().write(text);
		}
	}
	
	/**
	 * 向客户端响应Json。
	 * @param text
	 * @throws IOException
	 */
	public void json(Json<String,Object> json) throws IOException {
		if (route.isActive()) {
			route.setActive(false);
			response.setContentType("application/x-json");
			response.getWriter().write(json.toString());
		}
	}

	/**
	 * 向客户端发出一个错误响应代码。
	 * @param code 错误代码
	 * @throws IOException
	 */
	public void sendError(int code) throws IOException {
		if (route.isActive()) {
			log.debug("SendError : " + code);
			route.setActive(false);
			response.sendError(code);
			return;
		}
	}

	/**
	 * 向客户端发出一个错误响应代码和错误内容。
	 * @param code 错误代码
	 * @param text 错误内容
	 * @throws IOException
	 */
	public void sendError(int code, String text) throws IOException {
		if (route.isActive()) {
			log.debug("SendError : " + code + "," + text);
			route.setActive(false);
			response.sendError(code, text);
			return;
		}
	}

}

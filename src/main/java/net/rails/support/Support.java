package net.rails.support;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.rails.ext.AbsGlobal;
import net.rails.ext.Json;
import net.rails.support.worker.ArrayWorker;
import net.rails.support.worker.Base64Worker;
import net.rails.support.worker.CalendarWorker;
import net.rails.support.worker.CodeWorker;
import net.rails.support.worker.ConfigWorker;
import net.rails.support.worker.EnvWorker;
import net.rails.support.worker.InflectWorker;
import net.rails.support.worker.MapWorker;
import net.rails.support.worker.NumberWorker;
import net.rails.support.worker.ObjectWorker;
import net.rails.support.worker.StringWorker;
import net.rails.support.worker.UserAgentWorker;
import net.rails.support.worker.ValidateMessageWorker;
import net.rails.support.worker.WebClientWorker;

/**
 * Support entry.
 * @author Jack
 *
 */
public final class Support {

	public Support(){
		super();
	}
	
	/**
	 * Get a Json object.
	 * @return Json
	 */
	public static Json<String,Object> json(){
		return new Json<String,Object>();
	}

	/**
	 * Get a CodeWorker object.
	 * @return CodeWorker
	 */
	public static CodeWorker code() {
		return new CodeWorker();
	}

	/**
	 * 获取一个InflectWorker(语型变化)实例,用法：String name = Support.inflect("account group").camelcase();  name将等于 AccountGroup 。
	 * @param target 要进行语型转换的名称
	 * @return InflectWorker
	 */
	public static InflectWorker inflect(String target) {
		return new InflectWorker(target);
	}
	
	/**
	 * 获取一个StringWorker(字符串处理实例)，用法：String str = Support.string(text).def('Default String'); 如果text为空白内容将返回 "Default String"。
	 * @param target 输入的字符串
	 * @return
	 */
	public static StringWorker string(String target) {
		return new StringWorker(target);
	}
	
	/**
	 * 获取一个NumberWorker实例，包含一些格式化转换方法。
	 * @param target 接受一个要被处理的数字
	 * @return NumberWorker
	 */
	public static NumberWorker number(Number target) {
		return new NumberWorker(target);
	}
	
	/**
	 * 获取一个NumberWorker实例，包含一些格式化转换方法。
	 * @param pattern 数字格式化字符串
	 * @param source 数字格式的字符串
	 * @return NumberWorker
	 * @throws ParseException 转换失败时抛出异常
	 */
	public static NumberWorker number(String pattern,String source) throws ParseException {
		return new NumberWorker(pattern,source);
	}
	
	/**
	 * 获取一个CalendarWorker实例。
	 * @param millis 时间的毫秒数
	 * @return CalendarWorker
	 */
	public static CalendarWorker calendar(long millis) {
		return new CalendarWorker(millis);
	}
	
	/**
	 * 获取一个CalendarWorker实例。
	 * @param target java.util.Date类型的参数
	 * @return CalendarWorker
	 */
	public static CalendarWorker calendar(Date target) {
		return new CalendarWorker(target.getTime());
	}
	
	/**
	 * 获取一个CalendarWorker实例。
	 * @param pattern 日期格式化字符串
	 * @param source 日期格式的字符串参数
	 * @return CalendarWorker
	 * @throws ParseException 转换失败时抛出的异常
	 */
	public static CalendarWorker calendar(String pattern,String source) throws ParseException {
		return new CalendarWorker(pattern,source);
	}
	
	/**
	 * 获取一个MapWorker。
	 * @param target 目标Map
	 * @return MapWorker
	 */
	public static <K, V> MapWorker<K,V> map(Map<K,V> target) {
		return new MapWorker<K,V>(target);
	}
	
	/**
	 * 获取一个ArrayWorker。
	 * @param target 目标List
	 * @return ArrayWorker
	 */
	public static <T> ArrayWorker<T> array(List<T> target) {
		return new ArrayWorker<T>(target);
	}
	
	/**
	 * 获取一个ObjectWorker，包含def(target,def) 等方法。
	 * @param target 目标对象
	 * @return ObjectWorker
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <O> ObjectWorker object(O target) {
		return new ObjectWorker(target);
	}

	/**
	 * 获取一个ConfigWorker,该类是获取config/*.yml所有配置内容,文件名称(不包含后缀名)将会是一个key。
	 * @return ConfigWorker
	 */
	public static ConfigWorker config(){
		return new ConfigWorker();
	}
	
	/**
	 * 获取验证错误信息。
	 * @return ValidateMessageWorker
	 */
	public static ValidateMessageWorker validateMessage(AbsGlobal g,String of,String model,String attr){
		return new ValidateMessageWorker(g,of,model,attr);
	}
	
	/**
	 * 获取一个Base64Worker，包含了对Base64的所有处理方法。
	 * @return Base64Worker
	 */
	public static Base64Worker base64(){
		return new Base64Worker();
	}
	
	/**
	 * 获取一个EnvWorker实例，该类是读取config/env.yml的配置内容。
	 * @return EnvWorker
	 */
	public static EnvWorker env(){
		return new EnvWorker();
	}
	
	/**
	 * 获取一个WebClientWorker实例，支持http,https单向证书请求。
	 * @param url 目标路径
	 * @return WebClientWorker
	 */
	public static WebClientWorker webClient(String url){
		return new WebClientWorker(url);
	}
	
	/**
	 * 获取一个WebClientWorker实例，支持http,https单向证书请求。
	 * @param url  目标路径
	 * @param qs  查询字符串
	 * @return WebClientWorker
	 */
	public static WebClientWorker webClient(String url,String qs){
		return new WebClientWorker(url,qs);
	}
	
	/**
	 * 获取一个UserAgentWorker实例。
	 * @param ua UserAgent字符串
	 * @return UserAgentWorker
	 */
	public static UserAgentWorker userAgent(String ua){
		return new UserAgentWorker(ua);
	}

}

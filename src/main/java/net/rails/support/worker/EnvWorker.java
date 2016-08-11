package net.rails.support.worker;

import java.util.List;
import java.util.Map;
import net.rails.support.Support;

/**
 * Get project/config/env.yml config.
 * @author Jack
 *
 */
@SuppressWarnings("unchecked")
public final class EnvWorker {
	
	public EnvWorker(){
		super();
	}

	/**
	 * 获取env.yml文件根节点。
	 * @return
	 */
	public Map<String,Object> getRoot(){
		return Support.config().getConfig().get("env");
	}
	
	/**
	 * 获取一个数字类型的值。
	 * @param key
	 * @param def 默认值
	 * @return
	 */
	public Number getNumber(String key,Number def){
		return Support.number(getNumber(key)).def(def);
	}
	
	/**
	 * 获取一个数字类型的值。
	 * @param key
	 * @return
	 */
	public Number getNumber(String key){
		return (Number)get(key);
	}
	
	/**
	 * 获取一个字符串类型的值。
	 * @param key
	 * @param def 默认值
	 * @return
	 */
	public String getString(String key,String def){
		return Support.string(getString(key)).def(def);
	}
	
	/**
	 * 获取一个字符串类型的值。
	 * @param key
	 * @return
	 */
	public String getString(String key){
		return (String)get(key);
	}
	
	/**
	 * 获取一个布尔类型的值。
	 * @param key
	 * @param def 默认值
	 * @return
	 */
	public Boolean getBoolean(String key,Boolean def){
		return (Boolean) Support.object(getBoolean(key)).def(def);
	}
	
	/**
	 * 获取一个布尔类型的值。
	 * @param key
	 * @return
	 */
	public Boolean getBoolean(String key){
		return (Boolean)get(key);
	}
	
	/**
	 * 获取应用的统一编码。
	 * @return 默认是UTF-8
	 */
	public String getApplicationCharset(){
		return getString("application_charset","UTF-8");
	}
	
	/**
	 * 获取通过配置文件设置的J2EE容器请求编码。
	 * @return 默认是UTF-8
	 */
	public String getServerCharset(){
		return getString("server_charset","ISO-8859-1");
	}
	
	/**
	 * 获取语言名称。
	 * @return
	 */
	public String getLocale(){
		return getString("locale","default");
	}
	
	/**
	 * 获取数据库环境名称。
	 * @return 默认是production
	 */
	public String getEnv(){
		return getString("env","production");
	}
	
	/**
	 * 获取表名称前缀
	 * @return
	 */
	public String getPrefix(){
		return getString("prefix","");
	}
	
	public List<Object> getList(String key){
		return (List<Object>)get(key);
	}
	
	public Map<String,Object> getMap(String key){
		return (Map<String,Object>)get(key);
	}
	
	public <T extends Object> T get(String key) {
		return (T) getRoot().get(key);
	}
	
	public <T extends Object> T get(String key,T def) {
		T t = (T) getRoot().get(key);
		if(t == null){
			return def;
		}
		return t;
	}
	
	public Object gets(String keys){
		return Support.map(getRoot()).gets(keys);
	}
	
	public Object gets(String...keyarr){
		return Support.map(getRoot()).gets(keyarr);
	}

}

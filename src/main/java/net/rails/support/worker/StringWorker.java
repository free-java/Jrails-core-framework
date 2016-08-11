package net.rails.support.worker;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工人。
 * @author Jack
 *
 */
public class StringWorker {
	
	public String target;
	
	/**
	 * 构造方法。
	 * @param target 目标内容
	 */
	public StringWorker(String target){
		super();
		this.target = target;
	}

	/**
	 * 检查目标内容是否为NULL。
	 * @return
	 */
	public boolean nil(){
		return target == null;
	}
	
	/**
	 * 检查目标内容是否为空白引号内容或者为NULL。
	 * @return
	 */
	public boolean blank(){
		return nil() || target.trim().equals("");
	}
	
	/**
	 * 首字母大写。
	 * @return
	 */
	public String firstUpCase(){
		if(blank())
			return target;
		
		String s = target.substring(0,1).toUpperCase();
		if(target.length() > 1)
			 s += target.substring(1);
		return s;
	}
	
	/**
	 * 首字母小写。
	 * @return
	 */
	public String firstLowerCase(){
		if(blank())
			return target;
		
		String s = target.substring(0,1).toLowerCase();
		if(target.length() > 1)
			 s += target.substring(1);
		return s;
	}
	
	/**
	 * 尾字母大写。
	 * @return
	 */
	public String lastUpCase(){
		if(blank())
			return target;
		
		String s = "";
		if(target.length() > 1){
			s = target.substring(0,target.length() - 1);
			s += target.substring(target.length() - 1).toUpperCase();
		}else{
			s = target.toUpperCase();
		}
		return s;
	}
	
	/**
	 * 尾字母小写。
	 * @return
	 */
	public String lastLowerCase(){
		if(blank())
			return target;
		
		String s = "";
		if(target.length() > 1){
			s = target.substring(0,target.length() - 1);
			s += target.substring(target.length() - 1).toLowerCase();
		}else{
			s = target.toUpperCase();
		}
		return s;
	}
	
	/**
	 * 去掉最后一字符。
	 * @return
	 */
	public String chop(){
		if(blank())
			return target;
		
		return target.substring(0,target.length() - 1);		
	}
	
	/**
	 * 当目标为空白绰号时返回默认值。
	 * @param def 默认值
	 * @return
	 */
	public String def(String def){
		if(blank())
			return def;
		else
			return target;
	}
	
	/**
	 * 检查内容是否符合指定的日期格式
	 * @param dateFormat 例: yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public boolean isDateFormat(String dateFormat){
		String f = dateFormat + "";
		Map<String, String> fs = new HashMap<String,String>();
		fs.put("y", "[0-9]{1}");
		fs.put("M", "[0-9]{1}");
		fs.put("MM", "([0-9]{1}|(0\\d){1}|10|11|12){1}");
		fs.put("d", "[0-9]{1}");
		fs.put("dd", "([1-9]{1}|(0\\d){1}|(1\\d){1}|(2[0-9]){1}|30|31){1}");
		fs.put("a", "(AM|am|PM|pm){1}");
		fs.put("H", "[0-9]{1}");
		fs.put("HH", "([0-9]{1}|(0\\d){1}|(1\\d){1}|(2[0-3])){1}");
		fs.put("k", "[0-9]{1}");
		fs.put("kk", "([1-9]{1}|(0\\d){1}|(1\\d){1}|(2[0-4])){1}");
		fs.put("K", "[0-9]{1}");
		fs.put("KK", "([0-9]{1}|(0\\d){1}|(10)|(11)){1}");
		fs.put("h", "[0-9]{1}");
		fs.put("hh", "([1-9]{1}|(0\\d){1}|(10)|(11)|(12)){1}");
		fs.put("m", "[0-9]{1}");
		fs.put("mm", "([0-9]{1}|(0\\d){1}|(1\\d){1}|(2\\d){1}|(3\\d){1}|(4\\d){1}|(5\\d){1}){1}");
		fs.put("s", "[0-9]{1}");
		fs.put("ss", "([0-9]{1}|(0\\d){1}|(1\\d){1}|(2\\d){1}|(3\\d){1}|(4\\d){1}|(5\\d){1}){1}");
		fs.put("S", "[0-9]{1}");
		fs.put("SS", "[0-9]{2}");
		fs.put("SSS", "([0-9]{1}|([0-9]\\d{1,2}){1}){1}");
		
		f = f.replaceAll("MM",fs.get("MM"));
		f = f.replaceAll("dd",fs.get("dd"));
		f = f.replaceAll("HH",fs.get("HH"));
		f = f.replaceAll("kk",fs.get("kk"));
		f = f.replaceAll("KK",fs.get("KK"));
		f = f.replaceAll("hh",fs.get("hh"));
		f = f.replaceAll("mm",fs.get("mm"));
		f = f.replaceAll("ss",fs.get("ss"));
		f = f.replaceAll("SSS",fs.get("SSS"));
		f = f.replaceAll("SS",fs.get("SS"));
		f = f.replaceAll("(y|M|d|H|k|K|h|m|s|S)","[0-9]{1}");
		f = f.replaceAll("a",fs.get("a"));
		
		Pattern p = Pattern.compile("^" + f + "$");
		Matcher m = p.matcher(target);
		return m.matches();
	}

}

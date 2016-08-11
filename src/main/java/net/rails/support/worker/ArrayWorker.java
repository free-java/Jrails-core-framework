package net.rails.support.worker;

import java.util.HashSet;
import java.util.List;
import net.rails.ext.Json;
import net.rails.support.Support;

/**
 * 集合工人。
 * @author Jack
 *
 * @param <T>
 */
public class ArrayWorker<T> {
	
	private List<T> target;
	
	/**
	 * 构造方法。
	 * @param target 目标对象
	 */
	public ArrayWorker(List<T> target){
		super();
		this.target = target;
	}
	
	/**
	 * 将目标对象的和子项用符号连接起来。
	 * @param sym 连接符号
	 * @return 连接后的字符串
	 */
	public String join(String sym){
		if(target == null)
			return "";
		
		StringBuffer sbf = new StringBuffer();
		for(int i = 0;i < target.size();i++){
			sbf.append(target.get(i));
			if(i < target.size() - 1)
				sbf.append(sym);
		}
		return sbf.toString();
	}
	
	/**
	 * 去除重复项
	 * @param list
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void unique(){
		HashSet hs = new HashSet(target);
		target.clear();
		target.addAll(hs);
	}
	
	/**
	 * 设置默认值,JsonArray格式的默认值。
	 * @param def
	 * @return
	 */
	public List<T> def(String def){
		if(Support.object(target).blank())
			return (List<T>) Json.parse(def);
		else
			return target;
	}
	
}

package net.rails.support.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.rails.ext.Json;
import net.rails.support.Support;

/**
 * Map Worker
 * @author Jack
 *
 * @param <K>
 * @param <V>
 */
@SuppressWarnings("hiding")
public class MapWorker<K,V> {
	
	private Map<K,V> target;
	
	/**
	 * constructor method.
	 * @param target
	 */
	public MapWorker(Map<K,V> target){
		super();
		this.target = target;
	}
	
	/**
	 * Get value for key, return def on value is null or blank.
	 * @param key
	 * @param def is default value.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <K,V> V get(K key,V def){
		return (V) Support.object(target.get(key)).def(def);
	}
	
	/**
	 * 键列表。
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <K extends Object> List<K> keys() {
		return new ArrayList<K>(((Collection<? extends K>) target.keySet()));
	}

	/**
	 * 循环获取Map里面的值，直到获取不到将返回上一值。
	 * @param keys "key1:key2:...keyN"
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object gets(String keys) {
		Object o = null;
		if (target == null)
			return null;

		Map<K, V> m = (Map<K, V>) target;
		String[] keyarr = keys.split(":");
		for (int i = 0; i < keyarr.length; i++) {
			o = m.get(keyarr[i]);
			if (o == null) {
				break;
			} else {
				if (o instanceof Map) {
					m = (Map<K, V>) o;
					continue;
				} else {
					if (i != keyarr.length - 1)
						o = null;
				}
			}
		}
		return o;
	}

	/**
	 * 循环获取Map里面的值，直到获取不到将返回上一值。
	 * @param keyarr key1,key2,...keyN
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object gets(String... keyarr) {
		Object o = null;
		if (target == null)
			return null;
		
		Map<K, V> m = (Map<K, V>) target;
		for (int i = 0; i < keyarr.length; i++) {
			o = m.get(keyarr[i]);
			if (o == null) {
				break;
			} else {
				if (o instanceof Map) {
					m = (Map<K, V>) o;
					continue;
				} else {
					if (i != keyarr.length - 1)
						o = null;
				}
			}
		}
		return o;
	}
	
	public Map<String,V> containsKey(String regex){
		List<String> keys = Support.map(target).keys();
		Map<String,V> map = new HashMap<String,V>();
		for(String k : keys){
			if(k.matches(regex))
			   map.put(k,target.get(k));
		}
		return map;
	}
	
	/**
	 * 设置默认值,Json格式的默认值。
	 * @param def
	 * @return
	 */
	public Map<K, V> def(String def){
		if(Support.object(target).blank())
			return (Map<K, V>) Json.parse(def);
		else
			return target;
	}

}

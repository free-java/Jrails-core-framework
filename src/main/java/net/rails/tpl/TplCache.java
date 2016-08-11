package net.rails.tpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.rails.cache.Cache;
import net.rails.support.Support;
import net.rails.web.Route;

/**
 * 模板缓存管理类
 * @author Jack
 *
 */
public abstract class TplCache {

	private String tplContent;
	protected String cacheKey;	
	protected int live;
	protected boolean force = false;
	protected abstract String execution();
	
	/**
	 * 构造方法。
	 * @param force 是否强制刷新缓存
	 * @param live 缓存生命周期, 0是不使用缓存,-1是永不过期，其它值是指定生命的秒数。 
	 * @param route 路由器,缓存键生成是根据route的controller,action来记录的。
	 * @param cacheKeys 缓存键生成规则。
	 */
	public TplCache(boolean force,int live,Route route,Object...cacheKeys) {
		super();
		this.force = force;
		this.live = live;
		List<Object> keys = null;
		if(cacheKeys == null)
			cacheKeys = new Object[]{};
		
		keys = new ArrayList<Object>(Arrays.asList(cacheKeys));
		keys.add(0,route.getController());
		keys.add(1,route.getAction());
		cacheKey = Support.code().md5(Support.array(keys).join("."));
		init();
	}
	
	/**
	 * 构造方法。
	 * @param live 
	 * @param route
	 * @param cacheKeys
	 */
	public TplCache(int live,Route route,Object...cacheKeys) {
		super();
		this.live = live;
		List<Object> keys = null;
		if(cacheKeys == null)
			cacheKeys = new Object[]{};
			
		keys = new ArrayList<Object>(Arrays.asList(cacheKeys));		
		keys.add(0,route.getController());
		keys.add(1,route.getAction());
		cacheKey = Support.code().md5(Support.array(keys).join("."));
		init();
	}
	
	public TplCache(boolean force,int live,String controller,String action,Object...cacheKeys) {
		super();
		this.force = force;
		this.live = live;
		List<Object> keys = null;
		if(cacheKeys == null)
			cacheKeys = new Object[]{};
		
		keys = new ArrayList<Object>(Arrays.asList(cacheKeys));
		keys.add(0,controller);
		keys.add(1,action);
		cacheKey = Support.code().md5(Support.array(keys).join("."));
		init();
	}
	
	public TplCache(int live,String controller,String action,Object...cacheKeys) {
		super();
		this.live = live;
		List<Object> keys = null;
		if(cacheKeys == null)
			cacheKeys = new Object[]{};
		
		keys = new ArrayList<Object>(Arrays.asList(cacheKeys));
		keys.add(0,controller);
		keys.add(1,action);
		cacheKey = Support.code().md5(Support.array(keys).join("."));
		init();
	}
	
	public TplCache(boolean force,int live,Object...cacheKeys) {
		super();
		this.force = force;
		this.live = live;
		List<Object> keys = null;
		if(cacheKeys == null)
			cacheKeys = new Object[]{};
		
		keys = new ArrayList<Object>(Arrays.asList(cacheKeys));
		cacheKey = Support.code().md5(Support.array(keys).join("."));
		init();
	}
	
	public TplCache(int live,Object...cacheKeys) {
		super();
		this.live = live;
		List<Object> keys = null;
		if(cacheKeys == null)
			cacheKeys = new Object[]{};
		
		keys = new ArrayList<Object>(Arrays.asList(cacheKeys));
		cacheKey = Support.code().md5(Support.array(keys).join("."));
		init();
	}
	
	private void init(){
		if(force)
			Cache.remove(cacheKey);
		
		if(Cache.included(cacheKey))
			tplContent = (String)Cache.get(cacheKey);
		else{
			tplContent = execution();
			Cache.set(cacheKey,tplContent,live);
		}
	}	
	
	@Override
	public String toString(){
		return Support.string(tplContent).def("");
	}

}

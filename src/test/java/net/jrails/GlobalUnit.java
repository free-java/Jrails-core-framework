package net.jrails;

import net.rails.ext.AbsGlobal;
import net.rails.support.worker.AbsConfigWorker;

public class GlobalUnit extends AbsGlobal {
	
	static{
		AbsConfigWorker.CONFIG_PATH = "./config";
	}
	
	private Object userId;
	
	public GlobalUnit(){
		super();
		this.options.put("protocol","http");
		this.options.put("domain","www.jrails.net");
		this.options.put("port", 80);
		this.options.put("path", "/core");
		this.options.put("domainRoot", "www.jrails.net");	
		this.options.put("domainUrl", "www.jrails.net/core");		
//		this.options.put("os-category", ua.os().getCategory());
//		this.options.put("os-name", ua.os().getName());
//		this.options.put("os-version", ua.os().getVersion());
//		this.options.put("browser-category",ua.browser().getCategory());
//		this.options.put("browser-name",ua.browser().getName());
//		this.options.put("browser-engine",ua.engine().getName());
//		this.options.put("browser-version",ua.engine().getVersion());	
	}

	@Override
	public void setUserId(Object userId) {
		this.userId = userId;
	}
	@Override
	public void setSessionId(Object sessionId) {

	}
	@Override
	public Object getUserId() {
		return userId;
	}
	@Override
	public Object getSessionId() {
		return "MySessionId";
	}

	@Override
	public String getRealPath() {
		return "./";
	}

}

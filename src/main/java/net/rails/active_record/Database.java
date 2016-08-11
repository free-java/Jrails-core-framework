package net.rails.active_record;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.support.Support;
import net.rails.support.worker.AbsConfigWorker;

public class Database {
	
	private Logger log = LoggerFactory.getLogger(Database.class);
	
	private final static AbsConfigWorker configur = Support.config().getConfig();
	public final static String READER = "_reader";
	public final static String WRITER = "_writer";
	
	private String env;	
	private String model;
	private Adapter adapter;
	private Map<String,Object> dbcnf;	
	protected Map<String,Object> db;	
	
	@SuppressWarnings("unchecked")
	public Database(String model,String rw){
		super();
		this.model = model;
		db = configur.get("database");
		env = getEnvEachValue(model,"env","Production").toString();
		String adaName = null;
		if(db.containsKey(env + rw)){
			dbcnf = (Map<String, Object>) db.get(env + rw);
			adaName = (String)Support.map(db).gets(env + rw,"adapter");
		}else{
			dbcnf = (Map<String, Object>) db.get(env);
			adaName = (String)Support.map(db).gets(env,"adapter");
		}		
		try {
			Class cls = null;
			if(adaName == null){
				cls = Adapter.class;
			}else{
				cls = Class.forName(adaName);
			}
			adapter = (Adapter) cls.getConstructor(Map.class,String.class).newInstance(dbcnf,this.model);			
		} catch (Exception e) {
			log.error(e.getCause().getMessage(),e.getCause());
		}
	}
	
	public String getEnv(){
		return env;
	}
	
	public Map<String,Object> getDbcnf(){
		return dbcnf;
	}
	
	public Adapter getAdapter(){
		return adapter;
	}
	
	protected Object getEnvEachValue(String model,String key,Object def){
		Map<String,Object> modcnf = Support.config().getModels().get(model);
		Object value = null;
		if(modcnf != null && modcnf.containsKey(key)){
				value = modcnf.get(key);
		}else{
			value = Support.config().getConfig().get("env").get(key);
			if(value == null){
				value = def;
			}
		}		
		return value;
	}
	
}

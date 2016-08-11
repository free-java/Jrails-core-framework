package net.rails.support.worker;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.ho.yaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.support.Support;

@SuppressWarnings("unchecked")
public abstract class AbsConfigWorker {
	
	public static String CONFIG_PATH;
	public abstract Map<String,Map<String,Object>> getConfs();
	protected abstract String getResource();
	protected Logger log;
	
	public AbsConfigWorker(){
		super();
		log = LoggerFactory.getLogger(getClass());
	}

	public Map<String, Object> get(String key) {				
		return getConfs().get(key);
	}
	
	public String getFolder() {	
		URL url = null;
		String path = null;
		try {
			if(CONFIG_PATH == null){
			    url = Thread.currentThread().getContextClassLoader().getResource("config/" + getResource());
			}else{
				url = new URL("file:" + CONFIG_PATH + "/" + getResource());
			}
			if(url != null){
				path = url.getFile();	
			}else{
				path = new File(System.getProperty("user.dir") + "/config/" + getResource()).getAbsolutePath();
			}
			
			return URLDecoder.decode(path,System.getProperty("file.encoding"));
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			return path;
		}
	}
	
	public File[] getYmls() throws UnsupportedEncodingException {
		File dir = new File(getFolder());
		return dir.listFiles(getFilter());
	}	
	
	public Map<String, Map<String,Object>> loadYmls(){
		Map<String, Map<String,Object>> confs = new HashMap<String, Map<String,Object>>();
			try {
				File[] files = getYmls();
				if(files != null){					
					for(File file : files){
						try{
							Map<String,Object> map = (Map<String,Object>)Yaml.load(FileUtils.readFileToString(file));
							confs.put(file.getName().replaceFirst("(.[yY][mM][lL])$",""),map);
						}catch(Exception e){
							log.error("(File: "+ file.getName() +")" + e.getMessage(),e);
						}
					}
				}
				return confs;
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage(),e);
				return null;
			}
	}
	
	public Object getValues(String file,String...keys){
		Map<String,Object> vs = get(file);
		if(keys.length == 0)
			return vs;
		
		int len = keys.length;
		
		for(int i = 0;i < len - 1;i++){
			String key = keys[i];
			if(vs == null)
				return null;
			else
				vs = (Map<String,Object>)vs.get(key);
		}
		if(vs == null)
			return null;
		else
			return vs.get(keys[len - 1]);
	}
	
	public Object gets(String...keyarr){
		return Support.map(getConfs()).gets(keyarr);
	}
	
	public Object gets(String keys){
		return Support.map(getConfs()).gets(keys);
	}
	
	private FilenameFilter getFilter(){
		return new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".yml");
			}
		};
	}

}


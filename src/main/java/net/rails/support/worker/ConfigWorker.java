package net.rails.support.worker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.rails.support.Support;

/**
 * 配置工人；
 * 读取config/*.yml文件内容并转换成Map格式，文件名（不包含后缀）将会是key。
 * @author Jack
 *
 */
public class ConfigWorker {

	public static Map<String, Map<String, Object>> CONFS = null;
	public static Map<String, Map<String, Object>> MODEL_CONFS = null;
	public static Map<String, Map<String, Object>> LOCALE_CONFS = null;
	public static Map<String, List<Map<String, Object>>> DELETE_CONFS = null;
	public static Map<String, List<Map<String, Object>>> DESTROY_CONFS = null;

	/**
	 * 构造方法
	 */
	public ConfigWorker(){
		super();
	}
	
	/**
	 * 获取config/根目录的配置文件内容并转换成Map格式。
	 * @return AbsConfigWorker
	 */
	public AbsConfigWorker getConfig() {
		AbsConfigWorker c = new AbsConfigWorker() {
			@Override
			public Map<String, Map<String, Object>> getConfs() {
				if (CONFS == null) {
					CONFS = this.loadYmls();
					log.debug("{}",CONFS);
				}
				return CONFS;
			}

			@Override
			protected String getResource() {
				return "/";
			}
		};
		return c;
	}

	/**
	 * 获取config/env.yml配置文件内容。
	 * @return EnvWorker
	 */
	public EnvWorker env() {
		return new EnvWorker();
	}

	/**
	 * 获取config/models/*.yml配置文件内容。
	 * @return AbsConfigWorker
	 */
	public AbsConfigWorker getModels() {
		AbsConfigWorker c = new AbsConfigWorker() {
			@Override
			public synchronized Map<String, Map<String, Object>> getConfs() {
				if (MODEL_CONFS == null) {
					MODEL_CONFS = this.loadYmls();
				}
				return MODEL_CONFS;
			}

			@Override
			protected String getResource() {
				return "/models/";
			}
		};
		return c;
	}
	
	/**
	 * 获取config/models/*.yml里面的依赖关系，一般用在框架内部。
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, List<Map<String, Object>>> getDelete() {
		if (DELETE_CONFS == null) {
			DELETE_CONFS = new HashMap<String, List<Map<String, Object>>>();
			Map<String, Map<String, Object>> cnfs = getModels().getConfs();
			List<String> models = Support.map(cnfs).keys();
			for (String model : models) {
				List<Map<String, Object>> depcnf = (List<Map<String, Object>>) cnfs.get(model).get("delete");
				if (depcnf != null) {
					DELETE_CONFS.put(model, depcnf);
				}
			}
		}
		return DELETE_CONFS;
	}
	
	/**
	 * 获取config/models/*.yml里面的依赖关系，一般用在框架内部。
	 * @return
	 */
	public Map<String, List<Map<String, Object>>> getDestroy() {
		if (DESTROY_CONFS == null) {
			DESTROY_CONFS = new HashMap<String, List<Map<String, Object>>>();
			Map<String, Map<String, Object>> cnfs = getModels().getConfs();
			List<String> models = Support.map(cnfs).keys();
			for (String model : models) {
				List<Map<String, Object>> depcnf = (List<Map<String, Object>>) cnfs.get(model).get("destroy");
				if (depcnf != null) {
					DESTROY_CONFS.put(model, depcnf);
				}
			}
		}
		return DESTROY_CONFS;
	}

	/**
	 * 获取config/locales/*.yml获取国际化语言配置文件内容并以Map输出。
	 * @return AbsConfigWorker
	 */
	public AbsConfigWorker getLocales() {
		AbsConfigWorker c = new AbsConfigWorker() {
			@Override
			public synchronized Map<String, Map<String, Object>> getConfs() {
				if (LOCALE_CONFS == null) {
					LOCALE_CONFS = this.loadYmls();
				}
				return LOCALE_CONFS;
			}

			@Override
			protected String getResource() {
				return "/locales/";
			}
		};
		return c;
	}
}


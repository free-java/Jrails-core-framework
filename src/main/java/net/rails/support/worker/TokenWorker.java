package net.rails.support.worker;

public class TokenWorker implements Cloneable {

	private String family;
	private String group;
	private String name;
	private float version;
	
	public TokenWorker() {
		super();
	}

	/**
	 * 获取分类。
	 * @return
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * 设置分类。
	 * @param category
	 */
	public void setFamily(String family) {
		this.family = family;
	}

	/**
	 * 获取分组。
	 * @return
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * 设置分组。
	 * @param group
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * 获取名称。
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置名称。
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取版本号。
	 * @return
	 */
	public float getVersion() {
		return version;
	}

	/**
	 * 设置版本号。
	 * @param version
	 */
	public void setVersion(float version) {
		this.version = version;
	}
	
	@Override
	public TokenWorker clone(){
		TokenWorker o = new TokenWorker();
		o.setFamily(this.family);
		o.setGroup(this.group);
		o.setName(this.name);
		o.setVersion(this.version);
		return o;
	}

}
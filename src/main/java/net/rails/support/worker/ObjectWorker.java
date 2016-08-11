package net.rails.support.worker;

/**
 * 对象工人。
 * @author Jack
 *
 * @param <O>
 */
public class ObjectWorker<O> {

	private O target;
	
	/**
	 * 构造方法。
	 * @param target 目标对象
	 */
	public ObjectWorker(O target) {
		super();
		this.target = target;
	}
	
	/**
	 * 检查目标是否NULL。
	 * @return
	 */
	public boolean nil(){
		return target == null;
	}
	
	/**
	 * 检查目标是否为NULL或者toString()后是否为空引号。
	 * @return
	 */
	public boolean blank(){
		return nil() || target.toString().trim().equals("");
	}
	
	/**
	 * 目标对空为NULL时返回默认值。
	 * @param def
	 * @return
	 */
	public O def(O def){
		if(blank())
			return def;
		else
			return target;
	}

}

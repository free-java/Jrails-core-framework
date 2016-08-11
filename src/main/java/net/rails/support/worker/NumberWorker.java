package net.rails.support.worker;

import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * 数字工人。
 * @author Jack
 *
 */
public class NumberWorker {
	
	private Number target;
	
	/**
	 * 构造方法。
	 * @param target 目标对象。
	 */
	public NumberWorker(Number target) {
		super();
		this.target = target;
	}
	
	/**
	 * 构造方法。
	 * @param pattern 指定日期格式
	 * @param source 目标对象的字符串格式
	 * @throws ParseException
	 */
	public NumberWorker(String pattern,String source) throws ParseException {
		super();
		DecimalFormat df = new DecimalFormat(pattern);
		this.target = df.parse(source);
	}
	
	/**
	 * 格式化日期。
	 * @param pattern
	 * @return
	 */
	public String format(String pattern){		
		DecimalFormat df = new DecimalFormat(pattern);
		return df.format(target);
	}
	
	/**
	 * 目标值为空时返回默认值。
	 * @param def
	 * @return
	 */
	public Number def(Number def){
		if(target == null)
			return def;
		
		return target;
	}
	
	/**
	 * 获取目标对象
	 */
	public Number getTarget(){
		return this.target;
	}

}

package net.rails.support.worker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 日历工人。
 * @author Jack
 *
 */
public class CalendarWorker {

	private Calendar target;
	
	/**
	 * 根据毫秒数实例化。
	 * @param millis 目标对象的毫秒形式
	 */
	public CalendarWorker(long millis) {
		super();
		target = Calendar.getInstance();
		target.setTimeInMillis(millis);
	}
	
	/**
	 * 按指定日期格式处理。
	 * @param pattern 指定日期格式
	 * @param source 日期格式的字符串参数
	 * @throws ParseException 转换失败时招聘的异常
	 */
	public CalendarWorker(String pattern,String source) throws ParseException {
		super();
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		target.setTimeInMillis(sdf.parse(source).getTime());		
	}
	
	/**
	 * 按指定日期格式处理。
	 * @param pattern 日期格式化字符串
	 * @return 已经被格式化的字符串
	 */
	public String format(String pattern){
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(target.getTime());
	}
	
	public boolean check(String pattern,String text){
		
		return false;
	}
	
	/**
	 * 获取目标对象。
	 * @return
	 */
	public Calendar getTarget(){
		return target;
	}

}

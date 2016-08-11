package net.rails.support.worker;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Random;
import org.apache.velocity.tools.generic.EscapeTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代码工人。
 * @author Jack
 *
 */
public final class CodeWorker {
	
	private final Logger log = LoggerFactory.getLogger(CodeWorker.class);
	
	/**
	 * HTML文本编译。
	 * @param text
	 * @return
	 */
	public String html(String text){
		return new EscapeTool().html(text);
	}
	
	/**
	 * JavaScript编译
	 * @param text
	 * @return 
	 */
	public String js(String text){
		return new EscapeTool().javascript(text);
	}
	
	/**
	 * Sql语句编译
	 * @param text
	 * @return
	 */
	public String sql(String text){
		return new EscapeTool().sql(text);
	}
	
	/**
	 * 生成16位的唯一ID值。
	 * @return
	 */
	public String id() {
		final Character[] CHARS = new Character[] { '0', '1',
				'2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
				'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
				'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

			Calendar cal = Calendar.getInstance();
			String[] ds = new String[] {
					cal.get(Calendar.YEAR) + "",
					(cal.get(Calendar.MONDAY) + 1) + ""
							+ cal.get(Calendar.DATE),
					cal.get(Calendar.HOUR_OF_DAY) + ""
							+ cal.get(Calendar.MINUTE),
					cal.get(Calendar.SECOND) + "",
					new Random().nextInt(10000) + "" };
			StringBuffer sbf = new StringBuffer();
			for (String d : ds) {
				StringBuffer n = new StringBuffer(Integer.toString(
						Integer.parseInt(d), 36));
				int len = 3 - n.length();
				for (int i = 0; i < len; i++)
					n.append(CHARS[new Random().nextInt(36)]);

				sbf.append(n);
			}
			sbf.append(CHARS[new Random().nextInt(36)]);
			String id16 = sbf.toString();
			return id16;

	}

	/**
	 * MD5加密。
	 */
	public String md5(String text) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };

		try {
			byte[] btInput = text.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}

	}

}

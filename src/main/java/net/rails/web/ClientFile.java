package net.rails.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * 处理客户端上传文件。
 * @author Jack
 *
 */
@SuppressWarnings("serial")
public class ClientFile implements Serializable{
	
	private String name;
	private byte[] data;
	
	/**
	 * 构造方法。
	 * @param name 文件名称
	 * @param is 输入流
	 * @throws IOException
	 */
	public ClientFile(String name,InputStream is) throws IOException{
		super();
		this.name = name;
		if(is != null){
			byte[] buf = new byte[1024];
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			int len = -1;
			while((len = is.read(buf)) > -1){
				os.write(buf,0,len);
				os.flush();
			}
			data = os.toByteArray();
			os.close();
			if(is != null)
				is.close();
		}
	}

	/**
	 * 获取文件名称
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 获取文件数据包。
	 * @return
	 */
	public byte[] getData() {
		return data;
	}
	
	@Override
	public String toString(){
		return "[Name:" + name +",Size: " + data.length +"]";
	}

}

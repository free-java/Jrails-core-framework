package net.jrails;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;

import net.rails.active_record.Database;
import net.rails.ciphertext.Ciphertext;
import net.rails.ciphertext.Ciphertext.DESWorker;
import net.rails.ciphertext.Ciphertext.ThreeDESWorker;
import net.rails.ciphertext.exception.CiphertextException;
import net.rails.ext.AbsGlobal;
import net.rails.support.Support;
//import net.rails.log.Log;
import net.rails.tpl.Tpl;
import net.rails.tpl.TplText;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
	public static AbsGlobal g;
	
	static{
		g = new GlobalUnit();
	}
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     * @throws IOException 
     */
    public void testApp() throws IOException
    {
    	try{
        	g.setLocale("default");
        	System.out.println("-------");
//        	Log.e(AppTest.class,"-------------------------"+ Log.level());
        	StringBuffer sbf = new StringBuffer("function test( dfsdfdf){ return dfsdfdf;}");
        	TplText text = new TplText("test", g, sbf);
        	Tpl tpl = new Tpl(g, text);
        	tpl.setDocType(Tpl.DOCTYPE_JS);
        	tpl.setCompressed(true);
        	String s = tpl.generate();
        	System.out.println("s : " + s);
            assertTrue( true );
    	}catch(Exception e){
//    		Log.e(AppTest.class, e.getMessage(),e);
    	}

    }
    
    public void testhtml() throws IOException
    {
    	try{
        	g.setLocale("default");
        	StringBuffer sbf = new StringBuffer();
        	String data = FileUtils.readFileToString(new File("/Users/jack/Documents/Repositories/Workspaces/Jrails/Jrails-core-framework/test/index.html"),"UTF-8");
        	sbf.append(data);
        	System.out.println("Length 1 : " + sbf.toString().length());
        	TplText text = new TplText("test", g, sbf);
        	Tpl tpl = new Tpl(g, text);
        	tpl.setDocType(Tpl.DOCTYPE_HTML);
        	tpl.setCompressed(true);
        	String s = tpl.generate();
//        	System.out.println("s : " + s);
        	System.out.println("Length 2 : " + s.length());
            assertTrue( true );
    	}catch(Exception e){
    		e.printStackTrace();
//    		Log.e(AppTest.class, e.getMessage(),e);
    	}

    }
    
    public void testEnv(){
    	String prefix = Support.env().getPrefix();
    	System.out.println(Support.env().getRoot());
    	System.out.println(Support.config().getConfig().get("database"));
    }
    
    public void testApp2()
    {
		Database db = new Database("Robot",Database.WRITER);
		System.out.println("----" + db.getDbcnf());
//    	System.out.println(getEnvEachValue("Robot","env","Production").toString());
    }
    
	public static Object getEnvEachValue(String model,String key,Object def){
		Map<String,Object> modcnf = Support.config().getModels().get(model);
		System.out.println("modcnf : " + modcnf);
		Object value = null;
		if(modcnf != null && modcnf.containsKey(key)){
				value = modcnf.get(key);
		}else{
			System.out.println("env: " + Support.config().getConfig().get("env"));
			value = Support.config().getConfig().get("env").get(key);
			System.out.println("key : " + key);
			System.out.println("value : " + value);
			if(value == null){
				value = def;
			}
		}		
		return value;
	}
	
	public void testEncode() throws UnsupportedEncodingException{
		String s = "%E9%A6%99%E6%B8%AF";
		System.out.println(URLDecoder.decode(s,"UTF-8"));
	}
    
//    public void testSSL() {
//    	try{
//	    	ThreeDESWorker ciphertext = new Ciphertext.ThreeDESWorker("ujn49uo698gm5491dop609gq","85l486kp");
//	    	String en = ciphertext.encrypt("1\n".getBytes());
//	    	System.out.println(en);
//	    	System.out.println("---" + new String(ciphertext.decrypt(en)) + "---");
//	    	System.out.println("---" + new String(ciphertext.decrypt("Fi15ZsZ/umJlyqU+Iy97lg==")).trim() + "---");
//	    	String hex = String.format("%x", new BigInteger(1, "ujn49uo698gm5491dop609gq".getBytes()));
//	    	System.out.println(hex);
//	    	byte[] bytes = DatatypeConverter.parseHexBinary("756a6e3439756f363938676d35343931646f703630396771");
//	    	String result= new String(bytes, "UTF-8");
//	    	System.out.println(result);
//    	}catch(Exception e){
//    		e.printStackTrace();
//    	}
//    }
    
}

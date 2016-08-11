package net.rails.active_record;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.rails.cache.Cache;
import net.rails.ext.IndexMap;
import net.rails.sql.Sql;
import net.rails.sql.worker.CreateWorker;
import net.rails.sql.worker.SqlWorker;
import net.rails.support.Support;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@SuppressWarnings("unchecked")
public class Adapter {
	
	private Logger log = LoggerFactory.getLogger(Adapter.class);	
	private DataSource dataSource;

	protected PreparedStatement statement;
	protected Connection connection;
	protected String model;
	protected String tableName;
	protected String primaryKey;
	protected String schema;
	protected Boolean pluralized = false;
	protected String prefix;
	protected Map<String,Object> modcnf;	
	protected Map<String,Object> dbcnf;
	
	protected static Map<String,Map<String,String>> COLUMN_TYPES;
	protected static Map<String,Map<String,String>> COLUMN_CLASSES;
	protected static Map<String,List<String>> COLUMN_NAMES;
	
	static{
		COLUMN_TYPES = new IndexMap<String,Map<String,String>>();
		COLUMN_CLASSES = new IndexMap<String,Map<String,String>>();
		COLUMN_NAMES = new IndexMap<String,List<String>>();
	}
	
	public Adapter(Map<String,Object> dbcnf,String model) {
		super();
		this.dbcnf = dbcnf;
		this.model = model;
		init();		
	}
	
	private void init() {
		initDataSource();
		modcnf = Support.config().getModels().get(model);
		schema = Support.map(dbcnf).get("schema","");
		pluralized = Support.map(dbcnf).get("pluralized",false);
		prefix = Support.map(dbcnf).get("prefix","");
		String tname = null;
		if(pluralized){
		   tname = Support.inflect(Support.inflect(model).underscore()).pluralize();
		}else{
		   tname = Support.inflect(model).underscore();
		}
		tableName = prefix + tname;
		if(modcnf == null){
			primaryKey = "id";
		}else{
			tableName = Support.map(modcnf).get("table_name",tableName);
			primaryKey = Support.map(modcnf).get("primary_key","id");
		}
	}
	
	protected void bindValues(List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			if (params.get(i) != null){
				Object v = params.get(i);
				if(v instanceof Timestamp){
					String frm = (String)Support.map(dbcnf).gets("formats","datetime");
					frm = Support.string(frm).def("yyyy-MM-dd HH:mm:ss.SSS");
					SimpleDateFormat df = new SimpleDateFormat(frm);
					statement.setObject(i + 1,df.format(v));
				}else if(v instanceof java.sql.Date){
					String frm = (String)Support.map(dbcnf).gets("formats","date");
					frm = Support.string(frm).def("yyyy-MM-dd");
					SimpleDateFormat df = new SimpleDateFormat(frm);
					statement.setObject(i + 1,df.format(v));
				}else if(v instanceof java.sql.Time){
					String frm = (String)Support.map(dbcnf).gets("formats","time");
					frm = Support.string(frm).def("HH:mm:ss.SSS");
					SimpleDateFormat df = new SimpleDateFormat(frm);
					statement.setObject(i + 1,df.format(v));
				}else
					statement.setObject(i + 1,v);
			}else
				statement.setNull(i + 1, Types.VARCHAR);
		}
	}	
	
	public synchronized List<Map<String,Object>> syncFind(SqlWorker sql) throws SQLException{
		String cacheName = sql.getCacheName();		
		if(sql.getCacheName() == null){
			cacheName = Support.code().md5(sql.getSql() + sql.getParams() + sql.getMaxRows());
		}		
		if(sql.isCacheForced()){
			Cache.remove(cacheName);
		}
		List<Map<String, Object>> list = null;
		if(Cache.included(cacheName)){
			if(log.isDebugEnabled()){
				log.debug("Find sql for (Cache) : " + sql.getSql());
				log.debug("Params for (Cache) : " + sql.getParams());
			}
			list = (List<Map<String, Object>>) Cache.get(cacheName);
			if(list == null){
				list =  findSql(sql);
				Cache.set(cacheName,list,sql.getCacheSecond());				
				return list;
			}else{
				if(log.isDebugEnabled()){
					log.debug("Rows : " + list.size());
					log.debug("Result(Cache) : " + list);
				}
				return list;
			}
		}else{
			list = findSql(sql);
			Cache.set(cacheName,list,sql.getCacheSecond());				
			return list;
		}
	}
	
	public List<Map<String,Object>> find(SqlWorker sql) throws SQLException{		
		if(sql.isCached()){
			return syncFind(sql);
		}else
			return findSql(sql);
	}
	
	private List<Map<String,Object>> findSql(SqlWorker sql) throws SQLException{
		Map<String, Object> row = null;
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		try{
			if(log.isDebugEnabled()){
				log.debug("Find sql for : " + sql.getSql());
				log.debug("Params for : " + sql.getParams());	
			}
			open();
			statement = this.connection.prepareStatement(sql.getSql());
			if(sql.getMaxRows() > 0)
				statement.setMaxRows(sql.getMaxRows());
			
			bindValues(sql.getParams());
			final ResultSet result = statement.executeQuery();
			final ResultSetMetaData rsmd = result.getMetaData();
			final int c = rsmd.getColumnCount();
			while (result.next()) {
				row = new HashMap<String, Object>();
				for (int i = 0; i < c; i++) {
					row.put((rsmd.getColumnLabel(i + 1)).toLowerCase(), result.getObject(i + 1));
				}
				rows.add(row);
			}
			result.close();
			if(log.isDebugEnabled()){
				log.debug("Rows : " + rows.size());
				log.debug("Result(DB) : " + rows);
			}
			return rows;
		}finally{
			close();
		}
	}
	
	public boolean create(ActiveRecord record) throws SQLException{
		try{
			CreateWorker cw = Sql.create(record);
			SqlWorker sql = Sql.sql(cw.getSql(),cw.params());
			if(log.isDebugEnabled()){
				log.debug("Execute sql for : " + sql.getSql());
				log.debug("Params for : " + sql.getParams());
			}		
			open();	
			statement = this.connection.prepareStatement(sql.getSql(),new String[] {record.getWriterAdapter().getPrimaryKey()});
			bindValues(sql.getParams());
			final int r = statement.executeUpdate();
			final ResultSet rs = statement.getGeneratedKeys();
			if(rs.next()) {
				record.setId(rs.getObject(1));
			}
			rs.close();
			return r > 0;
		}catch(SQLException e){
			throw e;
		}finally{
			close();
		}
	}
	
	public <T extends ActiveRecord> int[] create(List<T> records) throws SQLException{
		boolean first = true;
		try{			
			Timestamp curr = new Timestamp(new Date().getTime());
			for(ActiveRecord record : records){
				record.put("created_at", curr);
				record.put("created_user_id",record.getGlobal().getUserId());
				record.put("deleted", false);				
				CreateWorker cw = Sql.create(record);
				SqlWorker sql = Sql.sql(cw.getSql(),cw.params());								
				if(first){
					open();					
					statement = this.connection.prepareStatement(sql.getSql(),new String[] {record.getWriterAdapter().getPrimaryKey()});
					first = false;
				}
				if(log.isDebugEnabled()){
					log.debug("Execute sql for : " + sql.getSql());
					log.debug("Params for : " + sql.getParams());
				}
				bindValues(sql.getParams());
				statement.addBatch();
			}
			if(records.size() > 0)
				return statement.executeBatch();
			else
				return null;
		}catch(SQLException e){
			throw e;
		}finally{
			close();
		}
	}
	
	public int execute(SqlWorker sql) throws SQLException{
		if(log.isDebugEnabled()){
			log.debug("Execute sql for : " + sql.getSql());
			log.debug("Params for : " + sql.getParams());
		}		
		try{
			open();
			statement = connection.prepareStatement(sql.getSql());
			bindValues(sql.getParams());
			return statement.executeUpdate();
		}catch(SQLException e){
			throw e;
		}finally{
			close();
		}
	}
	
	public List<String> getColumnNames() {
		if(COLUMN_NAMES.containsKey(model))
			return new ArrayList<String>(COLUMN_NAMES.get(model));
		
		if(log.isDebugEnabled())
			log.debug("Get Column names for " + model);	
		
		try{
			open();
			statement = connection.prepareStatement("SELECT * FROM " + quoteSchemaAndTableName() + " WHERE 1 = 0");
			final List<String> list = new ArrayList<String>();
			final ResultSet result = statement.executeQuery();
			final ResultSetMetaData rsmd = result.getMetaData();
			for (int i = 0; i < rsmd.getColumnCount(); i++) {
				list.add(rsmd.getColumnLabel(i + 1).toLowerCase().trim());
			}
			result.close();
			COLUMN_NAMES.put(model,list);
			return getColumnNames();
		}catch(SQLException e){
			log.error( e.getMessage(),e);
			return null;
		}finally{
			close();
		}		
	}
	
	public Map<String,String> getColumnClasses() {	
		if(COLUMN_CLASSES.containsKey(model))
			return new HashMap<String,String>(COLUMN_CLASSES.get(model));
		
		if(log.isDebugEnabled())
			log.debug("Get column classes for " + model);
		
		final Map<String,String> types = new HashMap<String,String>();
		try{
			open();
			statement = connection.prepareStatement("SELECT * FROM " + quoteSchemaAndTableName() + " WHERE 1 = 0");
			final ResultSet result = statement.executeQuery();
			final ResultSetMetaData rsmd = result.getMetaData();
			for (int i = 0; i < rsmd.getColumnCount(); i++) {
				String cn = rsmd.getColumnClassName(i + 1).replaceFirst("java.lang.","");
				types.put(rsmd.getColumnLabel(i + 1).toLowerCase(),cn);
			}
			result.close();
			COLUMN_CLASSES.put(model,types);
			return getColumnClasses();
		}catch(SQLException e){
			log.error(e.getMessage(),e);
			return null;
		}finally{
			close();
		}
	}
	
	public Map<String,String> getColumnTypes() {
		if(COLUMN_TYPES.containsKey(model))
			return new HashMap<String,String>(COLUMN_TYPES.get(model));
		
		if(log.isDebugEnabled())
			log.debug("Get column types for " + model);
		
		final Map<String,String> types = new HashMap<String,String>();
		try{
			open();
			statement = connection.prepareStatement("SELECT * FROM " + quoteSchemaAndTableName() + " WHERE 1 = 0");
			final ResultSet result = statement.executeQuery();
			final ResultSetMetaData rsmd = result.getMetaData();
			for (int i = 0; i < rsmd.getColumnCount(); i++) {
				types.put(rsmd.getColumnLabel(i + 1), rsmd.getColumnTypeName(i + 1));
			}
			
			result.close();
			COLUMN_TYPES.put(model,types);
			return getColumnTypes();
		}catch(SQLException e){
			log.error(e.getMessage(),e);
			return null;
		}finally{
			close();
		}
	}
	
	public String getTableName(){
		return tableName;
	}
	
	public String getPrimaryKey(){
		return primaryKey;
	}	
	
	public String getSchema(){
		return schema;
	}

	public Boolean isPluralized(){
		return pluralized;
	}
	
	public String getPrefix(){
		return prefix;
	}
	
	public String quotePrimaryKey(){
		return  quote(getPrimaryKey());
	}
	
	public String quote(String keyword){
		String symbol = Support.map(dbcnf).get("quote","keyword");
		return symbol.replace("keyword", keyword);
	}
	
	public String quoteTableName(){
		return quote(getTableName());
	}
	
	public String quoteSchemaDot(){
		if(Support.string(schema).blank()){
			return "";
		}else{
			return quote(getSchema()) + ".";
		}
	}
	
	public String quoteSchema(){
		return quote(getSchema());
	}
	
	public String quoteSchemaAndTableName(){
		return quoteSchemaDot() + quoteTableName();
	}

	public Map<String,Object> getDbcnf(){
		return dbcnf;
	}
	
	public String getModel(){
		return model;
	}
	
	public DataSource getDataSource(){
		return dataSource;
	}
	
	protected void open() throws SQLException{		
		boolean initConn = (connection == null || connection.isClosed());
		if(initConn){
			log.debug("Open Connection");
			connection = dataSource.getConnection();
			connection.setAutoCommit(true);
		}
	}
	
	protected void close() {
		log.debug("Close Connection");
		try{
			if (statement != null){
				statement.close();
			}
			if (connection != null){
				connection.close();
			}
		}catch(SQLException e){
			log.error(e.getMessage(),e);
		}
	}
	
	protected void initDataSource() {
		try {
			if(dbcnf.containsKey("jndi")){
				Context cxt = new InitialContext();
			    dataSource = (DataSource)cxt.lookup(dbcnf.get("jndi").toString());
			}else{
				final Class<DataSource> c = (Class<DataSource>) Class.forName(dbcnf.get("datasource").toString());
				dataSource = c.newInstance();
				Method mtd = c.getDeclaredMethod("setDriverClassName", String.class);
				mtd.invoke(dataSource,dbcnf.get("driver"));
				mtd = c.getDeclaredMethod("setUrl", String.class);
				mtd.invoke(dataSource,dbcnf.get("url")); 
				mtd = c.getDeclaredMethod("setUsername", String.class);
				mtd.invoke(dataSource, dbcnf.get("username"));
				mtd = c.getDeclaredMethod("setPassword", String.class);
				mtd.invoke(dataSource,dbcnf.get("password"));
			}
		}catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
}

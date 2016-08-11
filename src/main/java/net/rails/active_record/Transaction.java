package net.rails.active_record;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.active_record.exception.FieldNotFoundException;
import net.rails.active_record.exception.TranException;
import net.rails.active_record.validate.TypeException;
import net.rails.active_record.validate.exception.ConfigurException;
import net.rails.active_record.validate.exception.ValidateException;
import net.rails.ext.AbsGlobal;
import net.rails.sql.Sql;
import net.rails.sql.query.Query;
import net.rails.sql.worker.CreateWorker;
import net.rails.sql.worker.DestroyWorker;
import net.rails.sql.worker.SqlWorker;
import net.rails.sql.worker.UpdateWorker;
import net.rails.support.Support;

/**
 * 提供包含事务处理能力的数据持久化类
 * @author Jack
 *
 */
final class Transaction {

	private static final Logger log = LoggerFactory.getLogger(Transaction.class);
	
	private Adapter adapter;
	private Connection connection;
	private PreparedStatement statement;
	private AbsGlobal g;
	
	/**
	 * 构造
	 * @param g
	 * @param adapter
	 * @throws TranException
	 * @throws SQLException
	 */
	public Transaction(AbsGlobal g,Adapter adapter) throws TranException, SQLException{
		super();
		this.g = g;
		this.adapter = adapter;
		this.connection = adapter.getDataSource().getConnection();
		try{
			log.debug("Open Connection");
			log.debug("Set auto commit is false");
			log.debug("Start transaction");
			if(this.connection.getAutoCommit())
				this.connection.setAutoCommit(false);
		}catch (SQLException e) {
			throw new TranException(e.getMessage(),e);
		}
	}
	
	/**
	 * 执行sql语句。
	 * @param sql
	 * @return 受影响行数
	 * @throws SQLException
	 */
	public int execute(SqlWorker sql) throws SQLException{
		log.debug("Execute sql for : " + sql.getSql());
		log.debug("Params for : " + sql.getParams());
		
		try{
			statement = connection.prepareStatement(sql.getSql());
			bindValues(sql.getParams());
			return statement.executeUpdate();
		}catch(SQLException e){
			throw e;
		}
	}
	
	/**
	 * 执行插入操作。
	 * @param record
	 * @return 插入成功返回true否则返回false
	 * @throws SQLException
	 * @throws TypeException 数据类型错误抛出
	 */
	public boolean create(ActiveRecord record) throws SQLException,TypeException {
		List<String> attrs = record.getAttributes();
		Timestamp cur = new Timestamp(new Date().getTime());
		
		if (attrs.contains("deleted"))
			record.put("deleted", false);

		if (attrs.contains("created_at"))
			record.put("created_at", cur);

		if (attrs.contains("created_user_id"))
			record.put("created_user_id", g.getUserId());
		
		try{
			if(record.beforeCreate()){
				CreateWorker cw = Sql.create(record);
				SqlWorker sql = Sql.sql(cw.getSql(),cw.params());
				log.debug("Execute sql for : " + sql.getSql());
				log.debug("Params for : " + sql.getParams());
				statement = this.connection.prepareStatement(sql.getSql(),new String[] {record.getWriterAdapter().getPrimaryKey()});
				bindValues(sql.getParams());
				final int r = statement.executeUpdate();
				final ResultSet rs = statement.getGeneratedKeys();
				if(rs.next()) {
					record.setId(rs.getObject(1));
				}
				rs.close();
				boolean b = r > 0;
				if(b)
					record.afterCreate();
			}
			return false;
		}catch(SQLException e){
			throw e;
		}
	}
	
	/**
	 * 执行更新操作。
	 * @param record 
	 * @return 插入成功返回true否则返回false
	 * @throws SQLException
	 * @throws TypeException 数据类型错误抛出
	 */
	public boolean update(ActiveRecord record) throws SQLException ,TypeException{
		List<String> keys = Support.map(record).keys();
		List<String> attrs = record.getAttributes();
		boolean updated = false;
		for (String key : keys) {	
			if(!attrs.contains(key))
				continue;
			
			if (!Arrays.asList(record.writerAdapter.getPrimaryKey(),
					"created_user_id", "updated_user_id", "deleted_user_id",
					"created_at", "updated_at", "deleted_at", "deleted")
					.contains(key)) {
				Attribute a = record.getAttribute(key);
				Object ov = a.parse(record.beforeRecord.get(key));
				Object cv = record.get(key);
				Object pv = a.parse(cv);
				if(pv == null){					
					if(ov != null){
						updated = true;
						record.getUpdateValues().put(key, pv);
						record.updateTrigger(key, pv, ov);
					}
				}else{					
					if(!pv.equals(ov)){
						updated = true;
						record.getUpdateValues().put(key, pv);
						record.updateTrigger(key, pv, ov);
					}
				}
			}
		}
		if(updated){
			record.getUpdateValues().remove("created_user_id");
			record.getUpdateValues().remove("deleted_user_id");
			record.getUpdateValues().remove("created_at");
			record.getUpdateValues().remove("deleted_at");
			if (attrs.contains("updated_at")) 
				record.getUpdateValues().put("updated_at", new Timestamp(new Date().getTime()));
			
			if (attrs.contains("updated_user_id"))
				record.getUpdateValues().put("updated_user_id", g.getUserId());
			
			Object id = record.getId();
			if(record.beforeUpdate()){
				record.remove(record.readerAdapter.getPrimaryKey());
				UpdateWorker worker = Sql.update(record,record.getUpdateValues());
				worker.wheres().add(String.format("%s.%s = :_id", record.readerAdapter.quoteSchemaAndTableName(),
						record.readerAdapter.quotePrimaryKey()));
				worker.params().put("_id", id);
				boolean b = execute(Sql.sql(worker.getSql(), worker.params())) > 0;
				record.setId(id);
				if(b)
					record.afterUpdate();
			}
			return false;
		}else{
			return false;
		}
	}
	
	/**
	 * 软删除方法，将deleted字段标识成true状态；
	 * 若config/models/modelName.yml配置了dependent内容，将会引起循环去处理依赖模型。
	 * @param record
	 * @return 执行成功返回true否则返回false
	 * @throws Exception
	 */
	public boolean delete(ActiveRecord record) throws Exception{
		if(record.containsKey("deleted") && record.isBoolean("deleted"))
			return false;
		
		boolean b = false;
		if(record.getDelete() == null){
			b = delete(record,true);
		}else{
			recursion(record,"delete");
			b = delete(record,true);
		}
		if(b)
			record.afterDelete();
		
		return b;
	}
	
	/**
	 * 物理删除数据；
	 * 若config/models/modelName.yml配置了dependent内容，将会引起循环去处理依赖模型。
	 * @param record
	 * @return 执行成功返回true否则返回false
	 * @throws Exception
	 */
	public boolean destroy(ActiveRecord record) throws Exception {
		boolean b = false;
		if(record.beforeDestroy()){
			if(record.getDestroy() == null){
				b = destroy(record,true);
			}else{
				recursion(record,"destroy");
				b = destroy(record,true);
			}
			if(b)
				record.afterDestroy();
		}
		return b;
	}
	
	/**
	 * 提交请求到数据库。
	 * @throws TranException
	 */
	public void commit() throws TranException {
		if(log.isDebugEnabled())
			log.debug("Commit transaction");
		try{
			connection.commit();
			close();
		}catch (SQLException e) {
			throw new TranException(e.getMessage(),e);
		}
	}
	
	/**
	 * 回滚操作。
	 * @throws TranException
	 */
	public void rollback() throws TranException{
		log.debug("Rollback transaction");
		try{
			connection.rollback();
			close();
		}catch (SQLException e){
			throw new TranException(e.getMessage(),e);
		}
	}
	
	public AbsGlobal getGlobal(){
		return g;
	}
	
	
//*****************private motehds****************************//

	private boolean deleteClearValue(ActiveRecord record,String attr) throws Exception {
		if(record.getDelete() == null){
			return clearValue(true,record,attr);
		}else{
			recursion(record,"delete");			
			return clearValue(true,record,attr);
		}
	}
	
	private boolean destroyClearValue(ActiveRecord record,String attr) throws Exception {
		if(record.getDestroy() == null){
			return clearValue(true,record,attr);
		}else{
			recursion(record,"destroy");			
			return clearValue(true,record,attr);
		}
	}
	
	private void close() throws SQLException {
		log.debug("Close Connection");
		try{
			if (statement != null)
				statement.close();
			if (connection != null)
				connection.close();
		}catch(SQLException e){
			throw e;
		}
	}
	
	private void bindValues(List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			if (params.get(i) != null){
				Object v = params.get(i);
				if(v instanceof Timestamp){
					String frm = (String)Support.map(adapter.getDbcnf()).gets("formats","datetime");
					frm = Support.string(frm).def("yyyy-MM-dd HH:mm:ss.SSS");
					SimpleDateFormat df = new SimpleDateFormat(frm);
					statement.setObject(i + 1,df.format(v));
				}else if(v instanceof java.sql.Date){
					String frm = (String)Support.map(adapter.getDbcnf()).gets("formats","date");
					frm = Support.string(frm).def("yyyy-MM-dd");
					SimpleDateFormat df = new SimpleDateFormat(frm);
					statement.setObject(i + 1,df.format(v));
				}else if(v instanceof java.sql.Time){
					String frm = (String)Support.map(adapter.getDbcnf()).gets("formats","time");
					frm = Support.string(frm).def("HH:mm:ss.SSS");
					SimpleDateFormat df = new SimpleDateFormat(frm);
					statement.setObject(i + 1,df.format(v));
				}else
					statement.setObject(i + 1,v);
			}else
				statement.setNull(i + 1, Types.VARCHAR);
		}
	}
	
	private boolean delete(ActiveRecord record ,boolean shared) throws SQLException, FieldNotFoundException {
		Adapter adapter = record.getWriterAdapter();
		Map<String, Object> values = new HashMap<String, Object>();
		List<String> attrs = record.getAttributes();
		if (!record.isBoolean("deleted")) {
			if (attrs.contains("deleted_at"))
				values.put("deleted_at", new Timestamp(new Date().getTime()));
			
			if (attrs.contains("deleted_user_id"))
				values.put("deleted_user_id",g.getUserId());

			if (attrs.contains("deleted"))
				values.put("deleted", true);
			else
				throw new FieldNotFoundException(record.getClass().getSimpleName()
						+ ".deleted");

			UpdateWorker worker = Sql.update(record,values);
			worker.wheres().add(String.format("%s.%s = :_id", adapter.quoteSchemaAndTableName(), adapter.quotePrimaryKey()));
			worker.params().put("_id", record.getId());
			return execute(Sql.sql(worker.getSql(), worker.params())) > 0;
		} else {
			return false;
		}
	}
	
	private boolean destroy(ActiveRecord record,boolean shared) throws SQLException{
		Adapter adapter = record.getWriterAdapter();
		DestroyWorker worker = Sql.destroy(record);
		worker.wheres().add(String.format("%s.%s = :_id", adapter.quoteSchemaAndTableName(),
				adapter.quotePrimaryKey()));
		worker.params().put("_id", record.getId());
		return adapter.execute(Sql.sql(worker.getSql(), worker.params())) > 0;
	}
	
	private boolean clearValue(boolean shared,ActiveRecord record,String attr) throws SQLException, 
				ValidateException, ConfigurException, TypeException {
		record.put(attr,null);
		return update(record);
	}
	
	@SuppressWarnings("unchecked")
	protected void recursion(ActiveRecord record,String deleteOrDestroy) throws Exception {
		List<Map<String,Object>> deps = null;
		if(deleteOrDestroy.equals("delete"))
			deps = record.getDelete();
		else if(deleteOrDestroy.equals("destroy"))
			deps = record.getDestroy();
		
		String model = this.getClass().getSimpleName();	
		for (Map<String,Object> dep : deps) {
			if(dep != null){
				String depModel = Support.map(dep).keys().get(0).toString();
				Map<String,Object> depModelValues = (Map<String, Object>) dep.get(depModel);
				Map<String,Object> and = (Map<String, Object>) depModelValues.get("and");
				Map<String,Object> or = (Map<String, Object>) depModelValues.get("or");
				String defFk = Support.inflect(model).underscore() + "_id";
				String foreignKey = Support.object(depModelValues.get("foreign_key")).def(defFk).toString();
				String method = Support.object(depModelValues.get("method")).def("delete").toString();
				Query q = Query.from(g, depModel);
				q.and("eq_" + foreignKey,record.getId());
				q.and(and).or(or);
				List<ActiveRecord> list = q.find();				
				for (ActiveRecord a : list) {
					if (method.equals("delete")) {
						a.delete();
					} else if (method.equals("destroy")) {
						a.destroy();
					} else if (method.equals("clear")) {
						if(deleteOrDestroy.equals("delete"))
							a.deleteClearValue(foreignKey);
						
						if(deleteOrDestroy.equals("destroy"))
							a.destroyClearValue(foreignKey);
					}
				}
			}
		}
	}

}

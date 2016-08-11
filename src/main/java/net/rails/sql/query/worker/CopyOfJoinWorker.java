package net.rails.sql.query.worker;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.rails.active_record.ActiveRecord;
import net.rails.active_record.Adapter;
import net.rails.ext.IndexMap;
import net.rails.support.Support;

public class CopyOfJoinWorker {
	
	public List<String> generate(){
		List<String> js = new ArrayList<String>();
		List<String> ts = Support.map(joins).keys();
		for(String t: ts){
			js.add(joins.get(t));
		}
		js.addAll(otherJoins);
		return js;
	}
	
	public List<String> otherJoins(){
		return otherJoins;
	}
	
	public void inner(String t,String fk){
		join(t,createJoin("INNER",t,fk,""));
	}
	
	public void inner(String t){
		join(t,createJoin("INNER",t,""));
	}
	
	public void innerAs(String t,String as,String fk){
		join(t,createJoin("INNER",t,fk,as));
	}
	
	public void innerAs(String t,String as){
		join(t,createJoin("INNER",t,as));
	}
	
	public void left(String t,String fk){
		join(t,createJoin("LEFT",t,fk,""));
	}
	
	public void left(String t){
		join(t,createJoin("LEFT",t,""));
	}
	
	public void leftAs(String t,String as,String fk){
		join(t,createJoin("LEFT",t,fk,as));
	}
	
	public void leftAs(String t,String as){
		join(t,createJoin("LEFT",t,as));
	}
	
	public void right(String t,String fk){
		join(t,createJoin("RIGHT",t,fk,""));
	}
	
	public void right(String t){
		join(t,createJoin("RIGHT",t,""));
	}
	
	public void rightAs(String t,String as,String fk){
		join(t,createJoin("RIGHT",t,fk,as));
	}
	
	public void rightAs(String t,String as){
		join(t,createJoin("RIGHT",t,as));
	}
	
	public List<String> froms(){
		return froms;
	}
	
	public Map<String,String> joins(){
		return joins;
	}
	
	private String createJoin(String joinSym,String jtable, String fk,String as) {		
		String qjtable = quote(jtable);
		String qfk = quote(fk);		
		String frms = null;
//		ActiveRecord jtabar = null;
		String qjpk = quote("id");
		if(Support.string(as).blank()){
//			String fk2 = table + "_id";
//			String qfk2 = quote(fk2);
			frms = joinSym + " JOIN {0} ON {0}.{1} = {2}.{3}";
//			jtabar = ActiveRecord.eval(from.getGlobal(), jtable);
//			if(jtabar.getAttributes().contains(fk)){
//				return MessageFormat.format(frms,qjtable,qfk,qtable,qpk);
//			}else if(from.getAttributes().contains(fk)){
//				return MessageFormat.format(frms,qjtable,qpk,qtable,qfk);
//			}
//			else if(jtabar.getAttributes().contains(fk2)){
//				return MessageFormat.format(frms,qjtable,qfk2,qtable,qpk);
//			}else if(from.getAttributes().contains(fk2)){
//				return MessageFormat.format(frms,qjtable,qpk,qtable,qfk2);
//			}
//			else{
//			    String qjpk = jtabar.getReaderAdapter().quotePrimaryKey();
//			    String qjpk = quote("id");
				return MessageFormat.format(frms,qjtable,qjpk,qtable,qfk);
//			}
		}else{
			String qas = quote(as);
			frms = joinSym + " JOIN {0} AS {4} ON {4}.{1} = {2}.{3}";
			return MessageFormat.format(frms,qjtable,qjpk,qtable,qfk,qas);
		}
	}

	private String createJoin(String joinSym,String jtable,String as) {
		return createJoin(joinSym,jtable, jtable + "_id",as);
	}
	
	private void join(String t,String j){
		from(t);
		joins.put(t, j);
	}
	
	private void from(String f){
		if(!froms.contains(f))
			froms.add(f);
	}
	
	private String quote(String keyword){
		return fada.quote(keyword);
	}
	
	public CopyOfJoinWorker(ActiveRecord from){
		super();
		this.from = from;
		this.fada = from.getReaderAdapter();
		pk = fada.getPrimaryKey();
		qpk = fada.quotePrimaryKey();
		table = fada.getTableName();
		qtable= fada.quoteTableName();
	}
	
	private final List<String> otherJoins = new ArrayList<String>();
	private final Map<String,String> joins = new IndexMap<String, String>();
	private final List<String> froms = new ArrayList<String>();
	private Adapter fada;
	private ActiveRecord from;
	private String pk;
	private String qpk;
	private String table;
	private String qtable;
	
}

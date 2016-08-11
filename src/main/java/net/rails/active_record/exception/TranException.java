package net.rails.active_record.exception;

@SuppressWarnings("serial")
public class TranException extends Exception {
	
	public TranException(String msg,Throwable cause){
		super(msg,cause);
	}
}

package net.rails.support.job.worker;

import java.net.InetAddress;
import java.util.List;
import org.quartz.JobListener;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.ext.AbsGlobal;

public abstract class DefaultScheduleWorker {

	protected Logger log;
	protected AbsGlobal g;

	public abstract List<JobWorker> getScheduleJobs();
	public abstract TriggerListener getTriggerListener();
	public abstract JobListener getJobListener();
	
	public DefaultScheduleWorker(AbsGlobal g) {
		super();
		this.g = g;
		log = LoggerFactory.getLogger(getClass());
	}

	protected String getHostname() {
		try {
			InetAddress addr;
			addr = InetAddress.getLocalHost();
			return addr.getHostName();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

}

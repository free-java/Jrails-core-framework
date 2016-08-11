package net.rails.support.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.ext.AbsGlobal;
import net.rails.support.Support;
import net.rails.support.job.worker.DefaultScheduleWorker;
import net.rails.support.job.worker.JobWorker;
import net.rails.web.ApplicationListener;

public class Job {

	public static Scheduler GLOBAL_SCHEDULER;

	static {
		StdSchedulerFactory factory = new StdSchedulerFactory();
		try {
			GLOBAL_SCHEDULER = factory.getScheduler();
		} catch (SchedulerException e) {
			LoggerFactory.getLogger(ApplicationListener.class).error(e.getMessage(), e);
		}
	}
	
	public static DefaultScheduleWorker defaultSchedule(AbsGlobal g){
		return new DefaultScheduleWorker(g) {
			final List<JobWorker> SCHEDULE_JOBS = new ArrayList<JobWorker>();
			@Override
			public List<JobWorker> getScheduleJobs() {
				JobWorker jobWorker = new JobWorker();
				Object o = Support.env().get("jobs");
				if (o instanceof List) {
					List<Map<String, Object>> jobs = (List<Map<String, Object>>) o;
					for (Map<String, Object> job : jobs) {
						String jobName = (String) Support.map(job).keys().get(0);
						Map<String, String> jobItem = (Map<String, String>) job.get(jobName);
						String jobClass = jobItem.get("classify");
						String hostnames = jobItem.get("hostnames") == null ? "%" : jobItem.get("hostnames");
						String cronExpression = (String) jobItem.get("cron_expression");
						String jobGroup = "DEFAULT_JOB_GROUP";
						String triggerGroup = "DEFAULT_TRIGGER_GROUP";
						String triggerName = "Trigger_" + jobName;
						jobWorker = new JobWorker();
						jobWorker.setClassify(jobClass);
						jobWorker.setCronExpression(cronExpression);
						jobWorker.setJobGroup(jobGroup);
						jobWorker.setJobName(jobName);
						jobWorker.setTriggerGroup(triggerGroup);
						jobWorker.setTriggerName(triggerName);
						jobWorker.setHostnames(hostnames);
						SCHEDULE_JOBS.add(jobWorker);
					}
				}
				return SCHEDULE_JOBS;
			}
			
			@Override
			public TriggerListener getTriggerListener() {
				return new TriggerListener() {

					@Override
					public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
						boolean veto = false;
						String local = null;
						String currentTriggerName = null;
						String currentTriggerGroup = null;
						for (Iterator<JobWorker> iterator = SCHEDULE_JOBS.iterator(); iterator.hasNext();) {
							JobWorker jobWorker = iterator.next();
							List<String> hosts = Arrays.asList(jobWorker.getHostnames().split(","));
							local = getHostname();
							currentTriggerGroup = trigger.getKey().getGroup();
							currentTriggerName = trigger.getKey().getName();
							if (currentTriggerGroup.equals(jobWorker.getTriggerGroup()) && currentTriggerName.equals(jobWorker.getTriggerName())) {
								if (hosts.contains("%") || hosts.contains(local)) {
									veto = false;
								} else {
									veto = true;
								}
								log.debug(String.format("Job %s(%s.%s) veto status: %s", local,currentTriggerGroup,currentTriggerName, veto));
							}
						}
						return veto;
					}

					@Override
					public void triggerMisfired(Trigger trigger) {

					}

					@Override
					public void triggerFired(Trigger trigger, JobExecutionContext context) {

					}

					@Override
					public void triggerComplete(Trigger trigger, JobExecutionContext context,
							CompletedExecutionInstruction triggerInstructionCode) {
						
					}

					@Override
					public String getName() {
						return "DefaultScheduleWorker-TriggerListener";
					}
				};
			}

			@Override
			public JobListener getJobListener() {
				return null;
			}
		};
	}

}

package com.meteor.transfer.cron;

import java.io.IOException;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实时器，quartz集群，用于确保应用发布至多台机器做负载均衡，同一时刻只有一台机器执行某个任务
 * @author chenwu
 */
public class QuartzCluster {

	private static Logger logger = LoggerFactory.getLogger(QuartzCluster.class);
	
	private static SchedulerFactory factory;
	private static Scheduler scheduler;

	/**
	 * 定时器初始化启动
	 * @throws SchedulerException
	 * @throws IOException 
	 */
	public static void startup() throws SchedulerException, IOException {
		factory = new StdSchedulerFactory();
		scheduler = factory.getScheduler();
		scheduler.start();
		addJobIfNotExists();
	}

	/**
	 * 定时任务，初始化进quartz集群数据库
	 * @throws SchedulerException 
	 */
	private static void addJobIfNotExists() throws SchedulerException {
		addJobIfNotExists(CleanJob.class, "clean", "0 15 0 * * ?");
	}
	
	/**
	 * 添加定时任务
	 * @throws SchedulerException 
	 */
	private static void addJobIfNotExists(Class<? extends Job> jobClass, String jobName, String cronExp) throws SchedulerException {
		JobKey jobKey = new JobKey(jobName, Scheduler.DEFAULT_GROUP);
		if(!scheduler.checkExists(jobKey)) {
			addFlowTrigger(jobClass, jobName, Scheduler.DEFAULT_GROUP, jobName, Scheduler.DEFAULT_GROUP, cronExp);
		}
	}
	
	/**
	 * 添加定时任务
	 * @param jobClass
	 * @param jobName
	 * @param jobGroup
	 * @param cronTriggerName
	 * @param cronTriggerGroup
	 * @param cronExp
	 * @throws SchedulerException
	 */
	public static void addFlowTrigger(Class<? extends Job> jobClass, String jobName, String jobGroup, String cronTriggerName, String cronTriggerGroup, String cronExp) throws SchedulerException {
		JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroup).build();
		CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(cronTriggerName, cronTriggerGroup).withSchedule(CronScheduleBuilder.cronSchedule(cronExp)).build();
		scheduler.scheduleJob(job, trigger);
	}
}

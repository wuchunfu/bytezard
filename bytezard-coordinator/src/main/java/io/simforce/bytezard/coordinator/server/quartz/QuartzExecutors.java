/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.simforce.bytezard.coordinator.server.quartz;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import static io.simforce.bytezard.coordinator.CoordinatorConstants.*;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_POSTGRESQL_DRIVER;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_DATASOURCE_MYDS_CONNECTIONPROVIDER_CLASS;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_JOBSTORE_ACQUIRETRIGGERSWITHINLOCK;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_JOBSTORE_CLASS;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_JOBSTORE_CLUSTERCHECKININTERVAL;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_JOBSTORE_DATASOURCE;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_JOBSTORE_DRIVERDELEGATECLASS;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_JOBSTORE_ISCLUSTERED;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_JOBSTORE_MISFIRETHRESHOLD;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_JOBSTORE_TABLEPREFIX;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_JOBSTORE_USEPROPERTIES;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_SCHEDULER_INSTANCEID;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_SCHEDULER_INSTANCENAME;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_SCHEDULER_MAKESCHEDULERTHREADDAEMON;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_THREADPOOL_CLASS;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_THREADPOOL_MAKETHREADSDAEMONS;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_THREADPOOL_THREADCOUNT;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.ORG_QUARTZ_THREADPOOL_THREADPRIORITY;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.PROJECT_ID;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.QUARTZ_ACQUIRETRIGGERSWITHINLOCK;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.QUARTZ_CLUSTERCHECKININTERVAL;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.QUARTZ_DATASOURCE;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.QUARTZ_INSTANCEID;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.QUARTZ_INSTANCENAME;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.QUARTZ_MISFIRETHRESHOLD;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.QUARTZ_PROPERTIES_PATH;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.QUARTZ_TABLE_PREFIX;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.QUARTZ_THREADCOUNT;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.QUARTZ_THREADPRIORITY;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.SCHEDULE_ID;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.STRING_FALSE;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.STRING_TRUE;
import static io.simforce.bytezard.coordinator.CoordinatorConstants.UNDERLINE;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.jdbcjobstore.JobStoreTX;
import org.quartz.impl.jdbcjobstore.PostgreSQLDelegate;
import org.quartz.impl.jdbcjobstore.StdJDBCDelegate;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.simpl.SimpleThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.hibernate.DruidConnectionProvider;
import com.google.common.collect.Maps;

import io.simforce.bytezard.coordinator.utils.PropertyUtils;


/**
 * single Quartz executors instance
 */
public class QuartzExecutors {

  /**
   * logger of QuartzExecutors
   */
  private static final Logger logger = LoggerFactory.getLogger(QuartzExecutors.class);

  /**
   * read write lock
   */
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * A Scheduler maintains a registry of org.quartz.JobDetail and Trigger.
   */
  private static Scheduler scheduler;

  /**
   * instance of QuartzExecutors
   */
  private static volatile QuartzExecutors INSTANCE = null;

  /**
   * load conf
   */
  private static Configuration conf;


  private QuartzExecutors() {
    try {
      conf = new PropertiesConfiguration(QUARTZ_PROPERTIES_PATH);
    }catch (ConfigurationException e){
      logger.warn("not loaded quartz configuration file, will used default value",e);
    }
    this.init();
  }

  /**
   * thread safe and performance promote
   * @return instance of Quartz Executors
   */
  public static QuartzExecutors getInstance() {
    if (INSTANCE == null) {
      synchronized (QuartzExecutors.class) {
        if (INSTANCE == null) {
          INSTANCE = new QuartzExecutors();
        }
      }
    }
    return INSTANCE;
  }


  /**
   * init
   *
   * Returns a client-usable handle to a Scheduler.
   */
  private void init() {
    try {
      StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
      Properties properties = new Properties();

      String dataSourceDriverClass = PropertyUtils.getString(DATASOURCE_DRIVER_CLASS_NAME);
      if (dataSourceDriverClass.equals(ORG_POSTGRESQL_DRIVER)){
        properties.setProperty(ORG_QUARTZ_JOBSTORE_DRIVERDELEGATECLASS,conf.getString(ORG_QUARTZ_JOBSTORE_DRIVERDELEGATECLASS, PostgreSQLDelegate.class.getName()));
      } else {
        properties.setProperty(ORG_QUARTZ_JOBSTORE_DRIVERDELEGATECLASS,conf.getString(ORG_QUARTZ_JOBSTORE_DRIVERDELEGATECLASS, StdJDBCDelegate.class.getName()));
      }

      properties.setProperty(ORG_QUARTZ_SCHEDULER_INSTANCENAME, conf.getString(ORG_QUARTZ_SCHEDULER_INSTANCENAME, QUARTZ_INSTANCENAME));
      properties.setProperty(ORG_QUARTZ_SCHEDULER_INSTANCEID, conf.getString(ORG_QUARTZ_SCHEDULER_INSTANCEID, QUARTZ_INSTANCEID));
      properties.setProperty(ORG_QUARTZ_SCHEDULER_MAKESCHEDULERTHREADDAEMON,conf.getString(ORG_QUARTZ_SCHEDULER_MAKESCHEDULERTHREADDAEMON,STRING_TRUE));
      properties.setProperty(ORG_QUARTZ_JOBSTORE_USEPROPERTIES,conf.getString(ORG_QUARTZ_JOBSTORE_USEPROPERTIES,STRING_FALSE));
      properties.setProperty(ORG_QUARTZ_THREADPOOL_CLASS,conf.getString(ORG_QUARTZ_THREADPOOL_CLASS, SimpleThreadPool.class.getName()));
      properties.setProperty(ORG_QUARTZ_THREADPOOL_MAKETHREADSDAEMONS,conf.getString(ORG_QUARTZ_THREADPOOL_MAKETHREADSDAEMONS,STRING_TRUE));
      properties.setProperty(ORG_QUARTZ_THREADPOOL_THREADCOUNT,conf.getString(ORG_QUARTZ_THREADPOOL_THREADCOUNT, QUARTZ_THREADCOUNT));
      properties.setProperty(ORG_QUARTZ_THREADPOOL_THREADPRIORITY,conf.getString(ORG_QUARTZ_THREADPOOL_THREADPRIORITY, QUARTZ_THREADPRIORITY));
      properties.setProperty(ORG_QUARTZ_JOBSTORE_CLASS,conf.getString(ORG_QUARTZ_JOBSTORE_CLASS, JobStoreTX.class.getName()));
      properties.setProperty(ORG_QUARTZ_JOBSTORE_TABLEPREFIX,conf.getString(ORG_QUARTZ_JOBSTORE_TABLEPREFIX, QUARTZ_TABLE_PREFIX));
      properties.setProperty(ORG_QUARTZ_JOBSTORE_ISCLUSTERED,conf.getString(ORG_QUARTZ_JOBSTORE_ISCLUSTERED,STRING_TRUE));
      properties.setProperty(ORG_QUARTZ_JOBSTORE_MISFIRETHRESHOLD,conf.getString(ORG_QUARTZ_JOBSTORE_MISFIRETHRESHOLD, QUARTZ_MISFIRETHRESHOLD));
      properties.setProperty(ORG_QUARTZ_JOBSTORE_CLUSTERCHECKININTERVAL,conf.getString(ORG_QUARTZ_JOBSTORE_CLUSTERCHECKININTERVAL, QUARTZ_CLUSTERCHECKININTERVAL));
      properties.setProperty(ORG_QUARTZ_JOBSTORE_ACQUIRETRIGGERSWITHINLOCK,conf.getString(ORG_QUARTZ_JOBSTORE_ACQUIRETRIGGERSWITHINLOCK, QUARTZ_ACQUIRETRIGGERSWITHINLOCK));
      properties.setProperty(ORG_QUARTZ_JOBSTORE_DATASOURCE,conf.getString(ORG_QUARTZ_JOBSTORE_DATASOURCE, QUARTZ_DATASOURCE));
      properties.setProperty(ORG_QUARTZ_DATASOURCE_MYDS_CONNECTIONPROVIDER_CLASS,conf.getString(ORG_QUARTZ_DATASOURCE_MYDS_CONNECTIONPROVIDER_CLASS, DruidConnectionProvider.class.getName()));

      schedulerFactory.initialize(properties);
      scheduler = schedulerFactory.getScheduler();

    } catch (SchedulerException e) {
      logger.error(e.getMessage(),e);
      System.exit(1);
    }

  }

  /**
   * Whether the scheduler has been started.
   *
   * @throws SchedulerException scheduler exception
   */
  public void start() throws SchedulerException {
    if (!scheduler.isStarted()){
      scheduler.start();
      logger.info("Quartz service started" );
    }
  }

  /**
   * stop all scheduled tasks
   *
   * Halts the Scheduler's firing of Triggers,
   * and cleans up all resources associated with the Scheduler.
   *
   * The scheduler cannot be re-started.
   * @throws SchedulerException scheduler exception
   */
  public void shutdown() throws SchedulerException {
    if (!scheduler.isShutdown()) {
        // don't wait for the task to complete
        scheduler.shutdown();
        logger.info("Quartz service stopped, and halt all tasks");
    }
  }


  /**
   * add task trigger , if this task already exists, return this task with updated trigger
   *
   * @param clazz             job class name
   * @param jobName           job name
   * @param jobGroupName      job group name
   * @param startDate         job start date
   * @param endDate           job end date
   * @param cronExpression    cron expression
   * @param jobDataMap        job parameters data map
   */
  public void addJob(Class<? extends Job> clazz,
                     String jobName,
                     String jobGroupName,
                     Date startDate,
                     Date endDate,
                     String cronExpression,
                     Map<String, Object> jobDataMap) {
    lock.writeLock().lock();
    try {

      JobKey jobKey = new JobKey(jobName, jobGroupName);
      JobDetail jobDetail;
      //add a task (if this task already exists, return this task directly)
      if (scheduler.checkExists(jobKey)) {

        jobDetail = scheduler.getJobDetail(jobKey);
        if (jobDataMap != null) {
          jobDetail.getJobDataMap().putAll(jobDataMap);
        }
      } else {
        jobDetail = newJob(clazz).withIdentity(jobKey).build();

        if (jobDataMap != null) {
          jobDetail.getJobDataMap().putAll(jobDataMap);
        }

        scheduler.addJob(jobDetail, false, true);

        logger.info("Add job, job name: {}, group name: {}",
                jobName, jobGroupName);
      }

      TriggerKey triggerKey = new TriggerKey(jobName, jobGroupName);
      /**
       * Instructs the Scheduler that upon a mis-fire
       * situation, the CronTrigger wants to have it's
       * next-fire-time updated to the next time in the schedule after the
       * current time (taking into account any associated Calendar),
       * but it does not want to be fired now.
       */
      CronTrigger cronTrigger = newTrigger().withIdentity(triggerKey).startAt(startDate).endAt(endDate)
              .withSchedule(cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing())
              .forJob(jobDetail).build();

      if (scheduler.checkExists(triggerKey)) {
          // updateProcessInstance scheduler trigger when scheduler cycle changes
          CronTrigger oldCronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
          String oldCronExpression = oldCronTrigger.getCronExpression();

          if (!StringUtils.equalsIgnoreCase(cronExpression,oldCronExpression)) {
            // reschedule job trigger
            scheduler.rescheduleJob(triggerKey, cronTrigger);
            logger.info("reschedule job trigger, triggerName: {}, triggerGroupName: {}, cronExpression: {}, startDate: {}, endDate: {}",
                    jobName, jobGroupName, cronExpression, startDate, endDate);
          }
      } else {
        scheduler.scheduleJob(cronTrigger);
        logger.info("schedule job trigger, triggerName: {}, triggerGroupName: {}, cronExpression: {}, startDate: {}, endDate: {}",
                jobName, jobGroupName, cronExpression, startDate, endDate);
      }

    } catch (Exception e) {
      logger.error("add job failed", e);
      throw new RuntimeException("add job failed", e);
    } finally {
      lock.writeLock().unlock();
    }
  }


  /**
   * delete job
   *
   * @param jobName      job name
   * @param jobGroupName job group name
   * @return true if the Job was found and deleted.
   */
  public boolean deleteJob(String jobName, String jobGroupName) {
    lock.writeLock().lock();
    try {
      JobKey jobKey = new JobKey(jobName,jobGroupName);
      if(scheduler.checkExists(jobKey)){
        logger.info("try to delete job, job name: {}, job group name: {},", jobName, jobGroupName);
        return scheduler.deleteJob(jobKey);
      }else {
        return true;
      }

    } catch (SchedulerException e) {
      logger.error("delete job : {} failed",jobName, e);
    } finally {
      lock.writeLock().unlock();
    }
    return false;
  }

  /**
   * delete all jobs in job group
   *
   * @param jobGroupName job group name
   *
   * @return true if all of the Jobs were found and deleted, false if
   *      one or more were not deleted.
   */
  public boolean deleteAllJobs(String jobGroupName) {
    lock.writeLock().lock();
    try {
      logger.info("try to delete all jobs in job group: {}", jobGroupName);
      List<JobKey> jobKeys =
              new ArrayList<>(scheduler.getJobKeys(GroupMatcher.groupEndsWith(jobGroupName)));

      return scheduler.deleteJobs(jobKeys);
    } catch (SchedulerException e) {
      logger.error("delete all jobs in job group: {} failed",jobGroupName, e);
    } finally {
      lock.writeLock().unlock();
    }
    return false;
  }

  /**
   * build job name
   * @param scheduleId scheduleId
   * @return job name
   */
  public static String buildJobName(Long scheduleId) {
    return QUARTZ_JOB_PREFIX + UNDERLINE + scheduleId;
  }

  /**
   * build job group name
   * @param projectId project id
   * @return job group name
   */
  public static String buildJobGroupName(Long projectId) {
    return QUARTZ_JOB_GROUP_PREFIX + UNDERLINE + projectId;
  }

  /**
   * add params to map
   *
   * @param projectId   project id
   * @param scheduleId  schedule id
   * @return data map
   */
  public static Map<String, Object> buildDataMap(Long projectId, Long projectFlowId, int scheduleId) {
    Map<String, Object> dataMap = Maps.newHashMap();
    dataMap.put(PROJECT_ID, projectId);
    dataMap.put(PROJECT_JOB_ID, projectFlowId);
    dataMap.put(SCHEDULE_ID, scheduleId);
    return dataMap;
  }

}

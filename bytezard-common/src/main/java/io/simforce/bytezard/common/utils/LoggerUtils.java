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
package io.simforce.bytezard.common.utils;

/**
 *  logger utils
 */
public class LoggerUtils {

    /**
     * Job Logger's prefix
     */
    public static final String JOB_LOGGER_INFO_PREFIX = "JOB";

    /**
     * Job Logger Thread's name
     */
    public static final String JOB_LOGGER_THREAD_NAME = "JobLogInfo";

    /**
     * Job Logger Thread's name
     */
    public static final String JOB_UNIQUE_ID_LOG_FORMAT = "[JobUniqueId=";

    /**
     * job log info format
     */
    public static final String JOB_LOG_INFO_FORMAT = "JobLogInfo-%s";


    public static String buildJobUniqueId(String affix,
                                     int platformType,
                                     String jobType,
                                     long jobInstanceId){
        // - [JobUniqueId=JOB_79_4084_15210]
        return String.format(" - %s%s-%s-%s-%s]",JOB_UNIQUE_ID_LOG_FORMAT,affix,
                platformType,
                jobType,
                jobInstanceId);
    }

    public static String buildJobUniqueId(String affix,
                                          String jobType,
                                          long jobInstanceId){
        // - [JobUniqueId=JOB_79_4084_15210]
        return String.format(" - %s-%s-%s-%s]",
                JOB_UNIQUE_ID_LOG_FORMAT,
                affix,
                jobType,
                jobInstanceId);
    }
}
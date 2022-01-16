package io.simforce.bytezard.common.config;

public class CoreConfig {

    public static final String COORDINATOR_ACTIVE_DIR = "/bytezard/master/active";

    public static final String JOB_RESPONSE_CACHE_LOCK_PATH = "/bytezard/job/lock";

    public static final String JOB_RESPONSE_CACHE_PATH = "/bytezard/executor/response/cache";

    public static final String JOB_COORDINATOR_RESPONSE_CACHE_LOCK_PATH = "/bytezard/job/lock/coordinator";

    /**
     * default log cache rows num,output when reach the number
     */
    public static final String LOG_CACHE_ROW_NUM = "log.cache.row.num";

    public static final int LOG_CACHE_ROW_NUM_DEFAULT_VALUE = 4 * 16;

    public static final String LOG_FLUSH_INTERVAL = "log.flush.row.num";

    public static final int LOG_FLUSH_INTERVAL_DEFAULT_VALUE = 1000;

}

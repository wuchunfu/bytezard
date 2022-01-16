package io.simforce.bytezard.executor.cache;

import java.util.concurrent.ConcurrentHashMap;

public class JobExecutionCache {

    private final ConcurrentHashMap<Long,JobExecutionContext> cache = new ConcurrentHashMap<>();

    private JobExecutionCache(){}

    private static class Singleton{
        static JobExecutionCache instance = new JobExecutionCache();
    }

    public static JobExecutionCache getInstance(){
        return Singleton.instance;
    }

    public JobExecutionContext getById(Long jobInstanceId){
        return cache.get(jobInstanceId);
    }

    public void cache(JobExecutionContext jobExecutionContext){
        cache.put(jobExecutionContext.getExecutionJob().getJobInstanceId(), jobExecutionContext);
    }

    public void remove(Long jobInstanceId){
        cache.remove(jobInstanceId);
    }
}

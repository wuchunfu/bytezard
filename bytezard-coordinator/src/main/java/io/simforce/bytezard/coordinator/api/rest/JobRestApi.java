package io.simforce.bytezard.coordinator.api.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.common.entity.LogResult;
import io.simforce.bytezard.common.utils.JSONUtils;
import io.simforce.bytezard.coordinator.api.entity.Result;
import io.simforce.bytezard.coordinator.repository.entity.JobInstance;
import io.simforce.bytezard.coordinator.repository.module.BytezardCoordinatorInjector;
import io.simforce.bytezard.coordinator.repository.service.JobInstanceService;

@Path("/job")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobRestApi {

    private final static Logger logger  = LoggerFactory.getLogger(JobRestApi.class);

    private AtomicInteger id = new AtomicInteger(1);

    private final JobInstanceService jobInstanceService =
                        BytezardCoordinatorInjector.getInjector()
                        .getInstance(JobInstanceService.class);

    /**
     * execute
     * @return Result
     */
    @POST
    @Path("/execute")
    public Result execute(Map<String,Object> params) {
        Result<Boolean> result = new Result<>();
        ExecutionJob executionJob = new ExecutionJob();
        executionJob.setJobName((String)params.get("jobName"));
//        executionJob.setJobType((String)params.get("jobType"));
        executionJob.setJobParameters((String)params.get("jobParameters"));
        executionJob.setTimeout((Integer)params.get("timeout"));
        executionJob.setTimeoutStrategy((Integer)params.get("timeoutStrategy"));
        executionJob.setRetryNums((Integer)params.get("retryNums"));
        executionJob.setSubmitTime(new Date());
        executionJob.setTenantCode((String)params.get("tenantCode"));
        executionJob.setApplicationIds((String)params.get("applicationIds"));
        logger.info(JSONUtils.toJsonString(params));
        result.setCode(0);
        result.setData(true);
        return result;
    }

    /**
     * kill
     * @return Result
     */
    @POST
    @Path("/kill")
    public Result kill(Map<String,Object> params){

        Result<JobInstance> result = new Result<>();
        Long jobInstanceId = Long.valueOf((Integer)params.get("jobInstanceId"));
        result.setCode(0);
        result.setData(jobInstanceService.getById(jobInstanceId));
        return result;
    }

    /**
     * get the log by id,skipLine,limit
     * @param jobInstanceId
     * @param offsetLine
     * @param limit
     * @return
     */
    @GET
    @Path("/get-log/{jobInstanceId}/{offsetLine}/{limit}")
    public Result getLog(@PathParam("jobInstanceId") long jobInstanceId,@PathParam("offsetLine")int offsetLine,@PathParam("limit") int limit){
        Result<LogResult> result = new Result<>();

//        LogResult logResult = mangerClient.getLog(jobInstanceId,offsetLine,limit);
        result.setCode(0);
        result.setData(null);


        return result;
    }

    /**
     * get the whole log
     * @param jobInstanceId
     * @return
     */
    @GET
    @Path("/get-all-log/{jobInstanceId}")
    public Result getAllLog(@PathParam("jobInstanceId") long jobInstanceId){
        Result<LogResult> result = new Result<>();

//        LogResult logResult = mangerClient.getAllLog(jobInstanceId);
        result.setCode(0);
        result.setData(null);

        return result;
    }

    /**
     * download log
     * @param jobInstanceId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GET
    @Path("download-log/{jobInstanceId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] download(@PathParam("jobInstanceId") long jobInstanceId,
                           @Context HttpServletRequest request,
                           @Context HttpServletResponse response)throws Exception{

        response.setHeader("Content-Disposition","attachment;filename=\""+System.currentTimeMillis()+".log"+"\"");
        response.addHeader("content-type","application/log");
        return new byte[]{};
    }

}
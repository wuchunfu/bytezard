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

import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.common.entity.LogResult;
import io.simforce.bytezard.common.utils.JSONUtils;
import io.simforce.bytezard.coordinator.api.entity.Result;
import io.simforce.bytezard.coordinator.repository.entity.Task;
import io.simforce.bytezard.coordinator.repository.module.BytezardCoordinatorInjector;
import io.simforce.bytezard.coordinator.repository.service.TaskService;

@Path("/job")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobRestApi {

    private final static Logger logger  = LoggerFactory.getLogger(JobRestApi.class);

    private AtomicInteger id = new AtomicInteger(1);

    private final TaskService taskService =
                        BytezardCoordinatorInjector.getInjector()
                        .getInstance(TaskService.class);

    /**
     * execute
     * @return Result
     */
    @POST
    @Path("/execute")
    public Result execute(Map<String,Object> params) {
        Result<Boolean> result = new Result<>();
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTaskName((String)params.get("jobName"));
//        executionJob.setJobType((String)params.get("jobType"));
        taskRequest.setApplicationParameter((String)params.get("jobParameters"));
        taskRequest.setTimeout((Integer)params.get("timeout"));
        taskRequest.setTimeoutStrategy((Integer)params.get("timeoutStrategy"));
        taskRequest.setRetryTimes((Integer)params.get("retryNums"));
        taskRequest.setTenantCode((String)params.get("tenantCode"));
        taskRequest.setApplicationId((String)params.get("applicationIds"));
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

        Result<Task> result = new Result<>();
        Long taskId = Long.valueOf((Integer)params.get("taskId"));
        result.setCode(0);
        result.setData(taskService.getById(taskId));
        return result;
    }

    /**
     * get the log by id,skipLine,limit
     * @param taskId
     * @param offsetLine
     * @param limit
     * @return
     */
    @GET
    @Path("/get-log/{taskId}/{offsetLine}/{limit}")
    public Result getLog(@PathParam("taskId") long taskId,@PathParam("offsetLine")int offsetLine,@PathParam("limit") int limit){
        Result<LogResult> result = new Result<>();

//        LogResult logResult = mangerClient.getLog(taskId,offsetLine,limit);
        result.setCode(0);
        result.setData(null);


        return result;
    }

    /**
     * get the whole log
     * @param taskId
     * @return
     */
    @GET
    @Path("/get-all-log/{taskId}")
    public Result getAllLog(@PathParam("taskId") long taskId){
        Result<LogResult> result = new Result<>();

//        LogResult logResult = mangerClient.getAllLog(taskId);
        result.setCode(0);
        result.setData(null);

        return result;
    }

    /**
     * download log
     * @param taskId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GET
    @Path("download-log/{taskId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] download(@PathParam("taskId") long taskId,
                           @Context HttpServletRequest request,
                           @Context HttpServletResponse response)throws Exception{

        response.setHeader("Content-Disposition","attachment;filename=\""+System.currentTimeMillis()+".log"+"\"");
        response.addHeader("content-type","application/log");
        return new byte[]{};
    }

}
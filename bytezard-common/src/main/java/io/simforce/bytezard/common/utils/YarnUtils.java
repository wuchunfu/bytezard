package io.simforce.bytezard.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.simforce.bytezard.common.CommonConstants;
import io.simforce.bytezard.common.enums.ExecutionStatus;

public class YarnUtils {

    private static final Logger logger = LoggerFactory.getLogger(YarnUtils.class);

    private static final String YARN_RESOURCE_MANAGER_HA_IDS = "";
//            PropertyUtils.getString(ConfigKey.YARN_RESOURCE_MANAGER_HA_IDS,ConfigKey.YARN_RESOURCE_MANAGER_HA_IDS_DEFAULT_VALUE);
    private static final String YARN_APPLICATION_STATUS_ADDRESS = "";
//            PropertyUtils.getString(ConfigKey.YARN_APPLICATION_STATUS_ADDRESS,ConfigKey.YARN_APPLICATION_STATUS_ADDRESS_DEFAULT_VALUE);

    private static final String HADOOP_RM_STATE_ACTIVE = "ACTIVE";
    private static final String HADOOP_RM_STATE_STANDBY = "STANDBY";
    private static final String YARN_RESOURCE_MANAGER_HA_XX = "xx";

    private static boolean yarnEnabled = false;

    /**
     * get application url
     * if rmHaIds contains xx, it signs not use resource manager
     * otherwise:
     * if rmHaIds is empty, single resource manager enabled
     * if rmHaIds not empty: resource manager HA enabled
     *
     * @param applicationId application id
     * @return url of application
     */
    public static String getApplicationUrl(String applicationId) {

        String appUrl = "";
        //not use resource manager
        if (YARN_RESOURCE_MANAGER_HA_IDS.contains(YARN_RESOURCE_MANAGER_HA_XX)){
            yarnEnabled = false;
            logger.warn("should not step here");
            return appUrl;
        } else if (!StringUtils.isEmpty(YARN_RESOURCE_MANAGER_HA_IDS)) {
            //resource manager HA enabled
            appUrl = getAppAddress(YARN_APPLICATION_STATUS_ADDRESS, YARN_RESOURCE_MANAGER_HA_IDS);
            yarnEnabled = true;
            logger.info("application url : {}", appUrl);
        } else {
            //single resource manager enabled
            appUrl = YARN_APPLICATION_STATUS_ADDRESS;
            yarnEnabled = true;
            logger.info("application url : {}", appUrl);
        }

        return String.format(appUrl, applicationId);
    }

    /**
     * getAppAddress
     *
     * @param appAddress app address
     * @param rmHa       resource manager ha
     * @return app address
     */
    public static String getAppAddress(String appAddress, String rmHa) {

        //get active ResourceManager
        String activeResourceManager = getActiveResourceManagerName(rmHa);
        //http://ds1:8088/ws/v1/cluster/apps/%s
        String[] split1 = appAddress.split(CommonConstants.DOUBLE_SLASH);

        if (split1.length != 2) {
            return null;
        }

        String start = split1[0] + CommonConstants.DOUBLE_SLASH;
        String[] split2 = split1[1].split(CommonConstants.COLON);

        if (split2.length != 2) {
            return null;
        }

        String end = CommonConstants.COLON + split2[1];

        return start + activeResourceManager + end;
    }

    public static String getActiveResourceManagerName(String rmIds) {

        String[] rmIdArr = rmIds.split(CommonConstants.COMMA);
        int activeResourceManagerPort = 8080;
//                PropertyUtils.getInt(ConfigKey.HADOOP_RESOURCE_MANAGER_HTTP_ADDRESS_PORT, ConfigKey.HADOOP_RESOURCE_MANAGER_HTTP_ADDRESS_PORT_DEFAULT_VALUE);
        String yarnUrl = "http://%s:" + activeResourceManagerPort + "/ws/v1/cluster/info";

        String state = null;
        try {

            state = getResourceManagerState(String.format(yarnUrl, rmIdArr[0]));

            if (HADOOP_RM_STATE_ACTIVE.equals(state)) {
                return rmIdArr[0];
            } else if (HADOOP_RM_STATE_STANDBY.equals(state)) {
                state = getResourceManagerState(String.format(yarnUrl, rmIdArr[1]));
                if (HADOOP_RM_STATE_ACTIVE.equals(state)) {
                    return rmIdArr[1];
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            state = getResourceManagerState(String.format(yarnUrl, rmIdArr[1]));
            if (HADOOP_RM_STATE_ACTIVE.equals(state)) {
                return rmIdArr[0];
            }
        }

        return null;
    }

    public static String getResourceManagerState(String url) {

        String retStr = HttpUtils.get(url);
        if (StringUtils.isEmpty(retStr)) {
            return null;
        }

        JsonNode node = JSONUtils.parseNode(retStr);
        if (node != null) {
            JsonNode clusterInfo = JSONUtils.findNode(node,"clusterInfo");
            if (clusterInfo != null) {
                return JSONUtils.findValue(clusterInfo, "haState");
            }
        }

        return null;
    }

    public static boolean isYarnEnabled(){
        return yarnEnabled;
    }

    /**
     * get the state of an application
     *
     * @param applicationId application id
     * @return the return may be null or there may be other parse exceptions
     */
    public static ExecutionStatus getApplicationStatus(String applicationId){
        if (StringUtils.isEmpty(applicationId)) {
            return null;
        }

        String applicationUrl = getApplicationUrl(applicationId);
        String responseContent = HttpUtils.get(applicationUrl);
        String result = "";
        JsonNode node = JSONUtils.parseNode(responseContent);
        if (node != null) {
            JsonNode app = JSONUtils.findNode(node,"app");
            if (app != null) {
                result =  JSONUtils.findValue(app, "finalStatus");
            }
        }

        if (StringUtils.isEmpty(result)) {
            return ExecutionStatus.RUNNING_EXECUTION;
        }

        switch (result) {
            case CommonConstants.ACCEPTED:
                return ExecutionStatus.SUBMITTED_SUCCESS;
            case CommonConstants.SUCCEEDED:
                return ExecutionStatus.SUCCESS;
            case CommonConstants.NEW:
            case CommonConstants.NEW_SAVING:
            case CommonConstants.SUBMITTED:
            case CommonConstants.FAILED:
                return ExecutionStatus.FAILURE;
            case CommonConstants.KILLED:
                return ExecutionStatus.KILL;

            case CommonConstants.RUNNING:
            default:
                return ExecutionStatus.RUNNING_EXECUTION;
        }
    }

    public static boolean isSuccessOfYarnState(List<String> appIds) {
        boolean result = true;
        try {
            for (String appId : appIds) {
                while(Stopper.isRunning()){
                    ExecutionStatus applicationStatus = YarnUtils.getApplicationStatus(appId);
                    if (applicationStatus != null) {
                        logger.info("appId:{}, final state:{}",appId,applicationStatus.name());
                        if (applicationStatus.equals(ExecutionStatus.FAILURE) ||
                                applicationStatus.equals(ExecutionStatus.KILL)) {
                            return false;
                        }

                        if (applicationStatus.equals(ExecutionStatus.SUCCESS)){
                            break;
                        }
                    }

                    Thread.sleep(CommonConstants.SLEEP_TIME_MILLIS);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("yarn applications: %s  status failed ", appIds.toString()),e);
            result = false;
        }
        return result;
    }

    public static boolean isSuccessOfYarnState(String[] appIds,boolean cancel) {
        boolean result = true;
        try {
            for (String appId : appIds) {
                while(Stopper.isRunning() && !cancel){
                    ExecutionStatus applicationStatus = YarnUtils.getApplicationStatus(appId);
                    if (applicationStatus != null) {
                        logger.info("appId:{}, final state:{}",appId,applicationStatus.name());
                        if (applicationStatus.equals(ExecutionStatus.FAILURE) ||
                                applicationStatus.equals(ExecutionStatus.KILL)) {
                            return false;
                        }

                        if (applicationStatus.equals(ExecutionStatus.SUCCESS)){
                            break;
                        }
                    }

                    Thread.sleep(CommonConstants.SLEEP_TIME_MILLIS);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("yarn applications: %s  status failed ", Arrays.toString(appIds)),e);
            result = false;
        }

        return result;
    }
}

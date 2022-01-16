package io.simforce.bytezard.common.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zixi0825
 */
public class YarnUtils {

    private static final Logger logger = LoggerFactory.getLogger(YarnUtils.class);

    private static final String YARN_RESOURCE_MANAGER_HA_IDS =
            PropertyUtils.getString(ConfigKey.YARN_RESOURCE_MANAGER_HA_IDS,ConfigKey.YARN_RESOURCE_MANAGER_HA_IDS_DEFAULT_VALUE);
    private static final String YARN_APPLICATION_STATUS_ADDRESS =
            PropertyUtils.getString(ConfigKey.YARN_APPLICATION_STATUS_ADDRESS,ConfigKey.YARN_APPLICATION_STATUS_ADDRESS_DEFAULT_VALUE);

    private static final String HADOOP_RM_STATE_ACTIVE = "ACTIVE";
    private static final String HADOOP_RM_STATE_STANDBY = "STANDBY";
    private static final String YARN_RESOURCE_MANAGER_HA_XX = "xx";

    private static boolean yarnEnabled = false;

    /**
     * get application url
     *
     * @param applicationId application id
     * @return url of application
     */
    public static String getApplicationUrl(String applicationId) {
        /**
         * if rmHaIds contains xx, it signs not use resource manager
         * otherwise:
         *  if rmHaIds is empty, single resource manager enabled
         *  if rmHaIds not empty: resource manager HA enabled
         */
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
        String[] split1 = appAddress.split(Constants.DOUBLE_SLASH);

        if (split1.length != 2) {
            return null;
        }

        String start = split1[0] + Constants.DOUBLE_SLASH;
        String[] split2 = split1[1].split(Constants.COLON);

        if (split2.length != 2) {
            return null;
        }

        String end = Constants.COLON + split2[1];

        return start + activeResourceManager + end;
    }

    public static String getActiveResourceManagerName(String rmIds) {

        String[] rmIdArr = rmIds.split(Constants.COMMA);
        int activeResourceManagerPort = PropertyUtils.getInt(ConfigKey.HADOOP_RESOURCE_MANAGER_HTTP_ADDRESS_PORT, ConfigKey.HADOOP_RESOURCE_MANAGER_HTTP_ADDRESS_PORT_DEFAULT_VALUE);
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
        //to json
        JSONObject jsonObject = JSON.parseObject(retStr);
        //get ResourceManager state
        return jsonObject.getJSONObject("clusterInfo").getString("haState");
    }

    public static boolean isYarnEnabled(){
        return yarnEnabled;
    }

    /**
     * get the state of an application
     *
     * @param applicationId application id
     * @return the return may be null or there may be other parse exceptions
     * @throws JSONException json exception
     */
    public static ExecutionStatus getApplicationStatus(String applicationId) throws JSONException {
        if (StringUtils.isEmpty(applicationId)) {
            return null;
        }

        String applicationUrl = getApplicationUrl(applicationId);

        String responseContent = HttpUtils.get(applicationUrl);

        JSONObject jsonObject = JSON.parseObject(responseContent);
        String result = jsonObject.getJSONObject("app").getString("finalStatus");

        switch (result) {
            case Constants.ACCEPTED:
                return ExecutionStatus.SUBMITTED_SUCCESS;
            case Constants.SUCCEEDED:
                return ExecutionStatus.SUCCESS;
            case Constants.NEW:
            case Constants.NEW_SAVING:
            case Constants.SUBMITTED:
            case Constants.FAILED:
                return ExecutionStatus.FAILURE;
            case Constants.KILLED:
                return ExecutionStatus.KILL;

            case Constants.RUNNING:
            default:
                return ExecutionStatus.RUNNING_EXECUTION;
        }
    }

    public static boolean isSuccessOfYarnState(List<String> appIds) {
        boolean result = true;
        try {
            for (String appId : appIds) {
                while(Stopper.isRunning()){
                    ExecutionStatus applicationStatus = org.simfo.manger.core.utils.YarnUtils.getApplicationStatus(appId);
                    if(applicationStatus != null){
                        logger.info("appId:{}, final state:{}",appId,applicationStatus.name());
                        if (applicationStatus.equals(ExecutionStatus.FAILURE) ||
                                applicationStatus.equals(ExecutionStatus.KILL)) {
                            return false;
                        }

                        if (applicationStatus.equals(ExecutionStatus.SUCCESS)){
                            break;
                        }
                    }

                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
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
                    ExecutionStatus applicationStatus = org.simfo.manger.core.utils.YarnUtils.getApplicationStatus(appId);
                    if(applicationStatus != null){
                        logger.info("appId:{}, final state:{}",appId,applicationStatus.name());
                        if (applicationStatus.equals(ExecutionStatus.FAILURE) ||
                                applicationStatus.equals(ExecutionStatus.KILL)) {
                            return false;
                        }

                        if (applicationStatus.equals(ExecutionStatus.SUCCESS)){
                            break;
                        }
                    }

                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("yarn applications: %s  status failed ", appIds.toString()),e);
            result = false;
        }

        return result;
    }
}

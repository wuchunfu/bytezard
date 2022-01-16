package io.simforce.bytezard.coordinator.eunms;

import io.simforce.bytezard.common.enums.BaseEnum;

/**
 * @author zixi0825
 */
public enum ScheduleStatus implements BaseEnum {

    /**
     * 0 file, 1 udf
     */
    OFFLINE(0, "offline"),
    ONLINE(1, "online");

    ScheduleStatus(int code, String description){
        this.code = code;
        this.description = description;
    }

    int code;
    String description;
}

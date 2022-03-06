package io.datavines.coordinator.eunms;

import io.datavines.common.enums.BaseEnum;

/**
 * @author simfo
 * @date 2019/9/3 11:20
 */
public enum CronLevel implements BaseEnum {

    /**
     * crontab grain level
     */
    SECOND(0,"second"),
    MINUTE(1,"minute"),
    HOUR(2,"hour"),
    DAY(3,"day"),
    MONTH(4,"month"),
    WEEK(5,"week"),
    YEAR(6,"year");

    CronLevel(int code, String description){
        this.code = code;
        this.description = description;
    }

    int code;
    String description;

}

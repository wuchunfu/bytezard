package io.simforce.bytezard.coordinator.eunms;

import io.simforce.bytezard.common.enums.BaseEnum;

/**
 * @author zixi0825
 */
public enum ExpressionGrain implements BaseEnum {
    /**
     *
     */
    y(1,"y"),
    M(2,"M"),
    w(3,"w"),
    d(4,"d"),
    H(5,"h"),
    m(6,"m");

    ExpressionGrain(int code, String description){
        this.code = code;
        this.description = description;
    }

    int code;
    String description;
}

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
package io.datavines.coordinator.eunms;

import io.datavines.common.enums.BaseEnum;

/**
 * UDF type
 */
public enum UdfType implements BaseEnum {
    /**
     * 0 hive; 1 spark
     */
    HIVE(0, "hive"),
    SPARK(1, "spark");

    UdfType(int code, String description){
        this.code = code;
        this.description = description;
    }

    int code;
    String description;

    public static UdfType of(int type){
        for(UdfType ut : values()){
            if(ut.getCode() == type){
                return ut;
            }
        }
        throw new IllegalArgumentException("invalid type : " + type);
    }

}

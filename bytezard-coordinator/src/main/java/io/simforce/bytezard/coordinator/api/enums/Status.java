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

package io.simforce.bytezard.coordinator.api.enums;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

/**
 * status enum
 */
public enum Status {

    /**
     * 错误码分配 xx-xx-xxxx
     * 1-2: 服务代码
     * 3-4：模块代码
     * 5-8: 错误代码
     * 
     * 10 留给默认的服务 例如用户、权限
     * 11-项目管理
     * 12-作业开发
     * 13-任务管理
     */
    SUCCESS(200, "success", "成功"),
    FAIL(400, "Bad Request","错误的请求"),
    UNAUTHORIZED(401, "Unauthorized","未授权"),
    FORBIDDEN(403, "Forbidden","拒绝访问"),
    NOT_FOUND(404, "Not Found","未找到"),
    NO_PERMISSION(405, "NoPermission","没有权限"),
    SERVER_ERROR(500, "Internal Server Error","内部服务器错误"),

    //10 用户
    USERNAME_HAS_BEEN_REGISTERED_ERROR(10010001,"The username {0} has been registered","用户名{0}已被注册过"),
    REGISTER_USER_ERROR(10010002,"Register User {0} Error","注册用户{0}失败"),
    USERNAME_OR_PASSWORD_ERROR(10010003,"Username or Email Error","用户名或者密码错误"),

    //10 权限
    NO_PERMISSION_ERROR(10020001,"No Permission Error : {0}","没有权限错误：{0}"),

    //03 参数
    PARAM_CHECK_ERROR(10020001,"Param Check Error : {0}","参数校验错误：{0}"),

    PROJECT_IS_EXIST_ERROR(11010001,"Project {0} Is Exist Error","项目 {0} 已存在"),
    PROJECT_IS_NOT_EXIST_ERROR(11010002,"Project {0} Is Exist Not Error","项目 {0} 不存在"),
    PROJECT_CREATE_ERROR(11010003,"Create Project {0} Error","创建项目 {0} 失败"),
    PROJECT_UPDATE_ERROR(11010004,"Update Project {0} Error","更新项目 {0} 失败");

    private final int code;
    private final String enMsg;
    private final String zhMsg;

    Status(int code, String enMsg, String zhMsg) {
        this.code = code;
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
    }

    public int getCode() {
        return this.code;
    }

    public String getEnMsg() {
        return enMsg;
    }

    public String getZhMsg() {
        return zhMsg;
    }

    public String getMsg() {
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            return this.zhMsg;
        } else {
            return this.enMsg;
        }
    }
}

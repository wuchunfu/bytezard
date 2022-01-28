package io.simforce.bytezard.coordinator.api.entity;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import io.simforce.bytezard.coordinator.CoordinatorConstants;
import io.simforce.bytezard.coordinator.api.enums.Status;
import io.simforce.bytezard.coordinator.utils.TokenManager;

public class ResultMap extends HashMap<String, Object> {

    public static final String EMPTY = "";

    private TokenManager tokenManager;

    private int code;

    public ResultMap(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    public ResultMap() {

    }

    public ResultMap success() {
        this.code = Status.SUCCESS.getCode();
        this.put("code", this.code);
        this.put("msg", "Success");
        this.put("data", EMPTY);
        return this;
    }

    public ResultMap success(String token) {
        this.code = Status.SUCCESS.getCode();
        this.put("code", this.code);
        this.put("msg", "Success");
        this.put("token", token);
        this.put("data", EMPTY);
        return this;
    }

    public ResultMap successAndRefreshToken(HttpServletRequest request) {
        String token = request.getHeader(CoordinatorConstants.TOKEN_HEADER_STRING);
        if(StringUtils.isEmpty(token)) {
            token = (String)request.getAttribute(CoordinatorConstants.TOKEN_HEADER_STRING);
        }
        this.code = Status.SUCCESS.getCode();
        this.put("code", this.code);
        this.put("msg", "Success");
        this.put("token", this.tokenManager.refreshToken(token));
        this.put("data", EMPTY);
        return this;
    }


    public ResultMap fail() {
        this.code = Status.FAIL.getCode();
        this.put("code", code);
        this.put("data", EMPTY);
        return this;
    }

    public ResultMap fail(int code) {
        this.code = code;
        this.put("code", code);
        this.put("data", EMPTY);
        return this;
    }


    public ResultMap failWithToken(String token) {
        this.code = Status.FAIL.getCode();
        this.put("code", code);
        this.put("msg", Status.FAIL.getZhMsg());
        this.put("token", tokenManager.refreshToken(token));
        this.put("data", EMPTY);
        return this;
    }


    public ResultMap failAndRefreshToken(HttpServletRequest request) {
        this.code = Status.FAIL.getCode();
        this.put("code", code);
        this.put("msg", Status.FAIL.getZhMsg());

        String token = request.getHeader(CoordinatorConstants.TOKEN_HEADER_STRING);

        if (!StringUtils.isEmpty(token)) {
            this.put("token", this.tokenManager.refreshToken(token));
        }
        this.put("data", EMPTY);
        return this;
    }

    public ResultMap failAndRefreshToken(HttpServletRequest request, Status status) {
        
        this.put("code", status.getCode());
        this.put("msg", status.getZhMsg());

        String token = request.getHeader(CoordinatorConstants.TOKEN_HEADER_STRING);
        if(StringUtils.isEmpty(token)) {
            token = (String)request.getAttribute(CoordinatorConstants.TOKEN_HEADER_STRING);
        }

        if (!StringUtils.isEmpty(token)) {
            this.put("token", this.tokenManager.refreshToken(token));
        }
        
        this.put("data", EMPTY);
        return this;
    }

    public ResultMap message(String message) {
        this.put("msg", message);
        return this;
    }

    public ResultMap payload(Object object) {
        this.put("data", null == object ? EMPTY : object);
        return this;
    }

    public ResultMap payload(Map object) {
        this.put("data", null == object ? EMPTY : object);
        return this;
    }

    public ResultMap payloads(Collection list) {
        this.put("data", null == list ? new List[0] : list);
        return this;
    }

    public int getCode() {
        return code;
    }
}

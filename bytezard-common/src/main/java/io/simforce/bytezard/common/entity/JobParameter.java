package io.simforce.bytezard.common.entity;

import java.util.List;
import java.util.Map;

public class JobParameter {

    private String engineType;

    private Map<String,String> engineParameter;

    private List<String> connectorParameter;

    private String functionType;

    private Map<String,String> functionParameter;

    private Map<String,String> otherParameter;

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public Map<String, String> getEngineParameter() {
        return engineParameter;
    }

    public void setEngineParameter(Map<String, String> engineParameter) {
        this.engineParameter = engineParameter;
    }

    public List<String> getConnectorParameter() {
        return connectorParameter;
    }

    public void setConnectorParameter(List<String> connectorParameter) {
        this.connectorParameter = connectorParameter;
    }

    public String getFunctionType() {
        return functionType;
    }

    public void setFunctionType(String functionType) {
        this.functionType = functionType;
    }

    public Map<String, String> getFunctionParameter() {
        return functionParameter;
    }

    public void setFunctionParameter(Map<String, String> functionParameter) {
        this.functionParameter = functionParameter;
    }

    public Map<String, String> getOtherParameter() {
        return otherParameter;
    }

    public void setOtherParameter(Map<String, String> otherParameter) {
        this.otherParameter = otherParameter;
    }
}

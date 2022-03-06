package io.datavines.common.entity;

import java.util.List;
import java.util.Map;

public class JobParameter {

    private String engineType;

    private Map<String,Object> engineParameter;

    private List<ConnectorParameter> connectorParameter;

    private String functionType;

    private Map<String,Object> functionParameter;

    private Map<String,Object> otherParameter;

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public Map<String, Object> getEngineParameter() {
        return engineParameter;
    }

    public void setEngineParameter(Map<String, Object> engineParameter) {
        this.engineParameter = engineParameter;
    }

    public List<ConnectorParameter> getConnectorParameter() {
        return connectorParameter;
    }

    public void setConnectorParameter(List<ConnectorParameter> connectorParameter) {
        this.connectorParameter = connectorParameter;
    }

    public String getFunctionType() {
        return functionType;
    }

    public void setFunctionType(String functionType) {
        this.functionType = functionType;
    }

    public Map<String, Object> getFunctionParameter() {
        return functionParameter;
    }

    public void setFunctionParameter(Map<String, Object> functionParameter) {
        this.functionParameter = functionParameter;
    }

    public Map<String, Object> getOtherParameter() {
        return otherParameter;
    }

    public void setOtherParameter(Map<String, Object> otherParameter) {
        this.otherParameter = otherParameter;
    }
}

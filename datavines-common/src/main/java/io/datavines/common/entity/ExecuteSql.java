package io.datavines.common.entity;

public class ExecuteSql {

    private String sql;

    private String resultTable;

    private boolean isErrorOutput;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getResultTable() {
        return resultTable;
    }

    public void setResultTable(String resultTable) {
        this.resultTable = resultTable;
    }

    public boolean isErrorOutput() {
        return isErrorOutput;
    }

    public void setErrorOutput(boolean errorOutput) {
        isErrorOutput = errorOutput;
    }
}

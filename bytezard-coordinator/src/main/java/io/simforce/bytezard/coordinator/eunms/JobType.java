package io.simforce.bytezard.coordinator.eunms;

public enum JobType implements BaseEnum {
    /**
     * 0 SHELL
     * 1 SQL
     * 2 SUB_PROCESS
     * 3 PROCEDURE
     * 4 MR
     * 5 SPARK
     * 6 PYTHON
     * 7 DEPENDENT
     * 8 FLINK
     * 9 HTTP
     * 10 DATAX
     * 11 CONDITIONS
     * 12 SQOOP
     */
    SHELL(0, "shell"),
    JDBC(1, "jdbc"),
    SUB_PROCESS(2, "sub_process"),
    PROCEDURE(3, "procedure"),
    MR(4, "mr"),
    SPARK(5, "spark"),
    PYTHON(6, "python"),
    DEPENDENT(7, "dependent"),
    FLINK(8, "flink"),
    HTTP(9, "http"),
    DATAX(10, "datax"),
    CONDITIONS(11, "conditions"),
    SQOOP(12, "sqoop"),
    CHECK_APPLICATION_STATUS(13,"check application status");

    JobType(int code, String description){
        this.code = code;
        this.description = description;
    }


    int code;
    String description;

    public static JobType of(int code){
        for(JobType jobType : values()){
            if(jobType.getCode() == code){
                return jobType;
            }
        }
        throw new IllegalArgumentException("invalid type : " + code);
    }

    public static JobType of(String description){
        for(JobType jobType : values()){
            if(jobType.getDescription().equals(description)){
                return jobType;
            }
        }
        throw new IllegalArgumentException("invalid type : " + description);
    }
}
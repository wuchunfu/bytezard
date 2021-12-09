package io.simforce.bytezard.function.plugin;

import io.simforce.bytezard.common.entity.ExecuteSql;
import io.simforce.bytezard.function.api.SqlFunction;

public class NullCheck implements SqlFunction {

    @Override
    public String getName() {
        return "NullCheck";
    }

    @Override
    public String getType() {
        return "SingleTable";
    }

    @Override
    public ExecuteSql getInvalidateItems() {
        ExecuteSql executeSql = new ExecuteSql();
        executeSql.setResultTable("invalidate_items");
        executeSql.setSql("SELECT * FROM ${src_table} WHERE (${src_field} is null or ${src_field} = '') AND (${src_filter})");
        executeSql.setErrorOutput(true);
        return executeSql;
    }

    @Override
    public ExecuteSql getActualValue() {
        ExecuteSql executeSql = new ExecuteSql();
        executeSql.setResultTable("invalidate_count");
        executeSql.setSql("SELECT COUNT(*) AS invalidate_count FROM invalidate_items");
        executeSql.setErrorOutput(false);
        return executeSql;
    }
}

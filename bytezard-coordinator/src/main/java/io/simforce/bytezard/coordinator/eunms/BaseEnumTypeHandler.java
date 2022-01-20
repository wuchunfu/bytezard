package io.simforce.bytezard.coordinator.eunms;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.simforce.bytezard.common.enums.BaseEnum;

public class BaseEnumTypeHandler extends BaseTypeHandler<BaseEnum> {

    private Class<BaseEnum> type;

    public BaseEnumTypeHandler(){}

    public BaseEnumTypeHandler(Class<BaseEnum> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, BaseEnum parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public BaseEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convert(rs.getInt(columnName));
    }

    @Override
    public BaseEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convert(rs.getInt(columnIndex));
    }

    @Override
    public BaseEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convert(cs.getInt(columnIndex));
    }

    private BaseEnum convert(int code){
        BaseEnum[] enums = type.getEnumConstants();
        for(BaseEnum em: enums){
            if(em.getCode() == code){
                return  em;
            }
        }
        return null;
    }
}
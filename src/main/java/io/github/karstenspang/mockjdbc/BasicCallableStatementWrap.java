package io.github.karstenspang.mockjdbc;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface BasicCallableStatementWrap<S extends CallableStatement> extends BasicPreparedStatementWrap<S>{
    default void registerOutParameter​(int parameterIndex, int sqlType)
        throws SQLException
    {
        wrapped().registerOutParameter​(parameterIndex,sqlType);
    }
}

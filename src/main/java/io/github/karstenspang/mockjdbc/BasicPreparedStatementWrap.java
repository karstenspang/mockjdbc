package io.github.karstenspang.mockjdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public interface BasicPreparedStatementWrap<S extends PreparedStatement> extends BasicStatementWrap<S>{
    
    default ResultSet exceuteQuery()
        throws SQLException
    {
        return wrapped().executeQuery();
    }
}

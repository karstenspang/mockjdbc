package io.github.karstenspang.mockjdbc;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public interface BasicStatementWrap<S extends Statement>{
    S wrapped();
    
    // Methods from AutoCloesable
//    @Override
    default void close()
        throws SQLException
    {
        wrapped().close();
    }
    
    // Methods from java.sql.Wrapper
//    @Override
    default boolean isWrapperFor​(Class<?> iface)
        throws SQLException
    {
        return wrapped().isWrapperFor(iface);
    }
    
//    @Override
    default <T> T unwrap​(Class<T> iface)
        throws SQLException
    {
        return wrapped().unwrap(iface);
    }
    
    // Methods from Statement
//    @Override
    default public boolean isClosed()
        throws SQLException
    {
        return wrapped().isClosed();
    }
    
    default ResultSet exceuteQuery(String sql)
        throws SQLException
    {
        return wrapped().executeQuery(sql);
    }
}

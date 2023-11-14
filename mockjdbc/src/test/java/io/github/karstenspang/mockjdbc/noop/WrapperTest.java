package io.github.karstenspang.mockjdbc.noop;

import io.github.karstenspang.mockjdbc.wrap.PreparedStatementWrap;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class WrapperTest {
    @ParameterizedTest
    @ValueSource(classes={Object.class,AutoCloseable.class,Statement.class,PreparedStatement.class,NoopStatement.class,NoopPreparedStatement.class})
    @DisplayName("NoopPreparedStatement can be unwrapped to its superclasses")
    void testCanUnwrap(Class<?> clazz)
        throws SQLException
    {
        PreparedStatement stmt=new PreparedStatementWrap(NoopPreparedStatement.instance(),Arrays.asList());
        assertTrue(stmt.isWrapperFor(clazz),()->"isWrapper("+clazz+")");
        assertSame(NoopPreparedStatement.instance(),stmt.unwrap(clazz),()->"unwrap("+clazz+")");
    }
    
    @ParameterizedTest
    @ValueSource(classes={CallableStatement.class,NoopCallableStatement.class})
    @DisplayName("NoopPreparedStatement can not be unwrapped to its subclasses")
    void testCanNotUnwrap(Class<?> clazz)
        throws SQLException
    {
        PreparedStatement stmt=new PreparedStatementWrap(NoopPreparedStatement.instance(),Arrays.asList());
        assertFalse(stmt.isWrapperFor(clazz),()->"isWrapper("+clazz+")");
        SQLException ex=assertThrows(SQLException.class,()->stmt.unwrap(clazz),()->"unwrap("+clazz+")");
        assertInstanceOf(ClassCastException.class,ex.getCause());
    }
}
